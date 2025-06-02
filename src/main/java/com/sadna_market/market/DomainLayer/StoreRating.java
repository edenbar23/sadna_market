package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_ratings")
@Getter
@NoArgsConstructor // Required by JPA
public class StoreRating implements IRating {

    @Id
    @Column(name = "rating_id", updatable = false, nullable = false)
    private UUID ratingId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "rating_value", nullable = false)
    private int ratingValue;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "updated", nullable = false)
    private boolean updated;

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