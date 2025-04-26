package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;


//represents a request to rate a product
public class ProductRateRequest {
    private UUID userId;
    private UUID productId;
    private int rate;
    public ProductRateRequest(UUID userId, UUID productId, int rate) {
        this.userId = userId;
        this.productId = productId;
        this.rate = rate;
    }
    public UUID getUserId() {
        return userId;
    }
    public UUID getProductId() {
        return productId;
    }
    public int getRate() {
        return rate;
    }
}
