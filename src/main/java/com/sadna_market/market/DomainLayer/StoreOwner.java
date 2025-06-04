package com.sadna_market.market.DomainLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@DiscriminatorValue("STORE_OWNER")
@NoArgsConstructor
public class StoreOwner extends UserStoreRoles {
    private static final Logger logger = LogManager.getLogger(StoreOwner.class);

    /**
     * Constructor for the StoreOwner class.
     * Initializes the store owner with a username, store ID, and appointed by user.
     *
     * @param username   The username of the store owner
     * @param storeId    The ID of the store owned
     * @param appointedBy The user who appointed this owner
     */
    public StoreOwner(String username, UUID storeId, String appointedBy) {
        super(username, storeId, appointedBy);
        // Note: Removed duplicate field initialization since parent class handles everything
        logger.info("StoreOwner created for user: {}, store: {}, appointed by: {}",
                username, storeId, appointedBy);
    }

    @Override
    protected void initializePermissions() {
        super.addPermission(Permission.VIEW_STORE_INFO);
        super.addPermission(Permission.VIEW_PRODUCT_INFO);
        super.addPermission(Permission.MANAGE_DISCOUNT_POLICY);
        super.addPermission(Permission.MANAGE_PURCHASE_POLICY);
        super.addPermission(Permission.APPOINT_STORE_OWNER);
        super.addPermission(Permission.REMOVE_STORE_OWNER);
        super.addPermission(Permission.APPOINT_STORE_MANAGER);
        super.addPermission(Permission.REMOVE_STORE_MANAGER);
        super.addPermission(Permission.UPDATE_MANAGER_PERMISSIONS);
        super.addPermission(Permission.VIEW_STORE_PURCHASE_HISTORY);
        super.addPermission(Permission.MANAGE_INVENTORY);
        super.addPermission(Permission.ADD_PRODUCT);
        super.addPermission(Permission.REMOVE_PRODUCT);
        super.addPermission(Permission.UPDATE_PRODUCT);
        super.addPermission(Permission.RESPOND_TO_USER_INQUIRIES);

        logger.info("Initialized StoreOwner permissions: {}", getPermissions());
    }

    @Override
    public void addPermission(Permission permission) {
        logger.error("Exception in addPermission: store owner already has all the required permissions");
        throw new IllegalStateException("Store owner permissions are fixed and cannot be modified");
    }

    @Override
    public void removePermission(Permission permission) {
        logger.error("Exception in removePermission: can't remove permissions from a store owner");
        throw new IllegalStateException("Store owner permissions are fixed and cannot be modified");
    }

    /**
     * Override the parent's isAppointedByUser to use the correct field access
     * Note: Removed duplicate implementation since parent class handles this correctly
     */
    @Override
    public boolean isAppointedByUser(String username) {
        logger.info("Checking if user {} appointed this store owner", username);
        boolean result = super.isAppointedByUser(username);
        logger.info("isAppointedByUser result: {}", result);
        return result;
    }

    /**
     * Override to use parent's implementation (removed duplicate appointees field)
     */
    @Override
    public List<String> getAppointees() {
        logger.info("Getting appointees for store owner");
        List<String> result = super.getAppointees();
        logger.info("Appointees: {}", result);
        return result;
    }

    /**
     * Override to use parent's implementation (removed duplicate appointedBy field)
     */
    @Override
    public String getAppointedBy() {
        logger.info("Getting who appointed this store owner");
        String result = super.getAppointedBy();
        logger.info("Appointed by: {}", result);
        return result;
    }

    /**
     * Override to use parent's implementation (removed duplicate appointees field)
     */
    @Override
    public void addAppointee(String appointee) {
        logger.info("Adding appointee: {}", appointee);
        super.addAppointee(appointee);
        logger.info("Appointee added successfully");
    }

    @Override
    public RoleType getRoleType() {
        return RoleType.STORE_OWNER;
    }

    @Override
    public void processRoleRemoval(UserRoleVisitor visitor, User user) {
        logger.info("Processing role removal for StoreOwner with username={} and storeId={}",
                username, storeId);
        visitor.processOwnerRoleRemoval(this, storeId, user);
        logger.info("Role removal processing completed for StoreOwner");
    }

    @Override
    public String toString() {
        return String.format("StoreOwner[username=%s, storeId=%s, appointedBy=%s, permissions=%s]",
                getUsername(), getStoreId(), getAppointedBy(), getPermissions());
    }
}