package com.sadna_market.market.DomainLayer.Events;

import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import lombok.Getter;

import java.util.UUID;

/**
 * Event triggered when checkout is initiated
 */
@Getter
public class CheckoutInitiatedEvent extends DomainEvent {
    private final String username;
    private final Cart cart;
    private final PaymentMethod paymentMethod;
    private final boolean isGuest;

    public CheckoutInitiatedEvent(String username, Cart cart, PaymentMethod paymentMethod, boolean isGuest) {
        super();
        this.username = username;
        this.cart = cart;
        this.paymentMethod = paymentMethod;
        this.isGuest = isGuest;
    }

}