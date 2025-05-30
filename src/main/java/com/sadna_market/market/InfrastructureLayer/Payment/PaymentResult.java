package com.sadna_market.market.InfrastructureLayer.Payment;

import lombok.Getter;

/**
 * Result object for payment processing operations
 * Contains transaction information needed for tracking and rollback
 */
@Getter
public class PaymentResult {
    private final boolean success;

    private final int transactionId;
    private final String errorMessage;
    private final PaymentMethod paymentMethod;
    private final double amount;

    /**
     * Constructor for successful payment
     */
    public PaymentResult(boolean success, int transactionId, PaymentMethod paymentMethod, double amount) {
        this.success = success;
        this.transactionId = transactionId;
        this.errorMessage = null;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    /**
     * Constructor for failed payment
     */
    public PaymentResult(boolean success, String errorMessage, PaymentMethod paymentMethod, double amount) {
        this.success = success;
        this.transactionId = -1;
        this.errorMessage = errorMessage;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    /**
     * Static factory method for successful payment
     */
    public static PaymentResult success(int transactionId, PaymentMethod paymentMethod, double amount) {
        return new PaymentResult(true, transactionId, paymentMethod, amount);
    }

    /**
     * Static factory method for failed payment
     */
    public static PaymentResult failure(String errorMessage, PaymentMethod paymentMethod, double amount) {
        return new PaymentResult(false, errorMessage, paymentMethod, amount);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Check if transaction ID is valid
     */
    public boolean hasValidTransactionId() {
        return success && transactionId > 0;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("PaymentResult[SUCCESS: transactionId=%d, amount=%.2f]",
                    transactionId, amount);
        } else {
            return String.format("PaymentResult[FAILURE: error='%s', amount=%.2f]",
                    errorMessage, amount);
        }
    }
}