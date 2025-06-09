package com.sadna_market.market.InfrastructureLayer.Payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Generated;
import lombok.Getter;

/**
 * DTO for credit card payment information
 * Updated to return PaymentResult
 */
public class CreditCardDTO implements PaymentMethod {
    @JsonProperty("cardNumber")
    @Getter
    public String cardNumber;

    @JsonProperty("cardHolderName")
    @Getter
    public String cardHolderName;

    @JsonProperty("expiryDate")
    @Getter
    public String expiryDate;

    @JsonProperty("cvv")
    @Getter
    public String cvv;

    @JsonCreator
    public CreditCardDTO(
            @JsonProperty("cardNumber") String cardNumber,
            @JsonProperty("cardHolderName") String cardHolderName,
            @JsonProperty("expiryDate") String expiryDate,
            @JsonProperty("cvv") String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Default constructor for JSON deserialization
    public CreditCardDTO() {}

    public String getCardPrefix() {
        return cardNumber != null && cardNumber.length() >= 8 ?
                cardNumber.substring(0, 8) : "00000000";
    }

    @Override
    public PaymentResult accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }

    @Override
    public String toString() {
        return String.format("CreditCard[holder=%s, number=****%s, expiry=%s]",
                cardHolderName,
                cardNumber != null && cardNumber.length() > 4 ?
                        cardNumber.substring(cardNumber.length() - 4) : "****",
                expiryDate);
    }
}