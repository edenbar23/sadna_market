package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    private static final Logger logger = LogManager.getLogger(Admin.class);

    public Admin() {
        super();
    }

    public Admin(String userName, String password, String email, String firstName, String lastName) {
        super(userName, password, email, firstName, lastName);
        this.setAdmin(true);
        logger.info("Admin created: {}", userName);
    }

    @Override
    public String toString() {
        return String.format("Admin[username=%s, email=%s]", getUserName(), getEmail());
    }
}