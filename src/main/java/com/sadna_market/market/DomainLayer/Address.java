package com.sadna_market.market.DomainLayer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {
    // ────────────────────────────────────────────────────────────────
    // Primary key: auto-generated UUID
    // ────────────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "address_id", updatable = false, nullable = false)
    private UUID addressId;


    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 200)
    private String addressLine1;

    @Column(length = 200)
    private String addressLine2; // Optional

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(length = 50)
    private String label; // e.g., "Home", "Work", "Other"


    public Address() {
    }

    public Address(String username, String fullName, String addressLine1, String addressLine2,
                   String city, String state, String postalCode, String country, String phoneNumber, String label){
        this.addressId = UUID.randomUUID();
        this.username = username;
        this.fullName = fullName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2; // Optional, can be null
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.isDefault = false;
        this.label = label; // e.g., "Home", "Work", "Other"
    }


    public String getFormattedAddress() {
        StringBuilder formatted = new StringBuilder();
        formatted.append(fullName).append("\n");
        formatted.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            formatted.append("\n").append(addressLine2);
        }
        formatted.append("\n")
                .append(city).append(", ")
                .append(state).append(" ")
                .append(postalCode);
        formatted.append("\n").append(country);
        return formatted.toString();
    }

    public boolean isValidAddress() {
        return username != null && !username.trim().isEmpty() &&
                fullName != null && !fullName.trim().isEmpty() &&
                addressLine1 != null && !addressLine1.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty() &&
                country != null && !country.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Address{id=%s, user=%s, label=%s, default=%s}",
                addressId, username, label, isDefault);
    }
}
