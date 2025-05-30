package com.sadna_market.market.InfrastructureLayer.Payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for payment methods using visitor pattern
 * Updated to return PaymentResult instead of boolean
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreditCardDTO.class, name = "creditCard"),
        @JsonSubTypes.Type(value = BankAccountDTO.class, name = "bankAccount"),
        @JsonSubTypes.Type(value = PayPalDTO.class, name = "paypal")
})

public interface PaymentMethod {

    /**
     * Accept a payment visitor to process this payment method
     * @param visitor The payment visitor to process this payment
     * @param amount The payment amount
     * @return PaymentResult containing transaction information and status
     */
    PaymentResult accept(PaymentVisitor visitor, double amount);
}