package com.sadna_market.market.ApplicationLayer;

import java.util.Date;
import java.util.UUID;

public class ProductReviewRequest {
    private UUID userId;
    private UUID productId;
    private String reviewText;
    private Date reviewDate;
    public ProductReviewRequest(UUID userId, UUID productId, String reviewText) {
        this.userId = userId;
        this.productId = productId;
        this.reviewText = reviewText;
        this.reviewDate = new Date();
    }
    public UUID getUserId() {
        return userId;
    }
    public UUID getProductId() {
        return productId;
    }
    public String getReviewText() {
        return reviewText;
    }
    public Date getReviewDate() {
        return reviewDate;
    }
}
