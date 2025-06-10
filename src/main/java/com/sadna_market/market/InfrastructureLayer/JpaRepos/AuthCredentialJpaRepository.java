package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.InfrastructureLayer.Authentication.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Simple JPA Repository for AuthCredential
 * Matches existing project pattern - basic interface only
 */
@Repository
public interface AuthCredentialJpaRepository extends JpaRepository<AuthCredential, String> {

    /**
     * Find credentials by username
     */
    Optional<AuthCredential> findByUsername(String username);

    /**
     * Check if user exists
     */
    boolean existsByUsername(String username);

    /**
     * Delete by username
     */
    void deleteByUsername(String username);
}