package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import java.util.UUID;

public class CartProductDTO {
    @Getter
    private UUID productId;
    @Getter
    private String name;
    @Getter
    private double price;
    @Getter
    private int quantity;
    @Getter
    private String description;
    @Getter
    private String category;
    @Getter
    private String image;

    public CartProductDTO(UUID productId, String name, double price, int quantity,
                          String description, String category, String image) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.category = category;
        this.image = image;
    }
}