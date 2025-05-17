package com.sadna_market.market.ApplicationLayer.Requests;

//import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ProductRequest {
    private UUID productId; // For updates
    private String name;
    private String description;
    private String category;
    private double price;

    public ProductRequest(UUID uuid, String name, String category, String description, double price) {
        this.productId = uuid;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    public UUID getProductId() {
        return productId;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getCategory() {
        return category;
    }
    public double getPrice() {
        return price;
    }

}
