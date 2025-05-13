package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.IRatingRepository;
import com.sadna_market.market.DomainLayer.ProductRating;
import com.sadna_market.market.DomainLayer.StoreRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryRatingRepository implements IRatingRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryRatingRepository.class);

    private final Map<UUID, ProductRating> productRatings = new ConcurrentHashMap<>();
    private final Map<UUID, StoreRating> storeRatings = new ConcurrentHashMap<>();

    // Product rating methods
    @Override
    public ProductRating saveProductRating(ProductRating rating) {
        if (rating == null) {
            logger.error("Attempt to save null product rating");
            throw new IllegalArgumentException("Product rating cannot be null");
        }

        logger.debug("Saving product rating: {}", rating.getRatingId());
        productRatings.put(rating.getRatingId(), rating);
        logger.info("Product rating saved: {}", rating.getRatingId());
        return rating;
    }

    @Override
    public Optional<ProductRating> findProductRatingById(UUID ratingId) {
        if (ratingId == null) {
            logger.warn("Attempt to find product rating with null ID");
            return Optional.empty();
        }

        logger.debug("Finding product rating by ID: {}", ratingId);
        return Optional.ofNullable(productRatings.get(ratingId));
    }

    @Override
    public Optional<ProductRating> findProductRatingByUserAndProduct(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            logger.warn("Attempt to find product rating with null user ID or product ID");
            return Optional.empty();
        }

        logger.debug("Finding product rating by user: {} and product: {}", userId, productId);
        return productRatings.values().stream()
                .filter(rating -> rating.getUserId().equals(userId) && rating.getProductId().equals(productId))
                .findFirst();
    }

    @Override
    public List<ProductRating> findProductRatingsByProduct(UUID productId) {
        if (productId == null) {
            logger.warn("Attempt to find product ratings with null product ID");
            return Collections.emptyList();
        }

        logger.debug("Finding product ratings by product: {}", productId);
        return productRatings.values().stream()
                .filter(rating -> rating.getProductId().equals(productId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRating> findProductRatingsByUser(UUID userId) {
        if (userId == null) {
            logger.warn("Attempt to find product ratings with null user ID");
            return Collections.emptyList();
        }

        logger.debug("Finding product ratings by user: {}", userId);
        return productRatings.values().stream()
                .filter(rating -> rating.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageProductRating(UUID productId) {
        if (productId == null) {
            logger.warn("Attempt to get average product rating with null product ID");
            return 0.0;
        }

        List<ProductRating> ratings = findProductRatingsByProduct(productId);
        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = ratings.stream()
                .mapToInt(ProductRating::getRatingValue)
                .sum();

        return sum / ratings.size();
    }

    @Override
    public int getProductRatingCount(UUID productId) {
        if (productId == null) {
            logger.warn("Attempt to get product rating count with null product ID");
            return 0;
        }

        return findProductRatingsByProduct(productId).size();
    }

    // Store rating methods
    @Override
    public StoreRating saveStoreRating(StoreRating rating) {
        if (rating == null) {
            logger.error("Attempt to save null store rating");
            throw new IllegalArgumentException("Store rating cannot be null");
        }

        logger.debug("Saving store rating: {}", rating.getRatingId());
        storeRatings.put(rating.getRatingId(), rating);
        logger.info("Store rating saved: {}", rating.getRatingId());
        return rating;
    }

    @Override
    public Optional<StoreRating> findStoreRatingById(UUID ratingId) {
        if (ratingId == null) {
            logger.warn("Attempt to find store rating with null ID");
            return Optional.empty();
        }

        logger.debug("Finding store rating by ID: {}", ratingId);
        return Optional.ofNullable(storeRatings.get(ratingId));
    }

    @Override
    public Optional<StoreRating> findStoreRatingByUserAndStore(UUID userId, UUID storeId) {
        if (userId == null || storeId == null) {
            logger.warn("Attempt to find store rating with null user ID or store ID");
            return Optional.empty();
        }

        logger.debug("Finding store rating by user: {} and store: {}", userId, storeId);
        return storeRatings.values().stream()
                .filter(rating -> rating.getUserId().equals(userId) && rating.getStoreId().equals(storeId))
                .findFirst();
    }

    @Override
    public List<StoreRating> findStoreRatingsByStore(UUID storeId) {
        if (storeId == null) {
            logger.warn("Attempt to find store ratings with null store ID");
            return Collections.emptyList();
        }

        logger.debug("Finding store ratings by store: {}", storeId);
        return storeRatings.values().stream()
                .filter(rating -> rating.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreRating> findStoreRatingsByUser(UUID userId) {
        if (userId == null) {
            logger.warn("Attempt to find store ratings with null user ID");
            return Collections.emptyList();
        }

        logger.debug("Finding store ratings by user: {}", userId);
        return storeRatings.values().stream()
                .filter(rating -> rating.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageStoreRating(UUID storeId) {
        if (storeId == null) {
            logger.warn("Attempt to get average store rating with null store ID");
            return 0.0;
        }

        List<StoreRating> ratings = findStoreRatingsByStore(storeId);
        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = ratings.stream()
                .mapToInt(StoreRating::getRatingValue)
                .sum();

        return sum / ratings.size();
    }

    @Override
    public int getStoreRatingCount(UUID storeId) {
        if (storeId == null) {
            logger.warn("Attempt to get store rating count with null store ID");
            return 0;
        }

        return findStoreRatingsByStore(storeId).size();
    }

    @Override
    public void clear() {
        productRatings.clear();
        storeRatings.clear();
        logger.info("Rating repository cleared");
    }
}