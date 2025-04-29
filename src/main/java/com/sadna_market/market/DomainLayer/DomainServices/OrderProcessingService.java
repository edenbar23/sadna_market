package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain service responsible for processing orders and managing the purchase flow.
 * Handles the business logic of converting shopping carts into orders and updating inventory.
 */
public class OrderProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    // Singleton instance
    private static OrderProcessingService instance;

    private final IStoreRepository storeRepository;
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private RepositoryConfiguration RC;

    private PaymentService paymentService = new PaymentService();
    //private SupplyService supplyService = new SupplyService();

    /**
     * Private constructor to prevent instantiation from outside
     */
    private OrderProcessingService(RepositoryConfiguration RC) {
        this.RC=RC;
        this.storeRepository = RC.storeRepository();
        this.orderRepository = RC.orderRepository();
        this.userRepository = RC.userRepository();
        this.productRepository = RC.productRepository();;
    }

    /**
     * Get the singleton instance of OrderProcessingService.
     * If no instance exists, throws an exception as repositories must be provided.
     *
     * @return the singleton instance
     * @throws IllegalStateException if instance hasn't been initialized
     */
    public static synchronized OrderProcessingService getInstance(RepositoryConfiguration RC) {
        if (instance == null) {
            instance = new OrderProcessingService(RC);
        }
        return instance;
    }


    /**
     * Process a purchase from a user's cart.
     * Creates orders for each store and updates inventory.
     *
     * @param username The username of the purchaser
     * @param cart The cart containing items to purchase
     * @return List of created orders
     * @throws IllegalArgumentException if user or products are not found
     * @throws IllegalStateException if inventory validation fails
     */
    public List<Order> processPurchase(String username, Cart cart,PaymentMethod paymentMethod) {
        logger.info("Processing purchase for user: {}", username);

        // Validate user exists
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if cart is empty
        if (cart.getShoppingBaskets().isEmpty()) {
            logger.warn("Cannot process empty cart for user: {}", username);
            throw new IllegalStateException("Cannot process empty cart");
        }

        List<Order> orders = new ArrayList<>();

        // Process each shopping basket (per store)
        for (Map.Entry<UUID, ShoppingBasket> entry : cart.getShoppingBaskets().entrySet()) {
            UUID storeId = entry.getKey();
            ShoppingBasket basket = entry.getValue();

            try {
                Order order = processStoreBasket(user, storeId, basket,paymentMethod);
                orders.add(order);
                logger.info("Successfully processed order {} for store {}", order.getOrderId(), storeId);
            } catch (Exception e) {
                logger.error("Failed to process basket for store {}: {}", storeId, e.getMessage());
                // Rollback any successful orders
                rollbackOrders(orders);
                //rollback(orders,paymentMethod);
                throw new RuntimeException("Purchase failed: " + e.getMessage(), e);
            }
        }

        // Clear the user's cart after successful purchase
        clearUserCart(user);

        //supplyService.supplyOrders(orders);
        logger.info("Successfully processed {} orders for user {}", orders.size(), username);
        return orders;
    }

    /**
     * Process purchase for a guest user
     *
     * @param cart The cart containing items to purchase
     * @return List of created orders
     */
    public List<Order> processGuestPurchase(Cart cart,PaymentMethod paymentMethod) {
        logger.info("Processing purchase for guest: {}");

        // For guests, we use the guest ID as the "username" in orders
        ;

        // Check if cart is empty
        if (cart.getShoppingBaskets().isEmpty()) {
            logger.warn("Cannot process empty cart for guest: {}");
            throw new IllegalStateException("Cannot process empty cart");
        }

        List<Order> orders = new ArrayList<>();

        // Process each shopping basket (per store)
        for (Map.Entry<UUID, ShoppingBasket> entry : cart.getShoppingBaskets().entrySet()) {
            UUID storeId = entry.getKey();
            ShoppingBasket basket = entry.getValue();

            try {
                Order order = processGuestStoreBasket(storeId, basket, paymentMethod);
                orders.add(order);
                logger.info("Successfully processed order {} for store {}", order.getOrderId(), storeId);
            } catch (Exception e) {
                logger.error("Failed to process basket for store {}: {}", storeId, e.getMessage());
                // Rollback any successful orders
                rollbackOrders(orders);
                rollbackPayment(orders);
                throw new RuntimeException("Purchase failed: " + e.getMessage(), e);
            }
        }

        logger.info("Successfully processed {} orders for guest {}", orders.size());
        //should return empty CartRequest object
        return orders;
    }

    private void rollbackPayment(List<Order> orders) {
        logger.warn("Rolling back payment for {} orders", orders.size());

        for (Order order : orders) {
            try {
                // Simulate payment rollback
                UUID paymentId = order.getPaymentId();
                if (paymentId != null) {
                    //paymentService.refund(paymentId);
                    logger.info("Successfully rolled back payment {}", paymentId);
                }
            } catch (Exception e) {
                logger.error("Failed to rollback payment {}: {}", order.getPaymentId(), e.getMessage());
            }
        }
    }

    private Order processStoreBasket(User user, UUID storeId, ShoppingBasket basket,PaymentMethod paymentMethod) {
        logger.debug("Processing basket for store: {} and user: {}", storeId, user.getUserName());
        return processBasketCommon(user.getUserName(), storeId, basket, paymentMethod);
    }

    private Order processGuestStoreBasket(UUID storeId, ShoppingBasket basket,PaymentMethod paymentMethod) {
        logger.debug("Processing basket for store: {} and guest: {}", storeId);
        return processGuestBasketCommon(storeId, basket, paymentMethod);
    }

    private Order processBasketCommon(String username, UUID storeId, ShoppingBasket basket,PaymentMethod paymentMethod) {
        // Get the store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        // Validate store is active
        if (!store.isActive()) {
            throw new IllegalStateException("Cannot purchase from inactive store: " + store.getName());
        }

        // Get basket items
        Map<UUID, Integer> items = basket.getProductsList();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot process empty basket");
        }

        // Validate inventory
        Set<String> errors = store.checkCart(items);
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            logger.error("Inventory validation failed: {}", errorMessage);
            throw new IllegalStateException("Inventory validation failed: " + errorMessage);
        }

        // Calculate total price (in Version 1, no discounts)
        double totalPrice = calculateTotalPrice(items);

        // Create order with PENDING status
        UUID orderId = orderRepository.createOrder(
                storeId,
                username,
                items,
                totalPrice,
                totalPrice, // No discounts in Version 1
                LocalDateTime.now(),
                OrderStatus.PENDING,
                null // Payment ID will be set later
        );

        // Simulate payment processing (in Version 1, assume payment always succeeds)
        UUID paymentId = operatePayment(username, totalPrice, paymentMethod);

        // Update order with payment ID and status
        orderRepository.setDeliveryId(orderId, paymentId);
        orderRepository.updateOrderStatus(orderId, OrderStatus.PAID);

        // Update inventory after successful payment
        Set<String> updateErrors = store.updateStockAfterPurchase(items);
        if (!updateErrors.isEmpty()) {
            // This shouldn't happen since we validated inventory, but handle it just in case
            logger.error("Failed to update inventory: {}", String.join(", ", updateErrors));
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELED);
            throw new IllegalStateException("Failed to update inventory after payment");
        }

        // Save the updated store
        storeRepository.save(store);

        // Add order to store's order list
        store.addOrder(orderId);
        storeRepository.save(store);

        // Return the created order
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order creation failed"));
    }

    private Order processGuestBasketCommon(UUID storeId, ShoppingBasket basket,PaymentMethod paymentMethod) {
        // Get the store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        // Validate store is active
        if (!store.isActive()) {
            throw new IllegalStateException("Cannot purchase from inactive store: " + store.getName());
        }

        // Get basket items
        Map<UUID, Integer> items = basket.getProductsList();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot process empty basket");
        }

        // Validate inventory
        Set<String> errors = store.checkCart(items);
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            logger.error("Inventory validation failed: {}", errorMessage);
            throw new IllegalStateException("Inventory validation failed: " + errorMessage);
        }

        // Calculate total price (in Version 1, no discounts)
        double totalPrice = calculateTotalPrice(items);
        String guestId = UUID.randomUUID().toString(); // Use a unique guest ID
        // Create order with PENDING status
        UUID orderId = orderRepository.createOrder(
                storeId,
                guestId,
                items,
                totalPrice,
                totalPrice, // No discounts in Version 1
                LocalDateTime.now(),
                OrderStatus.PENDING,
                null // Payment ID will be set later
        );

        // Simulate payment processing (in Version 1, assume payment always succeeds)
        UUID paymentId = operatePayment(guestId, totalPrice, paymentMethod);

        // Update order with payment ID and status
        orderRepository.setDeliveryId(orderId, paymentId);
        orderRepository.updateOrderStatus(orderId, OrderStatus.PAID);

        // Update inventory after successful payment
        Set<String> updateErrors = store.updateStockAfterPurchase(items);
        if (!updateErrors.isEmpty()) {
            // This shouldn't happen since we validated inventory, but handle it just in case
            logger.error("Failed to update inventory: {}", String.join(", ", updateErrors));
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELED);
            throw new IllegalStateException("Failed to update inventory after payment");
        }

        // Save the updated store
        storeRepository.save(store);

        // Add order to store's order list
        store.addOrder(orderId);
        storeRepository.save(store);

        // Return the created order
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order creation failed"));
    }


    private double calculateTotalPrice(Map<UUID, Integer> items) {
        logger.debug("Calculating total price for {} items", items.size());

        double total = 0.0;

        for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            double itemTotal = product.getPrice() * quantity;
            total += itemTotal;

            logger.debug("Product {}: {} x {} = {}", product.getName(), product.getPrice(), quantity, itemTotal);
        }

        logger.debug("Total price calculated: {}", total);
        return total;
    }

    private UUID operatePayment(String username, double amount,PaymentMethod paymentMethod) {
        UUID paymentId = UUID.randomUUID();
        if(!paymentService.pay(paymentMethod, amount)) {
            logger.error("Payment failed for user {} amount {}", username, amount);
            throw new IllegalStateException("Payment failed");
        }
        logger.info("payment successful for user {} amount {} with ID {}",
                username, amount, paymentId);
        return paymentId;
    }

    private void rollbackOrders(List<Order> orders) {
        logger.warn("Rolling back {} orders", orders.size());

        for (Order order : orders) {
            try {
                orderRepository.updateOrderStatus(order.getOrderId(), OrderStatus.CANCELED);

                // Restore inventory for this order
                Store store = storeRepository.findById(order.getStoreId())
                        .orElse(null);

                if (store != null) {
                    Map<UUID, Integer> items = order.getProductsMap();
                    for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                        UUID productId = entry.getKey();
                        int quantity = entry.getValue();

                        // Add back the quantity to inventory
                        int currentQuantity = store.getProductQuantity(productId);
                        store.updateProductQuantity(productId, currentQuantity + quantity);
                    }
                    storeRepository.save(store);
                }

                logger.info("Successfully rolled back order {}", order.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to rollback order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
    }

    private void clearUserCart(User user) {
        // Clear the user's cart after successful purchase
        logger.info("Clearing cart for user: {}", user.getUserName());
        user.clearCart();
        userRepository.update(user);
    }
}