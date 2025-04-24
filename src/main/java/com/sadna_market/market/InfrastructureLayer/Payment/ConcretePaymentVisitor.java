package com.sadna_market.market.InfrastructureLayer.Payment;

public class ConcretePaymentVisitor implements PaymentVisitor {
    private ExternalPaymentAPI api = new ExternalPaymentAPI();

    @Override
    public boolean visit(CreditCardDTO card, double amount) {
        System.out.println("Visitor: Processing credit card...");
        return api.sendCreditCardPayment(card.cardNumber, card.cardHolderName, card.expiryDate, card.cvv, amount);
    }

    @Override
    public boolean visit(BankAccountDTO account, double amount) {
        System.out.println("Visitor: Processing bank account...");
        return api.sendBankPayment(account.accountNumber, account.bankName, amount);
    }

    @Override
    public boolean visit(PayPalDTO paypal, double amount) {
        System.out.println("Visitor: Processing PayPal payment for " + paypal.email + "...");
        return true; // simulate success
    }
}
