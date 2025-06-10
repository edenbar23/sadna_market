package com.sadna_market.market.DomainLayer.Events;

import com.sadna_market.market.DomainLayer.RoleType;
import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when a user is assigned a new role in a store
 */
@Getter
public class RoleAssignedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;
    private final String storeName;
    private final RoleType roleType;
    private final String assignedBy;

    public RoleAssignedEvent(String username, UUID storeId, String storeName,
                             RoleType roleType, String assignedBy) {
        super();
        this.username = username;
        this.storeId = storeId;
        this.storeName = storeName;
        this.roleType = roleType;
        this.assignedBy = assignedBy;
    }
}