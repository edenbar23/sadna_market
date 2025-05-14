package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when an admin replies to a violation report
 */
@Getter
public class ViolationReplyEvent extends DomainEvent {
    private final String admin;
    private final UUID reportId;
    private final String user;
    private final String message;

    public ViolationReplyEvent(String admin, UUID reportId, String user, String message) {
        super();
        this.admin = admin;
        this.reportId = reportId;
        this.user = user;
        this.message = message;
    }

}