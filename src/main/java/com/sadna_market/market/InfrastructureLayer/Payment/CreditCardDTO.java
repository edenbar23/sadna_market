package com.sadna_market.market.InfrastructureLayer.Payment;

public class CreditCardDTO implements PaymentMethod {
    public String cardNumber;
    public String cardHolderName;
    public String expiryDate;
    public String cvv;

    public CreditCardDTO(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public boolean accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }
}
