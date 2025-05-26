package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for marking a message as read
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkMessageReadRequest {
    private UUID messageId;
    private String username;

    /**
     * Constructor for marking a message as read
     *
     * @param username The username of the user marking the message as read
     */
    public MarkMessageReadRequest(String username) {
        this.username = username;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}