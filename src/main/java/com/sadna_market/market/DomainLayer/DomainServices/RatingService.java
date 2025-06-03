package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final IOrderRepository orderRepository;

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
        // check if the user has bought the product
        if (!orderRepository.hasUserPurchasedProduct(username, productId)) {
            logger.error("User {} has not bought product {}", username, productId);
            throw new IllegalArgumentException("User has not bought this product");
        }
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

    /**
     * Add a review for a product or update an existing one
     */
    public ProductReview reviewProduct(String username, UUID productId, UUID storeId, String reviewText) {
        logger.info("User {} reviewing product {} from store {}", username, productId, storeId);

        // Validate user exists
        if (!userRepository.contains(username)) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        // check if the user has bought the product
        if (!orderRepository.hasUserPurchasedProduct(username, productId)) {
            logger.error("User {} has not bought product {}", username, productId);
            throw new IllegalArgumentException("User has not bought this product");
        }

        // Validate product exists
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            logger.error("Product not found: {}", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        // Validate store exists and has this product
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store not found: {}", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        if (!store.hasProduct(productId)) {
            logger.error("Product {} not found in store {}", productId, storeId);
            throw new IllegalArgumentException("Product not found in this store");
        }

        // Validate review text
        if (reviewText == null || reviewText.trim().isEmpty()) {
            logger.error("Review text cannot be empty");
            throw new IllegalArgumentException("Review text cannot be empty");
        }

        // Check if user has already reviewed this product
        Optional<ProductReview> existingReviewOpt =
                ratingRepository.findProductReviewByUserAndProduct(username, productId);

        try {
            if (existingReviewOpt.isPresent()) {
                // Update existing review
                ProductReview existingReview = existingReviewOpt.get();
                existingReview.updateReview(reviewText);
                ProductReview savedReview = ratingRepository.saveProductReview(existingReview);

                logger.info("Updated product review: {}", savedReview.getReviewId());
                return savedReview;
            } else {
                // Create new review
                ProductReview newReview = new ProductReview(username, productId, storeId, reviewText);
                ProductReview savedReview = ratingRepository.saveProductReview(newReview);

                // Also update the product repository to associate the review
                productRepository.addProductReview(productId, username, reviewText);

                logger.info("Created new product review: {}", savedReview.getReviewId());
                return savedReview;
            }
        } catch (Exception e) {
            logger.error("Error saving product review", e);
            throw new RuntimeException("Failed to save product review: " + e.getMessage(), e);
        }
    }

    /**
     * Get all reviews for a product
     */
    public List<ProductReview> getProductReviews(UUID productId) {
        logger.info("Getting reviews for product: {}", productId);
        return ratingRepository.findProductReviewsByProduct(productId);
    }

    /**
     * Get all reviews by a user
     */
    public List<ProductReview> getUserReviews(String username) {
        logger.info("Getting reviews by user: {}", username);
        return ratingRepository.findProductReviewsByUser(username);
    }

    /**
     * Get all reviews for a store
     */
    public List<ProductReview> getStoreProductReviews(UUID storeId) {
        logger.info("Getting product reviews for store: {}", storeId);
        return ratingRepository.findProductReviewsByStore(storeId);
    }

    /**
     * Delete a review
     */
    public boolean deleteProductReview(String username, UUID reviewId) {
        logger.info("User {} attempting to delete review {}", username, reviewId);

        Optional<ProductReview> reviewOpt = ratingRepository.findProductReviewById(reviewId);
        if (reviewOpt.isEmpty()) {
            logger.error("Review not found: {}", reviewId);
            return false;
        }

        ProductReview review = reviewOpt.get();

        // Only the author or an admin can delete a review
        if (!review.getUsername().equals(username) && !"admin".equals(username)) {
            logger.error("User {} not authorized to delete review {}", username, reviewId);
            throw new IllegalStateException("Not authorized to delete this review");
        }

        boolean success = ratingRepository.deleteProductReview(reviewId);
        if (success) {
            logger.info("Review {} deleted successfully", reviewId);
        } else {
            logger.warn("Failed to delete review {}", reviewId);
        }

        return success;
    }

    /**
     * Get the count of reviews for a product
     */
    public int getProductReviewCount(UUID productId) {
        return ratingRepository.getProductReviewCount(productId);
    }

}