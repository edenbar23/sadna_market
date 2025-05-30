package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Address {
    private UUID addressId;
    private String username;
    private String fullName;
    private String addressLine1;
    private String addressLine2; // Optional
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber; // Optional
    private boolean isDefault;
    private String label; // e.g., "Home", "Work", "Other"

    public Address(String username, String fullName, String addressLine1,
                   String city, String state, String postalCode, String country) {
        this.addressId = UUID.randomUUID();
        this.username = username;
        this.fullName = fullName;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.isDefault = false;
        this.label = "Home";
    }

    public Address(String username, String fullName, String addressLine1, String addressLine2,
                   String city, String state, String postalCode, String country,
                   String phoneNumber, String label) {
        this.addressId = UUID.randomUUID();
        this.username = username;
        this.fullName = fullName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.isDefault = false;
        this.label = label;
    }

    public String getFormattedAddress() {
        StringBuilder formatted = new StringBuilder();
        formatted.append(fullName).append("\n");
        formatted.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            formatted.append("\n").append(addressLine2);
        }
        formatted.append("\n").append(city).append(", ").append(state).append(" ").append(postalCode);
        formatted.append("\n").append(country);
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            formatted.append("\nPhone: ").append(phoneNumber);
        }
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