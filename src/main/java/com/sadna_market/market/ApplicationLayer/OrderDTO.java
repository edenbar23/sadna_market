package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

public class OrderDTO {
    private final UUID orderID;
    public OrderDTO(UUID id) {
        this.orderID = id;
    }
    public UUID getOrderID() {
        return orderID;
    }
}
