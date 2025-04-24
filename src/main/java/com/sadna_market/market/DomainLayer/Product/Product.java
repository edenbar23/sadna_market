package com.sadna_market.market.DomainLayer.Product;

import java.util.UUID;

public class Product {
    private String name;

    private UUID productId;
    private String description;

    private Rate rate;
    private String category;
    private double price;
    private boolean isAvailable;


    // an initialize constructor: sets a new rate object an generates a new product id
    public Product(String name, String description, String category, double price, boolean isAvailable) {
        this.name = name;
        this.productId = UUID.randomUUID();
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rate = new Rate();
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

    public void updateRank(double newRank) {
        rate.updateRank(newRank);
    }

    public double getRate() {
        return rate.getRateVal();
    }
    public boolean isAvailable() {
        return isAvailable;
    }


}
