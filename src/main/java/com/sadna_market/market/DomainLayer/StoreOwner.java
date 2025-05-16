package com.sadna_market.market.DomainLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StoreOwner extends UserStoreRoles {
    private static final Logger logger = LogManager.getLogger(StoreOwner.class);
    private String appointedBy;
    private UUID storeId;

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
        this.storeId = storeId;
        this.appointedBy = appointedBy;
        this.appointees = new ArrayList<>();
    }

    @Override
    protected void initializePermissions() {
        // Call the parent's addPermission directly to bypass our overridden version
        // This avoids the exception during initialization
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
    }

    @Override
    public void addPermission(Permission permission) {
        logger.error("Exception in addPermission: store owner already has all the permissions");
        throw new IllegalStateException("Store owner has all the permissions");
    }

    public boolean isAppointedByUser(String username) {
        logger.info("Entering isAppointedByUser with username={}", username);
        boolean result = appointees.contains(username);
        logger.info("Exiting isAppointedByUser with result={}", result);
        return result;
    }

    @Override
    public List<String> getAppointees() {
        logger.info("Entering getAppointers");
        logger.info("Exiting getAppointers with result={}", appointees);
        return appointees;
    }

    @Override
    public String getAppointedBy() {
        logger.info("Entering getAppointedBy");
        logger.info("Exiting getApointee with result={}", appointedBy);
        return appointedBy;
    }

    @Override
    public void removePermission(Permission permission) {
        logger.error("Exception in removePermission: can't remove permissions from a store owner");
        throw new IllegalStateException("Store owner has all the permissions");
    }

    @Override
    public void addAppointee(String appointee) {
        logger.info("Entering addAppointers with apointee={}", appointee);
        appointees.add(appointee);
        logger.info("Exiting addAppointers");
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
}