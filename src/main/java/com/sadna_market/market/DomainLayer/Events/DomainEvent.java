package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events
 */
@Getter
public abstract class DomainEvent {
    private final UUID eventId;
    private final LocalDateTime timestamp;

    public DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

}