package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.Store;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StorePageDTO {
    private UUID storeId;
    private String name;
    private String description;
    private double rating;
    private boolean active;


    public StorePageDTO(UUID storeId, String name, String description, double rating, boolean active) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.active = active;

    }

    public StorePageDTO(Store store){
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.rating = store.getStoreRating();
        this.active = store.isActive();

    }


}
