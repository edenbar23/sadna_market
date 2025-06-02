package com.sadna_market.market.DomainLayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_reviews")
@Getter
@NoArgsConstructor // Required by JPA
public class ProductReview {

    @Id
    @Column(name = "review_id", updatable = false, nullable = false)
    private UUID reviewId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "review_text", nullable = false, length = 500)
    private String reviewText;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited;

    @Column(name = "last_edit_time")
    private LocalDateTime lastEditTime;

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