package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

public class StoreRating implements IRating {
    @Getter private final UUID ratingId;
    @Getter private final String username;  // Username instead of UUID
    @Getter private final UUID storeId;
    @Getter private int ratingValue;
    @Getter private String comment;  // Keeping comment for store ratings
    @Getter private LocalDateTime timestamp;
    @Getter private boolean updated;

    /**
     * Constructor for a new store rating
     */
    public StoreRating(String username, UUID storeId, int ratingValue, String comment) {
        this.ratingId = UUID.randomUUID();
        this.username = username;
        this.storeId = storeId;
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