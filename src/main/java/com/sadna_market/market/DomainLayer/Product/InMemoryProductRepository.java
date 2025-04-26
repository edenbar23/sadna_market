package com.sadna_market.market.DomainLayer.Product;

import com.sadna_market.market.DomainLayer.IProductRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
    public boolean addProduct(Product product) {
        synchronized (productStorage) {
            logger.debug("Adding new product with ID: {}", product.getProductId());
            String name = product.getName();
            // Check if a product with the same name already exists
            Optional<Product> existingProduct = productStorage.values().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (existingProduct.isPresent()) {
                logger.warn("Product with name '{}' already exists. Not adding.", name);
                return false;
            }
            productStorage.put(product.getProductId(), product);
            logger.info("Product successfully added: {}", product.getProductId());
            return true;
        }
    }


    @Override
    public void updateProduct(Product product) {
        synchronized (productStorage) {
            logger.debug("Updating product with ID: {}", product.getProductId());
            productStorage.put(product.getProductId(), product);
            logger.info("Product successfully updated: {}", product.getProductId());
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        synchronized (productStorage) {
            logger.debug("Deleting product with ID: {}", id);
            productStorage.remove(id);
            logger.info("Product successfully deleted: {}", id);
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
}
