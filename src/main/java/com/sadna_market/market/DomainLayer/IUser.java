package com.sadna_market.market.DomainLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * This is an abstract class representing a user in the system.
 * It serves as a base class for different types of users (i.e User and Guest).
 * Using @MappedSuperclass since this is abstract and we don't want a separate table.
 */
@MappedSuperclass
public abstract class IUser {
    private static final Logger logger = LogManager.getLogger(IUser.class);

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    protected Cart cart;

    /**
     * Constructor for the IUser class.
     * Initializes the cart for the user.
     */
    public IUser() {
        this.cart = new Cart();
    }

    // Abstract method
    public abstract boolean isLoggedIn();

    // Regular method - direct cart access
    public Cart getCart(){
        logger.info("get cart: {}",cart);
        return cart;
    }
}