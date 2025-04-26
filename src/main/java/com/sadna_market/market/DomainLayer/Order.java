package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;


public class Order {
    private static final Logger logger = LoggerFactory.getLogger(Order.class);
    private static final AtomicLong ORDER_ID_GENERATOR = new AtomicLong(1);
    
    @Getter
    private Long orderId;

    @Getter
    private Long storeId;

    @Getter
    private String userName;

    private Map<Long, Integer> products; // productId -> quantity

    @Getter
    private double totalPrice;

    @Getter
    private double finalPrice; // final price after discounts

    @Getter
    private LocalDateTime orderDate;

    @Getter @Setter
    private OrderStatus status; // e.g., "Pending", "Completed", "Cancelled"

    @Getter
    private String paymentId;

    @Getter
    private String deliveryId;

    @Getter
    private String DeliveryId; 

    private final Object statusLock = new Object();


    public Order(Long storeId, String userName, Map<Long, Integer> products, double totalPrice,
            double finalPrice,LocalDateTime orderDate, OrderStatus status, String paymentId) {
        logger.info("Creating new order for user: {} in store: {}", userName, storeId);
        this.orderId = ORDER_ID_GENERATOR.getAndIncrement();
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products);
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice; 
        this.orderDate = orderDate;
        this.status = status;
        this.paymentId = paymentId;
        this.deliveryId = null;
        logger.info("Order created with ID: {}", orderId);


    }

    /**
     * Constructor for reconstructing an order from the repository.
     */
    public Order(Long orderId, Long storeId, String userName, Map<Long, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate,
                 OrderStatus status, String paymentId, String deliveryId) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products);
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
        
        // Update ID_GENERATOR if needed to prevent ID collisions
        if (orderId >= ORDER_ID_GENERATOR.get()) {
            ORDER_ID_GENERATOR.set(orderId + 1);
        }
    }
    /**
     * Updates the status of the order in a thread-safe manner
     * 
     * @param newStatus The new status to set
     * @return true if the status was updated, false if the transition is not allowed
     */
    public boolean updateStatus(OrderStatus newStatus) {
        synchronized (statusLock) {
            logger.info("Attempting to update order {} status from {} to {}", 
                       orderId, status, newStatus);
            
            // Check if the status transition is valid
            if (!isValidStatusTransition(status, newStatus)) {
                logger.warn("Invalid status transition for order {}: {} to {}", 
                           orderId, status, newStatus);
                return false;
            }
            
            this.status = newStatus;
            logger.info("Order {} status updated to {}", orderId, newStatus);
            return true;
        }
    }
    
    /**
     * Validates if a status transition is allowed
     * 
     * @param currentStatus The current status
     * @param newStatus The new status
     * @return true if the transition is valid
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Implement business rules for valid status transitions
        // For example:
        switch (currentStatus) {
            case PENDING:
                // From PENDING, can move to PAID or CANCELED
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELED;
            case PAID:
                // From PAID, can move to SHIPPED or CANCELED
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED:
                // From SHIPPED, can only move to COMPLETED
                return newStatus == OrderStatus.COMPLETED;
            case COMPLETED:
            case CANCELED:
                // Terminal states, can't transition away from these
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Sets the delivery tracking ID for the order
     * 
     * @param deliveryId The delivery tracking ID
     * @return true if the delivery ID was set
     */
    public boolean setDeliveryTracking(String deliveryId) {
        if (this.status != OrderStatus.PAID) {
            logger.warn("Cannot set delivery tracking for order {} in status {}", 
                       orderId, status);
            return false;
        }
        
        this.deliveryId = deliveryId;
        logger.info("Delivery tracking set for order {}: {}", orderId, deliveryId);
        return true;
    }
    
    /**
     * Gets an unmodifiable view of the products in this order
     * 
     * @return Unmodifiable map of product IDs to quantities
     */
    public Map<Long, Integer> getProductsMap() {
        return Collections.unmodifiableMap(products);
    }
    
    /**
     * Checks if an order contains a specific product
     * 
     * @param productId The product ID to check
     * @return true if the order contains the product
     */
    public boolean containsProduct(Long productId) {
        return products.containsKey(productId);
    }
    
    /**
     * Gets the quantity of a specific product in the order
     * 
     * @param productId The product ID to check
     * @return The quantity ordered, or 0 if the product is not in the order
     */
    public int getProductQuantity(Long productId) {
        return products.getOrDefault(productId, 0);
    }
    
    /**
     * Calculates the discount applied to this order
     * 
     * @return The amount of discount applied
     */
    public double getDiscountAmount() {
        return totalPrice - finalPrice;
    }
    
    /**
     * Checks if the order is in a terminal state
     * 
     * @return true if the order is in a terminal state (COMPLETED or CANCELED)
     */
    public boolean isTerminal() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELED;
    }
    
    /**
     * Checks if the order can still be canceled
     * 
     * @return true if the order can be canceled
     */
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }
    
    @Override
    public String toString() {
        return String.format("Order[id=%d, store=%d, user=%s, status=%s, totalPrice=%.2f, finalPrice=%.2f, date=%s]",
                orderId, storeId, userName, status, totalPrice, finalPrice, orderDate);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Order other = (Order) obj;
        return orderId.equals(other.orderId);
    }
    
    @Override
    public int hashCode() {
        return orderId.hashCode();
    }
}

