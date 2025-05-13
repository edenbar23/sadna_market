package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductRating implements IRating {
    @Getter private final UUID ratingId;
    @Getter private final UUID userId;
    @Getter private final String username;
    @Getter private final UUID productId;
    @Getter private int ratingValue;
    @Getter private String comment;
    @Getter private LocalDateTime timestamp;
    @Getter private boolean updated;

    /**
     * Constructor for a new product rating
     */
    public ProductRating(UUID userId, String username, UUID productId, int ratingValue, String comment) {
        this.ratingId = UUID.randomUUID();
        this.userId = userId;
        this.username = username;
        this.productId = productId;
        this.ratingValue = validateRating(ratingValue);
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
        this.updated = false;
    }

    /**
     * Update the rating value and comment
     */
    public void updateRating(int newRatingValue, String newComment) {
        this.ratingValue = validateRating(newRatingValue);
        this.comment = newComment;
        this.timestamp = LocalDateTime.now();
        this.updated = true;
    }

    private int validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return rating;
    }
}