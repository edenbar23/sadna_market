package com.sadna_market.market.DomainLayer;
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
        logger.info("StoreManager created");
    }

    @Override
    protected void initializePermissions() {

}

    @Override
    public RoleType getRoleType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRoleType'");
    }
}
