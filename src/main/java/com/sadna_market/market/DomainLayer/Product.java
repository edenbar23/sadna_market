package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor // Required by JPA
public class Product {

    @Id
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Setter
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Setter
    @Column(name = "description", length = 1000)
    private String description;

    @Setter
    @Column(name = "category", length = 100)
    private String category;

    @Setter
    @Column(name = "price", nullable = false)
    private double price;

    @Setter
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "rating_sum", nullable = false)
    private double ratingSum;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

    // Constructor for creating new products
    public Product(String name, UUID storeId, String category, String description, double price, boolean isAvailable) {
        this.productId = UUID.randomUUID();
        this.name = name;
        this.storeId = storeId;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.ratingSum = 0.0;
        this.ratingCount = 0;
    }

    // Constructor for repository reconstruction (keep this for backward compatibility)
    public Product(UUID productId, String name, UUID storeId, String description, String category,
                   double price, boolean isAvailable, double ratingSum, int ratingCount) {
        this.productId = productId;
        this.name = name;
        this.storeId = storeId;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    public void updateProduct(String name, String description, String category, double price) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (category != null) {
            this.category = category;
        }
        if (price > 0) {
            this.price = price;
        }
    }

    // Add a new rating
    public void addRank(double newRank) {
        if (newRank < 1 || newRank > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.ratingSum += newRank;
        this.ratingCount++;
    }

    // Update an existing rating
    public void updateRank(double oldRank, double newRank) {
        if (newRank < 1 || newRank > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.ratingSum = this.ratingSum - oldRank + newRank;
    }

    public double getRate() {
        return ratingCount > 0 ? ratingSum / ratingCount : 0.0;
    }

    public int getNumOfRanks() {
        return ratingCount;
    }
}