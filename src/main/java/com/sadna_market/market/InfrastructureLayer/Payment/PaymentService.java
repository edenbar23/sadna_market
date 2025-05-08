package com.sadna_market.market.InfrastructureLayer.Payment;

import java.util.UUID;

public class PaymentService {
    private PaymentVisitor visitor = new ConcretePaymentVisitor();

    public boolean pay(PaymentMethod method, double amount) {
        return method.accept(visitor, amount);
    }

    public void refund(UUID paymentId) {
        // Logic to refund the payment
        // This could involve calling the payment gateway's API to process the refund
        // For simplicity, we'll just print a message here
        System.out.println("Refunding payment with ID: " + paymentId);
    }
}
