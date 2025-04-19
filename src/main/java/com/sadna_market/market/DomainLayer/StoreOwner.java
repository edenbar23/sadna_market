package com.sadna_market.market.DomainLayer;
public class StoreOwner extends UserStoreRoles {
    /**
     * Constructor for the StoreOwner class.
     * Initializes the store owner with a username, store ID, and appointed by user.
     *
     * @param username   The username of the store owner
     * @param storeId    The ID of the store owned
     * @param appointedBy The user who appointed this owner
     */
    public StoreOwner(String username, int storeId, String appointedBy) {
        super(username, storeId, appointedBy);
    }

    @Override
    protected void initializePermissions() {
        // TODO Auto-generated method stub

    }

    @Override
    public RoleType getRoleType() {
        return RoleType.STORE_OWNER;
    }

}
