package com.sadna_market.market.DomainLayer.Product;

import java.util.Date;
import java.util.UUID;

public class UserReview {
    private UUID userId;
    private UUID productId;
    private String reviewText;
    private Date date;
    private final int MAX_REVIEW_LENGTH = 300;
    public UserReview(UUID userId, UUID productId, String reviewText) {
        this.userId = userId;
        this.productId = productId;
        // enforce a maximum length for the review text - need to change/improve after we decide what to do with it
        this.reviewText = reviewText.substring(0, Math.min(reviewText.length(), MAX_REVIEW_LENGTH));
        this.date = new Date();
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
    public Date getDate() {
        return date;
    }
}
