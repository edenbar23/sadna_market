package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class StorePersonnelDTO {
    @Getter
    private UUID storeId;
    @Getter
    private String founderUsername;
    @Getter
    private List<String> ownerUsernames;
    @Getter
    private List<String> managerUsernames;

    // Simple constructor that just accepts data
    public StorePersonnelDTO(UUID storeId, String founderUsername,
                             List<String> owners, List<String> managers) {
        this.storeId = storeId;
        this.founderUsername = founderUsername;
        this.ownerUsernames = owners;
        this.managerUsernames = managers;
    }
}