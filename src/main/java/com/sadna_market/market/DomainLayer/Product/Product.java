package com.sadna_market.market.DomainLayer.Product;

import com.sadna_market.market.ApplicationLayer.ProductRequest;

import java.util.UUID;

public class Product {
    private String name;
    private UUID storeId;
    private UUID productId;
    private String description;

    private Rate rate;
    private String category;
    private double price;
    private boolean isAvailable;


    // an initialize constructor: sets a new rate object an generates a new product id
    public Product(String name,UUID storeId, String description, String category, double price, boolean isAvailable) {
        this.name = name;
        this.storeId = storeId;
        this.productId = UUID.randomUUID();
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rate = new Rate();
    }

    // a methods that updates a gets a product request object
    public void updateProduct(ProductRequest productRequest) {
        if (productRequest.getName() != null) {
            this.name = productRequest.getName();
        }
        if (productRequest.getDescription() != null) {
            this.description = productRequest.getDescription();
        }
        if (productRequest.getCategory() != null) {
            this.category = productRequest.getCategory();
        }
        if (productRequest.getPrice() != 0) {
            this.price = productRequest.getPrice();
        }
        if (productRequest.getProductId() != null) {
            this.productId = productRequest.getProductId();
        }

    }

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void addRank(double newRank) {
        rate.addRank(newRank);
    }
    public void updateRank(double oldRank, double newRank) {
        rate.updateRank(oldRank, newRank);
    }

    public double getRate() {
        return rate.getRateVal();
    }
    public int getNumOfRanks() {
        return rate.getNumOfRanks();
    }
    public boolean isAvailable() {
        return isAvailable;
    }

    public UUID getStoreId() {
        return storeId;
    }

}
