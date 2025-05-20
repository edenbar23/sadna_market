package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class StoreManagementService {
    private static final Logger logger = LoggerFactory.getLogger(StoreManagementService.class);

    private final IStoreRepository storeRepository;
    private final IUserRepository userRepository;
    private final IMessageRepository messageRepository;

    @Autowired
    public StoreManagementService(IStoreRepository storeRepository,
                                  IUserRepository userRepository,
                                  IMessageRepository messageRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;

        logger.info("StoreManagementService initialized");
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Subscribe to store-related events
        DomainEventPublisher.subscribe(StoreCreatedEvent.class, this::handleStoreCreated);
        DomainEventPublisher.subscribe(StoreClosedEvent.class, this::handleStoreClosed);
        DomainEventPublisher.subscribe(StoreReopenedEvent.class, this::handleStoreReopened);

        logger.info("StoreManagementService subscribed to events");
    }

    /**
     * Event handler for StoreCreatedEvent
     */
    private void handleStoreCreated(StoreCreatedEvent event) {
        logger.info("Handling store creation event for store: {}", event.getStoreName());

        try {
            createStore(
                    event.getFounderUsername(),
                    event.getStoreName(),
                    event.getDescription(),
                    event.getAddress(),
                    event.getEmail(),
                    event.getPhone()
            );

            logger.info("Store created successfully through event handler");
        } catch (Exception e) {
            logger.error("Error handling store creation event: {}", e.getMessage(), e);
        }
    }

    /**
     * Event handler for StoreClosedEvent
     */
    private void handleStoreClosed(StoreClosedEvent event) {
        logger.info("Handling store closed event for store: {}", event.getStoreId());

        try {
            closeStore(event.getUsername(), event.getStoreId());
            logger.info("Store closed successfully through event handler");
        } catch (Exception e) {
            logger.error("Error handling store closed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Event handler for StoreReopenedEvent
     */
    private void handleStoreReopened(StoreReopenedEvent event) {
        logger.info("Handling store reopened event for store: {}", event.getStoreId());

        try {
            reopenStore(event.getUsername(), event.getStoreId());
            logger.info("Store reopened successfully through event handler");
        } catch (Exception e) {
            logger.error("Error handling store reopened event: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a new store with the given details.
     * Business rules:
     * 1.The store name must be unique.
     * 2.Founder must be a registered user.
     * 3.Store name and email and phone must be valid
     */
    public Store createStore(String founderUserName, String storeName, String description,
                             String address, String email, String phone) {
        logger.debug("Creating store '{}' for founder '{}'", storeName, founderUserName);

        User founder = userRepository.findByUsername(founderUserName)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + founderUserName));

        if(storeRepository.findByName(storeName).isPresent()){
            logger.error("Store name '{}' already exists", storeName);
            throw new StoreAlreadyExistsException("Store name already exists: " + storeName);
        }

        validateStoreInfo(storeName, email);

        // Create the store through the repository
        UUID storeId = storeRepository.createStore(founderUserName, storeName, address, email, phone);

        // Get the created store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("Store creation failed"));

        // The StoreFounder is already created in the repository layer
        // We just need to add the role to the user
        StoreFounder storeFounder = new StoreFounder(founderUserName, storeId, null);
        founder.addStoreRole(storeFounder);
        userRepository.update(founder);

        logger.info("Store '{}' has been created", storeName);
        logger.info("Store id is '{}'", storeId);
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
        if(!appointer.hasPermission(storeId,Permission.APPOINT_STORE_OWNER)) {
            throw new IllegalArgumentException("User {} has no permit to appoint store owner!");
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

    public void leaveOwnership(String username, UUID storeId) {
        logger.debug("User '{}' attempting to leave ownership of store '{}'", username, storeId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (store.isFounder(username)) {
            logger.error("User '{}' is  the founder of store, so he cannot give up his ownership '{}'", username, storeId);
            throw new InsufficientPermissionsException("User is not the founder of the store: " + storeId);
        }

        if (!store.isStoreOwner(username)){
            logger.error("User '{}' is not a store manager", username);
            throw new InsufficientPermissionsException("User is not a store manager of the store: " + storeId);
        }

        if (store.getOwnerUsernames().size() <= 1) {
            logger.error("Cannot leave ownership as this is the only owner");
            throw new IllegalStateException("Cannot leave ownership as this is the only owner");
        }
        cascadeRemoveAppointees(user, storeId);
        store.removeStoreOwner(username);
        user.removeStoreRole(storeId, RoleType.STORE_FOUNDER);
        storeRepository.save(store);

        logger.info("User '{}' has left ownership of store '{}'", username, store.getName());
    }

    public List<Message> getStoreMessages(String username, UUID storeId) {
        logger.debug("User '{}' requesting messages for store '{}'", username, storeId);

        // Verify store exists and user has permission
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            logger.error("User '{}' is not authorized to view messages for store '{}'", username, storeId);
            throw new InsufficientPermissionsException("Only store owners and managers can view store messages");
        }

        // Publish an event that message service could listen to (for auditing or other purposes)
        DomainEventPublisher.publish(new StoreMessagesRequestedEvent(username, storeId));

        // Directly use the repository to get the messages
        List<Message> messages = messageRepository.findByStore(storeId);
        logger.info("User '{}' retrieved {} messages for store '{}'", username, messages.size(), store.getName());

        return messages;
    }

    public Set<Permission> getStoreManagerPermissions(String username, UUID storeId) {
        logger.debug("Getting permissions for manager '{}' in store '{}'", username, storeId);

        // Validate store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

        // Validate user exists
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // Get user's role for this store
        UserStoreRoles role = findUserStoreRole(user, storeId, RoleType.STORE_MANAGER);

        // Validate user is a manager
        if (role == null) {
            logger.error("User '{}' is not a manager of store '{}'", username, storeId);
            throw new UserNotManagerException("User is not a manager of this store");
        }

        // Cast to StoreManager and get permissions
        StoreManager managerRole = (StoreManager) role;
        Set<Permission> permissions = new HashSet<>(managerRole.getPermissions());

        logger.info("Retrieved {} permissions for manager '{}' in store '{}'",
                permissions.size(), username, storeId);

        return permissions;
    }

    public boolean getStoreStatus(UUID storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new RuntimeException("Store not found"));
        return store.isActive();
    }
}