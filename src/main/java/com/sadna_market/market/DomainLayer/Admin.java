package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

public class Admin extends User {
    private static final Logger logger = LogManager.getLogger(Admin.class);

    public Admin(String userName, String password, String email, String firstName, String lastName) {
        super(userName, password, email, firstName, lastName);
    }


//    public void closeStore(UUID storeId) {
//
//    }
}
