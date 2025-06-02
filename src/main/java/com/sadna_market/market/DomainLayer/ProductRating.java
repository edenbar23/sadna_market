package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_ratings")
@Getter
@NoArgsConstructor // Required by JPA
public class ProductRating implements IRating {

    @Id
    @Column(name = "rating_id", updatable = false, nullable = false)
    private UUID ratingId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "rating_value", nullable = false)
    private int ratingValue;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "updated", nullable = false)
    private boolean updated;

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