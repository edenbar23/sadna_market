package com.sadna_market.market.DomainLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Guest extends IUser {
    private static final Logger logger = LogManager.getLogger(Guest.class);

    /**
     * Constructor for the Guest class.
     * Initializes the cart for the guest user.
     */
    public Guest() {
        super();
        logger.info("Guest created");
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public String toString() {
        return "Guest{}";
    }

}
