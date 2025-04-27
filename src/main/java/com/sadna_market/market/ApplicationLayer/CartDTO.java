package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.Cart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CartDTO {
    private Map<UUID, ShoppingBasketDTO> baskets;
    private int totalItems;

    public CartDTO(Cart cart) {
        this.baskets = new HashMap<>();
        cart.getShoppingBaskets().forEach((storeId, basket) -> {
            baskets.put(storeId, new ShoppingBasketDTO(basket));
        });
        this.totalItems = cart.getTotalItems();
    }

    public Map<UUID, ShoppingBasketDTO> getBaskets() {
        return baskets;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public boolean isEmpty() {
        return baskets.isEmpty();
    }
}
