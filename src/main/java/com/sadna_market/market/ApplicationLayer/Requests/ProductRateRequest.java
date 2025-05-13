package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Request object for product rating operations.
 * Used when a user rates a product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRateRequest {
    private UUID productId;
    private String username; // User who is rating the product
    private UUID storeId;  // Store that contains the product
    private int rating;    // Rating value (typically 1-5)


    public boolean isValidRating() {
        return rating >= 1 && rating <= 5;
    }
}