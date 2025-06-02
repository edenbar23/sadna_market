package com.sadna_market.market.InfrastructureLayer.Adapters;


import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.ProductRatingJpaRepository;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.ProductReviewJpaRepository;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.StoreRatingJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class RatingJpaAdapter implements IRatingRepository {

    @Autowired
    private ProductRatingJpaRepository productRatingRepository;

    @Autowired
    private StoreRatingJpaRepository storeRatingRepository;

    @Autowired
    private ProductReviewJpaRepository productReviewRepository;

    // Product Rating Methods
    @Override
    public ProductRating saveProductRating(ProductRating rating) {
        return productRatingRepository.save(rating);
    }

    @Override
    public Optional<ProductRating> findProductRatingById(UUID ratingId) {
        return productRatingRepository.findById(ratingId);
    }

    @Override
    public Optional<ProductRating> findProductRatingByUserAndProduct(String username, UUID productId) {
        return productRatingRepository.findByUsernameAndProductId(username, productId);
    }

    @Override
    public List<ProductRating> findProductRatingsByProduct(UUID productId) {
        return productRatingRepository.findByProductId(productId);
    }

    @Override
    public List<ProductRating> findProductRatingsByUser(String username) {
        return productRatingRepository.findByUsername(username);
    }

    @Override
    public double getAverageProductRating(UUID productId) {
        Double average = productRatingRepository.getAverageRatingByProductId(productId);
        return average != null ? average : 0.0;
    }

    @Override
    public int getProductRatingCount(UUID productId) {
        return productRatingRepository.countByProductId(productId);
    }

    @Override
    public boolean deleteProductRating(UUID ratingId) {
        if (productRatingRepository.existsById(ratingId)) {
            productRatingRepository.deleteById(ratingId);
            return true;
        }
        return false;
    }

    // Store Rating Methods
    @Override
    public StoreRating saveStoreRating(StoreRating rating) {
        return storeRatingRepository.save(rating);
    }

    @Override
    public Optional<StoreRating> findStoreRatingById(UUID ratingId) {
        return storeRatingRepository.findById(ratingId);
    }

    @Override
    public Optional<StoreRating> findStoreRatingByUserAndStore(String username, UUID storeId) {
        return storeRatingRepository.findByUsernameAndStoreId(username, storeId);
    }

    @Override
    public List<StoreRating> findStoreRatingsByStore(UUID storeId) {
        return storeRatingRepository.findByStoreId(storeId);
    }

    @Override
    public List<StoreRating> findStoreRatingsByUser(String username) {
        return storeRatingRepository.findByUsername(username);
    }

    @Override
    public double getAverageStoreRating(UUID storeId) {
        Double average = storeRatingRepository.getAverageRatingByStoreId(storeId);
        return average != null ? average : 0.0;
    }

    @Override
    public int getStoreRatingCount(UUID storeId) {
        return storeRatingRepository.countByStoreId(storeId);
    }

    @Override
    public boolean deleteStoreRating(UUID ratingId) {
        if (storeRatingRepository.existsById(ratingId)) {
            storeRatingRepository.deleteById(ratingId);
            return true;
        }
        return false;
    }

    // Product Review Methods
    @Override
    public ProductReview saveProductReview(ProductReview review) {
        return productReviewRepository.save(review);
    }

    @Override
    public Optional<ProductReview> findProductReviewById(UUID reviewId) {
        return productReviewRepository.findById(reviewId);
    }

    @Override
    public List<ProductReview> findProductReviewsByProduct(UUID productId) {
        return productReviewRepository.findByProductId(productId);
    }

    @Override
    public List<ProductReview> findProductReviewsByUser(String username) {
        return productReviewRepository.findByUsername(username);
    }

    @Override
    public List<ProductReview> findProductReviewsByStore(UUID storeId) {
        return productReviewRepository.findByStoreId(storeId);
    }

    @Override
    public Optional<ProductReview> findProductReviewByUserAndProduct(String username, UUID productId) {
        return productReviewRepository.findByUsernameAndProductId(username, productId);
    }

    @Override
    public boolean deleteProductReview(UUID reviewId) {
        if (productReviewRepository.existsById(reviewId)) {
            productReviewRepository.deleteById(reviewId);
            return true;
        }
        return false;
    }

    @Override
    public int getProductReviewCount(UUID productId) {
        return productReviewRepository.countByProductId(productId);
    }

    @Override
    public void clear() {
        productRatingRepository.deleteAll();
        storeRatingRepository.deleteAll();
        productReviewRepository.deleteAll();
    }

    @Override
    public List<Object> findAll() {
        return Stream.of(
                        productRatingRepository.findAll().stream(),
                        storeRatingRepository.findAll().stream(),
                        productReviewRepository.findAll().stream()
                ).flatMap(stream -> stream)
                .collect(Collectors.toList());
    }
}