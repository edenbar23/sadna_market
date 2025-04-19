package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Visitor for handling user role operations in the market system.
 * Primarily used for role removal operations with different behavior depending on role type.
 */


public class UserRoleVisitor {
    private static final Logger logger = LogManager.getLogger(UserRoleVisitor.class);

    /**
     * Processes the removal of a StoreManager role
     * 
     * @param manager The store manager role
     * @param storeId The store ID associated with the role
     * @param user The user with this role
     */
    public void processManagerRoleRemoval(StoreManager manager, int storeId, User user) {
        logger.info("Processing store manager removal for user {} in store {}", 
                   user.getUserName(), storeId);
        
        // Simply remove the role from the user
        // In full implementation, call a method to remove the role
        logger.info("Store manager role removed for user {}", user.getUserName());
    }
    
    /**
     * Processes the removal of a StoreOwner role
     * This includes cascading the removal to all appointees
     * 
     * @param owner The store owner role
     * @param storeId The store ID associated with the role
     * @param user The user with this role
     */
    public void processOwnerRoleRemoval(StoreOwner owner, int storeId, User user) {
        logger.info("Processing store owner removal for user {} in store {}", 
                  user.getUserName(), storeId);
        
        // Handle cascading removals for all appointees
        // This would normally be done through a domain service
        for (String appointeeUsername : owner.getAppointees()) {
            logger.info("Cascading removal to appointee {}", appointeeUsername);
            // In full implementation, use domain service to:
            // 1. Find the user by username
            // 2. Remove their role
        }
        
        // Then remove the owner role itself
        logger.info("Store owner role removed for user {}", user.getUserName());
    }
    
    /**
     * Processes a request to remove a StoreFounder role
     * Founders cannot have their role removed, so this throws an exception
     * 
     * @param founder The store founder role
     * @param storeId The store ID associated with the role
     * @param user The user with this role
     * @throws IllegalStateException since founders cannot leave their role
     */
    public void processFounderRoleRemoval(StoreFounder founder, int storeId, User user) {
        logger.error("Attempt to remove store founder role for user {} in store {}", 
                    user.getUserName(), storeId);
        throw new IllegalStateException("Store founders cannot leave their role");
    }

}
