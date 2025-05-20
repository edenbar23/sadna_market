package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for replying to a message
 */
@Data
@NoArgsConstructor
public class MessageReplyRequest {
    private UUID messageId;
    private String content;
    private String senderUsername;


    // All-args constructor
    public MessageReplyRequest(UUID messageId, String senderUsername, String content) {
        this.messageId = messageId;
        this.senderUsername = senderUsername;
        this.content = content;
    }

    // Getters
    public UUID getMessageId() {
        return messageId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }
}


