package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.StoreRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRatingJpaRepository extends JpaRepository<StoreRating, UUID> {

    // Find rating by user and store (should be unique)
    Optional<StoreRating> findByUsernameAndStoreId(String username, UUID storeId);

    // Find all ratings for a specific store
    List<StoreRating> findByStoreId(UUID storeId);

    // Find all ratings by a specific user
    List<StoreRating> findByUsername(String username);

    // Get average rating for a store
    @Query("SELECT AVG(r.ratingValue) FROM StoreRating r WHERE r.storeId = :storeId")
    Double getAverageRatingByStoreId(@Param("storeId") UUID storeId);

    // Count ratings for a store
    int countByStoreId(UUID storeId);

    // Find ratings within a rating range for a store
    @Query("SELECT r FROM StoreRating r WHERE r.storeId = :storeId AND r.ratingValue BETWEEN :minRating AND :maxRating")
    List<StoreRating> findByStoreIdAndRatingValueBetween(@Param("storeId") UUID storeId,
                                                         @Param("minRating") int minRating,
                                                         @Param("maxRating") int maxRating);

    // Find ratings with comments
    @Query("SELECT r FROM StoreRating r WHERE r.storeId = :storeId AND r.comment IS NOT NULL AND r.comment != ''")
    List<StoreRating> findByStoreIdWithComments(@Param("storeId") UUID storeId);
}