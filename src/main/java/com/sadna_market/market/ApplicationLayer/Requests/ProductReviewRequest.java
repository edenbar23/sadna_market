package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.Date;
import java.util.UUID;

public class ProductReviewRequest {
    private UUID userId;
    private UUID productId;
    private UUID storeId;
    private String reviewText;
    private Date reviewDate;
    private String username;
    private int rating;
    private String comment;

    public ProductReviewRequest(UUID userId, UUID productId,UUID storeId, String reviewText,String username,int rating, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.reviewText = reviewText;
        this.reviewDate = new Date();
        this.storeId = storeId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
    }
    public UUID getUserId() {
        return userId;
    }
    public UUID getProductId() {
        return productId;
    }
    public String getReviewText() {
        return reviewText;
    }
    public Date getReviewDate() {
        return reviewDate;
    }
    public String getUsername() {
        return this.username ;
    }
    public UUID getStoreId() {
        return storeId;
    }
    public String getComment() {
        return comment;
    }
    public int getRating() {
        return rating;
    }
}
