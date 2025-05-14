package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;
import java.util.UUID;

/**
 * Event triggered when someone requests messages for a store
 */
@Getter
public class StoreMessagesRequestedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;

    public StoreMessagesRequestedEvent(String username, UUID storeId) {
        super();
        this.username = username;
        this.storeId = storeId;
    }
}