package com.sadna_market.market.InfrastructureLayer.Payment;

public class ExternalPaymentAPI {
    public boolean sendCreditCardPayment(String cardNumber, String name, String exp, String cvv, double amount) {
        System.out.println("External API: Paying " + amount + " with credit card.");
        return true;
    }

    public boolean sendBankPayment(String account, String bank, double amount) {
        System.out.println("External API: Paying " + amount + " from bank account.");
        return true;
    }
}
