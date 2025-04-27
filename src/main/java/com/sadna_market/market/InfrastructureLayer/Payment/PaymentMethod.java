package com.sadna_market.market.InfrastructureLayer.Payment;
public interface PaymentMethod {
    boolean accept(PaymentVisitor visitor, double amount);
}

