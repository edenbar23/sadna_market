package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRatingJpaRepository extends JpaRepository<ProductRating, UUID> {

    // Find rating by user and product (should be unique)
    Optional<ProductRating> findByUsernameAndProductId(String username, UUID productId);

    // Find all ratings for a specific product
    List<ProductRating> findByProductId(UUID productId);

    // Find all ratings by a specific user
    List<ProductRating> findByUsername(String username);

    // Get average rating for a product
    @Query("SELECT AVG(r.ratingValue) FROM ProductRating r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);

    // Count ratings for a product
    int countByProductId(UUID productId);

    // Find ratings within a rating range for a product
    @Query("SELECT r FROM ProductRating r WHERE r.productId = :productId AND r.ratingValue BETWEEN :minRating AND :maxRating")
    List<ProductRating> findByProductIdAndRatingValueBetween(@Param("productId") UUID productId,
                                                             @Param("minRating") int minRating,
                                                             @Param("maxRating") int maxRating);
}