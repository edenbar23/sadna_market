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

        Product product = productOpt.get();

        // Standardized validation for rating range
        validateRatingValue(ratingValue);

        try {
            // Check if user has already rated this product
            Optional<ProductRating> existingRatingOpt =
                    ratingRepository.findProductRatingByUserAndProduct(username, productId);

            ProductRating rating;
            if (existingRatingOpt.isPresent()) {
                // Update existing rating - transactional approach
                ProductRating existingRating = existingRatingOpt.get();
                int oldRatingValue = existingRating.getRatingValue();
                existingRating.updateRating(ratingValue);

                rating = ratingRepository.saveProductRating(existingRating);

                try {
                    // Update product's overall rating
                    productRepository.updateProductRating(productId, oldRatingValue, ratingValue);
                } catch (Exception e) {
                    logger.error("Failed to update product rating, attempting rollback", e);
                    // Try to rollback rating change - best effort
                    existingRating.updateRating(oldRatingValue);
                    ratingRepository.saveProductRating(existingRating);
                    throw new RuntimeException("Failed to update product rating: " + e.getMessage(), e);
                }

                logger.info("Updated product rating: {}", rating.getRatingId());
            } else {
                // Create new rating - transactional approach
                rating = new ProductRating(username, productId, ratingValue);

                rating = ratingRepository.saveProductRating(rating);

                try {
                    // Update product's overall rating
                    productRepository.addProductRating(productId, username, ratingValue);
                } catch (Exception e) {
                    logger.error("Failed to update product rating, attempting rollback", e);
                    // Try to delete the newly created rating - best effort rollback
                    try {
                        // This method would need to be added to the repository
                        deleteProductRating(rating.getRatingId());
                    } catch (Exception ex) {
                        logger.error("Rollback failed", ex);
                    }
                    throw new RuntimeException("Failed to update product rating: " + e.getMessage(), e);
                }

                logger.info("Created new product rating: {}", rating.getRatingId());
            }

            return rating;
        } catch (Exception e) {
            if (!(e instanceof RuntimeException && e.getCause() != null)) {
                logger.error("Error during rating operation", e);
            }
            throw e;
        }
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

    private void validateRatingValue(int ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            logger.error("Rating value must be between 1 and 5: {}", ratingValue);
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }
    }

    /**
     * Helper method to delete a product rating - used for transaction rollback
     *
     * @param ratingId ID of the rating to delete
     * @return true if successfully deleted, false otherwise
     */
    private boolean deleteProductRating(UUID ratingId) {
        logger.debug("Attempting to delete product rating: {}", ratingId);

        try {
            boolean result = ratingRepository.deleteProductRating(ratingId);

            if (result) {
                logger.info("Successfully deleted product rating: {}", ratingId);
            } else {
                logger.warn("Failed to delete product rating, not found: {}", ratingId);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error deleting product rating: {}", ratingId, e);
            return false;
        }
    }
}