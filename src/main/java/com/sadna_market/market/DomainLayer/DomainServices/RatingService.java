package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final IRatingRepository ratingRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private final IStoreRepository storeRepository;

    /**
     * Rate a product or update an existing rating
     */
    public ProductRating rateProduct(String username, UUID productId, int ratingValue) {
        logger.info("User {} rating product {} with value {}", username, productId, ratingValue);

        // Validate inputs
        if (!userRepository.contains(username)) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            logger.error("Product not found: {}", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        if (ratingValue < 1 || ratingValue > 5) {
            logger.error("Rating value must be between 1 and 5");
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }

        // Check if user has already rated this product
        Optional<ProductRating> existingRatingOpt =
                ratingRepository.findProductRatingByUserAndProduct(username, productId);

        ProductRating rating;
        if (existingRatingOpt.isPresent()) {
            // Update existing rating
            ProductRating existingRating = existingRatingOpt.get();
            int oldRatingValue = existingRating.getRatingValue();
            existingRating.updateRating(ratingValue);
            rating = ratingRepository.saveProductRating(existingRating);

            // Update product's overall rating
            Product product = productOpt.get();
            product.updateRank(oldRatingValue, ratingValue);

            // Update product in repository (in a real system, you'd use a transaction)
            productRepository.updateProduct(
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    product.getPrice()
            );

            logger.info("Updated product rating: {}", rating.getRatingId());
        } else {
            // Create new rating
            rating = new ProductRating(username, productId, ratingValue);
            rating = ratingRepository.saveProductRating(rating);

            // Update product's overall rating
            Product product = productOpt.get();
            product.addRank(ratingValue);

            // Update product in repository
            productRepository.updateProduct(
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    product.getPrice()
            );

            logger.info("Created new product rating: {}", rating.getRatingId());
        }

        return rating;
    }

    /**
     * Rate a store or update an existing rating
     */
    public StoreRating rateStore(String username, UUID storeId, int ratingValue, String comment) {
        logger.info("User {} rating store {} with value {}", username, storeId, ratingValue);

        // Validate inputs
        if (!userRepository.contains(username)) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        if (ratingValue < 1 || ratingValue > 5) {
            logger.error("Rating value must be between 1 and 5");
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }

        // Check if user has already rated this store
        Optional<StoreRating> existingRatingOpt =
                ratingRepository.findStoreRatingByUserAndStore(username, storeId);

        StoreRating rating;
        if (existingRatingOpt.isPresent()) {
            // Update existing rating
            StoreRating existingRating = existingRatingOpt.get();
            int oldRatingValue = existingRating.getRatingValue();
            existingRating.updateRating(ratingValue, comment);
            rating = ratingRepository.saveStoreRating(existingRating);

            // Update store's overall rating
            store.updateRating(oldRatingValue, ratingValue);

            logger.info("Updated store rating: {}", rating.getRatingId());
        } else {
            // Create new rating
            rating = new StoreRating(username, storeId, ratingValue, comment);
            rating = ratingRepository.saveStoreRating(rating);

            // Update store's overall rating
            store.addRating(ratingValue);

            logger.info("Created new store rating: {}", rating.getRatingId());
        }

        // Save updated store
        storeRepository.save(store);

        return rating;
    }

    /**
     * Get average product rating
     */
    public double getAverageProductRating(UUID productId) {
        return ratingRepository.getAverageProductRating(productId);
    }

    /**
     * Get product rating count
     */
    public int getProductRatingCount(UUID productId) {
        return ratingRepository.getProductRatingCount(productId);
    }

    /**
     * Get average store rating
     */
    public double getAverageStoreRating(UUID storeId) {
        return ratingRepository.getAverageStoreRating(storeId);
    }

    /**
     * Get store rating count
     */
    public int getStoreRatingCount(UUID storeId) {
        return ratingRepository.getStoreRatingCount(storeId);
    }
}