package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;
import java.util.UUID;

/**
 * Event triggered when a store is reopened
 */
@Getter
public class StoreReopenedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;

    public StoreReopenedEvent(String username, UUID storeId) {
        super();
        this.username = username;
        this.storeId = storeId;
    }
}