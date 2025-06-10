package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing the result of a checkout operation
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResultDTO {

    /**
     * List of order IDs created during checkout
     */
    private List<UUID> orderIds;

    /**
     * Payment transaction ID
     */
    private int paymentTransactionId;

    /**
     * List of tracking numbers for shipments
     */
    private List<String> trackingNumbers;

    /**
     * Total amount paid
     */
    private double totalAmount;

    /**
     * Success message
     */
    private String message;

    @Override
    public String toString() {
        return String.format("CheckoutResult[orders=%d, paymentId=%d, totalAmount=%.2f]",
                orderIds != null ? orderIds.size() : 0,
                paymentTransactionId,
                totalAmount);
    }

    public List<UUID> getOrderIds() {
        return orderIds;
    }
}