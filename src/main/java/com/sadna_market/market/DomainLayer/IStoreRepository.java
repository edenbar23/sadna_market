package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.Set;
import java.util.Map;

public interface IStoreRepository {
    // -----------------All the basic CRUD operations--------------------

    /**
     * Finds a store by its ID
     * 
     * @param id The store ID
     * @return Optional containing the store if found
     * 
     */
    Optional<Store> findById(Long id);

    /**
     * 
     * @param name
     * @return Store with the given name
     * 
     */
    Optional<Store> findByName(String name);
    
    /**
     * Gets all stores in the system
     * 
     * @return List of all stores
     * 
     */
    List<Store> findAll();
    
    /**
     * Deletes a store by its ID
     * 
     * @param id The store ID to delete
     * 
     */
    void deleteById(Long id);
    
    /**
     * 
     * @param storeId
     * @return true if the store exist
     * 
     */
    boolean exists(int storeId);

    /**
     * 
     * @param Store - the store to save
     * @return the Saved store
     * 
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
     * @return The ID of the newly created store
     * 
     */
    int createStore(String founderUsername, String storeName, String address, String email, String phoneNumber);
    
    /**
     * Updates the status (open/closed) of a store
     * 
     * @param storeId The ID of the store to update
     * @param isOpen The new status of the store (true = open, false = closed)
     */
    void updateStoreStatus(int storeId, boolean isOpen);

    //------------------------------------Store personnel management-----------------------------------

    /**
     * Adds a user as an owner of a store
     * 
     * @param storeId The ID of the store
     * @param username The username of the user to be added as owner
     */
    void addOwner(int storeId, String username);
    
    /**
     * TODO: think where we want it
     * Removes a user from being an owner of a store
     * 
     * @param storeId The ID of the store
     * @param username The username of the owner to remove
     */

    void removeOwner(int storeId, String username);
    

    /**
     * Adds a user as a manager of a store
     * 
     * @param storeId The ID of the store
     * @param username The username of the user to be added as manager
     */
    void addManager(int storeId, String username);
    
    /**
     * TODO: think where we want it
     * Removes a user from being a manager of a store
     * 
     * @param storeId The ID of the store
     * @param username The username of the manager to remove
    */
    void removeManager(int storeId, String username);
    

    /**
     * //TODO: need to think if we want this to be in the user repository or here

     * Updates the permissions of a store manager
     * 
     * @param storeId The ID of the store
     * @param username The username of the manager
     * @param permissions Set of permission keys to be assigned to the manager
     
    void updateManagerPermissions(int storeId, String username, Set<String> permissions);
    */

    /**
     * TODO: think where we want it
     * Gets all owners of a store
     * 
     * @param storeId The ID of the store
     * @return Set of usernames of store owners
     
    Set<String> getStoreOwners(int storeId);
    */
    
    /**
     * TODO: think where we want it
     * Gets all managers of a store
     * 
     * @param storeId The ID of the store
     * @return Set of usernames of store managers
     
    Set<String> getStoreManagers(int storeId);
    */
    
    /**
     * TODO: think where we want it
     * Gets permissions for all managers of a store
     * 
     * @param storeId The ID of the store
     * @return Map of manager usernames to their permission sets
     
    Map<String, Set<String>> getManagerPermissions(int storeId);
    */

    /**
     * Checks if a user is an owner of a store
     * 
     * @param storeId The ID of the store
     * @param username The username to check
     * @return true if the user is an owner, false otherwise
     */
    boolean isOwner(int storeId, String username);
    
    /**
     * Checks if a user is a manager of a store
     * 
     * @param storeId The ID of the store
     * @param username The username to check
     * @return true if the user is a manager, false otherwise
     */
    boolean isManager(int storeId, String username);
    
    /**
     * Gets all owners of a store
     * 
     * @param storeId The ID of the store
     * @return Set of usernames of store owners
     */
    Set<String> getStoreOwners(int storeId);
    
    /**
     * Gets all managers of a store
     * 
     * @param storeId The ID of the store
     * @return Set of usernames of store managers
     */
    Set<String> getStoreManagers(int storeId);
    
    /**
     * Gets the founder of a store
     * 
     * @param storeId The ID of the store
     * @return The username of the store founder
     */
    String getStoreFounder(int storeId);

    //--------------------------Inventory management-----------------------------

    /**
     * Checks if a product exists in a store
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product to check
     * @return true if the product exists in the store, false otherwise
     */
    boolean hasProduct(int storeId, int productId);
    
    /**
     * Checks if a product is in stock in the specified quantity
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param quantity The quantity to check for
     * @return true if the required quantity is available, false otherwise
     */
    boolean hasProductInStock(int storeId, int productId, int quantity);
    
    /**
     * Adds a product to a store with the specified quantity
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product to add
     * @param quantity The initial quantity of the product
     */
    void addProduct(int storeId, int productId, int quantity);
    
    /**
     * Removes a product from a store
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product to remove
     */
    void removeProduct(int storeId, int productId);
    
    /**
     * Updates the quantity of a product in a store
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param newQuantity The new quantity value
     */
    void updateProductQuantity(int storeId, int productId, int newQuantity);
    
    /**
     * Gets the quantity of a product in a store
     * 
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @return The quantity of the product in the store
     */
    int getProductQuantity(int storeId, int productId);
    
    /**
     * Gets all products in a store with their quantities
     * 
     * @param storeId The ID of the store
     * @return Map of product IDs to their quantities
     */
    Map<Integer, Integer> getAllProductsInStore(int storeId);

    //------------------------------Order management-------------------------------
    /**
     * Associates an order with a store
     * 
     * @param storeId The ID of the store
     * @param orderId The ID of the order to add
     */
    void addOrderIdToStore(int storeId, int orderId);

    /**
     * Gets all orders associated with a store
     * 
     * @param storeId The ID of the store
     * @return List of order IDs
     */
    List<Integer> getStoreOrdersIds(int storeId);

    //------------------------------Search & Filtering------------------------------
    
    /**
     * Finds stores that sell a specific product
     * 
     * @param productId The ID of the product
     * @return Set of stores selling the product
     */
    Set<Store> findByProductId(int productId);
    
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
     * @param storeId The ID of the store
     * @param productName The product name to filter by (optional)
     * @param category The product category to filter by (optional)
     * @param maxPrice The maximum price to filter by (optional)
     * @param minRating The minimum product rating to filter by (optional)
     * @return Map of product DTOs to their quantities
     */
    Set<Integer> getFilteredProductIds(int storeId, 
                                     String namePattern, 
                                     String category, 
                                     Double maxPrice, 
                                     Double minRating);







    

} 
