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
}