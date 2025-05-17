package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
public class Product {
    @Setter private String name;
    private final UUID storeId;
    private final UUID productId;
    @Setter private String description;
    @Setter private String category;
    @Setter private double price;
    @Setter private boolean isAvailable;

    // Direct rating properties
    private double ratingSum;
    private int ratingCount;

    // Constructor
    public Product(String name, UUID storeId, String category, String description, double price, boolean isAvailable) {
        this.name = name;
        this.storeId = storeId;
        this.productId = UUID.randomUUID();
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.ratingSum = 0.0;
        this.ratingCount = 0;
    }

    // Constructor for repository reconstruction
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