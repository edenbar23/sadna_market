package com.sadna_market.market.DomainLayer;

import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Product {
    @Setter
    @Getter
    private String name;
    @Getter
    private UUID storeId;

    @Getter
    private UUID productId;
    @Setter
    @Getter
    private String description;


    private double ratingValue;
    private int ratingCount;

    @Setter
    @Getter
    private String category;
    @Setter
    @Getter
    private double price;
    @Getter
    private boolean isAvailable;

    // Constructor
    public Product(String name, UUID storeId, String description, String category, double price, boolean isAvailable) {
        this.name = name;
        this.storeId = storeId;
        this.productId = UUID.randomUUID();
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.ratingValue = 0.0;
        this.ratingCount = 0;
    }


    public void updateDetails(String name, String description, String category, double price) {
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


    public void setRating(double ratingValue, int ratingCount) {
        this.ratingValue = ratingValue;
        this.ratingCount = ratingCount;
    }


    public double getRate() {
        return ratingValue;
    }

    public int getNumOfRanks() {
        return ratingCount;
    }

}