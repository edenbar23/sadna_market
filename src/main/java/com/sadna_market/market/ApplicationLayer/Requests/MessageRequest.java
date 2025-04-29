package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for sending a new message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private UUID storeId;
    private String content;
}