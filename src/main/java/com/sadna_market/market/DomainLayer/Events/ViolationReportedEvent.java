package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when a violation report is submitted
 */
@Getter
public class ViolationReportedEvent extends DomainEvent {
    private final String username;
    private final UUID storeId;
    private final UUID productId;
    private final String comment;

    public ViolationReportedEvent(String username, UUID storeId, UUID productId, String comment) {
        super();
        this.username = username;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
    }

}