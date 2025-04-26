package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

public class ProductRequest {
    UUID productId; //can be null if the product is new
    String name;
    String category;
    String description;
    double price;

    public ProductRequest(String name, String category, String description, double price) {
        this.productId = null; // new product does not have an id till it is created in the system
        this.name = name;
        this.category = category;
        this.description = description;

        this.price = price;
    }
    public ProductRequest(UUID productId, String name, String category, String description, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }
    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }
}
