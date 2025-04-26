package com.sadna_market.market.DomainLayer.Product;
import java.util.UUID;

// represents a user's rating of a product

public class UserRate {
    private UUID userId;
    private UUID productId;
    private int ratingValue;


    public UserRate(UUID userId, UUID productId, int ratingValue) {
        this.userId = userId;
        this.productId = productId;
        this.ratingValue = ratingValue;
    }

    public UUID getUserId() {
        return userId;
    }
    public UUID getProductId() {
        return productId;
    }
    public int getRatingValue() {
        return ratingValue;
    }
    public void changeRatingValue(int ratingValue) {
        this.ratingValue = ratingValue;
    }
}
