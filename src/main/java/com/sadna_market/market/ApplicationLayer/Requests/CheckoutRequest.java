package com.sadna_market.market.ApplicationLayer.Requests;

import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Supply.SupplyMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request object for registered user checkout
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    /**
     * Payment method selected by user
     */
    private PaymentMethod paymentMethod;

    /**
     * Supply/shipping method selected by user
     */
    private SupplyMethod supplyMethod;


    private String shippingAddress;

    /**
     * Special delivery instructions
     */
    private String deliveryInstructions;

    @Override
    public String toString() {
        return String.format("CheckoutRequest[paymentMethod=%s, supplyMethod=%s]",
                paymentMethod != null ? paymentMethod.getClass().getSimpleName() : "null",
                supplyMethod != null ? supplyMethod.getClass().getSimpleName() : "null");
    }
}