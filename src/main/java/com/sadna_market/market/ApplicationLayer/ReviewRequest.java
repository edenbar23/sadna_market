package com.sadna_market.market.ApplicationLayer;

import lombok.Getter;

import java.util.UUID;

public class ReviewRequest {
    @Getter
    private final String username;
    @Getter
    private final UUID storeId;
    @Getter
    private final UUID productId;
    @Getter
    private final String comment;
    @Getter
    private final int rating; //can be -1 if violationReport

    public ReviewRequest(String username, UUID storeId, UUID productId, String comment) {
        this.username = username;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
        this.rating = -1; // -1 indicates that the rating is not applicable
    }

    public ReviewRequest(String username, UUID storeId, UUID productId, String comment, int rating) {
        this.username = username;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
        this.rating = rating;
    }
}
