package com.sadna_market.market.InfrastructureLayer.Payment;

public interface PaymentInterface {
    boolean payWithCreditCard(CreditCardDTO card, double amount);
    boolean payWithBankAccount(BankAccountDTO account, double amount);
}
