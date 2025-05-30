package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import java.util.Map;
import java.util.UUID;

public class StoreCartDTO {
    @Getter
    private UUID storeId;
    @Getter
    private String storeName;
    @Getter
    private Map<UUID, CartProductDTO> products;
    @Getter
    private int totalQuantity;
    @Getter
    private double totalPrice;

    public StoreCartDTO(UUID storeId, String storeName, Map<UUID, CartProductDTO> products,
                        int totalQuantity, double totalPrice) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.products = products;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
    }
}