package com.sadna_market.market.ApplicationLayer;

/**
 * Request object for store creation and update operations.
 * Contains the necessary information to create or update a store.
 */
public class StoreRequest {
    private String storeName;
    private String description;
    private String address;
    private String email;
    private String phoneNumber;
    private String founderUsername;
    
    /**
     * Default constructor for StoreRequest
     */
    public StoreRequest() {
    }
    
    /**
     * Constructor for StoreRequest
     * 
     * @param storeName The name of the store
     * @param description The description of the store
     * @param address The physical address of the store
     * @param email The contact email for the store
     * @param phoneNumber The contact phone number for the store
     * @param founderUsername The username of the store founder
     */
    public StoreRequest(String storeName, String description, String address, String email, String phoneNumber, String founderUsername) {
        this.storeName = storeName;
        this.description = description;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.founderUsername = founderUsername;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getFounderUsername() {
        return founderUsername;
    }
    
    public void setFounderUsername(String founderUsername) {
        this.founderUsername = founderUsername;
    }
    
    @Override
    public String toString() {
        return "StoreRequest{" +
                "storeName='" + storeName + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", founderUsername='" + founderUsername + '\'' +
                '}';
    }
}