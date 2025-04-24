package com.sadna_market.market.ApplicationLayer;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for Store entities.
 * Used to transfer store data between layers without exposing domain objects.
 */
public class StoreDTO {
    private UUID storeId;
    private String name;
    private String description;
    private boolean active;
    private String founderUsername;
    private Set<String> ownerUsernames;
    private Set<String> managerUsernames;
    
    /**
     * Constructor for StoreDTO
     * 
     * @param storeId The UUID of the store
     * @param name The name of the store
     * @param description The description of the store
     * @param active Whether the store is active
     * @param founderUsername The username of the store founder
     * @param ownerUsernames Set of usernames of store owners
     * @param managerUsernames Set of usernames of store managers
     */
    public StoreDTO(UUID storeId, String name, String description, boolean active,
                  String founderUsername, Set<String> ownerUsernames, Set<String> managerUsernames) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.active = active;
        this.founderUsername = founderUsername;
        this.ownerUsernames = ownerUsernames;
        this.managerUsernames = managerUsernames;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
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