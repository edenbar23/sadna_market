package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when an order has been successfully processed
 */
@Getter
public class OrderProcessedEvent extends DomainEvent {
    private final String username;
    private final UUID orderId;
    private final UUID storeId;

    public OrderProcessedEvent(String username, UUID orderId, UUID storeId) {
        super();
        this.username = username;
        this.orderId = orderId;
        this.storeId = storeId;
    }

}