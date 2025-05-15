package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//StoreFounder can close the store
//StoreFounder can open a store that he closed
public class StoreFounder extends UserStoreRoles {
    //logger
    private static final Logger logger = LogManager.getLogger(StoreFounder.class);

    public StoreFounder(String username, UUID storeId, String appointedBy) {
        super(username, storeId, appointedBy);
        logger.info("Entering StoreFounder constructor with storeId={} and appointedBy={}", storeId, appointedBy);
        logger.info("Exiting StoreFounder constructor");

    }

    @Override
    public boolean hasPermission(Permission permission) {
        // Store founders always have all permissions
        logger.info("Checking permission {} for store founder: granted", permission);
        return true;
    }


    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = "store founder";
        logger.info("Exiting toString with result={}", result);
        return result;
    }

    @Override
    protected void initializePermissions() {
        logger.info("Entering initializePermissions");
        addPermission(Permission.CLOSE_STORE);
        addPermission(Permission.REOPEN_STORE);
        logger.info("Exiting initializePermissions");
    }

    @Override
    public RoleType getRoleType() {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("Unimplemented method 'getRoleType'");
        logger.info("Entering getRoleType");
        RoleType result = RoleType.STORE_FOUNDER;
        logger.info("Exiting getRoleType with result={}", result);
        return result;

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
        // Optional: we could allow adding permissions during initialization only
        // But for founders it's cleaner to just reject all modifications
        logger.warn("Attempted to add permission {} to store founder (unnecessary)", permission);
        // Don't throw an exception, just ignore - founders always have all permissions
    }

    @Override
    public void removePermission(Permission permission) {
        // Never allow removing permissions from a founder
        logger.error("can't remove permission {} from store founder", permission);
        throw new IllegalStateException("can't remove permissions from store founder");
    }

    public List<Permission> getPermissions() {
        // Return ALL possible permissions when asked
        logger.info("Returning all permissions for store founder");
        return Arrays.asList(Permission.values());
    }

}