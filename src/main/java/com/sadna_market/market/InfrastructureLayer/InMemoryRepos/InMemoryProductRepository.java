package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product;
import com.sadna_market.market.DomainLayer.ProductRating;
import com.sadna_market.market.DomainLayer.IRatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductRepository implements IProductRepository {
    private final Map<UUID, Product> productStorage = new ConcurrentHashMap<>();
    private final List<String> productReviews = new ArrayList<>(); // Simple storage for reviews
    private static final Logger logger = LoggerFactory.getLogger(IProductRepository.class);

    private final IRatingRepository ratingRepository;

    @Autowired
    public InMemoryProductRepository(IRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
        logger.info("InMemoryProductRepository initialized");
    }

    @Override
    public Optional<Product> findById(UUID id) {
        logger.debug("Searching for product with ID: {}", id);
        return Optional.ofNullable(productStorage.get(id));
    }

    @Override
    public List<Optional<Product>> filterByName(String name) {
        logger.debug("Filtering products by name: '{}'", name);
        return productStorage.values().stream()
                .filter(product -> product.getName().equalsIgnoreCase(name) && product.isAvailable())
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByCategory(String category) {
        logger.debug("Filtering products by category: '{}'", category);
        return productStorage.values().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category) && product.isAvailable())
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice) {
        logger.debug("Filtering products by price range: {} to {}", minPrice, maxPrice);
        return productStorage.values().stream()
                .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice && product.isAvailable())
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByRate(double minRate, double maxRate) {
        logger.debug("Filtering products by rate range: {} to {}", minRate, maxRate);
        return productStorage.values().stream()
                .filter(product -> product.getRate() >= minRate && product.getRate() <= maxRate && product.isAvailable())
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public UUID addProduct(UUID storeId, String name, String category, String description, double price, boolean isAvailable) {
        logger.debug("Adding new product");

        // Check if a product with the same name already exists
        Optional<Product> existingProduct = productStorage.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name) && p.getStoreId().equals(storeId))
                .findFirst();

        if (existingProduct.isPresent()) {
            logger.warn("Product with name '{}' already exists in store {}", name, storeId);
            throw new IllegalArgumentException("Product with the same name already exists in this store");
        }

        Product product = new Product(name, storeId, description, category, price, isAvailable);
        productStorage.put(product.getProductId(), product);
        logger.info("Product successfully added: {}", product.getProductId());

        return product.getProductId();
    }

    @Override
    public void updateProduct(UUID productId, String name, String category, String description, double price) {
        logger.debug("Updating product with ID: {}", productId);

        if (productId == null) {
            logger.error("Product ID should not be null for existing products");
            throw new IllegalArgumentException("Product ID should not be null for existing products");
        }

        Optional<Product> existingProductOpt = findById(productId);
        if (existingProductOpt.isEmpty()) {
            logger.error("Product not found");
            throw new IllegalArgumentException("Product not found");
        }

        Product existingProduct = existingProductOpt.get();
        existingProduct.updateProduct(name, description, category, price);
        productStorage.put(existingProduct.getProductId(), existingProduct);
        logger.info("Product successfully updated: {}", productId);
    }

    @Override
    public void deleteProduct(UUID productId) {
        logger.debug("Deleting product with ID: {}", productId);

        if (productId == null) {
            logger.error("Product ID should not be null");
            throw new IllegalArgumentException("Product ID should not be null");
        }

        if (!productStorage.containsKey(productId)) {
            logger.error("Product not found");
            throw new IllegalArgumentException("Product not found");
        }

        productStorage.remove(productId);
        logger.info("Product successfully deleted: {}", productId);
    }

    @Override
    public List<Optional<Product>> getProductsByIds(Set<UUID> productIds) {
        logger.debug("Retrieving {} products by their IDs", productIds.size());
        return productIds.stream()
                .map(id -> Optional.ofNullable(productStorage.get(id)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> findByStoreId(UUID storeId) {
        logger.debug("Finding products by store ID: {}", storeId);
        return productStorage.values().stream()
                .filter(product -> product.getStoreId().equals(storeId) && product.isAvailable())
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByStoreWithCriteria(UUID storeId, String name, String category,
                                                             Double minPrice, Double maxPrice,
                                                             Double minRate, Double maxRate) {
        logger.debug("Filtering products by store with criteria");

        // Start with all products from this store
        List<Product> storeProducts = productStorage.values().stream()
                .filter(product -> product.getStoreId().equals(storeId) && product.isAvailable())
                .collect(Collectors.toList());

        // Apply filters based on provided criteria
        if (name != null && !name.isEmpty()) {
            storeProducts = storeProducts.stream()
                    .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            storeProducts = storeProducts.stream()
                    .filter(product -> product.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        if (minPrice != null && maxPrice != null) {
            storeProducts = storeProducts.stream()
                    .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        if (minRate != null && maxRate != null) {
            storeProducts = storeProducts.stream()
                    .filter(product -> product.getRate() >= minRate && product.getRate() <= maxRate)
                    .collect(Collectors.toList());
        }

        return storeProducts.stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public void addProductRating(UUID productId, String username, int ratingValue) {
        logger.debug("Adding rating for product {} by user {}: {}", productId, username, ratingValue);

        Optional<Product> productOpt = findById(productId);
        if (productOpt.isEmpty()) {
            logger.error("Product not found: {}", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Product product = productOpt.get();

        // Check if user already rated this product using the ratingRepository
        Optional<ProductRating> existingRating = ratingRepository.findProductRatingByUserAndProduct(username, productId);

        if (existingRating.isPresent()) {
            // Update existing rating
            ProductRating rating = existingRating.get();
            int oldRatingValue = rating.getRatingValue();
            rating.updateRating(ratingValue);
            ratingRepository.saveProductRating(rating);

            // Update product's overall rating
            product.updateRank(oldRatingValue, ratingValue);
        } else {
            // Create new rating
            ProductRating rating = new ProductRating(username, productId, ratingValue);
            ratingRepository.saveProductRating(rating);

            // Update product's overall rating
            product.addRank(ratingValue);
        }

        // Update the product in storage
        productStorage.put(product.getProductId(), product);
        logger.info("Rating added/updated successfully");
    }

    @Override
    public void addProductReview(UUID productId, String username, String reviewText) {
        logger.debug("Adding review for product {} by user {}", productId, username);

        if (!productStorage.containsKey(productId)) {
            logger.error("Product not found: {}", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        // Simple storage of reviews - in a real system you'd have a Review entity
        String reviewEntry = String.format("%s|%s|%s|%s",
                UUID.randomUUID(), // Generate review ID
                username,
                productId,
                reviewText);

        productReviews.add(reviewEntry);
        logger.info("Review added successfully");
    }

    @Override
    public List<Optional<Product>> searchProduct(String name, String category, Double minPrice, Double maxPrice, Double minRate, Double maxRate) {
        logger.debug("Searching products with criteria");

        // Start with all products
        Set<UUID> resultIds = new HashSet<>(productStorage.keySet());

        // Apply filters based on provided criteria
        if (name != null && !name.isEmpty()) {
            Set<UUID> nameIds = productStorage.values().stream()
                    .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());
            resultIds.retainAll(nameIds);
        }

        if (category != null && !category.isEmpty()) {
            Set<UUID> categoryIds = productStorage.values().stream()
                    .filter(product -> product.getCategory().equalsIgnoreCase(category))
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());
            resultIds.retainAll(categoryIds);
        }

        if (minPrice != null && maxPrice != null) {
            Set<UUID> priceIds = productStorage.values().stream()
                    .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());
            resultIds.retainAll(priceIds);
        }

        if (minRate != null && maxRate != null) {
            Set<UUID> rateIds = productStorage.values().stream()
                    .filter(product -> product.getRate() >= minRate && product.getRate() <= maxRate)
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());
            resultIds.retainAll(rateIds);
        }

        return getProductsByIds(resultIds);
    }

    @Override
    public void clear() {
        productStorage.clear();
        productReviews.clear();
        logger.info("Product repository cleared");
    }
}