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
    private UUID paymentId;
    @Getter
    private UUID deliveryId;

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

    public OrderDTO(UUID orderId, UUID storeId, String userName, Map<UUID, Integer> products,
            double totalPrice, double finalPrice, LocalDateTime orderDate, OrderStatus status,
            UUID paymentId, UUID deliveryId) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.userName = userName;
        this.products = products;
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.paymentId = paymentId;
        this.deliveryId = deliveryId;
    }

}
