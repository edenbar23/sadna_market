package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReportDTO {
    private UUID reportId;               // maps to Report.reportId (UUID)
    private String reporterUsername;     // maps to Report.username
    private String comment;              // maps to Report.comment
    private UUID storeId;                // maps to Report.storeId (UUID)
    private String storeName;            // fetched from store repository
    private UUID productId;              // maps to Report.productId (UUID)
    private String productName;         // fetched from product repository
    private LocalDateTime createdAt;    // maps to Report.createdAt

    // Constructor
    public ReportDTO(UUID reportId, String reporterUsername, String comment,
                     UUID storeId, String storeName, UUID productId,
                     String productName, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.reporterUsername = reporterUsername;
        this.comment = comment;
        this.storeId = storeId;
        this.storeName = storeName;
        this.productId = productId;
        this.productName = productName;
        this.createdAt = createdAt;
    }
}