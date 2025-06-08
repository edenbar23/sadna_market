package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.Store;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

public class StoreDTO {
    @Getter
    private UUID storeId;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private boolean active;
    @Getter
    private String founderUsername;
    @Getter
    private Set<String> ownerUsernames;
    @Getter
    private Set<String> managerUsernames;
    @Getter
    private double rating;
    public StoreDTO(Store store) {
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.active = store.isActive();
        this.founderUsername = store.getFounder().getUsername();
        this.ownerUsernames = store.getOwnerUsernames();
        this.managerUsernames = store.getManagerUsernames();
    }

    public StoreDTO(UUID storeId, String name, String description, boolean active, String founderUsername, Set<String> ownerUsernames, Set<String> managerUsernames,double rating) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.active = active;
        this.founderUsername = founderUsername;
        this.ownerUsernames = ownerUsernames;
        this.managerUsernames = managerUsernames;
        this.rating = rating;
    }
}