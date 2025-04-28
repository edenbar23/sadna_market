package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.Cart;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

public class CartDTO {
    @Getter
    private Map<UUID, ShoppingBasketDTO> baskets;
    @Getter
    private int totalItems;

    public CartDTO(Cart cart) {
        this.baskets = new HashMap<>();
        cart.getShoppingBaskets().forEach((storeId, basket) -> {
            baskets.put(storeId, new ShoppingBasketDTO(basket));
        });
        this.totalItems = cart.getTotalItems();
    }

    public CartDTO(Map<UUID, ShoppingBasketDTO> baskets, int totalItems) {
        this.baskets = new HashMap<>(baskets);
        this.totalItems = totalItems;
    }
}

