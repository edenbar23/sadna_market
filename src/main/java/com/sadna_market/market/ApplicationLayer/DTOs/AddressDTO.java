package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.Address;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AddressDTO {
    private final UUID addressId;
    private final String fullName;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;
    private final String phoneNumber;
    private final String label;
    private final boolean isDefault;
    private final String formattedAddress;

    public AddressDTO(Address address) {
        this.addressId = address.getAddressId();
        this.fullName = address.getFullName();
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.city = address.getCity();
        this.state = address.getState();
        this.postalCode = address.getPostalCode();
        this.country = address.getCountry();
        this.phoneNumber = address.getPhoneNumber();
        this.label = address.getLabel();
        this.isDefault = address.isDefault();
        this.formattedAddress = address.getFormattedAddress();
    }
}