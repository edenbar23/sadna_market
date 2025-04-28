package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request class for store creation and update operations.
 * Contains all necessary fields for creating a new store or updating an existing store's information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {
    // Basic store information
    private String storeName;
    private String description;
    private String address;
    private String email;
    private String phoneNumber;
    
    // For store creation
    private String founderUsername;
    
    // For store update
    private UUID storeId;
    
   
    /**
     * Constructor for store creation
     * 
     * @param storeName The name of the store
     * @param description The description of the store
     * @param address The physical address of the store
     * @param email The contact email for the store
     * @param phoneNumber The contact phone number for the store
     * @param founderUsername The username of the store founder
     */
    public StoreRequest(String storeName, String description, String address, String email, 
                        String phoneNumber, String founderUsername) {
        this.storeName = storeName;
        this.description = description;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.founderUsername = founderUsername;
    }
    
    /**
     * Constructor for store update
     * 
     * @param storeId The ID of the store to update
     * @param storeName The new name of the store
     * @param description The new description of the store
     * @param address The new address of the store
     * @param email The new email of the store
     * @param phoneNumber The new phone number of the store
     */
    public StoreRequest(UUID storeId, String storeName, String description, String address, 
                        String email, String phoneNumber) {
        this.storeId = storeId;
        if(storeId != null) {
            this.storeId = storeId; 
        }
        if(storeName != null) {
            this.storeName = storeName; 
        }
        if(description != null) {
            this.description = description; 
        }
        if(address != null) {
            this.address = address; 
        }
        if(email != null) {
            this.email = email; 
        }
        if(phoneNumber != null) {
            this.phoneNumber = phoneNumber; 
        }
     
    }

    public String getStoreName() {
        return storeName;
    }
    public String getDescription() {
        return description;
    }
    public String getAddress() {
        return address;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getFounderUsername() {
        return founderUsername;
    }
    public UUID getStoreId() {
        return storeId;
    }
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setFounderUsername(String founderUsername) {
        this.founderUsername = founderUsername;
    }
    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }
    
    
}