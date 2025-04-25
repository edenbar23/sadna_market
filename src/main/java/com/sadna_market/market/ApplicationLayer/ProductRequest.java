package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

public class ProductRequest {
    UUID productId; //can be null if the product is new
    String name;
    String description;
    int quantity;
    double price;

    public ProductRequest(String name, String description, int quantity, double price) {
        this.productId = null; // new product does not have an id till it is created in the system
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }
    public ProductRequest(UUID productId, String name, String description, int quantity, double price) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }
}
