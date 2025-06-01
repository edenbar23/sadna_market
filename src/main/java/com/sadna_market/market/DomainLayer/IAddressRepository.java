package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAddressRepository {

    /**
     * Save an address
     */
    Address save(Address address);

    /**
     * Find address by ID
     */
    Optional<Address> findById(UUID addressId);

    /**
     * Find all addresses for a user
     */
    List<Address> findByUsername(String username);

    /**
     * Find user's default address
     */
    Optional<Address> findDefaultByUsername(String username);

    /**
     * Update an address
     */
    Address update(Address address);

    /**
     * Delete an address
     */
    boolean deleteById(UUID addressId);

    /**
     * Set an address as default (and unset others)
     */
    boolean setAsDefault(String username, UUID addressId);

    /**
     * Check if user owns the address
     */
    boolean isAddressOwnedByUser(String username, UUID addressId);

    /**
     * Clear all addresses (for testing)
     */
    void clear();
}