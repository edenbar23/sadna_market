package com.sadna_market.market.DomainLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@NoArgsConstructor
public class Store {
    private static final AtomicLong STORE_ID_GENERATOR = new AtomicLong(1);
    
    @Getter
    private Long storeId;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String description;

    private boolean active = true;

    @Getter
    private Date creationDate;

    @Getter StoreFounder founder;

    // Using ConcurrentHashMap for thread safety in a multi-user environment
    @Getter
    private final Map<Long, Integer> productQuantities = new ConcurrentHashMap<>();
    
    // Using ConcurrentHashMap.newKeySet for thread safety
    @Getter
    private final Set<String> ownerUsernames = ConcurrentHashMap.newKeySet();
    
    @Getter
    private final Set<String> managerUsernames = ConcurrentHashMap.newKeySet();
    
    @Getter
    private final Set<Long> orderIds = ConcurrentHashMap.newKeySet();

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
        this.storeId = STORE_ID_GENERATOR.getAndIncrement();
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.active = true;
        this.creationDate = new Date();

        //add the founder as the first owner of the store
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

    public Store(Long storeId, String name, String description, boolean active, Date creationDate, StoreFounder founder) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.active = active;
        this.creationDate = creationDate;
        this.founder = founder;
        //TODO: finish the owner and manager usernames

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
    public void addProduct(Long productId, int quantity) {
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
    public void updateProductQuantity(Long productId, int newQuantity) {
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
    public void removeProduct(Long productId) {
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
    public boolean hasProduct(Long productId) {
        return productQuantities.containsKey(productId);
    }

    /**
     * Gets the quantity of a product in stock
     * 
     * @param productId The product ID
     * @return The quantity in stock
     * @throws IllegalArgumentException if product doesn't exist
     */
    public int getProductQuantity(Long productId) {
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
    public void addOrder(Long orderId) {
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
    public Set<String> checkCart(Map<Long, Integer> items) {
        Set<String> errors = new HashSet<>();
        
        synchronized (productLock) {
            if (!isActive()) {
                errors.add("Store is not active");
                return errors;
            }
            
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
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
    public Set<String> updateStockAfterPurchase(Map<Long, Integer> items) {
        synchronized (productLock) {
            Set<String> checkResult = checkCart(items);
            if (!checkResult.isEmpty()) {
                return checkResult;
            }
            
            // Update quantities
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                Long productId = entry.getKey();
                Integer purchasedQuantity = entry.getValue();
                
                Integer currentQuantity = productQuantities.get(productId);
                productQuantities.put(productId, currentQuantity - purchasedQuantity);
            }
            
            return new HashSet<>();
        }
    }
}

