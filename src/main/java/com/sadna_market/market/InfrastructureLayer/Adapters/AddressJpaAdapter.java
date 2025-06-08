package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.Address;
import com.sadna_market.market.DomainLayer.IAddressRepository;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.AddressJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile({"dev", "prod", "default"})
public class AddressJpaAdapter implements IAddressRepository {
    private static final Logger logger = LoggerFactory.getLogger(AddressJpaAdapter.class);
    private final AddressJpaRepository jpaRepo;

    public AddressJpaAdapter(AddressJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public Address save(Address address) {
        // JPA save() does insert or update based on address.addressId being null or not.
        return jpaRepo.save(address);
    }

    @Override
    public Optional<Address> findById(UUID addressId) {
        return jpaRepo.findById(addressId);
    }

    @Override
    public List<Address> findByUsername(String username) {
        return jpaRepo.findByUsername(username);
    }

    @Override
    public Optional<Address> findDefaultByUsername(String username) {
        return jpaRepo.findByUsernameAndIsDefaultTrue(username);
    }

    @Override
    @Transactional
    public Address update(Address address) {
        // Because address.addressId is non-null, save() becomes an UPDATE in SQL.
        return jpaRepo.save(address);
    }

    @Override
    @Transactional
    public boolean setAsDefault(String username, UUID addressId) {
        // 1. Clear any existing default for that user
        List<Address> all = jpaRepo.findByUsername(username);
        boolean foundTarget = false;

        for (Address addr : all) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                jpaRepo.save(addr);
            }
            if (addr.getAddressId().equals(addressId)) {
                foundTarget = true;
            }
        }

        if (!foundTarget) {
            logger.warn("Address {} not found or not owned by user {}", addressId, username);
            return false;
        }

        // 2. Now fetch the target and set its isDefault = true
        Optional<Address> maybe = jpaRepo.findById(addressId);
        if (maybe.isEmpty()) {
            return false;
        }
        Address toDefault = maybe.get();
        toDefault.setDefault(true);
        jpaRepo.save(toDefault);
        return true;
    }

    @Override
    public boolean isAddressOwnedByUser(String username, UUID addressId) {
        // Fetch from JPA and compare the username field
        return jpaRepo.findById(addressId)
                .map(a -> username.equals(a.getUsername()))
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean deleteById(UUID addressId) {
        jpaRepo.deleteById(addressId);
        if( jpaRepo.existsById(addressId)) {
            logger.error("Failed to delete address with ID {}", addressId);
            return false;
        }
        logger.info("Address with ID {} deleted successfully", addressId);
        return true;
    }


    @Override
    @Transactional
    public void clear() {
        jpaRepo.deleteAll();
        logger.info("All addresses cleared from database");
    }
}
