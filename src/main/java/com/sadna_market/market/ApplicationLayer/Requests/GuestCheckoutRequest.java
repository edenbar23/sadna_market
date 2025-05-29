package com.sadna_market.market.ApplicationLayer.Requests;

import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Supply.SupplyMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request object for guest checkout
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestCheckoutRequest {

    /**
     * Guest cart items - Map of store ID to products (product ID -> quantity)
     */
    private Map<UUID, Map<UUID, Integer>> cartItems;

    /**
     * Payment method selected by guest
     */
    private PaymentMethod paymentMethod;

    /**
     * Supply/shipping method selected by guest
     */
    private SupplyMethod supplyMethod;

    /**
     * Shipping address for guest
     */
    private String shippingAddress;

    /**
     * Guest contact email
     */
    private String contactEmail;

    /**
     * Guest contact phone (optional)
     */
    private String contactPhone;

    /**
     * Special delivery instructions
     */
    private String deliveryInstructions;

    @Override
    public String toString() {
        return String.format("GuestCheckoutRequest[items=%d stores, paymentMethod=%s, supplyMethod=%s, email=%s]",
                cartItems != null ? cartItems.size() : 0,
                paymentMethod != null ? paymentMethod.getClass().getSimpleName() : "null",
                supplyMethod != null ? supplyMethod.getClass().getSimpleName() : "null",
                contactEmail);
    }
}