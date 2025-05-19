package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.ProductReview;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductReviewDTO {
    @Getter private final UUID reviewId;
    @Getter private final String username;
    @Getter private final UUID productId;
    @Getter private final UUID storeId;
    @Getter private final String reviewText;
    @Getter private final LocalDateTime timestamp;
    @Getter private final boolean isEdited;
    @Getter private final LocalDateTime lastEditTime;

    public ProductReviewDTO(ProductReview review) {
        this.reviewId = review.getReviewId();
        this.username = review.getUsername();
        this.productId = review.getProductId();
        this.storeId = review.getStoreId();
        this.reviewText = review.getReviewText();
        this.timestamp = review.getTimestamp();
        this.isEdited = review.isEdited();
        this.lastEditTime = review.getLastEditTime();
    }
}