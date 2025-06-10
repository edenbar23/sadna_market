package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import com.sadna_market.market.DomainLayer.Product;
import java.util.UUID;

/**
 * DTO that combines product information with store-specific inventory data
 */
public class StoreProductDTO {
    @Getter
    private UUID productId;
    @Getter
    private UUID storeId;
    @Getter
    private String name;
    @Getter
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
    @Getter
    private int quantity;

    /**
     * Constructor that takes a Product and available quantity
     */
    public StoreProductDTO(Product product, int quantity) {
        this.productId = product.getProductId();
        this.storeId = product.getStoreId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.isAvailable = product.isAvailable();
        this.rating = product.getRate();
        this.numberOfRatings = product.getNumOfRanks();
        this.quantity = Math.max(0, quantity); // Ensure non-negative
    }

    /**
     * Constructor that takes a ProductDTO and adds inventory data
     */
    public StoreProductDTO(ProductDTO product, int quantity) {
        this.productId = product.getProductId();
        this.storeId = product.getStoreId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.isAvailable = product.isAvailable();
        this.rating = product.getRating();
        this.numberOfRatings = product.getNumberOfRatings();
        this.quantity = Math.max(0, quantity); // Ensure non-negative
    }

    /**
     * Full constructor
     */
    public StoreProductDTO(UUID productId, UUID storeId, String name, String description,
                           String category, double price, boolean isAvailable, double rating,
                           int numberOfRatings, int quantity) {
        this.productId = productId;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rating = rating;
        this.numberOfRatings = numberOfRatings;
        this.quantity = Math.max(0, quantity); // Ensure non-negative
    }
}