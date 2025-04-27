package com.sadna_market.market.InfrastructureLayer.Payment;

public interface PaymentVisitor {
    boolean visit(CreditCardDTO card, double amount);
    boolean visit(BankAccountDTO account, double amount);
    boolean visit(PayPalDTO paypal, double amount); // Add more as needed
}
