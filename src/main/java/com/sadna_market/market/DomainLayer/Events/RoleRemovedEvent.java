package com.sadna_market.market.DomainLayer.Events;

import com.sadna_market.market.DomainLayer.RoleType;
import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when a user's role is removed from a store
 */
@Getter
public class RoleRemovedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;
    private final String storeName;
    private final RoleType roleType;
    private final String removedBy;

    public RoleRemovedEvent(String username, UUID storeId, String storeName,
                            RoleType roleType, String removedBy) {
        super();
        this.username = username;
        this.storeId = storeId;
        this.storeName = storeName;
        this.roleType = roleType;
        this.removedBy = removedBy;
    }
}