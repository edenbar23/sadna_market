package com.sadna_market.market.InfrastructureLayer.JpaRepos;


import com.sadna_market.market.DomainLayer.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportJpaRepository extends JpaRepository<Report, UUID> {

    // Find reports by username (sender)
    List<Report> findByUsername(String username);

    // Find reports by store ID
    List<Report> findByStoreId(UUID storeId);

    // Find reports by product ID
    List<Report> findByProductId(UUID productId);

    // Find reports by both store and product
    List<Report> findByStoreIdAndProductId(UUID storeId, UUID productId);

    // Find reports ordered by creation date (newest first)
    List<Report> findAllByOrderByCreatedAtDesc();

    // Find reports by username ordered by date
    List<Report> findByUsernameOrderByCreatedAtDesc(String username);

    // Count reports by user
    int countByUsername(String username);

    // Count reports by store
    int countByStoreId(UUID storeId);

    // Count reports by product
    int countByProductId(UUID productId);
}