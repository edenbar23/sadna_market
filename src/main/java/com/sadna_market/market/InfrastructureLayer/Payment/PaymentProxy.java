package com.sadna_market.market.InfrastructureLayer.Payment;

public class PaymentProxy implements PaymentInterface {
    private PaymentAdapter adapter = new PaymentAdapter();

    @Override
    public boolean payWithCreditCard(CreditCardDTO card, double amount) {
        System.out.println("Proxy: Logging credit card payment...");
        return adapter.payWithCreditCard(card, amount);
    }

    @Override
    public boolean payWithBankAccount(BankAccountDTO account, double amount) {
        System.out.println("Proxy: Logging bank account payment...");
        return adapter.payWithBankAccount(account, amount);
    }
}
