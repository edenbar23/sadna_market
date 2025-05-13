package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Product.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final IRatingRepository ratingRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private final IStoreRepository storeRepository;

    @Autowired
    public RatingService(IRatingRepository ratingRepository,
                         IUserRepository userRepository,
                         IProductRepository productRepository,
                         IStoreRepository storeRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Rate a product or update an existing rating
     */
    public ProductRating rateProduct(String username, UUID productId, int ratingValue, String comment) {
        logger.info("User {} rating product {} with value {}", username, productId, ratingValue);

        // Validate inputs
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            logger.error("Product not found: {}", productId);
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        if (ratingValue < 1 || ratingValue > 5) {
            logger.error("Rating value must be between 1 and 5");
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }

        UUID userId = UUID.fromString(user.getUserName()); // This assumes username can be parsed as UUID - probably not the case

        // Check if user has already rated this product
        Optional<ProductRating> existingRatingOpt =
                ratingRepository.findProductRatingByUserAndProduct(userId, productId);

        ProductRating rating;
        if (existingRatingOpt.isPresent()) {
            // Update existing rating
            ProductRating existingRating = existingRatingOpt.get();
            int oldRatingValue = existingRating.getRatingValue();
            existingRating.updateRating(ratingValue, comment);
            rating = ratingRepository.saveProductRating(existingRating);

            // Update product's overall rating
            Product product = productOpt.get();
            product.updateRank(oldRatingValue, ratingValue);

            logger.info("Updated product rating: {}", rating.getRatingId());
        } else {
            // Create new rating
            rating = new ProductRating(userId, username, productId, ratingValue, comment);
            rating = ratingRepository.saveProductRating(rating);

            // Update product's overall rating
            Product product = productOpt.get();
            product.addRank(ratingValue);

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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        if (ratingValue < 1 || ratingValue > 5) {
            logger.error("Rating value must be between 1 and 5");
            throw new IllegalArgumentException("Rating value must be between 1 and 5");
        }

        UUID userId = UUID.fromString(user.getUserName()); // This assumes username can be parsed as UUID

        // Check if user has already rated this store
        Optional<StoreRating> existingRatingOpt =
                ratingRepository.findStoreRatingByUserAndStore(userId, storeId);

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
            rating = new StoreRating(userId, username, storeId, ratingValue, comment);
            rating = ratingRepository.saveStoreRating(rating);

            // Update store's overall rating
            store.addRating(ratingValue);

            logger.info("Created new store rating: {}", rating.getRatingId());
        }

        return rating;
    }

    /**
     * Get product ratings by product ID
     */
    public List<ProductRating> getProductRatings(UUID productId) {
        logger.info("Getting product ratings for product: {}", productId);
        return ratingRepository.findProductRatingsByProduct(productId);
    }

    /**
     * Get store ratings by store ID
     */
    public List<StoreRating> getStoreRatings(UUID storeId) {
        logger.info("Getting store ratings for store: {}", storeId);
        return ratingRepository.findStoreRatingsByStore(storeId);
    }

    /**
     * Get average product rating
     */
    public double getAverageProductRating(UUID productId) {
        logger.info("Getting average product rating for product: {}", productId);
        return ratingRepository.getAverageProductRating(productId);
    }

    /**
     * Get average store rating
     */
    public double getAverageStoreRating(UUID storeId) {
        logger.info("Getting average store rating for store: {}", storeId);
        return ratingRepository.getAverageStoreRating(storeId);
    }
}