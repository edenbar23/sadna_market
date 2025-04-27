package com.sadna_market.market.ApplicationLayer.Requests;

/**
 * General search request class that serves as a wrapper for different types of searches.
 * Uses StoreRequest internally for store searches.
 */
public class SearchRequest {
    private String name;
    private String productCategory;
    
    /**
     * Default constructor
     */
    public SearchRequest() {
    }
    
    /**
     * Constructor with basic search parameters
     * 
     * @param name The name to search for
     * @param productCategory The product category to filter by
     */
    public SearchRequest(String name, String productCategory) {
        this.name = name;
        this.productCategory = productCategory;
    }
    
    /**
     * Converts this general SearchRequest to a StoreRequest for store searches
     * 
     * @return A StoreRequest with the search parameters
     */
    public StoreRequest toStoreRequest() {
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.setStoreName(this.name);
        storeRequest.setProductCategory(this.productCategory);
        storeRequest.setActiveOnly(true); // Default to active stores only
        return storeRequest;
    }
    
    // Getters and setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getProductCategory() {
        return productCategory;
    }
    
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }
    
    @Override
    public String toString() {
        return "SearchRequest{" +
                "name='" + name + '\'' +
                ", productCategory='" + productCategory + '\'' +
                '}';
    }
}