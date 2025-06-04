package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.StoreJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class StoreJpaAdapter implements IStoreRepository {
    private static final Logger logger = LoggerFactory.getLogger(StoreJpaAdapter.class);

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    // ================================================================================
    // BASIC CRUD OPERATIONS
    // ================================================================================

    @Override
    public Optional<Store> findById(UUID id) {
        logger.debug("Finding store by ID: {}", id);
        return storeJpaRepository.findById(id);
    }

    @Override
    public Optional<Store> findByName(String name) {
        logger.debug("Finding store by name: {}", name);
        return storeJpaRepository.findByName(name);
    }

    @Override
    public List<Store> findAll() {
        logger.debug("Getting all stores");
        return storeJpaRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        logger.debug("Deleting store with ID: {}", id);
        storeJpaRepository.deleteById(id);
    }

    @Override
    public boolean exists(UUID storeId) {
        boolean exists = storeJpaRepository.existsById(storeId);
        logger.debug("Checking if store exists with ID {}: {}", storeId, exists);
        return exists;
    }

    @Override
    @Transactional
    public Store save(Store store) {
        logger.debug("Saving store: {}", store.getStoreId());
        return storeJpaRepository.save(store);
    }

    @Override
    @Transactional
    public UUID createStore(String founderUsername, String storeName, String address, String email, String phoneNumber) {
        logger.info("Creating new store with name: {} for founder: {}", storeName, founderUsername);

        // Create description from address, email, and phone
        String description = String.format("Address: %s, Email: %s, Phone: %s", address, email, phoneNumber);

        // Create a new store
        Store store = new Store(storeName, description);

        // Create a StoreFounder object with the store's ID
        StoreFounder founder = new StoreFounder(founderUsername, store.getStoreId(), null);

        // Set the founder in the store
        store.setFounder(founder);

        // Save the store to the database
        Store savedStore = storeJpaRepository.save(store);

        logger.info("Store created with ID: {}", savedStore.getStoreId());
        return savedStore.getStoreId();
    }

    @Override
    @Transactional
    public void updateStoreStatus(UUID storeId, boolean isOpen) {
        logger.debug("Updating store status. ID: {}, isOpen: {}", storeId, isOpen);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            if (isOpen) {
                store.reopenStore();
            } else {
                store.closeStore();
            }
            storeJpaRepository.save(store);
            logger.info("Store status updated. ID: {}, isOpen: {}", storeId, isOpen);
        } else {
            logger.warn("Cannot update status - store not found with ID: {}", storeId);
        }
    }

    // ================================================================================
    // STORE PERSONNEL MANAGEMENT
    // ================================================================================

    @Override
    @Transactional
    public void addOwner(UUID storeId, String username) {
        logger.debug("Adding owner {} to store {}", username, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.addStoreOwner(username);
            storeJpaRepository.save(store);
            logger.info("Owner {} added to store {}", username, storeId);
        } else {
            logger.warn("Cannot add owner - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void removeOwner(UUID storeId, String username) {
        logger.debug("Removing owner {} from store {}", username, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.removeStoreOwner(username);
            storeJpaRepository.save(store);
            logger.info("Owner {} removed from store {}", username, storeId);
        } else {
            logger.warn("Cannot remove owner - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void addManager(UUID storeId, String username) {
        logger.debug("Adding manager {} to store {}", username, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.addStoreManager(username);
            storeJpaRepository.save(store);
            logger.info("Manager {} added to store {}", username, storeId);
        } else {
            logger.warn("Cannot add manager - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void removeManager(UUID storeId, String username) {
        logger.debug("Removing manager {} from store {}", username, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.removeStoreManager(username);
            storeJpaRepository.save(store);
            logger.info("Manager {} removed from store {}", username, storeId);
        } else {
            logger.warn("Cannot remove manager - store not found with ID: {}", storeId);
        }
    }

    @Override
    public boolean isOwner(UUID storeId, String username) {
        boolean isOwner = storeJpaRepository.isUserOwnerOfStore(storeId, username);
        logger.debug("Checking if {} is owner of store {}: {}", username, storeId, isOwner);
        return isOwner;
    }

    @Override
    public boolean isManager(UUID storeId, String username) {
        boolean isManager = storeJpaRepository.isUserManagerOfStore(storeId, username);
        logger.debug("Checking if {} is manager of store {}: {}", username, storeId, isManager);
        return isManager;
    }

    @Override
    public Set<String> getStoreOwners(UUID storeId) {
        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            logger.debug("Getting owners of store {}", storeId);
            return storeOpt.get().getOwnerUsernames();
        } else {
            logger.warn("Cannot get owners - store not found with ID: {}", storeId);
            return new HashSet<>();
        }
    }

    @Override
    public Set<String> getStoreManagers(UUID storeId) {
        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            logger.debug("Getting managers of store {}", storeId);
            return storeOpt.get().getManagerUsernames();
        } else {
            logger.warn("Cannot get managers - store not found with ID: {}", storeId);
            return new HashSet<>();
        }
    }

    @Override
    public String getStoreFounder(UUID storeId) {
        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            logger.debug("Getting founder of store {}", storeId);
            StoreFounder founder = storeOpt.get().getFounder();
            return founder != null ? founder.getUsername() : null;
        } else {
            logger.warn("Cannot get founder - store not found with ID: {}", storeId);
            return null;
        }
    }

    // ================================================================================
    // INVENTORY MANAGEMENT
    // ================================================================================

    @Override
    public boolean hasProduct(UUID storeId, UUID productId) {
        boolean hasProduct = storeJpaRepository.storeHasProduct(storeId, productId);
        logger.debug("Checking if store {} has product {}: {}", storeId, productId, hasProduct);
        return hasProduct;
    }

    @Override
    public boolean hasProductInStock(UUID storeId, UUID productId, int quantity) {
        boolean hasStock = storeJpaRepository.storeHasProductInStock(storeId, productId, quantity);
        logger.debug("Checking if store {} has product {} in quantity {}: {}", storeId, productId, quantity, hasStock);
        return hasStock;
    }

    @Override
    @Transactional
    public void addProduct(UUID storeId, UUID productId, int quantity) {
        logger.debug("Adding product {} with quantity {} to store {}", productId, quantity, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.addProduct(productId, quantity);
            storeJpaRepository.save(store);
            logger.info("Product {} added to store {} with quantity {}", productId, storeId, quantity);
        } else {
            logger.warn("Cannot add product - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void removeProduct(UUID storeId, UUID productId) {
        logger.debug("Removing product {} from store {}", productId, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.removeProduct(productId);
            storeJpaRepository.save(store);
            logger.info("Product {} removed from store {}", productId, storeId);
        } else {
            logger.warn("Cannot remove product - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void updateProductQuantity(UUID storeId, UUID productId, int newQuantity) {
        logger.debug("Updating product {} quantity to {} in store {}", productId, newQuantity, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.updateProductQuantity(productId, newQuantity);
            storeJpaRepository.save(store);
            logger.info("Product {} quantity updated to {} in store {}", productId, newQuantity, storeId);
        } else {
            logger.warn("Cannot update product quantity - store not found with ID: {}", storeId);
        }
    }

    @Override
    public int getProductQuantity(UUID storeId, UUID productId) {
        logger.debug("Getting quantity of product {} in store {}", productId, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isEmpty()) {
            logger.warn("Cannot get product quantity - store not found with ID: {}", storeId);
            throw new IllegalArgumentException("Store not found with ID: " + storeId);
        }

        Store store = storeOpt.get();
        int quantity = store.getProductQuantity(productId);
        logger.debug("Quantity of product {} in store {}: {}", productId, storeId, quantity);
        return quantity;
    }

    @Override
    public Map<UUID, Integer> getAllProductsInStore(UUID storeId) {
        logger.debug("Getting all products in store {}", storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isEmpty()) {
            logger.warn("Cannot get products - store not found with ID: {}", storeId);
            return Collections.emptyMap();
        }

        return storeOpt.get().getProductQuantities();
    }

    // ================================================================================
    // ORDER MANAGEMENT
    // ================================================================================

    @Override
    @Transactional
    public void addOrderIdToStore(UUID storeId, UUID orderId) {
        logger.debug("Adding order {} to store {}", orderId, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.addOrder(orderId);
            storeJpaRepository.save(store);
            logger.info("Order {} added to store {}", orderId, storeId);
        } else {
            logger.warn("Cannot add order - store not found with ID: {}", storeId);
        }
    }

    @Override
    public List<UUID> getStoreOrdersIds(UUID storeId) {
        logger.debug("Getting all orders for store {}", storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isEmpty()) {
            logger.warn("Cannot get orders - store not found with ID: {}", storeId);
            return Collections.emptyList();
        }

        return new ArrayList<>(storeOpt.get().getOrderIds());
    }

    // ================================================================================
    // SEARCH & FILTERING
    // ================================================================================

    @Override
    public Set<Store> findByProductId(UUID productId) {
        logger.debug("Finding stores with product {}", productId);
        return new HashSet<>(storeJpaRepository.findStoresByProductId(productId));
    }

    @Override
    public Set<Store> findByProductCategory(String category) {
        logger.debug("Finding stores with products in category: {}", category);
        // This would require integration with the product repository to find products by category
        // then find stores that have those products. For now, returning empty set as placeholder.
        logger.warn("findByProductCategory not fully implemented - requires product repository integration");
        return new HashSet<>();
    }

    @Override
    public Set<UUID> getFilteredProductIds(UUID storeId, String namePattern, String category,
                                           Double maxPrice, Double minRating) {
        logger.debug("Filtering products in store {} by criteria", storeId);
        // This would require integration with the product repository for filtering by name, category, price, rating
        // For now, returning empty set as placeholder.
        logger.warn("getFilteredProductIds not fully implemented - requires product repository integration");
        return new HashSet<>();
    }

    @Override
    public List<Store> getTopRatedStores() {
        logger.debug("Getting top rated stores");

        List<Store> topStores = storeJpaRepository.findTopRatedActiveStores()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        logger.info("Retrieved {} top rated stores", topStores.size());
        return topStores;
    }

    @Override
    @Transactional
    public void addStoreRating(UUID storeId, int rating) {
        logger.debug("Adding rating {} to store {}", rating, storeId);

        Optional<Store> storeOpt = storeJpaRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.addRating(rating);
            storeJpaRepository.save(store);
            logger.info("Rating {} added to store {}", rating, storeId);
        } else {
            logger.warn("Cannot add rating - store not found with ID: {}", storeId);
        }
    }

    @Override
    @Transactional
    public void clear() {
        storeJpaRepository.deleteAll();
        logger.info("Store repository cleared");
    }
}