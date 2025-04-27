package com.sadna_market.market.InfrastructureLayer.Payment;

public class PayPalDTO implements PaymentMethod {
    public String email;

    public PayPalDTO(String email) {
        this.email = email;
    }

    @Override
    public boolean accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }
}
