package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.ShoppingBasket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

public class ShoppingBasketDTO {
    @Getter
    private UUID storeId;
    @Getter
    private Map<UUID, Integer> products;
    @Getter
    private int totalQuantity;

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.products = new HashMap<>(basket.getProductsList());
        this.totalQuantity = basket.getTotalQuantity();
    }

    public ShoppingBasketDTO(UUID storeId, Map<UUID, Integer> products, int totalQuantity) {
        this.storeId = storeId;
        this.products = new HashMap<>(products);
        this.totalQuantity = totalQuantity;
    }

}