package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Domain service responsible for inventory management operations.
 * This includes adding, removing, and updating products, as well as
 * checking inventory availability.
 */
public class InventoryManagementService {

    private static InventoryManagementService instance;
    private static final Logger logger = LoggerFactory.getLogger(InventoryManagementService.class);

    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;
    private final RepositoryConfiguration RC;

    /**
     * Private constructor for singleton pattern
     */
    private InventoryManagementService(RepositoryConfiguration RC) {
        this.RC = RC;
        this.storeRepository = RC.storeRepository();
        this.productRepository = RC.productRepository();
        logger.info("InventoryManagementService initialized");
    }

    /**
     * Gets the singleton instance with repository dependency resolution
     */
    public static synchronized InventoryManagementService getInstance(RepositoryConfiguration RC) {
        if (instance == null) {
            instance = new InventoryManagementService(RC);
        }
        return instance;
    }

    /**
     * Adds a product to a store's inventory
     *
     * @param username The username of the user adding the product (must be authorized)
     * @param storeId The ID of the store
     * @param productRequest The product information
     * @param quantity The initial quantity of the product
     * @return The ID of the newly created product
     * @throws IllegalArgumentException If any parameters are invalid
     * @throws StoreNotFoundException If the store doesn't exist
     * @throws InsufficientPermissionsException If the user doesn't have permission
     */
    public UUID addProductToStore(String username, UUID storeId, ProductRequest productRequest, int quantity) {
        logger.info("Adding product to store: {}, by user: {}", storeId, username);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productRequest == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
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
            User user = RC.userRepository().findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            if (!user.hasPermission(storeId, Permission.MANAGE_INVENTORY) &&
                    !user.hasPermission(storeId, Permission.ADD_PRODUCT)) {
                throw new InsufficientPermissionsException("Manager does not have inventory management permission");
            }
        }

        productRepository.addProduct(
                storeId,
                productRequest.getName(),
                productRequest.getCategory(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                true // available by default
        );


        List<Optional<Product>> products = productRepository.filterByName(productRequest.getName());

        Product createdProduct = products.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> p.getStoreId().equals(storeId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Product was not created properly"));

        UUID productId = createdProduct.getProductId();

        store.addProduct(productId, quantity);
        storeRepository.save(store);

        logger.info("Product added successfully: {} to store: {}", productId, storeId);
        return productId;
    }

    /**
     * Removes a product from a store's inventory
     *
     * @param username The username of the user removing the product
     * @param storeId The ID of the store
     * @param productId The ID of the product to remove
     * @throws IllegalArgumentException If any parameters are invalid
     * @throws StoreNotFoundException If the store doesn't exist
     * @throws InsufficientPermissionsException If the user doesn't have permission
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
            User user = RC.userRepository().findByUsername(username)
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

        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductId(productId);

        try {
            productRepository.deleteProduct(productRequest);
        } catch (Exception e) {
            logger.error("Error deleting product from repository: {}", e.getMessage());
            store.addProduct(productId, 0);
            storeRepository.save(store);
            throw new RuntimeException("Failed to delete product: " + e.getMessage());
        }

        logger.info("Product: {} removed successfully from store: {}", productId, storeId);
    }

    /**
     * Updates product information and/or quantity in a store
     *
     * @param username The username of the user updating the product
     * @param storeId The ID of the store
     * @param productRequest The updated product information
     * @param newQuantity The new quantity (or -1 if not changing quantity)
     * @throws IllegalArgumentException If any parameters are invalid
     * @throws StoreNotFoundException If the store doesn't exist
     * @throws InsufficientPermissionsException If the user doesn't have permission
     */
    public void updateProductInStore(String username, UUID storeId, ProductRequest productRequest, int newQuantity) {
        logger.info("Updating product in store: {}, by user: {}", storeId, username);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (productRequest == null || productRequest.getProductId() == null) {
            throw new IllegalArgumentException("Product request and product ID cannot be null");
        }

        UUID productId = productRequest.getProductId();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot update product in inactive store");
        }

        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            throw new InsufficientPermissionsException("User does not have permission to update products");
        }

        if (store.isStoreManager(username)) {
            User user = RC.userRepository().findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            if (!user.hasPermission(storeId, Permission.MANAGE_INVENTORY) &&
                    !user.hasPermission(storeId, Permission.UPDATE_PRODUCT)) {
                throw new InsufficientPermissionsException("Manager does not have permission to update products");
            }
        }

        if (!store.hasProduct(productId)) {
            throw new IllegalArgumentException("Product does not exist in store: " + productId);
        }

        try {
            productRepository.updateProduct(productRequest);
        } catch (Exception e) {
            logger.error("Error updating product in repository: {}", e.getMessage());
            throw new RuntimeException("Failed to update product information: " + e.getMessage());
        }

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
     *
     * @param storeId The ID of the store
     * @return Map of product IDs to their quantities
     * @throws StoreNotFoundException If the store doesn't exist
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
     *
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @param quantity The quantity to check
     * @return true if the product is in stock in the specified quantity
     * @throws IllegalArgumentException If any parameters are invalid
     * @throws StoreNotFoundException If the store doesn't exist
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
     *
     * @param storeId The ID of the store
     * @param productId The ID of the product
     * @return A map containing product information and inventory status
     * @throws IllegalArgumentException If any parameters are invalid
     * @throws StoreNotFoundException If the store doesn't exist
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
        result.put("product", productOpt);
        result.put("quantity", quantity);
        result.put("inStock", quantity > 0);

        return result;
    }

    /**
     * Gets all products in a store with their inventory status
     *
     * @param storeId The ID of the store
     * @return List of maps containing product information and inventory status
     * @throws StoreNotFoundException If the store doesn't exist
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
     * Gets low stock products in a store (products with quantity below threshold)
     *
     * @param storeId The ID of the store
     * @param threshold The quantity threshold
     * @return List of maps containing product information and quantity
     * @throws StoreNotFoundException If the store doesn't exist
     */
    public List<Map<String, Object>> getLowStockProducts(UUID storeId, int threshold) {
        logger.info("Getting low stock products (below: {}) from store: {}", threshold, storeId);

        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        if (!storeRepository.exists(storeId)) {
            throw new StoreNotFoundException("Store not found: " + storeId);
        }

        Map<UUID, Integer> inventory = storeRepository.getAllProductsInStore(storeId);


        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : inventory.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity <= threshold) {
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("product", product);
                    productInfo.put("quantity", quantity);

                    results.add(productInfo);
                }
            }
        }

        return results;
    }

    /**
     * Validates product availability across multiple store items
     * Used for validating shopping carts before checkout
     *
     * @param storeProductMap Map of store IDs to maps of product IDs to quantities
     * @return Map of store IDs to lists of validation error messages, empty if all valid
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

    public static synchronized void reset() {
        instance = null;
        logger.info("InventoryManagementService instance reset");
    }
}