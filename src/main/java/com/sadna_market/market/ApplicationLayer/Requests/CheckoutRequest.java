package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.UUID;

import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;

public class CheckoutRequest {
    private String buyerUsername;
    private UUID storeId;
    private Cart cart;
    private PaymentMethod paymentMethod;

    // Constructors
    public CheckoutRequest() {}

    public CheckoutRequest(String buyerUsername, UUID storeId, Cart cart, PaymentMethod paymentMethod) {
        this.buyerUsername = buyerUsername;
        this.storeId = storeId;
        this.cart = cart;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
