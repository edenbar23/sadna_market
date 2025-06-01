package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<Address, UUID> {
    /**
     * Find all addresses for a given username.
     */
    List<Address> findByUsername(String username);

    /**
     * Find the single address (if any) that is marked as default for this user.
     */
    Optional<Address> findByUsernameAndIsDefaultTrue(String username);
}
