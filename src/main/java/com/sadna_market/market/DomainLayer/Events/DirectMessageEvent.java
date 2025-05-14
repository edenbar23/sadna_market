package com.sadna_market.market.DomainLayer.Events;

import lombok.Getter;

/**
 * Event triggered when sending a direct message between users
 */
@Getter
public class DirectMessageEvent extends DomainEvent {
    private final String sender;
    private final String recipient;
    private final String content;

    public DirectMessageEvent(String sender, String recipient, String content) {
        super();
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

}