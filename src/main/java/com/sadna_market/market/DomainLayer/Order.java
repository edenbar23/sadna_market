package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;


public class Order {
    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    @Getter
    private UUID orderId;

    @Getter
    private UUID storeId;

    @Getter
    private String userName;

    private HashMap<UUID, Integer> products; // productId -> quantity

    @Getter
    private double totalPrice;

    @Getter
    private double finalPrice; // final price after discounts

    @Getter
    private LocalDateTime orderDate;

    @Getter @Setter
    private OrderStatus status; // e.g., "Pending", "Completed", "Cancelled"

    @Getter
    private int transactionId;

    @Getter
    private UUID deliveryId;

    // NEW FIELDS for better order tracking
    @Getter @Setter
    private String storeName;

    @Getter @Setter
    private String paymentMethod;

    @Getter @Setter
    private String deliveryAddress;

    private final Object statusLock = new Object();

    // Original constructor - maintain backward compatibility
    public Order(UUID storeId, String userName, HashMap<UUID, Integer> products, double totalPrice,
                 double finalPrice, LocalDateTime orderDate, OrderStatus status, int transactionId) {
        logger.info("Creating new order for user: {} in store: {}", userName, storeId);
        this.orderId = UUID.randomUUID();
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products);
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.transactionId = transactionId;
        this.deliveryId = null;

        // Initialize new fields with defaults
        this.storeName = "Unknown Store";
        this.paymentMethod = "Unknown Payment Method";
        this.deliveryAddress = "Not specified";

        logger.info("Order created with ID: {}", orderId);
    }

    // Enhanced constructor with additional order details
    public Order(UUID storeId, String userName, HashMap<UUID, Integer> products, double totalPrice,
                 double finalPrice, LocalDateTime orderDate, OrderStatus status, int transactionId,
                 String storeName, String paymentMethod, String deliveryAddress) {
        this(storeId, userName, products, totalPrice, finalPrice, orderDate, status, transactionId);

        // Set the additional fields
        this.storeName = storeName != null ? storeName : "Unknown Store";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Unknown Payment Method";
        this.deliveryAddress = deliveryAddress != null ? deliveryAddress : "Not specified";

        logger.info("Order created with enhanced details - Store: {}, Payment: {}", storeName, paymentMethod);
    }

    /**
     * Constructor for reconstructing an order from the repository.
     */
    public Order(UUID orderId, UUID storeId, String userName, HashMap<UUID, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate,
                 OrderStatus status, int transactionId, UUID deliveryId) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products);
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.transactionId = transactionId;
        this.deliveryId = deliveryId;

        // Initialize new fields with defaults for backward compatibility
        this.storeName = "Unknown Store";
        this.paymentMethod = "Unknown Payment Method";
        this.deliveryAddress = "Not specified";
    }

    /**
     * Enhanced constructor for full order reconstruction from repository
     */
    public Order(UUID orderId, UUID storeId, String userName, HashMap<UUID, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate,
                 OrderStatus status, int transactionId, UUID deliveryId,
                 String storeName, String paymentMethod, String deliveryAddress) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products);
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.transactionId = transactionId;
        this.deliveryId = deliveryId;
        this.storeName = storeName != null ? storeName : "Unknown Store";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Unknown Payment Method";
        this.deliveryAddress = deliveryAddress != null ? deliveryAddress : "Not specified";
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
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELED;
            case PAID:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED:
                return newStatus == OrderStatus.COMPLETED;
            case COMPLETED:
            case CANCELED:
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
    public boolean setDeliveryTracking(UUID deliveryId) {
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
    public Map<UUID, Integer> getProductsMap() {
        return Collections.unmodifiableMap(products);
    }

    /**
     * Checks if an order contains a specific product
     *
     * @param productId The product ID to check
     * @return true if the order contains the product
     */
    public boolean containsProduct(UUID productId) {
        return products.containsKey(productId);
    }

    /**
     * Gets the quantity of a specific product in the order
     *
     * @param productId The product ID to check
     * @return The quantity ordered, or 0 if the product is not in the order
     */
    public int getProductQuantity(UUID productId) {
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
        return String.format("Order[id=%s, store=%s, user=%s, status=%s, totalPrice=%.2f, finalPrice=%.2f, date=%s]",
                orderId, storeName, userName, status, totalPrice, finalPrice, orderDate);
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