package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.Address;
import com.sadna_market.market.DomainLayer.IAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryAddressRepository implements IAddressRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryAddressRepository.class);

    private final Map<UUID, Address> addresses = new ConcurrentHashMap<>();

    public InMemoryAddressRepository() {
        logger.info("InMemoryAddressRepository initialized");
    }

    @Override
    public Address save(Address address) {
        if (address == null) {
            logger.error("Cannot save null address");
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (!address.isValidAddress()) {
            logger.error("Cannot save invalid address: {}", address);
            throw new IllegalArgumentException("Address is not valid");
        }

        logger.debug("Saving address: {}", address.getAddressId());
        addresses.put(address.getAddressId(), address);
        logger.info("Address saved successfully: {}", address.getAddressId());
        return address;
    }

    @Override
    public Optional<Address> findById(UUID addressId) {
        if (addressId == null) {
            logger.warn("Cannot find address with null ID");
            return Optional.empty();
        }

        logger.debug("Finding address by ID: {}", addressId);
        return Optional.ofNullable(addresses.get(addressId));
    }

    @Override
    public List<Address> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Cannot find addresses for null or empty username");
            return Collections.emptyList();
        }

        logger.debug("Finding addresses for user: {}", username);
        List<Address> userAddresses = addresses.values().stream()
                .filter(address -> username.equals(address.getUsername()))
                .sorted(Comparator.comparing((Address a) -> !a.isDefault())
                        .thenComparing(Address::getLabel))
                .collect(Collectors.toList());

        logger.debug("Found {} addresses for user: {}", userAddresses.size(), username);
        return userAddresses;
    }

    @Override
    public Optional<Address> findDefaultByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Cannot find default address for null or empty username");
            return Optional.empty();
        }

        logger.debug("Finding default address for user: {}", username);
        return addresses.values().stream()
                .filter(address -> username.equals(address.getUsername()) && address.isDefault())
                .findFirst();
    }

    @Override
    public Address update(Address address) {
        if (address == null) {
            logger.error("Cannot update null address");
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (!addresses.containsKey(address.getAddressId())) {
            logger.error("Cannot update non-existent address: {}", address.getAddressId());
            throw new IllegalArgumentException("Address does not exist");
        }

        if (!address.isValidAddress()) {
            logger.error("Cannot update to invalid address: {}", address);
            throw new IllegalArgumentException("Address is not valid");
        }

        logger.debug("Updating address: {}", address.getAddressId());
        addresses.put(address.getAddressId(), address);
        logger.info("Address updated successfully: {}", address.getAddressId());
        return address;
    }

    @Override
    public boolean deleteById(UUID addressId) {
        if (addressId == null) {
            logger.warn("Cannot delete address with null ID");
            return false;
        }

        logger.debug("Deleting address: {}", addressId);
        Address removed = addresses.remove(addressId);

        if (removed != null) {
            logger.info("Address deleted successfully: {}", addressId);
            return true;
        } else {
            logger.warn("Address not found for deletion: {}", addressId);
            return false;
        }
    }

    @Override
    public void deleteByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Cannot delete addresses for null or empty username");
            return;
        }

        logger.debug("Deleting all addresses for user: {}", username);
        List<UUID> toDelete = addresses.values().stream()
                .filter(address -> username.equals(address.getUsername()))
                .map(Address::getAddressId)
                .collect(Collectors.toList());

        int deletedCount = 0;
        for (UUID addressId : toDelete) {
            if (addresses.remove(addressId) != null) {
                deletedCount++;
            }
        }

        logger.info("Deleted {} addresses for user: {}", deletedCount, username);
    }

    @Override
    public boolean setAsDefault(String username, UUID addressId) {
        if (username == null || username.trim().isEmpty() || addressId == null) {
            logger.warn("Cannot set default address with null parameters");
            return false;
        }

        logger.debug("Setting address {} as default for user: {}", addressId, username);

        // First, unset all default addresses for this user
        addresses.values().stream()
                .filter(address -> username.equals(address.getUsername()) && address.isDefault())
                .forEach(address -> address.setDefault(false));

        // Then set the specified address as default
        Address targetAddress = addresses.get(addressId);
        if (targetAddress != null && username.equals(targetAddress.getUsername())) {
            targetAddress.setDefault(true);
            logger.info("Address {} set as default for user: {}", addressId, username);
            return true;
        } else {
            logger.warn("Cannot set default - address not found or not owned by user: {}", addressId);
            return false;
        }
    }

    @Override
    public boolean isAddressOwnedByUser(String username, UUID addressId) {
        if (username == null || username.trim().isEmpty() || addressId == null) {
            return false;
        }

        Address address = addresses.get(addressId);
        return address != null && username.equals(address.getUsername());
    }

    @Override
    public void clear() {
        addresses.clear();
        logger.info("Address repository cleared");
    }
}