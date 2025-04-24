package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.Setter;

public class Order {
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

    public Order(Long storeId, String userName, Map<Long, Integer> products, double totalPrice,
            double finalPrice,LocalDateTime orderDate, OrderStatus status, String paymentId) {
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
}

enum OrderStatus {
    PENDING,    // Order created but not paid
    PAID,       // Payment received but not shipped
    SHIPPED,    // Order has been shipped
    COMPLETED,  // Order has been delivered
    CANCELED    // Order was canceled
}
