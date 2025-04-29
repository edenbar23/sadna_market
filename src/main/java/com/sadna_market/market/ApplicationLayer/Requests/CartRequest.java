package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//this is the cart of the user as a guest, he will send it as a request to the api
@Data
@NoArgsConstructor
public class CartRequest {
    private Map<UUID, Map<UUID, Integer>> baskets = new HashMap<>();

    public CartRequest addItem(UUID storeId, UUID productId, int quantity) {
        baskets.computeIfAbsent(storeId, k -> new HashMap<>())
                .merge(productId, quantity, Integer::sum);
        return this;
    }

    public Map<UUID, Map<UUID, Integer>> getBaskets() {
        return this.baskets;
    }

    public void addToCartRequest(UUID storeId, UUID productId, int quantity) {
        if (baskets.containsKey(storeId)) {
            Map<UUID, Integer> storeBasket = baskets.get(storeId);
            storeBasket.put(productId, storeBasket.getOrDefault(productId, 0) + quantity);
        } else {
            Map<UUID, Integer> newBasket = new HashMap<>();
            newBasket.put(productId, quantity);
            baskets.put(storeId, newBasket);
        }
    }

    public void updateItem(UUID storeId, UUID productId, int quantity) {
        if (baskets.containsKey(storeId)) {
            Map<UUID, Integer> storeBasket = baskets.get(storeId);
            if (storeBasket.containsKey(productId)) {
                if (quantity <= 0) {
                    storeBasket.remove(productId);
                } else {
                    storeBasket.put(productId, quantity);
                }
            }
        }
    }

    public void removeFromCartRequest(UUID storeId, UUID productId) {
        if (baskets.containsKey(storeId)) {
            Map<UUID, Integer> storeBasket = baskets.get(storeId);
            storeBasket.remove(productId);
            if (storeBasket.isEmpty()) {
                baskets.remove(storeId);
            }
        }
    }
}

