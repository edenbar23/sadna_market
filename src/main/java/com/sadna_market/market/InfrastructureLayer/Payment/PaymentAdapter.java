package com.sadna_market.market.InfrastructureLayer.Payment;

public class PaymentAdapter implements PaymentInterface {
    private ExternalPaymentAPI api = new ExternalPaymentAPI();

    @Override
    public boolean payWithCreditCard(CreditCardDTO card, double amount) {
        return api.sendCreditCardPayment(card.cardNumber, card.cardHolderName, card.expiryDate, card.cvv, amount);
    }

    @Override
    public boolean payWithBankAccount(BankAccountDTO account, double amount) {
        return api.sendBankPayment(account.accountNumber, account.bankName, amount);
    }
}
