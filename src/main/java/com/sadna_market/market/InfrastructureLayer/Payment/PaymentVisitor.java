package com.sadna_market.market.InfrastructureLayer.Payment;

/**
 * Visitor interface for processing different payment methods
 * Returns PaymentResult instead of boolean for better transaction tracking
 */
public interface PaymentVisitor {

    /**
     * Process credit card payment
     * @param card Credit card details
     * @param amount Payment amount
     * @return PaymentResult containing transaction information
     */
    PaymentResult visit(CreditCardDTO card, double amount);

    /**
     * Process bank account payment
     * @param account Bank account details
     * @param amount Payment amount
     * @return PaymentResult containing transaction information
     */
    PaymentResult visit(BankAccountDTO account, double amount);

    /**
     * Process PayPal payment
     * @param paypal PayPal details
     * @param amount Payment amount
     * @return PaymentResult containing transaction information
     */
    PaymentResult visit(PayPalDTO paypal, double amount);
}