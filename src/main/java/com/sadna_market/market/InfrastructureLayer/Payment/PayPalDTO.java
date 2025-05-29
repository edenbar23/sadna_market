package com.sadna_market.market.InfrastructureLayer.Payment;

/**
 * DTO for PayPal payment information
 * Updated to return PaymentResult
 */
public class PayPalDTO implements PaymentMethod {
    public String email;

    public PayPalDTO(String email) {
        this.email = email;
    }

    @Override
    public PaymentResult accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }

    @Override
    public String toString() {
        return String.format("PayPal[email=%s]", email);
    }
}