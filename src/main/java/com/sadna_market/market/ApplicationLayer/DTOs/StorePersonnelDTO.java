package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StorePersonnelDTO {
    @Getter
    private UUID storeId;
    @Getter
    private String founderUsername;
    @Getter
    private Set<String> ownerUsernames;
    @Getter
    private Set<String> managerUsernames;

    // Simple constructor that just accepts data
    public StorePersonnelDTO(UUID storeId, String founderUsername,
                             Set<String> owners, Set<String> managers) {
        this.storeId = storeId;
        this.founderUsername = founderUsername;
        this.ownerUsernames = owners;
        this.managerUsernames = managers;
    }
}