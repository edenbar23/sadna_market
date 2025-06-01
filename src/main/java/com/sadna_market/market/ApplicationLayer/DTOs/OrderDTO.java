package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class OrderDTO {
    @Getter
    private UUID orderId;
    @Getter
    private UUID storeId;
    @Getter
    private String userName;
    @Getter
    private Map<UUID, Integer> products;
    @Getter
    private double totalPrice;
    @Getter
    private double finalPrice;
    @Getter
    private LocalDateTime orderDate;
    @Getter
    private OrderStatus status;
    @Getter
    private int transactionId;
    @Getter
    private UUID deliveryId;

    // NEW FIELDS - Add the missing order details
    @Getter
    private String storeName;
    @Getter
    private String paymentMethod;
    @Getter
    private String deliveryAddress;

    public OrderDTO(Order order) {
        this.orderId = order.getOrderId();
        this.storeId = order.getStoreId();
        this.userName = order.getUserName();
        this.products = order.getProductsMap();
        this.totalPrice = order.getTotalPrice();
        this.finalPrice = order.getFinalPrice();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.transactionId = order.getTransactionId();
        this.deliveryId = order.getDeliveryId();
        this.storeName = order.getStoreName();
        this.paymentMethod = order.getPaymentMethod();
        this.deliveryAddress = order.getDeliveryAddress();
    }

    public OrderDTO(UUID orderId, UUID storeId, String userName, Map<UUID, Integer> products,
                    double totalPrice, double finalPrice, LocalDateTime orderDate, OrderStatus status,
                    int transactionId, UUID deliveryId) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = products;
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

    public OrderDTO(UUID orderId, UUID storeId, String userName, Map<UUID, Integer> products,
                    double totalPrice, double finalPrice, LocalDateTime orderDate, OrderStatus status,
                    int transactionId, UUID deliveryId, String storeName, String paymentMethod,
                    String deliveryAddress) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = products;
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
}