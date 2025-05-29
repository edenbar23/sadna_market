package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain Service responsible for order business logic only.
 * Payment and supply are handled by CheckoutApplicationService.
 */
@Service
public class OrderProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    private final IStoreRepository storeRepository;
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;

    @Autowired
    public OrderProcessingService(
            IStoreRepository storeRepository,
            IOrderRepository orderRepository,
            IUserRepository userRepository,
            IProductRepository productRepository) {
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;

        logger.info("OrderProcessingService initialized");
    }

    // ==================== ORDER CREATION (PENDING STATUS) ====================

    /**
     * Creates pending orders for a registered user
     * Called by CheckoutApplicationService before payment processing
     */
    public List<Order> createPendingOrders(String username, Cart cart) {
        logger.info("Creating pending orders for user: {}", username);

        // Validate user exists
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if cart is empty
        if (cart.getShoppingBaskets().isEmpty()) {
            logger.warn("Cannot create orders from empty cart for user: {}", username);
            throw new IllegalStateException("Cannot create orders from empty cart");
        }

        List<Order> orders = new ArrayList<>();

        // Create order for each store in cart
        for (Map.Entry<UUID, ShoppingBasket> entry : cart.getShoppingBaskets().entrySet()) {
            UUID storeId = entry.getKey();
            ShoppingBasket basket = entry.getValue();

            try {
                Order order = createPendingOrder(username, storeId, basket);
                orders.add(order);
                logger.info("Created pending order {} for store {}", order.getOrderId(), storeId);
            } catch (Exception e) {
                logger.error("Failed to create pending order for store {}: {}", storeId, e.getMessage());
                // Rollback any created orders
                cancelOrders(orders);
                throw new RuntimeException("Order creation failed: " + e.getMessage(), e);
            }
        }

        logger.info("Successfully created {} pending orders for user {}", orders.size(), username);
        return orders;
    }

    /**
     * Creates pending orders for a guest user
     * Called by CheckoutApplicationService before payment processing
     */
    public List<Order> createGuestPendingOrders(Cart cart) {
        logger.info("Creating pending orders for guest");

        // Check if cart is empty
        if (cart.getShoppingBaskets().isEmpty()) {
            logger.warn("Cannot create orders from empty cart for guest");
            throw new IllegalStateException("Cannot create orders from empty cart");
        }

        List<Order> orders = new ArrayList<>();
        String guestId = "GUEST-" + UUID.randomUUID().toString().substring(0, 8);

        // Create order for each store in cart
        for (Map.Entry<UUID, ShoppingBasket> entry : cart.getShoppingBaskets().entrySet()) {
            UUID storeId = entry.getKey();
            ShoppingBasket basket = entry.getValue();

            try {
                Order order = createPendingOrder(guestId, storeId, basket);
                orders.add(order);
                logger.info("Created pending order {} for store {}", order.getOrderId(), storeId);
            } catch (Exception e) {
                logger.error("Failed to create pending order for store {}: {}", storeId, e.getMessage());
                // Rollback any created orders
                cancelOrders(orders);
                throw new RuntimeException("Order creation failed: " + e.getMessage(), e);
            }
        }

        logger.info("Successfully created {} pending orders for guest", orders.size());
        return orders;
    }

    /**
     * Creates a single pending order for a specific store
     */
    private Order createPendingOrder(String username, UUID storeId, ShoppingBasket basket) {
        logger.debug("Creating pending order for store: {} and user: {}", storeId, username);

        // Get and validate store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new IllegalStateException("Cannot create order for inactive store: " + store.getName());
        }

        // Get and validate basket items
        Map<UUID, Integer> items = basket.getProductsList();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty basket");
        }

        // Validate inventory availability
        Set<String> inventoryErrors = store.checkCart(items);
        if (!inventoryErrors.isEmpty()) {
            String errorMessage = String.join(", ", inventoryErrors);
            logger.error("Inventory validation failed: {}", errorMessage);
            throw new IllegalStateException("Inventory validation failed: " + errorMessage);
        }

        // Calculate total price
        double totalPrice = calculateTotalPrice(items);

        // Create order with PENDING status
        UUID orderId = orderRepository.createOrder(
                storeId,
                username,
                new HashMap<>(items), // Defensive copy
                totalPrice,
                totalPrice, // TODO: Apply discounts/promotions here
                LocalDateTime.now(),
                OrderStatus.PENDING,
                -1 // No transaction ID yet
        );

        // Return the created order
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Failed to create order"));
    }

    // ==================== ORDER FINALIZATION (AFTER PAYMENT/SUPPLY) ====================

    /**
     * Finalizes orders after successful payment and supply arrangement
     * Called by CheckoutApplicationService after payment/supply processing
     */
    public void finalizeOrders(List<Order> orders, int paymentTransactionId, List<Integer> supplyTransactionIds) {
        logger.info("Finalizing {} orders with payment ID: {}", orders.size(), paymentTransactionId);

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int supplyTransactionId = (i < supplyTransactionIds.size()) ? supplyTransactionIds.get(i) : -1;

            try {
                // Update order status to PAID
                orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.PAID);

                // Update inventory (reduce stock)
                updateInventoryAfterPayment(order);

                // Add order to store's order history
                addOrderToStore(order);

                // Add order to user's history (if registered user)
                addOrderToUserHistory(order);

                // Update order status to SHIPPED if supply was arranged
                if (supplyTransactionId != -1) {
                    orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPED);
                }

                logger.info("Successfully finalized order: {}", order.getOrderId());

                // Publish order processed event
                DomainEventPublisher.publish(
                        new OrderProcessedEvent(order.getUserName(), order.getOrderId(), order.getStoreId())
                );

            } catch (Exception e) {
                logger.error("Failed to finalize order {}: {}", order.getOrderId(), e.getMessage());
                // Continue with other orders, but log the error
            }
        }
    }

    /**
     * Updates inventory after successful payment
     */
    private void updateInventoryAfterPayment(Order order) {
        logger.debug("Updating inventory for order: {}", order.getOrderId());

        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new IllegalStateException("Store not found for order: " + order.getOrderId()));

        // Reduce inventory quantities
        Map<UUID, Integer> items = order.getProductsMap();
        Set<String> updateErrors = store.updateStockAfterPurchase(items);

        if (!updateErrors.isEmpty()) {
            logger.error("Failed to update inventory for order {}: {}", order.getOrderId(),
                    String.join(", ", updateErrors));
            throw new IllegalStateException("Failed to update inventory: " + String.join(", ", updateErrors));
        }

        // Save updated store
        storeRepository.save(store);
        logger.debug("Inventory updated successfully for order: {}", order.getOrderId());
    }

    /**
     * Adds order to store's order history
     */
    private void addOrderToStore(Order order) {
        try {
            Store store = storeRepository.findById(order.getStoreId())
                    .orElseThrow(() -> new IllegalStateException("Store not found"));

            store.addOrder(order.getOrderId());
            storeRepository.save(store);

            logger.debug("Added order {} to store {}", order.getOrderId(), order.getStoreId());
        } catch (Exception e) {
            logger.error("Failed to add order to store: {}", e.getMessage());
            // Non-critical error, don't fail the entire operation
        }
    }

    /**
     * Adds order to user's order history (if registered user)
     */
    private void addOrderToUserHistory(Order order) {
        // Skip for guest users
        if (order.getUserName().startsWith("GUEST-")) {
            return;
        }

        try {
            Optional<User> userOpt = userRepository.findByUsername(order.getUserName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.addOrderToHistory(order.getOrderId());
                userRepository.update(user);
                logger.debug("Added order {} to user {}", order.getOrderId(), order.getUserName());
            }
        } catch (Exception e) {
            logger.error("Failed to add order to user history: {}", e.getMessage());
            // Non-critical error, don't fail the entire operation
        }
    }

    // ==================== ORDER CANCELLATION & ROLLBACK ====================

    /**
     * Cancels a single order and restores inventory
     */
    public void cancelOrder(UUID orderId) {
        logger.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        try {
            // Update order status to CANCELED
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELED);

            // Restore inventory if order was paid
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED) {
                restoreInventory(order);
            }

            logger.info("Successfully cancelled order: {}", orderId);

        } catch (Exception e) {
            logger.error("Failed to cancel order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to cancel order: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels multiple orders (used for rollback)
     */
    public void cancelOrders(List<Order> orders) {
        logger.warn("Cancelling {} orders", orders.size());

        for (Order order : orders) {
            try {
                cancelOrder(order.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to cancel order {}: {}", order.getOrderId(), e.getMessage());
                // Continue with other orders
            }
        }
    }

    /**
     * Restores inventory for a cancelled order
     */
    private void restoreInventory(Order order) {
        logger.debug("Restoring inventory for cancelled order: {}", order.getOrderId());

        try {
            Store store = storeRepository.findById(order.getStoreId())
                    .orElseThrow(() -> new IllegalStateException("Store not found"));

            // Restore inventory quantities
            Map<UUID, Integer> items = order.getProductsMap();
            for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                UUID productId = entry.getKey();
                int quantity = entry.getValue();

                if (store.hasProduct(productId)) {
                    int currentQuantity = store.getProductQuantity(productId);
                    store.updateProductQuantity(productId, currentQuantity + quantity);
                }
            }

            storeRepository.save(store);
            logger.debug("Inventory restored for order: {}", order.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to restore inventory for order {}: {}", order.getOrderId(), e.getMessage());
            // Don't throw exception, as order is already cancelled
        }
    }

    // ==================== ORDER QUERIES ====================

    /**
     * Gets order by ID
     */
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Gets order status
     */
    public OrderStatus getOrderStatus(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getStatus)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    /**
     * Gets all orders for a user
     */
    public List<Order> getOrdersByUser(String username) {
        return orderRepository.findByUserName(username);
    }

    /**
     * Gets all orders for a store
     */
    public List<Order> getOrdersByStore(UUID storeId) {
        return orderRepository.findByStoreId(storeId);
    }

    // ==================== ORDER STATUS UPDATES ====================

    /**
     * Updates order status
     */
    public boolean updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);

        try {
            boolean updated = orderRepository.updateOrderStatus(orderId, newStatus);

            if (updated) {
                logger.info("Order {} status updated to {}", orderId, newStatus);
            } else {
                logger.warn("Failed to update order {} status to {}", orderId, newStatus);
            }

            return updated;
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Marks order as shipped with tracking info
     */
    public boolean markOrderAsShipped(UUID orderId, UUID trackingId) {
        logger.info("Marking order {} as shipped with tracking {}", orderId, trackingId);

        try {
            boolean statusUpdated = orderRepository.updateOrderStatus(orderId, OrderStatus.SHIPPED);
            boolean trackingSet = orderRepository.setDeliveryId(orderId, trackingId);

            return statusUpdated && trackingSet;
        } catch (Exception e) {
            logger.error("Error marking order as shipped: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Marks order as completed
     */
    public boolean markOrderAsCompleted(UUID orderId) {
        return updateOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Calculates total price for order items
     */
    private double calculateTotalPrice(Map<UUID, Integer> items) {
        logger.debug("Calculating total price for {} items", items.size());

        double total = 0.0;

        for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }

            Product product = productOpt.get();
            double itemTotal = product.getPrice() * quantity;
            total += itemTotal;

            logger.debug("Product {}: {} x {} = {}", product.getName(), product.getPrice(), quantity, itemTotal);
        }

        logger.debug("Total price calculated: {}", total);
        return total;
    }

    /**
     * Validates that all products in the order exist and are available
     */
    public boolean validateOrderProducts(Map<UUID, Integer> items) {
        for (UUID productId : items.keySet()) {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty() || !productOpt.get().isAvailable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if user can view order (owns the order or has admin privileges)
     */
    public boolean canUserViewOrder(String username, UUID orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        return order.getUserName().equals(username) || "admin".equals(username);
    }
}