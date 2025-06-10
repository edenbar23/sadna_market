package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor // Required by JPA
public class Order {
    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    // JPA SETTERS (needed for repository operations)
    @Setter
    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;

    @Setter
    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Setter
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();

    @Setter
    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Setter
    @Column(name = "final_price", nullable = false)
    private double finalPrice;

    @Setter
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Setter
    @Column(name = "transaction_id")
    private int transactionId;

    @Setter
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Setter
    @Column(name = "store_name", length = 200)
    private String storeName;

    @Setter
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Setter
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    // For thread-safe status updates (transient = not stored in DB)
    private final transient Object statusLock = new Object();

    public void setProducts(Map<UUID, Integer> products) {
        this.products = products != null ? products : new HashMap<>();
    }

    // KEEP ALL YOUR EXISTING CONSTRUCTORS AND METHODS
    // (Your service layer won't need to change!)

    /**
     * Constructor for creating new orders (backward compatibility)
     */
    public Order(UUID storeId, String userName, HashMap<UUID, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate,
                 OrderStatus status, int transactionId) {
        this.orderId = UUID.randomUUID();
        this.storeId = storeId;
        this.userName = userName;
        this.products = new HashMap<>(products); // Defensive copy
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.transactionId = transactionId;
        this.storeName = "Unknown Store";
        this.paymentMethod = "Unknown Payment Method";
        this.deliveryAddress = "Not specified";

        logger.info("Order created with ID: {}", orderId);
    }

    /**
     * Enhanced constructor with additional order details
     */
    public Order(UUID storeId, String userName, HashMap<UUID, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate,
                 OrderStatus status, int transactionId, String storeName,
                 String paymentMethod, String deliveryAddress) {
        this(storeId, userName, products, totalPrice, finalPrice, orderDate, status, transactionId);

        this.storeName = storeName != null ? storeName : "Unknown Store";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Unknown Payment Method";
        this.deliveryAddress = deliveryAddress != null ? deliveryAddress : "Not specified";

        logger.info("Order created with enhanced details - Store: {}, Payment: {}", storeName, paymentMethod);
    }

    /**
     * Constructor for repository reconstruction
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
        this.storeName = "Unknown Store";
        this.paymentMethod = "Unknown Payment Method";
        this.deliveryAddress = "Not specified";
    }

    /**
     * Full constructor for complete order reconstruction
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

    // KEEP ALL YOUR EXISTING METHODS EXACTLY THE SAME
    // (This ensures backward compatibility)

    /**
     * Gets products as map (same as original implementation)
     */
    public Map<UUID, Integer> getProductsMap() {
        return Collections.unmodifiableMap(products);
    }

    /**
     * Updates order status in a thread-safe manner
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
     * update Transaction ID for the order
     */
    public boolean updateTransactionId(int transactionId) {
        if (this.status != OrderStatus.PAID) {
            logger.warn("Cannot update transaction ID for order {} in status {}",
                    orderId, status);
            return false;
        }

        this.transactionId = transactionId;
        logger.info("Transaction ID updated for order {}: {}", orderId, transactionId);
        return true;
    }

    /**
     * Validates if a status transition is allowed
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
     * Checks if an order contains a specific product
     */
    public boolean containsProduct(UUID productId) {
        return products.containsKey(productId);
    }

    /**
     * Gets the quantity of a specific product in the order
     */
    public int getProductQuantity(UUID productId) {
        return products.getOrDefault(productId, 0);
    }

    /**
     * Calculates the discount applied to this order
     */
    public double getDiscountAmount() {
        return totalPrice - finalPrice;
    }

    /**
     * Checks if the order is in a terminal state
     */
    public boolean isTerminal() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELED;
    }

    /**
     * Checks if the order can still be canceled
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