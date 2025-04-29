package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for replying to a message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReplyRequest {
    private UUID messageId;
    private String content;
}