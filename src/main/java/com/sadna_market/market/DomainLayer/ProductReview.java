package com.sadna_market.market.DomainLayer;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReview {
    @Getter private final UUID reviewId;
    @Getter private final String username;
    @Getter private final UUID productId;
    @Getter private final UUID storeId;
    @Getter private String reviewText;
    @Getter private final LocalDateTime timestamp;
    @Getter private boolean isEdited;
    @Getter private LocalDateTime lastEditTime;

    private static final int MAX_REVIEW_LENGTH = 500;

    /**
     * Constructor for a new product review
     */
    public ProductReview(String username, UUID productId, UUID storeId, String reviewText) {
        this.reviewId = UUID.randomUUID();
        this.username = username;
        this.productId = productId;
        this.storeId = storeId;
        this.reviewText = validateAndTruncateText(reviewText);
        this.timestamp = LocalDateTime.now();
        this.isEdited = false;
        this.lastEditTime = null;
    }

    /**
     * Update the review text
     */
    public void updateReview(String newReviewText) {
        this.reviewText = validateAndTruncateText(newReviewText);
        this.isEdited = true;
        this.lastEditTime = LocalDateTime.now();
    }

    private String validateAndTruncateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Review text cannot be empty");
        }

        return text.substring(0, Math.min(text.length(), MAX_REVIEW_LENGTH));
    }
}