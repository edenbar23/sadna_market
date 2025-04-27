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
}

