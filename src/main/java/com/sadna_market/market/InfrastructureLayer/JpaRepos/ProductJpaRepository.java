package com.sadna_market.market.InfrastructureLayer.JpaRepos;


import com.sadna_market.market.DomainLayer.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    List<Product> findByStoreId(UUID storeId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(String category);

    List<Product> findByPriceBetween(double minPrice, double maxPrice);

    List<Product> findByProductIdIn(Set<UUID> productIds);

    @Query("SELECT p FROM Product p WHERE p.ratingCount > 0 AND (p.ratingSum / p.ratingCount) BETWEEN :minRate AND :maxRate")
    List<Product> findByRateRange(@Param("minRate") double minRate, @Param("maxRate") double maxRate);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:minRate IS NULL OR (p.ratingCount > 0 AND (p.ratingSum / p.ratingCount) >= :minRate)) " +
            "AND (:maxRate IS NULL OR (p.ratingCount > 0 AND (p.ratingSum / p.ratingCount) <= :maxRate))")
    List<Product> findByStoreWithCriteria(@Param("storeId") UUID storeId,
                                          @Param("name") String name,
                                          @Param("category") String category,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice,
                                          @Param("minRate") Double minRate,
                                          @Param("maxRate") Double maxRate);

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:minRate IS NULL OR (p.ratingCount > 0 AND (p.ratingSum / p.ratingCount) >= :minRate)) " +
            "AND (:maxRate IS NULL OR (p.ratingCount > 0 AND (p.ratingSum / p.ratingCount) <= :maxRate))")
    List<Product> searchProducts(@Param("name") String name,
                                 @Param("category") String category,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice,
                                 @Param("minRate") Double minRate,
                                 @Param("maxRate") Double maxRate);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.ratingCount > 0 ORDER BY (p.ratingSum / p.ratingCount) DESC")
    List<Product> findTopRatedByStore(@Param("storeId") UUID storeId);

    @Query("SELECT p FROM Product p WHERE p.ratingCount > 0 ORDER BY (p.ratingSum / p.ratingCount) DESC")
    List<Product> findTopTenRatedProducts();
}