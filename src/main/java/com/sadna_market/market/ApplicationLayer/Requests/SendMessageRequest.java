package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import java.util.UUID;

@Data
public class SendMessageRequest {
    private String senderUsername;
    private UUID receiverStoreId;
    private String content;
    private UUID messageId;
}