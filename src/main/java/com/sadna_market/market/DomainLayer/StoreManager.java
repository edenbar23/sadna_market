package com.sadna_market.market.DomainLayer;
import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class StoreManager extends UserStoreRoles{
    private static final Logger logger = LogManager.getLogger(StoreManager.class);

    /**
     * Constructor for the StoreManager class.
     * Initializes the store manager with a username, store ID, and appointed by user.
     *
     * @param username   The username of the store manager
     * @param storeId    The ID of the store managed
     * @param appointedBy The user who appointed this manager
     */
    public StoreManager(String username, int storeId, String appointedBy) {
        super(username, storeId, appointedBy);
        logger.info("StoreManager created for user: {}, store: {}, appointed by: {}", 
                   username, storeId, appointedBy);
    }

    @Override
    protected void initializePermissions() {
        /**
         * By default, the Store managers only have view permissions.
         * according to the requirements, specific permissions will be granted by the store owner.
         */

        addPermission(Permission.VIEW_STORE_INFO);
        addPermission(Permission.VIEW_PRODUCT_INFO);
    }

    @Override
    public RoleType getRoleType() {
        return RoleType.STORE_MANAGER;
    }

    /**
     * Adds specific permissions to this store manager
     * 
     * @param permissionsToAdd The permissions to add
     * @return true if permissions were modified
     */

    public boolean addPermissions (Set<Permission> permissionsToAdd) {
        if(permissionsToAdd == null || permissionsToAdd.isEmpty()) {
            logger.warn("No permissions to add");
            return false;
        }

        logger.info("Adding permissions for store manager: {}", username);
        boolean modified = false;

        for (Permission permission : permissionsToAdd){
            if(!hasPermission(permission)) {
                addPermission(permission);
                modified = true;
            }
        }

        if (modified) {
            logger.info("Added permissions for store manager: {}. Current Permissions: {}" ,
                        username, getPermissions());
        } else {
            logger.warn("No new permissions were added for store manager: {}", username);
        }

        return modified;
    }

    /**
     * Removes specific permissions from this store manager
     * 
     * @param permissionsToRemove The permissions to remove
     * @return true if permissions were modified
     */

     public boolean removePermissions(Set<Permission> permissionsToRemove) {
        if (permissionsToRemove == null || permissionsToRemove.isEmpty()) {
            return false;
        }
        
        logger.info("Removing permissions for store manager: {}", username);
        boolean modified = false;
        
        for (Permission permission : permissionsToRemove) {
            // Don't allow removing core view permissions
            if (permission != Permission.VIEW_STORE_INFO && 
                permission != Permission.VIEW_PRODUCT_INFO && 
                hasPermission(permission)) {
                removePermission(permission);
                modified = true;
            }
        }
        
        if (modified) {
            logger.info("Removed permissions for store manager: {}. Remaining permissions: {}", 
                        username, getPermissions());
        } else {
            logger.info("No permissions were removed for store manager: {}", username);
        }
        
        return modified;
    }


    /**
     * Sets the exact permissions for this manager (except for core view permissions)
     * This completely replaces existing permissions with the new set
     * 
     * @param newPermissions The new set of permissions
     */
    public void setPermissions(Set<Permission> newPermissions) {
        logger.info("Setting permissions for store manager: {}", username);
        
        // Clear existing permissions except core view permissions
        Set<Permission> toRemove = EnumSet.copyOf(permissions);
        toRemove.remove(Permission.VIEW_STORE_INFO);
        toRemove.remove(Permission.VIEW_PRODUCT_INFO);
        
        for (Permission permission : toRemove) {
            removePermission(permission);
        }
        
        // Add the new permissions
        if (newPermissions != null) {
            for (Permission permission : newPermissions) {
                if (permission != Permission.VIEW_STORE_INFO && 
                    permission != Permission.VIEW_PRODUCT_INFO) {
                    addPermission(permission);
                }
            }
        }
        
        logger.info("Set permissions for store manager: {}. New permissions: {}", 
                    username, getPermissions());
    }

    /**
     * Check if manager has permission to manage inventory
     */
    public boolean canManageInventory() {
        return hasPermission(Permission.MANAGE_INVENTORY);
    }
    
    /**
     * Check if manager has permission to manage purchase policy
     */
    public boolean canManagePurchasePolicy() {
        return hasPermission(Permission.MANAGE_PURCHASE_POLICY);
    }
    
    /**
     * Check if manager has permission to manage discount policy
     */
    public boolean canManageDiscountPolicy() {
        return hasPermission(Permission.MANAGE_DISCOUNT_POLICY);
    }
    
    /**
     * Check if manager has permission to respond to user inquiries
     */
    public boolean canRespondToInquiries() {
        return hasPermission(Permission.RESPOND_TO_USER_INQUIRIES);
    }
    
    /**
     * Check if manager has permission to view purchase history
     */
    public boolean canViewPurchaseHistory() {
        return hasPermission(Permission.VIEW_STORE_PURCHASE_HISTORY);
    }
    
    @Override
    public String toString() {
        return String.format("StoreManager[username=%s, storeId=%d, appointedBy=%s, permissions=%s]", 
                            getUsername(), getStoreId(), getAppointedBy(), getPermissions());
    }

    @Override
    public void processRoleRemoval(UserRoleVisitor visitor, User user) {
        logger.info("Processing role removal for StoreManager with username={} and storeId={}", 
                   username, storeId);
        visitor.processManagerRoleRemoval(this, storeId, user);
        logger.info("Role removal processing completed for StoreManager");
    }


}
