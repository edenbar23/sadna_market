package com.sadna_market.market.InfrastructureLayer.Payment;

/**
 * Interface for payment methods using visitor pattern
 * Updated to return PaymentResult instead of boolean
 */
public interface PaymentMethod {

    /**
     * Accept a payment visitor to process this payment method
     * @param visitor The payment visitor to process this payment
     * @param amount The payment amount
     * @return PaymentResult containing transaction information and status
     */
    PaymentResult accept(PaymentVisitor visitor, double amount);
}