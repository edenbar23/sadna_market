package com.sadna_market.market.ApplicationLayer;

import java.util.HashMap;
import java.util.UUID;

import com.sadna_market.market.DomainLayer.Cart;

public class CartDTO {
    public HashMap<UUID, HashMap<UUID,Integer>> shoppingBaskets; // dictionary of storeId , ShoppingBasketsDTO

    public CartDTO(Cart cart) {
        shoppingBaskets = new HashMap<>();
        cart.getShoppingBaskets().forEach((storeId, basket) -> {
            this.shoppingBaskets.put(storeId, basket.getProductsList());
        });
    }
}
