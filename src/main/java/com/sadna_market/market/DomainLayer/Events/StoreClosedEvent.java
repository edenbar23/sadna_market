package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;
import java.util.UUID;

/**
 * Event triggered when a store is closed
 */
@Getter
public class StoreClosedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;

    public StoreClosedEvent(String username, UUID storeId) {
        super();
        this.username = username;
        this.storeId = storeId;
    }
}