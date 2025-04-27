package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;

import com.sadna_market.market.DomainLayer.Product.Product;
import java.util.UUID;

public class ProductDTO {
    @Getter
    private UUID productId;
    @Getter
    private UUID storeId;
    @Getter
    private String name;
    Getter
    private String description;
    @Getter
    private String category;
    @Getter
    private double price;
    @Getter
    private boolean isAvailable;
    @Getter
    private double rating;
    @Getter
    private int numberOfRatings;

    public ProductDTO(Product product) {
        this.productId = product.getProductId();
        this.storeId = product.getStoreId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.isAvailable = product.isAvailable();
        this.rating = product.getRate();
        this.numberOfRatings = product.getNumOfRanks();
    }

    public ProductDTO(UUID productId, UUID storeId, String name, String description, String category, double price, boolean isAvailable, double rating, int numberOfRatings) {
        this.productId = productId;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rating = rating;
        this.numberOfRatings = numberOfRatings;
    }
}