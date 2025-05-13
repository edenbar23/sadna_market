package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRatingRepository {
    // Product rating methods
    ProductRating saveProductRating(ProductRating rating);
    Optional<ProductRating> findProductRatingById(UUID ratingId);
    Optional<ProductRating> findProductRatingByUserAndProduct(UUID userId, UUID productId);
    List<ProductRating> findProductRatingsByProduct(UUID productId);
    List<ProductRating> findProductRatingsByUser(UUID userId);
    double getAverageProductRating(UUID productId);
    int getProductRatingCount(UUID productId);

    // Store rating methods
    StoreRating saveStoreRating(StoreRating rating);
    Optional<StoreRating> findStoreRatingById(UUID ratingId);
    Optional<StoreRating> findStoreRatingByUserAndStore(UUID userId, UUID storeId);
    List<StoreRating> findStoreRatingsByStore(UUID storeId);
    List<StoreRating> findStoreRatingsByUser(UUID userId);
    double getAverageStoreRating(UUID storeId);
    int getStoreRatingCount(UUID storeId);


    void clear();
}