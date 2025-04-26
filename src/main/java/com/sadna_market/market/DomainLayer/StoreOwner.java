package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;


public class StoreOwner extends UserStoreRoles {
    private static final Logger logger = LogManager.getLogger(StoreOwner.class);
    private static final long serialVersionUID = 1L;
    private String appointedBy;
    private int storeId;

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
        //owner can manage products in the store
        //owner can manage policies in the store
        //owner can appoint other users to owners
        //owner can remove other users he appointed as owners
        //owner that IS NOT the founder of the store can remove himself from this role
        //owner can appoint other users to managers
        //owner can choose permissions to his appointed managers
        //owner can remove other users he appointed as managers
        //owner can view all the roles in the store + managers permissions
        //owner can respond to requests/questions from the customers
        //owner can view all history orders
        addPermission(Permission.VIEW_STORE_INFO);
        addPermission(Permission.VIEW_PRODUCT_INFO);
        addPermission(Permission.MANAGE_DISCOUNT_POLICY);
        addPermission(Permission.MANAGE_PURCHASE_POLICY);
        addPermission(Permission.APPOINT_STORE_OWNER);
        addPermission(Permission.REMOVE_STORE_OWNER);
        addPermission(Permission.APPOINT_STORE_MANAGER);
        addPermission(Permission.REMOVE_STORE_MANAGER);
        addPermission(Permission.VIEW_STORE_PURCHASE_HISTORY);
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
    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
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
