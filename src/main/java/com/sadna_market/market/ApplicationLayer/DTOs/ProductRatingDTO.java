package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.ProductRating;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductRatingDTO {
    @Getter private final UUID ratingId;
    @Getter private final String username;
    @Getter private final UUID productId;
    @Getter private final int ratingValue;
    @Getter private final LocalDateTime timestamp;
    @Getter private final boolean updated;

    public ProductRatingDTO(ProductRating rating) {
        this.ratingId = rating.getRatingId();
        this.username = rating.getUsername();
        this.productId = rating.getProductId();
        this.ratingValue = rating.getRatingValue();
        this.timestamp = rating.getTimestamp();
        this.updated = rating.isUpdated();
    }
}