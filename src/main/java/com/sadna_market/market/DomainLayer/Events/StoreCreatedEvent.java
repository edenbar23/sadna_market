package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;
import java.util.UUID;

/**
 * Event triggered when a store is created
 */
@Getter
public class StoreCreatedEvent extends DomainEvent {
    private final String founderUsername;
    private final String storeName;
    private final String description;
    private final String address;
    private final String email;
    private final String phone;

    public StoreCreatedEvent(String founderUsername, String storeName, String description,
                             String address, String email, String phone) {
        super();
        this.founderUsername = founderUsername;
        this.storeName = storeName;
        this.description = description;
        this.address = address;
        this.email = email;
        this.phone = phone;
    }
}