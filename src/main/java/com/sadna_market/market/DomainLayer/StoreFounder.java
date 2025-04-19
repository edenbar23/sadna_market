package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoreFounder extends UserStoreRoles{

    public StoreFounder(String username, int storeId, String appointedBy) {
        super(username, storeId, appointedBy);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected void initializePermissions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initializePermissions'");
    }

    @Override
    public RoleType getRoleType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRoleType'");
    }

}
