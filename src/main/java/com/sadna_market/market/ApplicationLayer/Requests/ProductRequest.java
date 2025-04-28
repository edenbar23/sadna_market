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
}
