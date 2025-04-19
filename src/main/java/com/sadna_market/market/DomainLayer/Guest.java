package com.sadna_market.market.DomainLayer;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class Guest extends IUser {
    private static final Logger logger = LogManager.getLogger(Guest.class);
    
    @Getter
    private UUID guestId;

    /**
     * Constructor for the Guest class.
     * Initializes the cart for the guest user.
     */
    public Guest() {
        logger.info("Creating a new guest user...");
        this.guestId = UUID.randomUUID();
        this.cart = new Cart();
        logger.info("Guest user created with ID: " + guestId);
    }


    @Override
    public boolean isLoggedIn() {
        logger.info("Checking if guest user is logged in (never happen)...");
        return false;

    }

}
