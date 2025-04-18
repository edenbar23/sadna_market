package com.sadna_market.market.DomainLayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of IStoreRepository interface for version 1
 * Uses collections to store and manage data without an actual database
 */
public class InMemoryStoreRepository implements IStoreRepository {
    
    // Thread-safe collection to store the stores
    private final Map<Long, Store> stores = new ConcurrentHashMap<>();
    
    @Override
    public Optional<Store> findById(Long id) {
        return Optional.ofNullable(stores.get(id));
    }
    
    @Override
    public Optional<Store> findByName(String name) {
        return stores.values().stream()
                .filter(store -> store.getName().equals(name))
                .findFirst();
    }
    
    @Override
    public List<Store> findAll() {
        return new ArrayList<>(stores.values());
    }
    
    @Override
    public void deleteById(Long id) {
        stores.remove(id);
    }
    
    @Override
    public boolean exists(int storeId) {
        return stores.containsKey((long) storeId);
    }
    
    @Override
    public Store save(Store store) {
        stores.put(store.getStoreId(), store);
        return store;
    }
    
    @Override
    public int createStore(String founderUsername, String storeName, String address, String email, String phoneNumber) {
        // Create a StoreFounder object
        StoreFounder founder = new StoreFounder(founderUsername);
        
        // Create store description from address, email, and phone
        String description = String.format("Address: %s, Email: %s, Phone: %s", 
                address, email, phoneNumber);
        
        // Create a new store
        Store store = new Store(storeName, description, founder);
        
        // Save the store to our in-memory repository
        stores.put(store.getStoreId(), store);
        
        // Return the store ID as an int (as required by the interface)
        return store.getStoreId().intValue();
    }
    
    @Override
    public void updateStoreStatus(int storeId, boolean isOpen) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            if (isOpen) {
                store.reopenStore();
            } else {
                store.closeStore();
            }
        }
    }
    
    @Override
    public void addOwner(int storeId, String username) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.addStoreOwner(username);
        }
    }
    
    @Override
    public void removeOwner(int storeId, String username) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            // In the current implementation, Store class doesn't have a removeStoreOwner method
            // We would need to add it to the Store class or handle it here
            // For now, using reflection as a workaround (not recommended for production)
            Set<String> ownerUsernames = store.getOwnerUsernames();
            ownerUsernames.remove(username);
        }
    }
    
    @Override
    public void addManager(int storeId, String username) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.addStoreManager(username);
        }
    }
    
    @Override
    public void removeManager(int storeId, String username) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            // In the current implementation, Store class doesn't have a removeStoreManager method
            // We would need to add it to the Store class or handle it here
            // For now, using reflection as a workaround (not recommended for production)
            Set<String> managerUsernames = store.getManagerUsernames();
            managerUsernames.remove(username);
        }
    }
    
    @Override
    public boolean isOwner(int storeId, String username) {
        Store store = stores.get((long) storeId);
        return store != null && store.isStoreOwner(username);
    }
    
    @Override
    public boolean isManager(int storeId, String username) {
        Store store = stores.get((long) storeId);
        return store != null && store.isStoreManager(username);
    }
    
    @Override
    public Set<String> getStoreOwners(int storeId) {
        Store store = stores.get((long) storeId);
        return store != null ? new HashSet<>(store.getOwnerUsernames()) : new HashSet<>();
    }
    
    @Override
    public Set<String> getStoreManagers(int storeId) {
        Store store = stores.get((long) storeId);
        return store != null ? new HashSet<>(store.getManagerUsernames()) : new HashSet<>();
    }
    
    @Override
    public String getStoreFounder(int storeId) {
        Store store = stores.get((long) storeId);
        return store != null ? store.getFounder().getUsername() : null;
    }
    
    @Override
    public boolean hasProduct(int storeId, int productId) {
        Store store = stores.get((long) storeId);
        return store != null && store.hasProduct((long) productId);
    }
    
    @Override
    public boolean hasProductInStock(int storeId, int productId, int quantity) {
        Store store = stores.get((long) storeId);
        if (store == null || !store.hasProduct((long) productId)) {
            return false;
        }
        return store.getProductQuantity((long) productId) >= quantity;
    }
    
    @Override
    public void addProduct(int storeId, int productId, int quantity) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.addProduct((long) productId, quantity);
        }
    }
    
    @Override
    public void removeProduct(int storeId, int productId) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.removeProduct((long) productId);
        }
    }
    
    @Override
    public void updateProductQuantity(int storeId, int productId, int newQuantity) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.updateProductQuantity((long) productId, newQuantity);
        }
    }
    
    @Override
    public int getProductQuantity(int storeId, int productId) {
        Store store = stores.get((long) storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
        return store.getProductQuantity((long) productId);
    }
    
    @Override
    public Map<Integer, Integer> getAllProductsInStore(int storeId) {
        Store store = stores.get((long) storeId);
        if (store == null) {
            return Collections.emptyMap();
        }
        
        // Convert from Map<Long, Integer> to Map<Integer, Integer>
        return store.getProductQuantities().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().intValue(),
                        Map.Entry::getValue
                ));
    }
    
    @Override
    public void addOrderIdToStore(int storeId, int orderId) {
        Store store = stores.get((long) storeId);
        if (store != null) {
            store.addOrder((long) orderId);
        }
    }
    
    @Override
    public List<Integer> getStoreOrdersIds(int storeId) {
        Store store = stores.get((long) storeId);
        if (store == null) {
            return Collections.emptyList();
        }
        
        // Convert from Set<Long> to List<Integer>
        return store.getOrderIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
    }
    
    @Override
    public Set<Store> findByProductId(int productId) {
        return stores.values().stream()
                .filter(store -> store.hasProduct((long) productId))
                .collect(Collectors.toSet());
    }
    
    @Override
    public Set<Store> findByProductCategory(String category) {
        // Note: This implementation is limited since the Store class doesn't have
        // product category information directly. In a real implementation, you'd need
        // to access products with their categories.
        // For now, returning empty set as a placeholder
        return Collections.emptySet();
    }
    
    @Override
    public Set<Integer> getFilteredProductIds(int storeId, String namePattern, String category,
                                             Double maxPrice, Double minRating) {
        // Note: This implementation is limited since the Store class doesn't have
        // product details like name, category, price, or rating.
        // In a real implementation, you'd need to access product details.
        // For now, returning empty set as a placeholder
        return Collections.emptySet();
    }
}