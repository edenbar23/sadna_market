package com.sadna_market.market.DomainLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * StoreFounder can close the store
 * StoreFounder can open a store that he closed
 * StoreFounder has ALL permissions - they cannot be added or removed
 */
@Entity
@DiscriminatorValue("STORE_FOUNDER")
@NoArgsConstructor // Required by JPA
public class StoreFounder extends UserStoreRoles {
    private static final Logger logger = LogManager.getLogger(StoreFounder.class);

    public StoreFounder(String username, UUID storeId, String appointedBy) {
        super(username, storeId, appointedBy);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        // Store founders always have all permissions
        return true;
    }

    @Override
    public String toString() {
        return "store founder";
    }

    @Override
    protected void initializePermissions() {
        super.addPermission(Permission.CLOSE_STORE);
        super.addPermission(Permission.REOPEN_STORE);

    }

    @Override
    public RoleType getRoleType() {
        return RoleType.STORE_FOUNDER;
    }

    @Override
    public void processRoleRemoval(UserRoleVisitor visitor, User user) {
        logger.info("Processing role removal for StoreFounder with username={} and storeId={}",
                username, storeId);
        visitor.processFounderRoleRemoval(this, storeId, user);
        logger.info("Role removal processing completed for StoreFounder");
    }

    @Override
    public void addPermission(Permission permission) {
        logger.info("founder already has all permissions");
    }

    @Override
    public void removePermission(Permission permission) {
        logger.error("can't remove permission {} from store founder", permission);
        throw new IllegalStateException("Store founder permissions cannot be removed");
    }

    @Override
    public List<Permission> getPermissions() {
        return Arrays.asList(Permission.values());
    }
}