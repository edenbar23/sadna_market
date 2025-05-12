package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryStoreRepository implements IStoreRepository {

    private static final Logger logger = LogManager.getLogger(InMemoryStoreRepository.class);

    // Thread-safe collection to store the stores
    private final Map<UUID, Store> stores = new ConcurrentHashMap<>();

    public InMemoryStoreRepository() {
        logger.info("InMemoryStoreRepository initialized");
    }

    @Override
    public Optional<Store> findById(UUID id) {
        logger.debug("Finding store by ID: {}", id);
        return Optional.ofNullable(stores.get(id));
    }

    @Override
    public Optional<Store> findByName(String name) {
        logger.debug("Finding store by name: {}", name);
        return stores.values().stream()
                .filter(store -> store.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Store> findAll() {
        logger.debug("Getting all stores");
        return new ArrayList<>(stores.values());
    }

    @Override
    public void deleteById(UUID id) {
        logger.debug("Deleting store with ID: {}", id);
        stores.remove(id);
    }

    @Override
    public boolean exists(UUID storeId) {
        boolean exists = stores.containsKey(storeId);
        logger.debug("Checking if store exists with ID {}: {}", storeId, exists);
        return exists;
    }

    @Override
    public Store save(Store store) {
        logger.debug("Saving store: {}", store.getStoreId());
        stores.put(store.getStoreId(), store);
        return store;
    }

    @Override
    public UUID createStore(String founderUsername, String storeName, String address, String email, String phoneNumber) {
        logger.info("Creating new store with name: {} for founder: {}", storeName, founderUsername);

        // Create a StoreFounder object
        StoreFounder founder = new StoreFounder(founderUsername, UUID.randomUUID(), null);

        // Create store description from address, email, and phone
        String description = String.format("Address: %s, Email: %s, Phone: %s",
                address, email, phoneNumber);

        // Create a new store
        Store store = new Store(storeName, description, founder);

        // Save the store to our in-memory repository
        stores.put(store.getStoreId(), store);

        logger.info("Store created with ID: {}", store.getStoreId());

        // Return the store ID
        return store.getStoreId();
    }

    @Override
    public void updateStoreStatus(UUID storeId, boolean isOpen) {
        logger.debug("Updating store status. ID: {}, isOpen: {}", storeId, isOpen);
        Store store = stores.get(storeId);
        if (store != null) {
            if (isOpen) {
                store.reopenStore();
            } else {
                store.closeStore();
            }
            logger.info("Store status updated. ID: {}, isOpen: {}", storeId, isOpen);
        } else {
            logger.warn("Cannot update status - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void addOwner(UUID storeId, String username) {
        logger.debug("Adding owner {} to store {}", username, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.addStoreOwner(username);
            logger.info("Owner {} added to store {}", username, storeId);
        } else {
            logger.warn("Cannot add owner - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void removeOwner(UUID storeId, String username) {
        logger.debug("Removing owner {} from store {}", username, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.removeStoreOwner(username);
            logger.info("Owner {} removed from store {}", username, storeId);
        } else {
            logger.warn("Cannot remove owner - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void addManager(UUID storeId, String username) {
        logger.debug("Adding manager {} to store {}", username, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.addStoreManager(username);
            logger.info("Manager {} added to store {}", username, storeId);
        } else {
            logger.warn("Cannot add manager - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void removeManager(UUID storeId, String username) {
        logger.debug("Removing manager {} from store {}", username, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.removeStoreManager(username);
            logger.info("Manager {} removed from store {}", username, storeId);
        } else {
            logger.warn("Cannot remove manager - store not found with ID: {}", storeId);
        }
    }

    @Override
    public boolean isOwner(UUID storeId, String username) {
        Store store = stores.get(storeId);
        boolean isOwner = store != null && store.isStoreOwner(username);
        logger.debug("Checking if {} is owner of store {}: {}", username, storeId, isOwner);
        return isOwner;
    }

    @Override
    public boolean isManager(UUID storeId, String username) {
        Store store = stores.get(storeId);
        boolean isManager = store != null && store.isStoreManager(username);
        logger.debug("Checking if {} is manager of store {}: {}", username, storeId, isManager);
        return isManager;
    }

    @Override
    public Set<String> getStoreOwners(UUID storeId) {
        Store store = stores.get(storeId);
        if (store != null) {
            logger.debug("Getting owners of store {}", storeId);
            return new HashSet<>(store.getOwnerUsernames());
        } else {
            logger.warn("Cannot get owners - store not found with ID: {}", storeId);
            return new HashSet<>();
        }
    }

    @Override
    public Set<String> getStoreManagers(UUID storeId) {
        Store store = stores.get(storeId);
        if (store != null) {
            logger.debug("Getting managers of store {}", storeId);
            return new HashSet<>(store.getManagerUsernames());
        } else {
            logger.warn("Cannot get managers - store not found with ID: {}", storeId);
            return new HashSet<>();
        }
    }

    @Override
    public String getStoreFounder(UUID storeId) {
        Store store = stores.get(storeId);
        if (store != null) {
            logger.debug("Getting founder of store {}", storeId);
            return store.getFounder().getUsername();
        } else {
            logger.warn("Cannot get founder - store not found with ID: {}", storeId);
            return null;
        }
    }

    @Override
    public boolean hasProduct(UUID storeId, UUID productId) {
        Store store = stores.get(storeId);
        boolean hasProduct = store != null && store.hasProduct(productId);
        logger.debug("Checking if store {} has product {}: {}", storeId, productId, hasProduct);
        return hasProduct;
    }

    @Override
    public boolean hasProductInStock(UUID storeId, UUID productId, int quantity) {
        Store store = stores.get(storeId);
        if (store == null || !store.hasProduct(productId)) {
            logger.debug("Store {} doesn't exist or doesn't have product {}", storeId, productId);
            return false;
        }
        boolean hasStock = store.getProductQuantity(productId) >= quantity;
        logger.debug("Checking if store {} has product {} in quantity {}: {}",
                storeId, productId, quantity, hasStock);
        return hasStock;
    }

    @Override
    public void addProduct(UUID storeId, UUID productId, int quantity) {
        logger.debug("Adding product {} with quantity {} to store {}", productId, quantity, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.addProduct(productId, quantity);
            logger.info("Product {} added to store {} with quantity {}", productId, storeId, quantity);
        } else {
            logger.warn("Cannot add product - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void removeProduct(UUID storeId, UUID productId) {
        logger.debug("Removing product {} from store {}", productId, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.removeProduct(productId);
            logger.info("Product {} removed from store {}", productId, storeId);
        } else {
            logger.warn("Cannot remove product - store not found with ID: {}", storeId);
        }
    }

    @Override
    public void updateProductQuantity(UUID storeId, UUID productId, int newQuantity) {
        logger.debug("Updating product {} quantity to {} in store {}", productId, newQuantity, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.updateProductQuantity(productId, newQuantity);
            logger.info("Product {} quantity updated to {} in store {}", productId, newQuantity, storeId);
        } else {
            logger.warn("Cannot update product quantity - store not found with ID: {}", storeId);
        }
    }

    @Override
    public int getProductQuantity(UUID storeId, UUID productId) {
        logger.debug("Getting quantity of product {} in store {}", productId, storeId);
        Store store = stores.get(storeId);
        if (store == null) {
            logger.warn("Cannot get product quantity - store not found with ID: {}", storeId);
            throw new IllegalArgumentException("Store not found with ID: " + storeId);
        }
        int quantity = store.getProductQuantity(productId);
        logger.debug("Quantity of product {} in store {}: {}", productId, storeId, quantity);
        return quantity;
    }

    @Override
    public Map<UUID, Integer> getAllProductsInStore(UUID storeId) {
        logger.debug("Getting all products in store {}", storeId);
        Store store = stores.get(storeId);
        if (store == null) {
            logger.warn("Cannot get products - store not found with ID: {}", storeId);
            return Collections.emptyMap();
        }

        // Return the product quantities map directly since it already has UUID keys
        return new HashMap<>(store.getProductQuantities());
    }

    @Override
    public void addOrderIdToStore(UUID storeId, UUID orderId) {
        logger.debug("Adding order {} to store {}", orderId, storeId);
        Store store = stores.get(storeId);
        if (store != null) {
            store.addOrder(orderId);
            logger.info("Order {} added to store {}", orderId, storeId);
        } else {
            logger.warn("Cannot add order - store not found with ID: {}", storeId);
        }
    }

    @Override
    public List<UUID> getStoreOrdersIds(UUID storeId) {
        logger.debug("Getting all orders for store {}", storeId);
        Store store = stores.get(storeId);
        if (store == null) {
            logger.warn("Cannot get orders - store not found with ID: {}", storeId);
            return Collections.emptyList();
        }

        // Convert from Set<UUID> to List<UUID>
        return new ArrayList<>(store.getOrderIds());
    }

    @Override
    public Set<Store> findByProductId(UUID productId) {
        logger.debug("Finding stores with product {}", productId);
        return stores.values().stream()
                .filter(store -> store.hasProduct(productId))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Store> findByProductCategory(String category) {
        logger.debug("Finding stores with products in category: {}", category);
        // Implementation will need knowledge of product categories
        // which isn't directly available in the Store class
        return stores.values().stream()
                .filter(store -> true) // Replace with actual category check
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> getFilteredProductIds(UUID storeId, String namePattern, String category,
                                           Double maxPrice, Double minRating) {
        logger.debug("Filtering products in store {} by criteria: namePattern={}, category={}, maxPrice={}, minRating={}",
                storeId, namePattern, category, maxPrice, minRating);
        // Implementation would require knowledge of product details
        // which aren't directly accessible from the Store class
        return new HashSet<>();
    }

    @Override
    public void clear() {
        stores.clear();
        logger.info("Store repository cleared");
    }
}