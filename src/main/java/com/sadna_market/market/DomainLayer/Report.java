package com.sadna_market.market.DomainLayer;

import lombok.Getter;

import java.util.UUID;

public class Report {
    @Getter
    private String username;
    @Getter
    private String comment;
    @Getter
    private UUID storeId;
    @Getter
    private UUID productId;
    @Getter
    private UUID reportId;

    public Report(String username, String comment, UUID storeId, UUID productId) {
        this.username = username;
        this.comment = comment;
        this.storeId = storeId;
        this.productId = productId;
        this.reportId = UUID.randomUUID();
    }

}
