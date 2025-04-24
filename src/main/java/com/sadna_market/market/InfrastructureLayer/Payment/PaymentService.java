package com.sadna_market.market.InfrastructureLayer.Payment;

public class PaymentService {
    private PaymentVisitor visitor = new ConcretePaymentVisitor();

    public boolean pay(PaymentMethod method, double amount) {
        return method.accept(visitor, amount);
    }
}
