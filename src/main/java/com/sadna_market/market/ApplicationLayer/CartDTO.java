package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import java.util.UUID;

public class CartDTO {
    HashMap<UUID, HashMap<UUID,Integer>> shoppingBaskets; // dictionary of storeId , ShoppingBasketsDTO

    public CartDTO(Cart cart) {
        shoppingBaskets = new HashMap<>();
        cart.getShoppingBaskets().forEach((storeId, basket) -> {
            this.shoppingBaskets.put(storeId, basket.getProductsList());
        });
    }
}
