package com.sadna_market.market.DomainLayer;

public enum OrderStatus {
    PENDING,    // Order created but not paid
    PAID,       // Payment received but not shipped
    SHIPPED,    // Order has been shipped
    COMPLETED,  // Order has been delivered
    CANCELED    // Order was canceled
}
