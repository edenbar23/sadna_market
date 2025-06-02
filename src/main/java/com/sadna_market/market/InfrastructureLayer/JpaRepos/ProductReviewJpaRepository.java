package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductReviewJpaRepository extends JpaRepository<ProductReview, UUID> {

    // Find reviews by product
    List<ProductReview> findByProductId(UUID productId);

    // Find reviews by user
    List<ProductReview> findByUsername(String username);

    // Find reviews by store
    List<ProductReview> findByStoreId(UUID storeId);

    // Find review by user and product (should be unique)
    Optional<ProductReview> findByUsernameAndProductId(String username, UUID productId);

    // Count reviews for a product
    int countByProductId(UUID productId);

    // Find recent reviews for a product (ordered by timestamp)
    List<ProductReview> findByProductIdOrderByTimestampDesc(UUID productId);

    // Find edited reviews
    List<ProductReview> findByIsEditedTrue();

    // Find reviews for a product by a specific store
    List<ProductReview> findByProductIdAndStoreId(UUID productId, UUID storeId);

    // Get recent reviews across all products
    @Query("SELECT r FROM ProductReview r ORDER BY r.timestamp DESC")
    List<ProductReview> findRecentReviews();
}