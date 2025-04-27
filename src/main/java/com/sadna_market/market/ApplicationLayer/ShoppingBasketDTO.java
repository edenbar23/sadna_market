package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.ShoppingBasket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingBasketDTO {
    private UUID storeId;
    private Map<UUID, Integer> products;
    private int totalQuantity;

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.products = new HashMap<>(basket.getProductsList());
        this.totalQuantity = basket.getTotalQuantity();
    }

    public UUID getStoreId() {
        return storeId;
    }

    public Map<UUID, Integer> getProducts() {
        return products;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }
}