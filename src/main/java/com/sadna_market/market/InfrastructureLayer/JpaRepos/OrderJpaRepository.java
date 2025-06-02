package com.sadna_market.market.InfrastructureLayer.JpaRepos;
import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    // Basic finder methods to match your existing IOrderRepository
    List<Order> findByStoreId(UUID storeId);
    List<Order> findByUserName(String userName);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByUserNameOrderByOrderDateDesc(String userName);
    List<Order> findByStoreIdOrderByOrderDateDesc(UUID storeId);
    List<Order> findByUserNameAndStatus(String userName, OrderStatus status);

    // Simple product queries using @ElementCollection
    @Query("SELECT DISTINCT o FROM Order o JOIN o.products p WHERE KEY(p) = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") UUID productId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.products p WHERE KEY(p) = :productId AND o.userName = :username AND o.status = 'COMPLETED'")
    boolean hasUserPurchasedProduct(@Param("username") String username, @Param("productId") UUID productId);
}