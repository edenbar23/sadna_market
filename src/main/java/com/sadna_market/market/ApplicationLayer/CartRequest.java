package com.sadna_market.market.ApplicationLayer;

import java.util.HashMap;
import java.util.UUID;

//this is the cart of the user as a guest, he will send it as a request to the api
public class CartRequest {
    private HashMap<UUID, HashMap<UUID, Integer>> baskets; //storeId -> productId -> quantity
    public CartRequest() {
        baskets = new HashMap<>();
    }
    public CartRequest addToCartRequest(UUID storeId,UUID productId, int quantity) {
        if (baskets.containsKey(storeId)) {
            HashMap<UUID, Integer> storeBasket = baskets.get(storeId);
            if (storeBasket.containsKey(productId)) {
                storeBasket.put(productId, storeBasket.get(productId) + quantity);
            } else {
                storeBasket.put(productId, quantity);
            }
        } else {
            HashMap<UUID, Integer> storeBasket = new HashMap<>();
            storeBasket.put(productId, quantity);
            baskets.put(storeId, storeBasket);
        }
        return this;
    }
    //this is the cart of the user as a guest, he will send it as a request to the api
}
