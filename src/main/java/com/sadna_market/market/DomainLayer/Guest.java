package com.sadna_market.market.DomainLayer;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class Guest extends IUser {
    private static final Logger logger = LogManager.getLogger(Guest.class);
    private static final AtomicLong GUEST_ID_GENERATOR = new AtomicLong(1);

    @Getter
    private long Id;

    /**
     * Constructor for the Guest class.
     * Initializes the cart for the guest user.
     */
    public Guest() {
        logger.info("Creating a new guest user...");
        this.Id = GUEST_ID_GENERATOR.getAndIncrement();
        this.cart = new Cart();
        logger.info("Guest user created with ID: " + Id);
    }

    public Guest(long id) {
        logger.info("Creating a new guest user with ID: " + id);
        this.Id = id;
        this.cart = new Cart();
        logger.info("Guest user created with ID: " + Id);
    }

    @Override
    public boolean isLoggedIn() {
        logger.info("Checking if guest user is logged in (never happen)...");
        return false;

    }

}
