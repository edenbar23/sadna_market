package com.sadna_market.market.InfrastructureLayer.Payment;

/**
 * DTO for credit card payment information
 * Updated to return PaymentResult
 */
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

    public String getCardPrefix() {
        return cardNumber.substring(0, 8); // Binlist works best with 6-8 digits
    }

    @Override
    public PaymentResult accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }

    @Override
    public String toString() {
        return String.format("CreditCard[holder=%s, number=****%s, expiry=%s]",
                cardHolderName,
                cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : "****",
                expiryDate);
    }
}