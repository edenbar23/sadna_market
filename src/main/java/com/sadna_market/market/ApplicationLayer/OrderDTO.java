package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;

import
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class OrderDTO {
    private UUID orderId;
    private UUID storeId;
    private String userName;
    private Map<UUID, Integer> products;
    private double totalPrice;
    private double finalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String paymentId;
    private String deliveryId;

    public OrderDTO(Order order) {
        this.orderId = order.getOrderId();
        this.storeId = order.getStoreId();
        this.userName = order.getUserName();
        this.products = order.getProductsMap();
        this.totalPrice = order.getTotalPrice();
        this.finalPrice = order.getFinalPrice();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.paymentId = order.getPaymentId();
        this.deliveryId = order.getDeliveryId();
    }

    // Getters
    public UUID getOrderId() {
        return orderId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getUserName() {
        return userName;
    }

    public Map<UUID, Integer> getProducts() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }
}
