package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.DomainLayer.IProductRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.DomainLayer.Product.UserRate;
import com.sadna_market.market.DomainLayer.Product.UserReview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryProductRepository implements IProductRepository {
    // In-memory storage for products
    private Map<UUID, Product> productStorage = new ConcurrentHashMap<>();
    private List<UserRate> userRates = new ArrayList<>();
    private List<UserReview> userReviews = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(IProductRepository.class);
    @Override
    public Optional<Product> findById(UUID id) {
        synchronized (productStorage) {
            logger.debug("Searching for product with ID: {}", id);

            Optional<Product> result = Optional.ofNullable(productStorage.get(id));

            if (result.isPresent()) {
                logger.info("Product found: ID={}, Name={}", id, result.get().getName());
            } else {
                logger.warn("Product not found for ID: {}", id);
            }

            return result;
        }
    }

    @Override
    public List<Optional<Product>> filterByName(String name) {
        synchronized (productStorage) {
            logger.debug("Attempting to filter products by name: '{}'", name);

            List<Optional<Product>> results = productStorage.values().stream()
                    .filter(product -> product.getName().equalsIgnoreCase(name) && product.isAvailable())
                    .map(Optional::of)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                logger.warn("No products found matching name: '{}'", name);
            }

            return results;
        }
    }

    @Override
    public List<Optional<Product>> filterByCategory(String category) {
        synchronized (productStorage) {
            logger.debug("Attempting to filter products by category: '{}'", category);

            List<Optional<Product>> results = productStorage.values().stream()
                    .filter(product -> product.getCategory().equalsIgnoreCase(category) && product.isAvailable())
                    .map(Optional::of)
                    .collect(Collectors.toList());
            if (results.isEmpty()) {
                logger.warn("No products found matching category: '{}'", category);
            }

            return results;
        }
    }

    @Override
    public List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice) {
        synchronized (productStorage) {
            logger.debug("Attempting to filter products by price range: {} to {}", minPrice, maxPrice);

            List<Optional<Product>> results = productStorage.values().stream()
                    .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice && product.isAvailable())
                    .map(Optional::of)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                logger.warn("No products found in price range: {} to {}", minPrice, maxPrice);
            }

            return results;
        }
    }

    @Override
    public List<Optional<Product>> filterByRate(double minRate, double maxRate) {
        synchronized (productStorage) {
            logger.debug("Attempting to filter products by rate range: {} to {}", minRate, maxRate);

            List<Optional<Product>> results = productStorage.values().stream()
                    .filter(product -> product.getRate() >= minRate && product.getRate() <= maxRate && product.isAvailable())
                    .map(Optional::of)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                logger.warn("No products found in rate range: {} to {}", minRate, maxRate);
            }

            return results;
        }
    }

    @Override
    public void addProduct(UUID storeId, String name, String category, String description, double price, boolean isAvailable) {
        synchronized (productStorage) {
            Product product = new Product( name, storeId, category, description, price, isAvailable);
            logger.debug("Adding new product with ID: {}", product.getProductId());

            // Check if a product with the same name already exists
            Optional<Product> existingProduct = productStorage.values().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (existingProduct.isPresent()) {
                logger.warn("Product with name '{}' already exists. Not adding.", name);
                throw new IllegalArgumentException("Product with the same name already exists.");
            }
            productStorage.put(product.getProductId(), product);
            logger.info("Product successfully added: {}", product.getProductId());
        }
    }


    @Override
    public void updateProduct(ProductRequest product) {
        synchronized (productStorage) {
            if (product.getProductId() == null){
                logger.error("Product ID should not be null for existing products");
                throw new IllegalArgumentException("Product ID should not be null for existing products");
            }
            Optional<Product> existingProduct_ = findById(product.getProductId());
            if (existingProduct_.isEmpty()) {
                logger.error("Product not found");
                throw new IllegalArgumentException("Product not found");
            }
            Product existingProduct = existingProduct_.get();
            existingProduct.updateProduct(product);
            logger.debug("Updating product with ID: {}", product.getProductId());
            productStorage.put(existingProduct.getProductId(),existingProduct);
            logger.info("Product successfully updated: {}", product.getProductId());
        }
    }

    @Override
    public void deleteProduct(ProductRequest product) {
        synchronized (productStorage) {
            if (product.getProductId() == null){
                logger.error("Product ID should not be null for existing products");
                throw new IllegalArgumentException("Product ID should not be null for existing products");
            }
            Optional<Product> existingProduct_ = findById(product.getProductId());
            if (existingProduct_.isEmpty()) {
                logger.error("Product not found");
                throw new IllegalArgumentException("Product not found");
            }
            Product existingProduct = existingProduct_.get();
            logger.debug("Deleting product with ID: {}", existingProduct.getProductId());
            productStorage.remove(existingProduct.getProductId());
            logger.info("Product successfully deleted: {}", existingProduct.getProductId());
        }
    }

    /**
     * Retrieves products corresponding to the provided set of UUIDs.
     *
     * @param intersectionIds Set of product UUIDs to retrieve
     * @return List of Optional<Product> objects corresponding to the provided IDs
     */
    public List<Optional<Product>> getProductsByIds(Set<UUID> intersectionIds) {
        logger.debug("Retrieving {} products by their IDs", intersectionIds.size());

        List<Optional<Product>> result = intersectionIds.stream()
                .map(id -> Optional.ofNullable(productStorage.get(id)))
                .collect(Collectors.toList());

        int foundCount = (int) result.stream().filter(Optional::isPresent).count();
        logger.debug("Found {}/{} products from the provided IDs", foundCount, intersectionIds.size());

        return result;
    }


    // Method to handle user rating of a product
    /**
     * Handles the user rating for a product.
     *
     * @param userId    UUID of the user
     * @param productId UUID of the product
     * @param rate      Rating value
     */
    // if the user already rated the product, update the rating
    // if the user didn't rate the product, add a new rating
    public Optional<UserRate> handleUserRate(UUID userId, UUID productId, int rate) {
        synchronized (userRates) {
            logger.debug("Handling user rate: User ID={}, Product ID={}, Rate={}", userId, productId, rate);

            // check if product exist in the system
            Optional<Product> productOpt = findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                // check if user already rated the product
                Optional<UserRate> existingRate = userRates.stream()
                        .filter(userRate -> userRate.getUserId().equals(userId) && userRate.getProductId().equals(productId))
                        .findFirst();
                if (existingRate.isPresent()) {
                    logger.warn("User has already rated this product. Updating rate.");
                    // update userRate list
                    UserRate userRate = existingRate.get();
                    int oldRate = userRate.getRatingValue();
                    userRates.remove(userRate);
                    userRate.changeRatingValue(rate);
                    userRates.add(userRate);
                    // Update the product's rate
                    product.updateRank(oldRate, rate);
                    productStorage.put(product.getProductId(), product);
                    return Optional.of(userRate);
                } else {
                    logger.info("Adding new user rate.");
                    UserRate userRate = new UserRate(userId, productId, rate);
                    userRates.add(userRate);
                    // Update the product's rate
                    product.addRank(rate);
                    productStorage.put(product.getProductId(), product);
                    return Optional.of(userRate);
                }
            } else {
                logger.error("Product not found for ID: {}", productId);
                return Optional.empty();
            }
        }
    }

    // Method to handle user review of a product
    /**
     * Handles the user review for a product.
     *
     * @param userId    UUID of the user
     * @param productId UUID of the product
     * @param reviewText Review text
     */
    public void handleUserReview(UUID userId, UUID productId, String reviewText) {
        synchronized (productStorage) {
            logger.debug("Handling user review: User ID={}, Product ID={}, Review={}", userId, productId, reviewText);
            // check if product exist in the system
            Optional<Product> productOpt = findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                // add the review to the product
                userReviews.add(new UserReview(userId, productId, reviewText));
                logger.info("User review added successfully.");
            } else {
                logger.error("Product not found for ID: {}", productId);
            }
        }
    }

    public List<Optional<Product>> searchProduct(ProductSearchRequest request) {
        logger.debug("Searching for products with parameters: {}", request);
        // Get products by parameters
        List<Optional<Product>> results = getProductsByParameters(request);

        if (results.isEmpty()) {
            logger.warn("No products found matching the search criteria.");
        } else {
            logger.info("Found {} products matching the search criteria.", results.size());
        }

        return results;
    }
    // helper method to search products by parameters
    private List<Optional<Product>> getProductsByParameters(ProductSearchRequest request) {
        // Initialize the result as all products
        List<Optional<Product>> result = getAllProducts();
        Set<UUID> resultIds = result.stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getProductId())
                .collect(Collectors.toSet());

        // Apply each filter only if the corresponding parameter is provided

        // Filter by name if it's not null
        if (request.getName() != null && !request.getName().isEmpty()) {
            List<Optional<Product>> nameFiltered = filterByName(request.getName());
            Set<UUID> nameIds = nameFiltered.stream()
                    .filter(Optional::isPresent)
                    .map(opt -> opt.get().getProductId())
                    .collect(Collectors.toSet());

            resultIds.retainAll(nameIds); // Intersect with current results
        }

        // Filter by category if it's not null
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            List<Optional<Product>> categoryFiltered = filterByCategory(request.getCategory());
            Set<UUID> categoryIds = categoryFiltered.stream()
                    .filter(Optional::isPresent)
                    .map(opt -> opt.get().getProductId())
                    .collect(Collectors.toSet());

            resultIds.retainAll(categoryIds); // Intersect with current results
        }

        // Filter by price range if both min and max are not -1
        if (request.getMinPrice() != -1 && request.getMaxPrice() != -1) {
            List<Optional<Product>> priceFiltered = filterByPriceRange(
                    request.getMinPrice(), request.getMaxPrice());
            Set<UUID> priceIds = priceFiltered.stream()
                    .filter(Optional::isPresent)
                    .map(opt -> opt.get().getProductId())
                    .collect(Collectors.toSet());

            resultIds.retainAll(priceIds); // Intersect with current results
        }

        // Filter by rate range if both min and max are not -1
        if (request.getMinRank() != -1 && request.getMaxRank() != -1) {
            List<Optional<Product>> rateFiltered = filterByRate(
                    request.getMinRank(), request.getMaxRank());
            Set<UUID> rateIds = rateFiltered.stream()
                    .filter(Optional::isPresent)
                    .map(opt -> opt.get().getProductId())
                    .collect(Collectors.toSet());

            resultIds.retainAll(rateIds); // Intersect with current results
        }

        // Get the final list of products from the filtered IDs
        return getProductsByIds(resultIds);
    }
    public List<Optional<Product>> findByStoreId(UUID storeId){
        synchronized (productStorage) {
            logger.debug("Searching for products by store ID: {}", storeId);

            List<Optional<Product>> results = productStorage.values().stream()
                    .filter(product -> product.getStoreId().equals(storeId) && product.isAvailable())
                    .map(Optional::of)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                logger.warn("No products found for store ID: {}", storeId);
            }

            return results;
        }
    }

    public List<Optional<Product>> filterByStoreWithRequest(UUID storeId, ProductSearchRequest request) {
        logger.debug("Searching for products by store ID: {}", storeId);
        // First get all products by the search parameters
        List<Optional<Product>> allResults = getProductsByParameters(request);

        // Then filter to include only products with matching storeId
        List<Optional<Product>> filteredResults = allResults.stream()
                .filter(productOpt -> productOpt.isPresent() &&
                        productOpt.get().getStoreId().equals(storeId))
                .collect(Collectors.toList());

        if (filteredResults.isEmpty()) {
            logger.warn("No products found for store ID: {}", storeId);
        }

        return filteredResults;
    }

}
