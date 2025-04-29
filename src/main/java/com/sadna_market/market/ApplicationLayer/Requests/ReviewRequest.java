package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request object for both product reviews and violation reports.
 * This class serves a dual purpose:
 * 1. For product reviews when a user reviews a product they purchased
 * 2. For violation reports when a user reports a policy violation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    // Required fields
    private UUID storeId;          // The store ID containing the product
    private UUID productId;        // The product being reviewed or reported
    private String comment;        // The review content or violation description
    
    // Optional fields - null when not applicable
    private Integer rating;        // The product rating (null for violation reports)
    private String username;       // The username of the reviewer/reporter
    private LocalDateTime timestamp; // When the review/report was created
    private boolean isViolationReport; // Whether this is a violation report or product review
    
    /**
     * Constructor for product reviews
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param comment The review text
     * @param rating The rating value (1-5)
     */
    public ReviewRequest(UUID storeId, UUID productId, String comment, Integer rating) {
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = LocalDateTime.now();
        this.isViolationReport = false;
    }
    
    /**
     * Constructor for product reviews with username
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param comment The review text
     * @param rating The rating value (1-5)
     * @param username The username of the reviewer
     */
    public ReviewRequest(UUID storeId, UUID productId, String comment, Integer rating, String username) {
        this(storeId, productId, comment, rating);
        this.username = username;
    }
    
    /**
     * Constructor for violation reports
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param comment The description of the violation
     */
    public ReviewRequest(UUID storeId, UUID productId, String comment) {
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
        this.rating = null; // No rating for violation reports
        this.timestamp = LocalDateTime.now();
        this.isViolationReport = true;
    }
    
    /**
     * Constructor for violation reports with username
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param comment The description of the violation
     * @param username The username of the reporter
     */
    public ReviewRequest(UUID storeId, UUID productId, String comment, String username) {
        this(storeId, productId, comment);
        this.username = username;
    }
    
    /**
     * Validates that the review is properly formed
     * 
     * @return true if the review is valid, false otherwise
     */
    public boolean isValid() {
        // Basic validation - fields must not be null
        if (storeId == null || productId == null || comment == null || comment.isEmpty()) {
            return false;
        }
        
        // For product reviews, rating must be present and valid
        if (!isViolationReport && (rating == null || rating < 1 || rating > 5)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if this is a product review (not a violation report)
     * 
     * @return true if this is a product review, false if it's a violation report
     */
    public boolean isProductReview() {
        return !isViolationReport;
    }
}