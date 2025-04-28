package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request class for store-related operations.
 * Used for creating stores, updating store information, and searching stores.
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
    
    // For store search
    private String productCategory;
    private boolean activeOnly = true;
    
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
     * Constructor for store search
     * 
     * @param storeName The name to search for
     * @param productCategory The product category to filter by
     * @param activeOnly Flag to include only active stores
     */
    public StoreRequest(String storeName, String productCategory, boolean activeOnly) {
        this.storeName = storeName;
        this.productCategory = productCategory;
        this.activeOnly = activeOnly;
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
        this.storeName = storeName;
        this.description = description;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}