package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class Store {

    @Getter
    private UUID storeId;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String description;

    private boolean active = true;

    @Getter
    private Date creationDate;

    @Getter 
    private StoreFounder founder;


    private Rate storeRate;

    // Using ConcurrentHashMap for thread safety in a multi-user environment
    @Getter
    private final Map<UUID, Integer> productQuantities = new ConcurrentHashMap<>();
    
    // Using ConcurrentHashMap.newKeySet for thread safety
    @Getter
    private final Set<String> ownerUsernames = ConcurrentHashMap.newKeySet();
    
    @Getter
    private final Set<String> managerUsernames = ConcurrentHashMap.newKeySet();
    
    @Getter
    private final Set<UUID> orderIds = ConcurrentHashMap.newKeySet();



    private final Object activeLock = new Object();
    private final Object productLock = new Object();
    private final Object orderLock = new Object();
    private final Object ownerLock = new Object();
    private final Object managerLock = new Object();

    /**
     * Constructor for creating a new store
     * 
     * @param name The store name
     * @param description The store description
     * @param founder The user who founded the store
     */
    public Store(String name, String description, StoreFounder founder) {
        this.storeId = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.active = true;
        this.creationDate = new Date();

        // Add the founder as the first owner of the store
        this.ownerUsernames.add(founder.getUsername());
    }

    /**
     * Constructor for reconstructing a store from the repository
     * 
     * @param storeId The store ID
     * @param name The store name
     * @param description The store description
     * @param active The store's active status
     * @param creationDate The date the store was created
     * @param founder The user who founded the store
     */
    public Store(UUID storeId, String name, String description, boolean active, Date creationDate, StoreFounder founder) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.active = active;
        this.creationDate = creationDate;
        this.founder = founder;
        
        // Add the founder as the first owner of the store
        this.ownerUsernames.add(founder.getUsername());
    }

    /**
     * Gets the store's active status in a thread-safe manner
     * 
     * @return true if the store is active
     */
    public boolean isActive() {
        synchronized (activeLock) {
            return active;
        }
    }

    /**
     * Adds a product to the store inventory
     * 
     * @param productId The product ID
     * @param quantity Initial quantity in stock
     * @throws IllegalArgumentException if quantity is negative or product already exists
     */
    public void addProduct(UUID productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        
        synchronized (productLock) {
            if (productQuantities.containsKey(productId)) {
                throw new IllegalArgumentException("Product already exists in store");
            }
            
            productQuantities.put(productId, quantity);
        }
    }

    /**
     * Updates the quantity of a product in inventory
     * 
     * @param productId The product ID
     * @param newQuantity The new quantity
     * @throws IllegalArgumentException if quantity is negative or product doesn't exist
     * @throws IllegalStateException if store is inactive
     */
    public void updateProductQuantity(UUID productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
        
        synchronized (productLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot update product quantity in inactive store");
            }
            
            if (!productQuantities.containsKey(productId)) {
                throw new IllegalArgumentException("Product does not exist in store");
            }
            
            productQuantities.put(productId, newQuantity);
        }
    }
    
    /**
     * Removes a product from the store
     * 
     * @param productId The ID of the product to remove
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if product doesn't exist
     */
    public void removeProduct(UUID productId) {
        synchronized (productLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot remove product from inactive store");
            }
            
            if (!productQuantities.containsKey(productId)) {
                throw new IllegalArgumentException("Product does not exist in store");
            }
            
            productQuantities.remove(productId);
        }
    }
    
    /**
     * Checks if a product exists in the store
     * 
     * @param productId The product ID to check
     * @return true if the product exists
     */
    public boolean hasProduct(UUID productId) {
        return productQuantities.containsKey(productId);
    }

    /**
     * Gets the quantity of a product in stock
     * 
     * @param productId The product ID
     * @return The quantity in stock
     * @throws IllegalArgumentException if product doesn't exist
     */
    public int getProductQuantity(UUID productId) {
        Integer quantity = productQuantities.get(productId);
        if (quantity == null) {
            throw new IllegalArgumentException("Product does not exist in store");
        }
        return quantity;
    }

    /**
     * Adds a new owner to the store
     * 
     * @param newOwnerUsername The username of the new owner
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if user is already an owner
     */
    public void addStoreOwner(String newOwnerUsername) {
        synchronized (ownerLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot add owner to inactive store");
            }
            
            // Check if user is already an owner
            if (isStoreOwner(newOwnerUsername)) {
                throw new IllegalArgumentException("User is already an owner of this store");
            }
            
            ownerUsernames.add(newOwnerUsername);
        }
    }

    /**
     * Adds a new manager to the store
     * 
     * @param newManagerUsername The username of the new manager
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if user is already a manager
     */
    public void addStoreManager(String newManagerUsername) {
        synchronized (managerLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot add manager to inactive store");
            }
            
            // Check if user is already a manager
            if (isStoreManager(newManagerUsername)) {
                throw new IllegalArgumentException("User is already a manager of this store");
            }
            
            managerUsernames.add(newManagerUsername);
        }
    }
    
    /**
     * Removes a store owner
     * 
     * @param ownerUsername The username of the owner to remove
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if user is not an owner
     */
    public void removeStoreOwner(String ownerUsername) {
        synchronized (ownerLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot remove owner from inactive store");
            }
            
            // Check if user is the founder
            if (isFounder(ownerUsername)) {
                throw new IllegalArgumentException("Cannot remove the founder of the store");
            }
            
            // Check if user is an owner
            if (!isStoreOwner(ownerUsername)) {
                throw new IllegalArgumentException("User is not an owner of this store");
            }
            
            ownerUsernames.remove(ownerUsername);
        }
    }
    
    /**
     * Removes a store manager
     * 
     * @param managerUsername The username of the manager to remove
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if user is not a manager
     */
    public void removeStoreManager(String managerUsername) {
        synchronized (managerLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot remove manager from inactive store");
            }
            
            // Check if user is a manager
            if (!isStoreManager(managerUsername)) {
                throw new IllegalArgumentException("User is not a manager of this store");
            }
            
            managerUsernames.remove(managerUsername);
        }
    }

    /**
     * Closes the store (sets it to inactive)
     * 
     * @throws IllegalStateException if store is already closed
     */
    public void closeStore() {
        synchronized (activeLock) {
            if (!isActive()) {
                throw new IllegalStateException("Store is already closed");
            }
            
            this.active = false;
            
            // Note: In Version 1, notifications are not implemented yet
        }
    }

    /**
     * Reopens a closed store (sets it to active)
     * 
     * @throws IllegalStateException if store is already open
     */
    public void reopenStore() {
        synchronized (activeLock) {
            if (isActive()) {
                throw new IllegalStateException("Store is already open");
            }
            
            this.active = true;
            
            // Note: In Version 1, notifications are not implemented yet
        }
    }

    /**
     * Checks if a user is the founder of the store
     * 
     * @param username The username to check
     * @return true if the user is the founder
     */
    public boolean isFounder(String username) {
        return founder.getUsername().equals(username);
    }
    
    /**
     * Checks if a user is an owner of the store
     * 
     * @param username The username to check
     * @return true if the user is an owner
     */
    public boolean isStoreOwner(String username) {
        return ownerUsernames.contains(username);
    }
    
    /**
     * Checks if a user is a manager of the store
     * 
     * @param username The username to check
     * @return true if the user is a manager
     */
    public boolean isStoreManager(String username) {
        return managerUsernames.contains(username);
    }

    /**
     * Records a new order for this store
     * 
     * @param orderId The ID of the order
     * @throws IllegalStateException if store is inactive
     * @throws IllegalArgumentException if order already exists
     */
    public void addOrder(UUID orderId) {
        synchronized (orderLock) {
            if (!isActive()) {
                throw new IllegalStateException("Cannot add order to inactive store");
            }
            
            if (orderIds.contains(orderId)) {
                throw new IllegalArgumentException("Order already exists in store");
            }
            
            orderIds.add(orderId);
        }
    }

    /**
     * Checks inventory availability for a purchase
     * 
     * @param items Map of product IDs to quantities
     * @return Set of error messages, empty if all items are available
     */
    public Set<String> checkCart(Map<UUID, Integer> items) {
        Set<String> errors = new HashSet<>();
        
        synchronized (productLock) {
            if (!isActive()) {
                errors.add("Store is not active");
                return errors;
            }
            
            for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                UUID productId = entry.getKey();
                Integer requestedQuantity = entry.getValue();
                
                if (!productQuantities.containsKey(productId)) {
                    errors.add("Product " + productId + " does not exist in store");
                } else if (requestedQuantity > productQuantities.get(productId)) {
                    errors.add("Not enough stock for product " + productId + 
                              ". Available: " + productQuantities.get(productId) + 
                              ", Requested: " + requestedQuantity);
                }
            }
        }

        return errors;
    }

    /**
     * Updates inventory after a successful purchase
     * 
     * @param items Map of product IDs to quantities
     * @return Set of error messages, empty if update was successful
     */
    public Set<String> updateStockAfterPurchase(Map<UUID, Integer> items) {
        synchronized (productLock) {
            Set<String> checkResult = checkCart(items);
            if (!checkResult.isEmpty()) {
                return checkResult;
            }
            
            // Update quantities
            for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                UUID productId = entry.getKey();
                Integer purchasedQuantity = entry.getValue();
                
                Integer currentQuantity = productQuantities.get(productId);
                productQuantities.put(productId, currentQuantity - purchasedQuantity);
            }
            
            return new HashSet<>();
        }
    }

    public void addRating(int rateValue) {
        if (rateValue < 1 || rateValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        storeRate.addRank(rateValue);
    }

    public void updateRating(int oldRate, int newRate) {
        if (newRate < 1 || newRate > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        storeRate.updateRank(oldRate, newRate);
    }

    public double getStoreRate() {
        return storeRate.getRateVal();
    }

    public int getNumOfRanks() {
        return storeRate.getNumOfRanks();
    }
}