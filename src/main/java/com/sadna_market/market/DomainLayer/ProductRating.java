package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductRating implements IRating {
    @Getter private final UUID ratingId;
    @Getter private final String username;  // Username instead of UUID
    @Getter private final UUID productId;
    @Getter private int ratingValue;
    @Getter private LocalDateTime timestamp;
    @Getter private boolean updated;

    /**
     * Constructor for a new product rating
     */
    public ProductRating(String username, UUID productId, int ratingValue) {
        this.ratingId = UUID.randomUUID();
        this.username = username;
        this.productId = productId;
        this.ratingValue = validateRating(ratingValue);
        this.timestamp = LocalDateTime.now();
        this.updated = false;
    }

    /**
     * Update the rating value
     */
    public void updateRating(int newRatingValue) {
        this.ratingValue = validateRating(newRatingValue);
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