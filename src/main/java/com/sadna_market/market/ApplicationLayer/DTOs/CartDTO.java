package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import java.util.Map;
import java.util.UUID;

public class CartDTO {
    @Getter
    private Map<UUID, StoreCartDTO> baskets;
    @Getter
    private int totalItems;
    @Getter
    private double totalPrice;

    public CartDTO(Map<UUID, StoreCartDTO> baskets, int totalItems, double totalPrice) {
        this.baskets = baskets;
        this.totalItems = totalItems;
        this.totalPrice = totalPrice;
    }
}