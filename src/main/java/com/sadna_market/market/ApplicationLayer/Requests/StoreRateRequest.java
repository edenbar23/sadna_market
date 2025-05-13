package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

/**
 * Request class for store rating operations.
 * Used when a user rates a store.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRateRequest {
    // The ID of the store being rated
    private UUID storeId;
    
    // The username of the user submitting the rating
    private String username;
    
    // The rating value (typically 1-5)
    private int rate;
    
    // Optional comment accompanying the rating
    private String comment;
    
    /**
     * Constructor for rating a store without a comment
     * 
     * @param storeId The ID of the store to rate
     * @param username The username of the user submitting the rating
     * @param rate The rating value
     */
    public StoreRateRequest(UUID storeId, String username, int rate) {
        this.storeId = storeId;
        this.username = username;
        this.rate = rate;
        this.comment = null;
    }

   
    /**
     * Validates that the rating is within acceptable bounds
     * 
     * @return true if the rating is valid, false otherwise
     */
    public boolean isValidRating() {
        return rate >= 1 && rate <= 5;
    }


}