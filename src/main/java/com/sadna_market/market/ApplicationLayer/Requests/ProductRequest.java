package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private UUID productId; // For updates
    private String name;
    private String description;
    private String category;
    private double price;

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
