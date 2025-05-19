package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    private final IStoreRepository storeRepository;
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private final PaymentService paymentService;

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
        this.paymentService = new PaymentService(); // This would ideally be injected too

        logger.info("OrderProcessingService initialized");
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Subscribe to checkout events
        DomainEventPublisher.subscribe(CheckoutInitiatedEvent.class, this::handleCheckoutInitiated);
        logger.info("OrderProcessingService subscribed to events");
    }

    /**
     * Event handler for CheckoutInitiatedEvent
     */
    private void handleCheckoutInitiated(CheckoutInitiatedEvent event) {
        logger.info("Handling checkout event for {}", event.isGuest() ? "guest" : event.getUsername());

        try {
            List<Order> orders;
            if (event.isGuest()) {
                orders = processGuestPurchase(event.getCart(), event.getPaymentMethod());
            } else {
                orders = processPurchase(event.getUsername(), event.getCart(), event.getPaymentMethod());

                // Clear the user's cart if registered user
                User user = userRepository.findByUsername(event.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                clearUserCart(user);
            }

            // Publish event for each processed order
            for (Order order : orders) {
                DomainEventPublisher.publish(
                        new OrderProcessedEvent(
                                order.getUserName(),
                                order.getOrderId(),
                                order.getStoreId()
                        )
                );
            }
        } catch (Exception e) {
            logger.error("Error processing checkout: {}", e.getMessage(), e);
            // Could publish a CheckoutFailedEvent here
        }
    }

    public List<Order> processPurchase(String username, Cart cart, PaymentMethod paymentMethod) {
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
                Order order = processStoreBasket(user, storeId, basket, paymentMethod);
                orders.add(order);
                logger.info("Successfully processed order {} for store {}", order.getOrderId(), storeId);
            } catch (Exception e) {
                logger.error("Failed to process basket for store {}: {}", storeId, e.getMessage());
                // Rollback any successful orders
                rollbackOrders(orders);
                logger.info("Rolled back orders successfully");
                rollbackPayment(orders);
                logger.info("Refunded orders successfully");
                throw new RuntimeException("Purchase failed: " + e.getMessage(), e);
            }
        }

        logger.info("Successfully processed {} orders for user {}", orders.size(), username);
        return orders;
    }

    public List<Order> processGuestPurchase(Cart cart, PaymentMethod paymentMethod) {
        logger.info("Processing purchase for guest");

        // Check if cart is empty
        if (cart.getShoppingBaskets().isEmpty()) {
            logger.warn("Cannot process empty cart for guest");
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
                logger.info("Rolled back orders successfully");
                rollbackPayment(orders);
                logger.info("Refunded orders successfully");
                throw new RuntimeException("Purchase failed: " + e.getMessage(), e);
            }
        }

        logger.info("Successfully processed {} orders for guest", orders.size());
        return orders;
    }

    private void rollbackPayment(List<Order> orders) {
        logger.warn("Rolling back payment for {} orders", orders.size());

        for (Order order : orders) {
            try {
                UUID paymentId = order.getPaymentId();
                if (paymentId != null) {
                    paymentService.refund(paymentId);
                    logger.info("Successfully rolled back payment {}", paymentId);
                }
            } catch (Exception e) {
                logger.error("Failed to rollback payment {}: {}", order.getPaymentId(), e.getMessage());
            }
        }
    }

    private Order processStoreBasket(User user, UUID storeId, ShoppingBasket basket, PaymentMethod paymentMethod) {
        logger.debug("Processing basket for store: {} and user: {}", storeId, user.getUserName());
        return processBasketCommon(user.getUserName(), storeId, basket, paymentMethod);
    }

    private Order processGuestStoreBasket(UUID storeId, ShoppingBasket basket, PaymentMethod paymentMethod) {
        logger.debug("Processing basket for store: {} and guest", storeId);
        return processGuestBasketCommon(storeId, basket, paymentMethod);
    }

    private Order processBasketCommon(String username, UUID storeId, ShoppingBasket basket, PaymentMethod paymentMethod) {
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

    private Order processGuestBasketCommon(UUID storeId, ShoppingBasket basket, PaymentMethod paymentMethod) {
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

        //Payment processing
        UUID paymentId = operatePayment(guestId, totalPrice, paymentMethod);
        //throws error if failed

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

    private UUID operatePayment(String username, double amount, PaymentMethod paymentMethod) {
        UUID paymentId = UUID.randomUUID();
        if (!paymentService.pay(paymentMethod, amount)) {
            logger.error("Payment failed for user {} amount {}", username, amount);
            throw new IllegalStateException("Payment failed");
        }
        logger.info("Payment successful for user {} amount {} with ID {}", username, amount, paymentId);
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

    void clearUserCart(User user) {
        // Clear the user's cart after successful purchase
        logger.info("Clearing cart for user: {}", user.getUserName());
        user.clearCart();
        userRepository.update(user);
    }

}