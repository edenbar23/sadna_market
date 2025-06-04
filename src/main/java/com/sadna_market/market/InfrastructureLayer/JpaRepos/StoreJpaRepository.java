package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface StoreJpaRepository extends JpaRepository<Store, UUID> {

    // Basic finder methods
    Optional<Store> findByName(String name);

    List<Store> findByActive(boolean active);

    Optional<Store> findByFounderUsername(String founderUsername);

    // Personnel management queries
    @Query("SELECT s FROM Store s JOIN s.ownerUsernames ou WHERE ou = :username")
    List<Store> findStoresByOwnerUsername(@Param("username") String username);

    @Query("SELECT s FROM Store s JOIN s.managerUsernames mu WHERE mu = :username")
    List<Store> findStoresByManagerUsername(@Param("username") String username);

    // Product-related queries using the productQuantities map
    @Query("SELECT s FROM Store s JOIN s.productQuantities pq WHERE KEY(pq) = :productId")
    List<Store> findStoresByProductId(@Param("productId") UUID productId);

    @Query("SELECT s FROM Store s JOIN s.productQuantities pq WHERE KEY(pq) = :productId AND VALUE(pq) >= :minQuantity")
    List<Store> findStoresByProductIdWithMinStock(@Param("productId") UUID productId, @Param("minQuantity") int minQuantity);

    // Order-related queries
    @Query("SELECT s FROM Store s JOIN s.orderIds oi WHERE oi = :orderId")
    Optional<Store> findByOrderId(@Param("orderId") UUID orderId);

    // Rating queries
    List<Store> findByActiveOrderByRatingDesc(boolean active);

    @Query("SELECT s FROM Store s WHERE s.active = true AND s.ratingCount > 0 ORDER BY s.rating DESC")
    List<Store> findTopRatedActiveStores();

    // Search queries
    @Query("SELECT s FROM Store s WHERE s.active = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Store> findActiveStoresByNameContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Store s WHERE s.active = true AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Store> findActiveStoresByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    // Statistics queries
    @Query("SELECT COUNT(s) FROM Store s WHERE s.active = true")
    long countActiveStores();

    @Query("SELECT COUNT(s) FROM Store s WHERE s.active = false")
    long countInactiveStores();

    // Custom validation queries
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s JOIN s.ownerUsernames ou WHERE s.storeId = :storeId AND ou = :username")
    boolean isUserOwnerOfStore(@Param("storeId") UUID storeId, @Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s JOIN s.managerUsernames mu WHERE s.storeId = :storeId AND mu = :username")
    boolean isUserManagerOfStore(@Param("storeId") UUID storeId, @Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s JOIN s.productQuantities pq WHERE s.storeId = :storeId AND KEY(pq) = :productId")
    boolean storeHasProduct(@Param("storeId") UUID storeId, @Param("productId") UUID productId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s JOIN s.productQuantities pq WHERE s.storeId = :storeId AND KEY(pq) = :productId AND VALUE(pq) >= :quantity")
    boolean storeHasProductInStock(@Param("storeId") UUID storeId, @Param("productId") UUID productId, @Param("quantity") int quantity);
}