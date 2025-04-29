package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


public class StoreManagementService {

    private static StoreManagementService instance;

    private static final Logger logger = LoggerFactory.getLogger(StoreManagementService.class);

    private final IStoreRepository storeRepository;
    private final IUserRepository userRepository;

    private StoreManagementService(IStoreRepository storeRepository, IUserRepository userRepository) {
        this.storeRepository = storeRepository.getInstance();
        this.userRepository = userRepository.getInstance();
    }

    public static StoreManagementService getInstance(IStoreRepository storeRepository, IUserRepository userRepository) {
        if (instance == null) {
            instance = new StoreManagementService(storeRepository, userRepository);
        }
        return instance;
    }

    /**
     * Creates a new store with the given details.
     * Business rules:
     * 1.The store name must be unique.
     * 2.Founder must be a registered user.
     * 3.Store name and email and phone must be valid
     */

    public Store createStore (String founderUserName, String storeName, String description,
                              String address, String email, String phone){
        logger.debug("Creating store '{}' for founder '{}'", storeName, founderUserName);

        User founder = userRepository.findByUsername(founderUserName)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + founderUserName));

        if(storeRepository.findByName(storeName).isPresent()){
            logger.error("Store name '{}' already exists", storeName);
            throw new StoreAlreadyExistsException("Store name already exists: " + storeName);
        }

        validateStoreInfo(storeName, email);

        UUID storeId = storeRepository.createStore(founderUserName, storeName, address, email, phone);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("Store creation failed"));

        StoreFounder storeFounder = new StoreFounder(founderUserName, storeId, null);
        founder.addStoreRole(storeFounder);
        userRepository.update(founder);

        logger.info("Store '{}' has been created", storeName);
        return store;

    }

    /**
     * Closes a store.
     * Business rules:
     * 1.Only the store founder can close the store.
     * 2.The store must be open.
     */

    public void closeStore(String founderUserName, UUID storeId) {
        logger.debug("User '{}' attempting to close store '{}'", storeId, founderUserName);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isFounder(founderUserName)) {
            logger.error("User '{}' is not the founder of store '{}'", founderUserName, storeId);
            throw new InsufficientPermissionsException("User is not the founder of the store: " + storeId);
        }

        if (!store.isActive()) {
            logger.error("Store '{}' is already closed", store.getName());
            throw new StoreAlreadyClosedException("Store is already closed");
        }

        store.closeStore();
        storeRepository.save(store);
        logger.info("Store '{}' has been closed by '{}'", store.getName(), founderUserName);
    }

    /**
     * Reopens a closed store.
     * Business rules:
     * 1.Only the store founder can reopen the store.
     * 2.The store must be closed.
     */

    public void reopenStore(String founderUserName, UUID storeId) {
        logger.debug("User'{}' attempting to reopen store '{}'", founderUserName, storeId);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isFounder(founderUserName)) {
            logger.error("User '{}' is not the founder of store '{}'", founderUserName, storeId);
            throw new InsufficientPermissionsException("User is not the founder of the store: " + storeId);
        }

        if (store.isActive()) {
            logger.error("Store '{}' is already open", store.getName());
            throw new StoreAlreadyOpenException("Store is already open");
        }

        store.reopenStore();
        storeRepository.save(store);
        logger.info("Store '{}' has been reopened by '{}'", store.getName(), founderUserName);

    }

    /**
     * Appoints a new store owner.
     * Business rules:
     * - Appointer must be a store owner
     * - New owner must be a registered user
     * - Store must be active
     */

    public void appointStoreOwner(String appointerUsername, UUID storeId, String newOwnerUsername) {
        logger.debug("User '{}' attempting to appoint new owner '{}' for store '{}'", appointerUsername, newOwnerUsername, storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        User appointer = userRepository.findByUsername(appointerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + appointerUsername));

        User newOwner = userRepository.findByUsername(newOwnerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + newOwnerUsername));

        if(!store.isStoreOwner(appointerUsername)){
            logger.error("User '{}' is not a store owner", appointerUsername);
            throw new InsufficientPermissionsException("Only store owners can appoint new owners");
        }



        StoreOwner newOwnerRole = new StoreOwner(newOwnerUsername, storeId, appointerUsername);
        newOwner.addStoreRole(newOwnerRole);

        store.addStoreOwner(newOwnerUsername);
        storeRepository.save(store);
        userRepository.update(newOwner);

        logger.info("User '{}' has been appointed as new owner of store '{}'", newOwnerUsername, store.getName());
    }

    /**
     * Removes a store owner.
     * Business rules:
     * - Remover must be the one who appointed the owner
     * - Cannot remove the founder
     * - Store must be active
     * - Cascading removal of all appointees
     */

    public void removeStoreOwner(String removerUsername, UUID storeId, String ownerToRemoveUsername) {
        logger.debug("User '{}' attempting to remove owner '{}' from store '{}'", removerUsername, ownerToRemoveUsername, storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot remove owners from inactive store");
        }

        if (store.isFounder(ownerToRemoveUsername)) {
            logger.error("Cannot remove the founder of the store");
            throw new IllegalStateException("Cannot remove the founder of the store");
        }

        if (!store.isStoreOwner(removerUsername)) {
            logger.error("User '{}' is not a store owner", removerUsername);
            throw new InsufficientPermissionsException("Only store owners can remove other owners");
        }


        User ownerUser = userRepository.findByUsername(ownerToRemoveUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + ownerToRemoveUsername));

        UserStoreRoles ownerRole = findUserStoreRole(ownerUser, storeId, RoleType.STORE_OWNER);

        if (ownerRole == null) {
            logger.error("User '{}' is not a store owner", ownerToRemoveUsername);
            throw new UserNotFoundException("User is not an owner if this store");
        }

        cascadeRemoveAppointees(ownerUser, storeId);
        ownerUser.removeStoreRole(storeId, RoleType.STORE_OWNER);
        store.removeStoreOwner(ownerToRemoveUsername);

        userRepository.update(ownerUser);
        storeRepository.save(store);

        logger.info("User '{}' has been removed as owner of store '{}'", ownerToRemoveUsername, store.getName());

    }

    /**
     * Appoints a store manager with specific permissions.
     * Business rules:
     * - Appointer must be a store owner
     * - New manager must be a registered user
     * - Store must be active
     */

    public void appointStoreManager(String appointerUsername, UUID storeId, String newManagerUsername, Set<Permission> permissions) {
        logger.debug("User '{}' attempting to appoint new manager '{}' for store '{}'", appointerUsername, newManagerUsername, storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            logger.error("Store '{}' is not active", store.getName());
            throw new StoreNotActiveException("Store is not active");
        }


        User appointer = userRepository.findByUsername(appointerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + appointerUsername));

        User newManager = userRepository.findByUsername(newManagerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + newManagerUsername));

        if (!store.isStoreOwner(appointerUsername)) {
            logger.error("User '{}' is not a store owner", appointerUsername);
            throw new InsufficientPermissionsException("Only store owners can appoint managers");
        }

        StoreManager newManagerRole = new StoreManager(newManagerUsername, storeId, appointerUsername);
        if (permissions != null && !permissions.isEmpty()) {
            newManagerRole.addPermissions(permissions);
        }

        newManager.addStoreRole(newManagerRole);
        store.addStoreManager(newManagerUsername);
        storeRepository.save(store);
        userRepository.update(newManager);

        logger.info("User '{}' has been appointed as manager of store '{}'", newManagerUsername, store.getName());

    }

    /**
     * Removes a store manager.
     * Business rules:
     * - Remover must be a store owner
     * - Store must be active
     * - Cannot remove the founder
     */

    public void removeStoreManager(String removerUsername, UUID storeId, String managerToRemoveUsername) {
        logger.debug("User '{}' attempting to remove manager '{}' from store '{}'", removerUsername, managerToRemoveUsername, storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            logger.error("Store '{}' is not active", store.getName());
            throw new StoreNotActiveException("Store is not active");
        }

        if (store.isFounder(managerToRemoveUsername)) {
            logger.error("Cannot remove the founder of the store");
            throw new IllegalStateException("Cannot remove the founder of the store");
        }

        if (!store.isStoreOwner(removerUsername)) {
            logger.error("User '{}' is not a store owner", removerUsername);
            throw new InsufficientPermissionsException("Only store owners can remove managers");
        }

        User managerUser = userRepository.findByUsername(managerToRemoveUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + managerToRemoveUsername));

        UserStoreRoles managerRole = findUserStoreRole(managerUser, storeId, RoleType.STORE_MANAGER);

        if (managerRole == null) {
            logger.error("User '{}' is not a store manager", managerToRemoveUsername);
            throw new UserNotFoundException("User is not a manager of this store");
        }

        managerUser.removeStoreRole(storeId, RoleType.STORE_MANAGER);
        store.removeStoreManager(managerToRemoveUsername);

        userRepository.update(managerUser);
        storeRepository.save(store);

        logger.info("User '{}' has been removed as manager of store '{}'", managerToRemoveUsername, store.getName());
    }

    /**
     * Updates manager permissions.
     * Business rules:
     * - Updater must be the one who appointed the manager
     * - Store must be active
     */
    public void updateManagerPermissions(String updaterUsername, UUID storeId, String managerUsername,
                                         Set<Permission> newPermissions) {
        logger.debug("User '{}' updating permissions for manager '{}' in store '{}'",
                updaterUsername, managerUsername, storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isActive()) {
            throw new StoreNotActiveException("Cannot update permissions in inactive store");
        }

        // Business rule: Updater must be store owner
        if (!store.isStoreOwner(updaterUsername)) {
            throw new InsufficientPermissionsException("Only store owners can update manager permissions");
        }

        User managerUser = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new UserNotFoundException("Manager not found: " + managerUsername));

        // Find the manager role
        UserStoreRoles role = findUserStoreRole(managerUser, storeId, RoleType.STORE_MANAGER);
        if (role == null) {
            throw new UserNotManagerException("User is not a manager of this store");
        }

        StoreManager managerRole = (StoreManager) role;
        managerRole.setPermissions(newPermissions);
        userRepository.update(managerUser);

        logger.info("User '{}' successfully updated permissions for manager '{}' in store '{}'",
                updaterUsername, managerUsername, storeId);
    }

    private void validateStoreInfo(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidStoreDataException("Store name cannot be empty");
        }

        if (email != null && !email.contains("@")) {
            throw new InvalidStoreDataException("Invalid email format");
        }
    }

    private UserStoreRoles findUserStoreRole(User user, UUID storeId, RoleType roleType) {
        return user.getUserStoreRoles().stream()
                .filter(role -> role.getStoreId().equals(storeId) && role.getRoleType() == roleType)
                .findFirst()
                .orElse(null);
    }

    private void cascadeRemoveAppointees(User user, UUID storeId) {
        // Find all roles for this store
        List<UserStoreRoles> roles = new ArrayList<>(user.getUserStoreRoles());

        for (UserStoreRoles role : roles) {
            if (role.getStoreId().equals(storeId)) {
                // For each appointee of this user
                for (String appointeeUsername : role.getAppointees()) {
                    User appointee = userRepository.findByUsername(appointeeUsername).orElse(null);
                    if (appointee != null) {
                        // Remove their role
                        UserStoreRoles appointeeRole = findUserStoreRole(appointee, storeId, role.getRoleType());
                        if (appointeeRole != null) {
                            // Recursively remove their appointees first
                            cascadeRemoveAppointees(appointee, storeId);

                            // Remove the role
                            appointee.removeStoreRole(storeId, appointeeRole.getRoleType());
                            userRepository.update(appointee);
                        }
                    }
                }
            }
        }
    }
}