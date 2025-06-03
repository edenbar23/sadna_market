package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

public interface IStoreRepository {
    // -----------------All the basic CRUD operations--------------------

    /**
     * Finds a store by its ID
     * 
     * @param id The store ID (UUID)
     * @return Optional containing the store if found
     */
    Optional<Store> findById(UUID id);

    /**
     * Finds a store by its name
     * 
     * @param name The store name
     * @return Optional containing the store if found
     */
    Optional<Store> findByName(String name);
    
    /**
     * Gets all stores in the system
     * 
     * @return List of all stores
     */
    List<Store> findAll();
    
    /**
     * Deletes a store by its ID
     * 
     * @param id The store ID (UUID) to delete
     */
    void deleteById(UUID id);
    
    /**
     * Checks if a store exists
     * 
     * @param storeId The store ID (UUID)
     * @return true if the store exists
     */
    boolean exists(UUID storeId);

    /**
     * Saves a store
     * 
     * @param store The store to save
     * @return The saved store
     */
    Store save(Store store);

    /**
     * Creates a new store with the given information
     * 
     * @param founderUsername The username of the store founder
     * @param storeName The name of the new store
     * @param address The physical address of the store
     * @param email The contact email for the store
     * @param phoneNumber The contact phone number for the store
     * @return The UUID of the newly created store
     */
    UUID createStore(String founderUsername, String storeName, String address, String email, String phoneNumber);
    
    /**
     * Updates the status (open/closed) of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param isOpen The new status of the store (true = open, false = closed)
     */
    void updateStoreStatus(UUID storeId, boolean isOpen);

    //------------------------------------Store personnel management-----------------------------------

    /**
     * Adds a user as an owner of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username of the user to be added as owner
     */
    void addOwner(UUID storeId, String username);
    
    /**
     * Removes a user from being an owner of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username of the owner to remove
     */
    void removeOwner(UUID storeId, String username);
    
    /**
     * Adds a user as a manager of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username of the user to be added as manager
     */
    void addManager(UUID storeId, String username);
    
    /**
     * Removes a user from being a manager of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username of the manager to remove
     */
    void removeManager(UUID storeId, String username);
    
    /**
     * Checks if a user is an owner of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username to check
     * @return true if the user is an owner, false otherwise
     */
    boolean isOwner(UUID storeId, String username);
    
    /**
     * Checks if a user is a manager of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param username The username to check
     * @return true if the user is a manager, false otherwise
     */
    boolean isManager(UUID storeId, String username);
    
    /**
     * Gets all owners of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @return Set of usernames of store owners
     */
    Set<String> getStoreOwners(UUID storeId);
    
    /**
     * Gets all managers of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @return Set of usernames of store managers
     */
    Set<String> getStoreManagers(UUID storeId);
    
    /**
     * Gets the founder of a store
     * 
     * @param storeId The ID of the store (UUID)
     * @return The username of the store founder
     */
    String getStoreFounder(UUID storeId);

    //--------------------------Inventory management-----------------------------

    /**
     * Checks if a product exists in a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product to check (UUID)
     * @return true if the product exists in the store, false otherwise
     */
    boolean hasProduct(UUID storeId, UUID productId);
    
    /**
     * Checks if a product is in stock in the specified quantity
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product (UUID)
     * @param quantity The quantity to check for
     * @return true if the required quantity is available, false otherwise
     */
    boolean hasProductInStock(UUID storeId, UUID productId, int quantity);
    
    /**
     * Adds a product to a store with the specified quantity
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product to add (UUID)
     * @param quantity The initial quantity of the product
     */
    void addProduct(UUID storeId, UUID productId, int quantity);
    
    /**
     * Removes a product from a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product to remove (UUID)
     */
    void removeProduct(UUID storeId, UUID productId);
    
    /**
     * Updates the quantity of a product in a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product (UUID)
     * @param newQuantity The new quantity value
     */
    void updateProductQuantity(UUID storeId, UUID productId, int newQuantity);
    
    /**
     * Gets the quantity of a product in a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param productId The ID of the product (UUID)
     * @return The quantity of the product in the store
     */
    int getProductQuantity(UUID storeId, UUID productId);
    
    /**
     * Gets all products in a store with their quantities
     * 
     * @param storeId The ID of the store (UUID)
     * @return Map of product IDs (UUID) to their quantities
     */
    Map<UUID, Integer> getAllProductsInStore(UUID storeId);

    //------------------------------Order management-------------------------------
    /**
     * Associates an order with a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param orderId The ID of the order to add (UUID)
     */
    void addOrderIdToStore(UUID storeId, UUID orderId);

    /**
     * Gets all orders associated with a store
     * 
     * @param storeId The ID of the store (UUID)
     * @return List of order IDs (UUIDs)
     */
    List<UUID> getStoreOrdersIds(UUID storeId);

    //------------------------------Search & Filtering------------------------------
    
    /**
     * Finds stores that sell a specific product
     * 
     * @param productId The ID of the product (UUID)
     * @return Set of stores selling the product
     */
    Set<Store> findByProductId(UUID productId);
    
    /**
     * Finds stores that sell products in a specific category
     * 
     * @param category The product category
     * @return Set of stores with products in the category
     */
    Set<Store> findByProductCategory(String category);
    
    /**
     * Gets filtered products from a store
     * 
     * @param storeId The ID of the store (UUID)
     * @param namePattern The product name pattern to filter by (optional)
     * @param category The product category to filter by (optional)
     * @param maxPrice The maximum price to filter by (optional)
     * @param minRating The minimum product rating to filter by (optional)
     * @return Set of product IDs (UUIDs) that match the criteria
     */
    Set<UUID> getFilteredProductIds(UUID storeId, 
                                  String namePattern, 
                                  String category, 
                                  Double maxPrice, 
                                  Double minRating);

    /**
     * Counts all the stores in the system
     * @return The total number of stores
     */
    int countAll();

    List<Store> getTopRatedStores();

    void addStoreRating(UUID storeId, int rating);

    void clear();
}