package com.sadna_market.market.ApplicationLayer;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for store personnel information.
 * Used to transfer store personnel data between layers.
 */
public class StorePersonnelDTO {
    private UUID storeId;
    private String founderUsername;
    private Set<String> ownerUsernames;
    private Set<String> managerUsernames;
    
    /**
     * Constructor for StorePersonnelDTO
     * 
     * @param storeId The UUID of the store
     * @param founderUsername The username of the store founder
     * @param ownerUsernames Set of usernames of store owners
     * @param managerUsernames Set of usernames of store managers
     */
    public StorePersonnelDTO(UUID storeId, String founderUsername, 
                           Set<String> ownerUsernames, Set<String> managerUsernames) {
        this.storeId = storeId;
        this.founderUsername = founderUsername;
        this.ownerUsernames = ownerUsernames;
        this.managerUsernames = managerUsernames;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getFounderUsername() {
        return founderUsername;
    }

    public Set<String> getOwnerUsernames() {
        return ownerUsernames;
    }

    public Set<String> getManagerUsernames() {
        return managerUsernames;
    }
}