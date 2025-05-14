package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when a message is sent to a store
 */
@Getter
public class MessageSentEvent extends DomainEvent {
    private final String senderUsername;
    private final UUID storeId;
    private final String content;

    public MessageSentEvent(String senderUsername, UUID storeId, String content) {
        super();
        this.senderUsername = senderUsername;
        this.storeId = storeId;
        this.content = content;
    }

}