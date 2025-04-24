package com.sadna_market.market.DomainLayer.Product;

import java.util.UUID;

public class ProductDTO {private String name;

    private UUID productId;
    private String description;
    private double rate;
    private int numOfRanks;
    private String category;
    private double price;
    public ProductDTO(Product product) {
        this.name = product.getName();
        this.productId = product.getProductId();
        this.description = product.getDescription();
        this.rate = product.getRate();
        this.numOfRanks = product.getNumOfRanks();
        this.category = product.getCategory();
        this.price = product.getPrice();
    }
}
