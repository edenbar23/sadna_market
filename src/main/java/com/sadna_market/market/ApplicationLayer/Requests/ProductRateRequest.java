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
    private UUID userId;
    private String username; // User who is rating the product
    private UUID storeId;  // Store that contains the product
    private int rating;    // Rating value (typically 1-5)
    

    public UUID getProductId() {
        return productId;
    }   

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public int getRating() {
        return rating;
    }
    
    /**
     * Constructor with essential fields for product rating
     * 
     * @param productId The ID of the product to rate
     * @param userId The ID of the user submitting the rating
     * @param rating The rating value
     */
    public ProductRateRequest(UUID productId, UUID userId, int rating) {
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
    }
    
    /**
     * Constructor with additional store context
     * 
     * @param productId The ID of the product to rate
     * @param userId The ID of the user submitting the rating
     * @param storeId The ID of the store containing the product
     * @param rating The rating value
     */
    public ProductRateRequest(UUID productId, UUID userId, UUID storeId, int rating) {
        this.productId = productId;
        this.userId = userId;
        this.storeId = storeId;
        this.rating = rating;
    }
    
    /**
     * Constructor with username instead of userId
     * 
     * @param productId The ID of the product to rate
     * @param username The username of the user submitting the rating
     * @param storeId The ID of the store containing the product
     * @param rating The rating value
     */
    public ProductRateRequest(UUID productId, String username, UUID storeId, int rating) {
        this.productId = productId;
        this.username = username;
        this.storeId = storeId;
        this.rating = rating;
    }
    
    /**
     * Validates that the rating is within acceptable bounds
     * 
     * @return true if the rating is valid, false otherwise
     */
    public boolean isValidRating() {
        return rating >= 1 && rating <= 5;
    }
}