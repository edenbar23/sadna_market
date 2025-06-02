package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor // Required by JPA
public class Report {

    @Id
    @Column(name = "report_id", updatable = false, nullable = false)
    private UUID reportId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "comment", nullable = false, length = 1000)
    private String comment;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    // Adding timestamp for better tracking (optional but recommended)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Report(String username, String comment, UUID storeId, UUID productId) {
        this.reportId = UUID.randomUUID();
        this.username = username;
        this.comment = comment;
        this.storeId = storeId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }

    public String toString() {
        return "Report{" +
                "username='" + username + '\'' +
                ", comment='" + comment + '\'' +
                ", storeId=" + storeId +
                ", productId=" + productId +
                ", reportId=" + reportId +
                ", createdAt=" + createdAt +
                '}';
    }
}