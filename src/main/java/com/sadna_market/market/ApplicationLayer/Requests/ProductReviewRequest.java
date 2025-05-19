package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.Date;
import java.util.UUID;

public class ProductReviewRequest {
    private String username;
    private UUID productId;
    private UUID storeId;
    private String reviewText;
    private Date reviewDate;


    public ProductReviewRequest(String username, UUID productId,UUID storeId, String reviewText) {
        this.username = username;
        this.productId = productId;
        this.reviewText = reviewText;
        this.reviewDate = new Date();
        this.storeId = storeId;
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
}
