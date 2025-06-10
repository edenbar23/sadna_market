package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;

import java.util.*;

/**
 * Domain service responsible for inventory management operations.
 */
@Service
@RequiredArgsConstructor
public class InventoryManagementService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryManagementService.class);

    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;

    /**
     * Adds a product to a store's inventory
     */
    public UUID addProductToStore(String username, UUID storeId, String name, String category,
                                  String description, double price, int quantity) {
        logger.info("Adding product to store: {}, by user: {}", storeId, username);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (price < 0){
            throw new IllegalArgumentException("Price cannot be negative");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot add product to inactive store");
        }

        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            throw new InsufficientPermissionsException("User does not have permission to add products");
        }

        if (store.isStoreManager(username)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            if (!user.hasPermission(storeId, Permission.MANAGE_INVENTORY) &&
                    !user.hasPermission(storeId, Permission.ADD_PRODUCT)) {
                throw new InsufficientPermissionsException("Manager does not have inventory management permission");
            }
        }

        // Add product to repository
        UUID productId = productRepository.addProduct(
                storeId,
                name,
                category,
                description,
                price,
                true // available by default
        );

        // Add product to store's inventory
        store.addProduct(productId, quantity);
        storeRepository.save(store);

        logger.info("Product added successfully: {} to store: {}", productId, storeId);
        return productId;
    }

    /**
     * Removes a product from a store's inventory
     */
    public void removeProductFromStore(String username, UUID storeId, UUID productId) {
        logger.info("Removing product: {} from store: {}, by user: {}", productId, storeId, username);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot remove product from inactive store");
        }

        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            throw new InsufficientPermissionsException("User does not have permission to remove products");
        }

        if (store.isStoreManager(username)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            if (!user.hasPermission(storeId, Permission.MANAGE_INVENTORY) &&
                    !user.hasPermission(storeId, Permission.REMOVE_PRODUCT)) {
                throw new InsufficientPermissionsException("Manager does not have permission to remove products");
            }
        }

        if (!store.hasProduct(productId)) {
            throw new IllegalArgumentException("Product does not exist in store: " + productId);
        }

        store.removeProduct(productId);
        storeRepository.save(store);

        // Also remove from product repository
        productRepository.deleteProduct(productId);

        logger.info("Product: {} removed successfully from store: {}", productId, storeId);
    }

    /**
     * Updates product information and/or quantity in a store
     */
    public void updateProductInStore(String username, UUID storeId, UUID productId,
                                     String name, String description, String category,
                                     double price, int newQuantity) {
        logger.info("Updating product in store: {}, by user: {}", storeId, username);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (price < 0){
            throw new IllegalArgumentException("Price cannot be negative");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot update product in inactive store");
        }

        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            throw new InsufficientPermissionsException("User does not have permission to update products");
        }

        if (store.isStoreManager(username)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            if (!user.hasPermission(storeId, Permission.MANAGE_INVENTORY) &&
                    !user.hasPermission(storeId, Permission.UPDATE_PRODUCT)) {
                throw new InsufficientPermissionsException("Manager does not have permission to update products");
            }
        }

        if (!store.hasProduct(productId)) {
            throw new IllegalArgumentException("Product does not exist in store: " + productId);
        }

        // Update product repository
        productRepository.updateProduct(productId, name, category, description, price);

        // Update quantity if specified
        if (newQuantity >= 0) {
            try {
                store.updateProductQuantity(productId, newQuantity);
                storeRepository.save(store);
            } catch (Exception e) {
                logger.error("Error updating product quantity: {}", e.getMessage());
                throw new RuntimeException("Failed to update product quantity: " + e.getMessage());
            }
        }

        logger.info("Product: {} updated successfully in store: {}", productId, storeId);
    }

    /**
     * Gets the inventory of a store
     */
    public Map<UUID, Integer> getStoreInventory(UUID storeId) {
        logger.info("Getting inventory for store: {}", storeId);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (!storeRepository.exists(storeId)) {
            throw new StoreNotFoundException("Store not found: " + storeId);
        }

        return storeRepository.getAllProductsInStore(storeId);
    }

    /**
     * Checks if a product is in stock in the specified quantity
     */
    public boolean isProductInStock(UUID storeId, UUID productId, int quantity) {
        logger.debug("Checking if product: {} is in stock in store: {} with quantity: {}",
                productId, storeId, quantity);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (!storeRepository.exists(storeId)) {
            throw new StoreNotFoundException("Store not found: " + storeId);
        }

        return storeRepository.hasProductInStock(storeId, productId, quantity);
    }

    /**
     * Gets product information including inventory status
     */
    public Map<String, Object> getProductWithInventoryStatus(UUID storeId, UUID productId) {
        logger.info("Getting product: {} with inventory status from store: {}", productId, storeId);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.hasProduct(productId)) {
            throw new IllegalArgumentException("Product does not exist in store: " + productId);
        }

        // Get product information
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found in product repository: " + productId);
        }

        Product product = productOpt.get();
        int quantity = store.getProductQuantity(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("quantity", quantity);
        result.put("inStock", quantity > 0);

        return result;
    }

    /**
     * Gets all products in a store with their inventory status
     */
    public List<Map<String, Object>> getAllProductsWithInventoryStatus(UUID storeId) {
        logger.info("Getting all products with inventory status from store: {}", storeId);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (!storeRepository.exists(storeId)) {
            throw new StoreNotFoundException("Store not found: " + storeId);
        }

        Map<UUID, Integer> inventory = storeRepository.getAllProductsInStore(storeId);

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : inventory.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("product", product);
                productInfo.put("quantity", quantity);
                productInfo.put("inStock", quantity > 0);

                results.add(productInfo);
            }
        }

        return results;
    }

    /**
     * Validates product availability across multiple store items
     * Used for validating shopping carts before checkout
     */
    public Map<UUID, List<String>> validateProductAvailability(Map<UUID, Map<UUID, Integer>> storeProductMap) {
        logger.info("Validating product availability across {} stores", storeProductMap.size());

        Map<UUID, List<String>> validationErrors = new HashMap<>();

        for (Map.Entry<UUID, Map<UUID, Integer>> storeEntry : storeProductMap.entrySet()) {
            UUID storeId = storeEntry.getKey();
            Map<UUID, Integer> products = storeEntry.getValue();

            Store store = storeRepository.findById(storeId).orElse(null);
            if (store == null) {
                List<String> errors = new ArrayList<>();
                errors.add("Store not found: " + storeId);
                validationErrors.put(storeId, errors);
                continue;
            }

            if (!store.isActive()) {
                List<String> errors = new ArrayList<>();
                errors.add("Store is not active: " + storeId);
                validationErrors.put(storeId, errors);
                continue;
            }

            Set<String> storeErrors = store.checkCart(products);
            if (!storeErrors.isEmpty()) {
                validationErrors.put(storeId, new ArrayList<>(storeErrors));
            }
        }

        return validationErrors;
    }

    /**
     * Checks if a specific product exists in a store's inventory with the given quantity
     *
     * @param storeId The ID of the store
     * @param productId The ID of the product to check
     * @param requiredQuantity The quantity to verify
     * @return true if the product exists in the store inventory with at least the required quantity
     * @throws IllegalArgumentException if storeId, productId is null or requiredQuantity is negative
     * @throws StoreNotFoundException if the store doesn't exist
     */
    public boolean checkProductAvailability(UUID storeId, UUID productId, int requiredQuantity) {
        logger.info("Checking if product: {} is available in store: {} with quantity: {}",
                productId, storeId, requiredQuantity);

        // Input validation
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (requiredQuantity < 0) {
            throw new IllegalArgumentException("Required quantity cannot be negative");
        }

        // Check if the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));
        logger.info("Store {} found", storeId);
        // Check if the product exists in the store
        if (!store.hasProduct(productId)) {
            logger.info("Product {} does not exist in store {}", productId, storeId);
            return false;
        }
        logger.info("Product {} exists in store {}", productId, storeId);
        // Check if there's enough quantity
        int availableQuantity = store.getProductQuantity(productId);
        logger.info("Available quantity for product {} in store {}: {}", productId, storeId, availableQuantity);
        boolean isAvailable = availableQuantity >= requiredQuantity;

        logger.debug("Product {} in store {}: available quantity = {}, required = {}, available = {}",
                productId, storeId, availableQuantity, requiredQuantity, isAvailable);

        return isAvailable;
    }

    /**
     * Get the available quantity of a product in a specific store
     * (uses existing storeRepository.getProductQuantity)
     */
    public int getAvailableQuantityInStore(UUID storeId, UUID productId) {
        logger.info("Getting available quantity for product {} in store {}", productId, storeId);
        try {
            // Use existing repository method to get current stock
            return storeRepository.getProductQuantity(storeId, productId);
        } catch (Exception e) {
            logger.error("Error getting available product quantity: {}", e.getMessage(), e);
            return 0;
        }
    }
}