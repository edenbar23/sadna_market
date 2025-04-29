package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRateRequest {
    private UUID productId;
    private UUID userId;
    private int rate;
    public ProductRateRequest(UUID productId, UUID userId, int rate) {
        this.productId = productId;
        this.userId = userId;
        this.rate = rate;
    }
    public UUID getProductId() {
        return productId;
    }
    public void setProductId(UUID productId) {
        this.productId = productId;
    }
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public int getRate() {
        return rate;
    }
    public void setRate(int rate) {
        this.rate = rate;
    }
}

