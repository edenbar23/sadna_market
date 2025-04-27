package com.sadna_market.market.ApplicationLayer;

/**
 * Request object for store operations including creation, updates, and searches.
 * Contains fields for both store details and search criteria.
 */
public class StoreRequest {
    // Store creation/update fields
    private String storeName;
    private String description;
    private String address;
    private String email;
    private String phoneNumber;
    private String founderUsername;
    
    // Search criteria fields
    private String productCategory;
    private Double minRating;
    private Double maxRating;
    private Boolean activeOnly;
    
    /**
     * Default constructor for StoreRequest
     */
    public StoreRequest() {
        this.activeOnly = true; // By default, include only active stores in search
    }
    
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
    public StoreRequest(String storeName, String description, String address, String email, String phoneNumber, String founderUsername) {
        this.storeName = storeName;
        this.description = description;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.founderUsername = founderUsername;
    }
    
    /**
     * Constructor for store searches
     * 
     * @param storeName The store name (or partial name) to search for
     * @param description The description (or partial description) to search for
     * @param productCategory The product category to filter stores by
     * @param minRating The minimum rating to filter stores by
     * @param maxRating The maximum rating to filter stores by
     * @param activeOnly Whether to include only active stores in results
     */
    public StoreRequest(String storeName, String description, String productCategory, Double minRating, Double maxRating, Boolean activeOnly) {
        this.storeName = storeName;
        this.description = description;
        this.productCategory = productCategory;
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.activeOnly = activeOnly != null ? activeOnly : true;
    }

    // Getters and setters for store details
    
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
    
    // Getters and setters for search criteria
    
    public String getProductCategory() {
        return productCategory;
    }
    
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }
    
    public Double getMinRating() {
        return minRating;
    }
    
    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }
    
    public Double getMaxRating() {
        return maxRating;
    }
    
    public void setMaxRating(Double maxRating) {
        this.maxRating = maxRating;
    }
    
    public Boolean getActiveOnly() {
        return activeOnly;
    }
    
    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly != null ? activeOnly : true;
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
                ", productCategory='" + productCategory + '\'' +
                ", minRating=" + minRating +
                ", maxRating=" + maxRating +
                ", activeOnly=" + activeOnly +
                '}';
    }
}