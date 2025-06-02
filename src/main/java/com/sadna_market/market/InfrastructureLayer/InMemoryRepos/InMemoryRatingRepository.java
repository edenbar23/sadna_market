//package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;
//
//import com.sadna_market.market.DomainLayer.IRatingRepository;
//import com.sadna_market.market.DomainLayer.ProductRating;
//import com.sadna_market.market.DomainLayer.ProductReview;
//import com.sadna_market.market.DomainLayer.StoreRating;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Repository;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@Repository
//public class InMemoryRatingRepository implements IRatingRepository {
//    private static final Logger logger = LoggerFactory.getLogger(InMemoryRatingRepository.class);
//
//    private final Map<UUID, ProductRating> productRatings = new ConcurrentHashMap<>();
//    private final Map<UUID, StoreRating> storeRatings = new ConcurrentHashMap<>();
//    private final Map<UUID, ProductReview> productReviews = new ConcurrentHashMap<>();
//
//    // Product rating methods
//    @Override
//    public ProductRating saveProductRating(ProductRating rating) {
//        if (rating == null) {
//            logger.error("Attempt to save null product rating");
//            throw new IllegalArgumentException("Product rating cannot be null");
//        }
//
//        logger.debug("Saving product rating: {}", rating.getRatingId());
//        productRatings.put(rating.getRatingId(), rating);
//        logger.info("Product rating saved: {}", rating.getRatingId());
//        return rating;
//    }
//
//    @Override
//    public Optional<ProductRating> findProductRatingById(UUID ratingId) {
//        if (ratingId == null) {
//            logger.warn("Attempt to find product rating with null ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding product rating by ID: {}", ratingId);
//        return Optional.ofNullable(productRatings.get(ratingId));
//    }
//
//    @Override
//    public Optional<ProductRating> findProductRatingByUserAndProduct(String username, UUID productId) {
//        if (username == null || username.isEmpty() || productId == null) {
//            logger.warn("Attempt to find product rating with null/empty username or null product ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding product rating by user: {} and product: {}", username, productId);
//        return productRatings.values().stream()
//                .filter(rating -> rating.getUsername().equals(username) && rating.getProductId().equals(productId))
//                .findFirst();
//    }
//
//    @Override
//    public List<ProductRating> findProductRatingsByProduct(UUID productId) {
//        if (productId == null) {
//            logger.warn("Attempt to find product ratings with null product ID");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding product ratings by product: {}", productId);
//        return productRatings.values().stream()
//                .filter(rating -> rating.getProductId().equals(productId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProductRating> findProductRatingsByUser(String username) {
//        if (username == null || username.isEmpty()) {
//            logger.warn("Attempt to find product ratings with null/empty username");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding product ratings by user: {}", username);
//        return productRatings.values().stream()
//                .filter(rating -> rating.getUsername().equals(username))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public double getAverageProductRating(UUID productId) {
//        if (productId == null) {
//            logger.warn("Attempt to get average product rating with null product ID");
//            return 0.0;
//        }
//
//        List<ProductRating> ratings = findProductRatingsByProduct(productId);
//        if (ratings.isEmpty()) {
//            return 0.0;
//        }
//
//        double sum = ratings.stream()
//                .mapToInt(ProductRating::getRatingValue)
//                .sum();
//
//        return sum / ratings.size();
//    }
//
//    @Override
//    public int getProductRatingCount(UUID productId) {
//        if (productId == null) {
//            logger.warn("Attempt to get product rating count with null product ID");
//            return 0;
//        }
//
//        return findProductRatingsByProduct(productId).size();
//    }
//
//    // Store rating methods
//    @Override
//    public StoreRating saveStoreRating(StoreRating rating) {
//        if (rating == null) {
//            logger.error("Attempt to save null store rating");
//            throw new IllegalArgumentException("Store rating cannot be null");
//        }
//
//        logger.debug("Saving store rating: {}", rating.getRatingId());
//        storeRatings.put(rating.getRatingId(), rating);
//        logger.info("Store rating saved: {}", rating.getRatingId());
//        return rating;
//    }
//
//    @Override
//    public Optional<StoreRating> findStoreRatingById(UUID ratingId) {
//        if (ratingId == null) {
//            logger.warn("Attempt to find store rating with null ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding store rating by ID: {}", ratingId);
//        return Optional.ofNullable(storeRatings.get(ratingId));
//    }
//
//    @Override
//    public Optional<StoreRating> findStoreRatingByUserAndStore(String username, UUID storeId) {
//        if (username == null || username.isEmpty() || storeId == null) {
//            logger.warn("Attempt to find store rating with null/empty username or null store ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding store rating by user: {} and store: {}", username, storeId);
//        return storeRatings.values().stream()
//                .filter(rating -> rating.getUsername().equals(username) && rating.getStoreId().equals(storeId))
//                .findFirst();
//    }
//
//    @Override
//    public List<StoreRating> findStoreRatingsByStore(UUID storeId) {
//        if (storeId == null) {
//            logger.warn("Attempt to find store ratings with null store ID");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding store ratings by store: {}", storeId);
//        return storeRatings.values().stream()
//                .filter(rating -> rating.getStoreId().equals(storeId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<StoreRating> findStoreRatingsByUser(String username) {
//        if (username == null || username.isEmpty()) {
//            logger.warn("Attempt to find store ratings with null/empty username");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding store ratings by user: {}", username);
//        return storeRatings.values().stream()
//                .filter(rating -> rating.getUsername().equals(username))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public double getAverageStoreRating(UUID storeId) {
//        if (storeId == null) {
//            logger.warn("Attempt to get average store rating with null store ID");
//            return 0.0;
//        }
//
//        List<StoreRating> ratings = findStoreRatingsByStore(storeId);
//        if (ratings.isEmpty()) {
//            return 0.0;
//        }
//
//        double sum = ratings.stream()
//                .mapToInt(StoreRating::getRatingValue)
//                .sum();
//
//        return sum / ratings.size();
//    }
//
//    @Override
//    public int getStoreRatingCount(UUID storeId) {
//        if (storeId == null) {
//            logger.warn("Attempt to get store rating count with null store ID");
//            return 0;
//        }
//
//        return findStoreRatingsByStore(storeId).size();
//    }
//
//    /**
//     * Deletes a product rating by its ID
//     *
//     * @param ratingId The ID of the rating to delete
//     * @return true if the rating was deleted, false if it didn't exist
//     */
//    @Override
//    public boolean deleteProductRating(UUID ratingId) {
//        if (ratingId == null) {
//            logger.warn("Attempt to delete product rating with null ID");
//            return false;
//        }
//
//        logger.debug("Deleting product rating by ID: {}", ratingId);
//        ProductRating removed = productRatings.remove(ratingId);
//
//        if (removed != null) {
//            logger.info("Successfully deleted product rating: {}", ratingId);
//            return true;
//        } else {
//            logger.warn("Product rating not found for deletion: {}", ratingId);
//            return false;
//        }
//    }
//
//    /**
//     * Deletes a store rating by its ID
//     *
//     * @param ratingId The ID of the rating to delete
//     * @return true if the rating was deleted, false if it didn't exist
//     */
//    @Override
//    public boolean deleteStoreRating(UUID ratingId) {
//        if (ratingId == null) {
//            logger.warn("Attempt to delete store rating with null ID");
//            return false;
//        }
//
//        logger.debug("Deleting store rating by ID: {}", ratingId);
//        StoreRating removed = storeRatings.remove(ratingId);
//
//        if (removed != null) {
//            logger.info("Successfully deleted store rating: {}", ratingId);
//            return true;
//        } else {
//            logger.warn("Store rating not found for deletion: {}", ratingId);
//            return false;
//        }
//    }
//
//    // Product review methods
//    @Override
//    public ProductReview saveProductReview(ProductReview review) {
//        if (review == null) {
//            logger.error("Attempt to save null product review");
//            throw new IllegalArgumentException("Product review cannot be null");
//        }
//
//        logger.debug("Saving product review: {}", review.getReviewId());
//        productReviews.put(review.getReviewId(), review);
//        logger.info("Product review saved: {}", review.getReviewId());
//        return review;
//    }
//
//    @Override
//    public Optional<ProductReview> findProductReviewById(UUID reviewId) {
//        if (reviewId == null) {
//            logger.warn("Attempt to find product review with null ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding product review by ID: {}", reviewId);
//        return Optional.ofNullable(productReviews.get(reviewId));
//    }
//
//    @Override
//    public List<ProductReview> findProductReviewsByProduct(UUID productId) {
//        if (productId == null) {
//            logger.warn("Attempt to find product reviews with null product ID");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding product reviews by product: {}", productId);
//        return productReviews.values().stream()
//                .filter(review -> review.getProductId().equals(productId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProductReview> findProductReviewsByUser(String username) {
//        if (username == null || username.isEmpty()) {
//            logger.warn("Attempt to find product reviews with null/empty username");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding product reviews by user: {}", username);
//        return productReviews.values().stream()
//                .filter(review -> review.getUsername().equals(username))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProductReview> findProductReviewsByStore(UUID storeId) {
//        if (storeId == null) {
//            logger.warn("Attempt to find product reviews with null store ID");
//            return Collections.emptyList();
//        }
//
//        logger.debug("Finding product reviews by store: {}", storeId);
//        return productReviews.values().stream()
//                .filter(review -> review.getStoreId().equals(storeId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Optional<ProductReview> findProductReviewByUserAndProduct(String username, UUID productId) {
//        if (username == null || username.isEmpty() || productId == null) {
//            logger.warn("Attempt to find product review with null/empty username or null product ID");
//            return Optional.empty();
//        }
//
//        logger.debug("Finding product review by user: {} and product: {}", username, productId);
//        return productReviews.values().stream()
//                .filter(review -> review.getUsername().equals(username) && review.getProductId().equals(productId))
//                .findFirst();
//    }
//
//    @Override
//    public boolean deleteProductReview(UUID reviewId) {
//        if (reviewId == null) {
//            logger.warn("Attempt to delete product review with null ID");
//            return false;
//        }
//
//        logger.debug("Deleting product review by ID: {}", reviewId);
//        ProductReview removed = productReviews.remove(reviewId);
//
//        if (removed != null) {
//            logger.info("Successfully deleted product review: {}", reviewId);
//            return true;
//        } else {
//            logger.warn("Product review not found for deletion: {}", reviewId);
//            return false;
//        }
//    }
//
//    @Override
//    public int getProductReviewCount(UUID productId) {
//        if (productId == null) {
//            logger.warn("Attempt to get product review count with null product ID");
//            return 0;
//        }
//
//        return findProductReviewsByProduct(productId).size();
//    }
//
//    @Override
//    public void clear() {
//        productRatings.clear();
//        storeRatings.clear();
//        productReviews.clear();
//        logger.info("Rating repository cleared");
//    }
//
//    @Override
//    public List<Object> findAll() {
//        List<Object> allRatings = new ArrayList<>();
//        allRatings.addAll(productRatings.values());
//        allRatings.addAll(storeRatings.values());
//        return allRatings;
//    }
//
//
//}