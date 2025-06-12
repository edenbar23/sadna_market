// ===================================================================
// SystemStateManager.java - Fresh Clean Implementation
// ===================================================================

package com.sadna_market.market.InfrastructureLayer.Initialization;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.DomainLayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SystemStateManager {
    private static final Logger logger = LoggerFactory.getLogger(SystemStateManager.class);

    @Autowired
    private InitializationStateRepository stateRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IStoreRepository storeRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private final Map<String, String> sessionTokens = new ConcurrentHashMap<>();

    public enum InitializationMode {
        CHECK_ONLY,     // Just check current state
        SELECTIVE,      // Only initialize missing components
        FORCE_FULL,     // Force reinitialize everything
        INTERACTIVE,    // Ask user what to do
        RESET_AND_INIT  // Clear everything and start fresh
    }

    public enum ComponentStatus {
        PENDING,    // Not started yet
        RUNNING,    // Currently executing
        COMPLETED,  // Successfully finished
        FAILED,     // Failed with error
        SKIPPED     // Intentionally skipped
    }

    @Transactional
    public InitializationResult executeInitialization(SystemConfig config) {
        logger.info("=== Starting System Initialization ===");
        logger.info("Mode: {}", config.getMode());

        InitializationResult result = new InitializationResult();

        try {
            switch (config.getMode()) {
                case CHECK_ONLY:
                    return checkCurrentState(config);
                case RESET_AND_INIT:
                    resetSystemState();
                    return executeFullInitialization(config);
                case FORCE_FULL:
                    return executeFullInitialization(config);
                case SELECTIVE:
                    return executeSelectiveInitialization(config);
                default:
                    throw new IllegalArgumentException("Unsupported mode: " + config.getMode());
            }
        } catch (Exception e) {
            logger.error("System initialization failed: {}", e.getMessage(), e);
            result.setOverallStatus(ComponentStatus.FAILED);
            result.setErrorMessage(e.getMessage());

            if (config.isRollbackOnError()) {
                logger.info("Rolling back due to failure...");
                rollbackChanges(result);
            }

            return result;
        }
    }

    private InitializationResult checkCurrentState(SystemConfig config) {
        logger.info("=== Checking Current System State ===");

        InitializationResult result = new InitializationResult();

        for (ComponentConfig component : config.getComponents()) {
            ComponentResult componentResult = new ComponentResult(component.getId());

            try {
                ComponentStatus status = checkComponentStatus(component);
                componentResult.setStatus(status);

                Map<String, Object> details = gatherComponentDetails(component);
                componentResult.setDetails(details);

                logger.info("Component '{}': {}", component.getId(), status);

            } catch (Exception e) {
                componentResult.setStatus(ComponentStatus.FAILED);
                componentResult.setErrorMessage(e.getMessage());
                logger.error("Failed to check component '{}': {}", component.getId(), e.getMessage());
            }

            result.addComponentResult(componentResult);
        }

        result.setOverallStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private InitializationResult executeSelectiveInitialization(SystemConfig config) {
        logger.info("=== Executing Selective Initialization ===");

        InitializationResult result = new InitializationResult();
        List<ComponentConfig> componentsToInitialize = new ArrayList<>();

        for (ComponentConfig component : config.getComponents()) {
            if (!component.isEnabled()) {
                logger.info("Component '{}' is disabled, skipping", component.getId());
                ComponentResult componentResult = new ComponentResult(component.getId());
                componentResult.setStatus(ComponentStatus.SKIPPED);
                result.addComponentResult(componentResult);
                continue;
            }

            ComponentStatus currentStatus = checkComponentStatus(component);

            if (currentStatus == ComponentStatus.COMPLETED && !component.isForce()) {
                logger.info("Component '{}' already completed, skipping", component.getId());
                ComponentResult componentResult = new ComponentResult(component.getId());
                componentResult.setStatus(ComponentStatus.SKIPPED);
                result.addComponentResult(componentResult);
            } else {
                logger.info("Component '{}' needs initialization (current: {})", component.getId(), currentStatus);
                componentsToInitialize.add(component);
            }
        }

        return executeComponents(componentsToInitialize, config, result);
    }

    private InitializationResult executeFullInitialization(SystemConfig config) {
        logger.info("=== Executing Full Initialization ===");

        InitializationResult result = new InitializationResult();
        List<ComponentConfig> enabledComponents = config.getComponents().stream()
                .filter(ComponentConfig::isEnabled)
                .toList();

        return executeComponents(enabledComponents, config, result);
    }

    private InitializationResult executeComponents(List<ComponentConfig> components, SystemConfig config, InitializationResult result) {
        List<ComponentConfig> sortedComponents = sortByDependencies(components);

        for (ComponentConfig component : sortedComponents) {
            ComponentResult componentResult = executeComponent(component);
            result.addComponentResult(componentResult);

            if (componentResult.getStatus() == ComponentStatus.FAILED) {
                if (config.getOnFailure() == SystemConfig.FailureAction.STOP) {
                    logger.error("Component '{}' failed, stopping initialization", component.getId());
                    result.setOverallStatus(ComponentStatus.FAILED);
                    return result;
                } else if (config.getOnFailure() == SystemConfig.FailureAction.ROLLBACK) {
                    logger.error("Component '{}' failed, rolling back", component.getId());
                    rollbackChanges(result);
                    result.setOverallStatus(ComponentStatus.FAILED);
                    return result;
                }
                logger.warn("Component '{}' failed, but continuing with remaining components", component.getId());
            }
        }

        result.setOverallStatus(ComponentStatus.COMPLETED);
        logger.info("=== System Initialization Completed Successfully ===");

        return result;
    }

    private ComponentResult executeComponent(ComponentConfig component) {
        logger.info("Executing component: {}", component.getId());

        ComponentResult result = new ComponentResult(component.getId());
        result.setStatus(ComponentStatus.RUNNING);

        stateRepository.saveComponentState(component.getId(), ComponentStatus.RUNNING, null, null);

        try {
            switch (component.getId()) {
                case "admin_setup":
                    result = executeAdminSetup(component);
                    break;
                case "user_registration":
                    result = executeUserRegistration(component);
                    break;
                case "user_login":
                    result = executeUserLogin(component);
                    break;
                case "store_creation":
                    result = executeStoreCreation(component);
                    break;
                case "product_management":
                    result = executeProductManagement(component);
                    break;
                case "role_management":
                    result = executeRoleManagement(component);
                    break;
                case "user_logout":
                    result = executeUserLogout(component);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown component: " + component.getId());
            }

            stateRepository.saveComponentState(
                    component.getId(),
                    result.getStatus(),
                    result.getCreatedEntities(),
                    result.getErrorMessage()
            );

            logger.info("Component '{}' completed with status: {}", component.getId(), result.getStatus());

        } catch (Exception e) {
            logger.error("Component '{}' failed: {}", component.getId(), e.getMessage(), e);
            result.setStatus(ComponentStatus.FAILED);
            result.setErrorMessage(e.getMessage());

            stateRepository.saveComponentState(component.getId(), ComponentStatus.FAILED, null, e.getMessage());
        }

        return result;
    }

    // ===================================================================
    // Component Execution Methods
    // ===================================================================

    private ComponentResult executeAdminSetup(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());
        Map<String, Object> config = component.getConfig();

        String adminUsername = (String) config.get("username");
        String adminPassword = (String) config.get("password");
        String adminEmail = (String) config.get("email");
        String adminFirstName = (String) config.get("firstName");
        String adminLastName = (String) config.get("lastName");

        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isPresent() && existingAdmin.get().isAdmin()) {
            logger.info("Admin '{}' already exists and configured", adminUsername);
            result.setStatus(ComponentStatus.COMPLETED);
            return result;
        }

        RegisterRequest adminRequest = new RegisterRequest(
                adminUsername, adminPassword, adminEmail, adminFirstName, adminLastName
        );

        Response<String> response = userService.registerUser(adminRequest);
        if (response.isError()) {
            throw new RuntimeException("Failed to register admin: " + response.getErrorMessage());
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin user not found after creation"));
        admin.setAdmin(true);
        userRepository.update(admin);

        result.setStatus(ComponentStatus.COMPLETED);
        result.addCreatedEntity("user", adminUsername);
        result.addRollbackAction("delete_user", adminUsername);

        logger.info("✓ Admin '{}' created and configured", adminUsername);
        return result;
    }

    private ComponentResult executeUserRegistration(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) component.getConfig().get("users");

        for (Map<String, Object> userConfig : users) {
            String username = (String) userConfig.get("username");
            String password = (String) userConfig.get("password");
            String email = (String) userConfig.get("email");
            String firstName = (String) userConfig.get("firstName");
            String lastName = (String) userConfig.get("lastName");

            if (userRepository.findByUsername(username).isPresent()) {
                logger.info("User '{}' already exists, skipping", username);
                continue;
            }

            RegisterRequest request = new RegisterRequest(username, password, email, firstName, lastName);
            Response<String> response = userService.registerUser(request);

            if (response.isError()) {
                throw new RuntimeException("Failed to register user '" + username + "': " + response.getErrorMessage());
            }

            result.addCreatedEntity("user", username);
            result.addRollbackAction("delete_user", username);
            logger.info("✓ User '{}' registered successfully", username);
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private ComponentResult executeUserLogin(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> logins = (List<Map<String, Object>>) component.getConfig().get("logins");

        for (Map<String, Object> loginConfig : logins) {
            String username = (String) loginConfig.get("username");
            String password = (String) loginConfig.get("password");

            // Check if user is already logged in and handle it
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get().isLoggedIn()) {
                logger.info("User '{}' is already logged in, logging out first", username);

                String existingToken = sessionTokens.get(username);
                if (existingToken != null) {
                    try {
                        Response<String> logoutResponse = userService.logoutUser(username, existingToken);
                        if (logoutResponse.isError()) {
                            logger.warn("Failed to logout user '{}': {}", username, logoutResponse.getErrorMessage());
                        }
                    } catch (Exception e) {
                        logger.warn("Exception while logging out user '{}': {}", username, e.getMessage());
                    }
                    sessionTokens.remove(username);
                }

                User user = userOpt.get();
                if (user.isLoggedIn()) {
                    user.setIsLoggedIn(false);
                    userRepository.update(user);
                    logger.info("Force logged out user '{}' in database", username);
                }
            }

            Response<String> response = userService.loginUser(username, password);
            if (response.isError()) {
                throw new RuntimeException("Failed to login user '" + username + "': " + response.getErrorMessage());
            }

            String token = response.getData();
            sessionTokens.put(username, token);

            result.addCreatedEntity("session", username);
            result.addRollbackAction("logout_user", username);
            logger.info("✓ User '{}' logged in successfully", username);
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private ComponentResult executeStoreCreation(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stores = (List<Map<String, Object>>) component.getConfig().get("stores");

        for (Map<String, Object> storeConfig : stores) {
            String storeName = (String) storeConfig.get("name");
            String ownerUsername = (String) storeConfig.get("owner");
            String description = (String) storeConfig.get("description");
            String address = (String) storeConfig.get("address");
            String email = (String) storeConfig.get("email");
            String phone = (String) storeConfig.get("phone");

            // Check if store already exists first
            if (storeRepository.findByName(storeName).isPresent()) {
                logger.info("Store '{}' already exists, skipping creation", storeName);
                continue;
            }

            String token = sessionTokens.get(ownerUsername);
            if (token == null) {
                throw new RuntimeException("User '" + ownerUsername + "' is not logged in");
            }

            try {
                StoreRequest storeRequest = new StoreRequest(storeName, description, address, email, phone, ownerUsername);
                Response<StoreDTO> response = storeService.createStore(ownerUsername, token, storeRequest);

                if (response.isError()) {
                    // Handle "already exists" error gracefully
                    if (response.getErrorMessage().toLowerCase().contains("already exists") ||
                            response.getErrorMessage().toLowerCase().contains("duplicate")) {
                        logger.info("Store '{}' already exists (from service response), skipping", storeName);
                        continue;
                    } else {
                        throw new RuntimeException("Failed to create store '" + storeName + "': " + response.getErrorMessage());
                    }
                }

                result.addCreatedEntity("store", storeName);
                result.addRollbackAction("delete_store", storeName);
                logger.info("✓ Store '{}' created successfully", storeName);

            } catch (Exception e) {
                // Handle domain event duplication errors
                String errorMsg = e.getMessage().toLowerCase();
                if (errorMsg.contains("already exists") ||
                        errorMsg.contains("duplicate") ||
                        errorMsg.contains("storealreadyexists")) {
                    logger.info("Store '{}' already exists (caught exception), skipping", storeName);
                } else {
                    // Re-throw other exceptions
                    throw new RuntimeException("Failed to create store '" + storeName + "': " + e.getMessage(), e);
                }
            }
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private ComponentResult executeProductManagement(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) component.getConfig().get("products");

        for (Map<String, Object> productConfig : products) {
            String username = (String) productConfig.get("username");
            String storeName = (String) productConfig.get("store");
            String productName = (String) productConfig.get("name");
            String description = (String) productConfig.get("description");
            String category = (String) productConfig.get("category");
            Double price = ((Number) productConfig.get("price")).doubleValue();
            Integer quantity = ((Number) productConfig.get("quantity")).intValue();

            String token = sessionTokens.get(username);
            if (token == null) {
                throw new RuntimeException("User '" + username + "' is not logged in");
            }

            UUID storeId;
            try {
                storeId = storeService.findStoreIdByName(storeName);
            } catch (Exception e) {
                throw new RuntimeException("Could not find store with name '" + storeName + "': " + e.getMessage(), e);
            }

            ProductRequest productRequest = new ProductRequest(null, productName, category, description, price);
            Response<String> response = productService.addProduct(username, token, productRequest, storeId, quantity);

            if (response.isError()) {
                throw new RuntimeException("Failed to add product '" + productName + "': " + response.getErrorMessage());
            }

            result.addCreatedEntity("product", productName + "@" + storeName);
            result.addRollbackAction("delete_product", productName + "@" + storeName);
            logger.info("✓ Product '{}' added to store '{}'", productName, storeName);
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private ComponentResult executeRoleManagement(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> managers = (List<Map<String, Object>>) component.getConfig().getOrDefault("managers", new ArrayList<>());

        for (Map<String, Object> managerConfig : managers) {
            String appointer = (String) managerConfig.get("appointer");
            String storeName = (String) managerConfig.get("store");
            String managerUsername = (String) managerConfig.get("username");
            @SuppressWarnings("unchecked")
            List<String> permissionsList = (List<String>) managerConfig.get("permissions");

            String token = sessionTokens.get(appointer);
            if (token == null) {
                throw new RuntimeException("User '" + appointer + "' is not logged in");
            }

            UUID storeId;
            try {
                storeId = storeService.findStoreIdByName(storeName);
            } catch (Exception e) {
                throw new RuntimeException("Could not find store with name '" + storeName + "': " + e.getMessage(), e);
            }

            if (storeRepository.isManager(storeId, managerUsername)) {
                logger.info("User '{}' is already a manager in store '{}', skipping appointment", managerUsername, storeName);
                continue;
            }

            Set<Permission> permissions = permissionsList.stream()
                    .map(Permission::valueOf)
                    .collect(java.util.stream.Collectors.toSet());

            PermissionsRequest permissionsRequest = new PermissionsRequest(permissions);
            Response<String> response = storeService.appointStoreManager(
                    appointer, token, storeId, managerUsername, permissionsRequest
            );

            if (response.isError()) {
                throw new RuntimeException("Failed to appoint manager '" + managerUsername + "': " + response.getErrorMessage());
            }

            result.addCreatedEntity("manager_role", managerUsername + "@" + storeName);
            result.addRollbackAction("remove_manager", managerUsername + "@" + storeName);
            logger.info("✓ User '{}' appointed as manager in store '{}'", managerUsername, storeName);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> owners = (List<Map<String, Object>>) component.getConfig().getOrDefault("owners", new ArrayList<>());

        for (Map<String, Object> ownerConfig : owners) {
            String appointer = (String) ownerConfig.get("appointer");
            String storeName = (String) ownerConfig.get("store");
            String ownerUsername = (String) ownerConfig.get("username");

            String token = sessionTokens.get(appointer);
            if (token == null) {
                throw new RuntimeException("User '" + appointer + "' is not logged in");
            }

            UUID storeId;
            try {
                storeId = storeService.findStoreIdByName(storeName);
            } catch (Exception e) {
                throw new RuntimeException("Could not find store with name '" + storeName + "': " + e.getMessage(), e);
            }

            if (storeRepository.isOwner(storeId, ownerUsername)) {
                logger.info("User '{}' is already an owner in store '{}', skipping appointment", ownerUsername, storeName);
                continue;
            }

            Response<String> response = storeService.appointStoreOwner(appointer, token, storeId, ownerUsername);

            if (response.isError()) {
                throw new RuntimeException("Failed to appoint owner '" + ownerUsername + "': " + response.getErrorMessage());
            }

            result.addCreatedEntity("owner_role", ownerUsername + "@" + storeName);
            result.addRollbackAction("remove_owner", ownerUsername + "@" + storeName);
            logger.info("✓ User '{}' appointed as owner in store '{}'", ownerUsername, storeName);
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    private ComponentResult executeUserLogout(ComponentConfig component) {
        ComponentResult result = new ComponentResult(component.getId());

        @SuppressWarnings("unchecked")
        List<String> usernames = (List<String>) component.getConfig().get("users");

        for (String username : usernames) {
            String token = sessionTokens.get(username);
            if (token == null) {
                logger.warn("User '{}' is not logged in, skipping logout", username);
                continue;
            }

            Response<String> response = userService.logoutUser(username, token);
            if (response.isError()) {
                throw new RuntimeException("Failed to logout user '" + username + "': " + response.getErrorMessage());
            }

            sessionTokens.remove(username);
            logger.info("✓ User '{}' logged out successfully", username);
        }

        result.setStatus(ComponentStatus.COMPLETED);
        return result;
    }

    // ===================================================================
    // State Management Helper Methods
    // ===================================================================

    private ComponentStatus checkComponentStatus(ComponentConfig component) {
        switch (component.getId()) {
            case "admin_setup":
                Map<String, Object> adminConfig = component.getConfig();
                String adminUsername = (String) adminConfig.get("username");
                Optional<User> admin = userRepository.findByUsername(adminUsername);
                return admin.isPresent() && admin.get().isAdmin() ?
                        ComponentStatus.COMPLETED : ComponentStatus.PENDING;

            case "user_registration":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> users = (List<Map<String, Object>>) component.getConfig().get("users");
                boolean allUsersExist = users.stream()
                        .allMatch(userConfig -> userRepository.findByUsername((String) userConfig.get("username")).isPresent());
                return allUsersExist ? ComponentStatus.COMPLETED : ComponentStatus.PENDING;

            case "user_login":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> logins = (List<Map<String, Object>>) component.getConfig().get("logins");
                boolean allUsersLoggedIn = logins.stream().allMatch(loginConfig -> {
                    String username = (String) loginConfig.get("username");
                    Optional<User> user = userRepository.findByUsername(username);
                    return user.isPresent() && user.get().isLoggedIn();
                });

                // If users are logged in but we don't have tokens, populate them
                if (allUsersLoggedIn) {
                    populateSessionTokensForLoggedInUsers(logins);
                }

                return allUsersLoggedIn ? ComponentStatus.COMPLETED : ComponentStatus.PENDING;

            case "product_management":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> products = (List<Map<String, Object>>) component.getConfig().get("products");
                // For now, assume products don't exist (we could add product existence checking later)
                return ComponentStatus.PENDING;

            case "store_creation":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stores = (List<Map<String, Object>>) component.getConfig().get("stores");
                boolean allStoresExist = stores.stream()
                        .allMatch(storeConfig -> storeRepository.findByName((String) storeConfig.get("name")).isPresent());
                return allStoresExist ? ComponentStatus.COMPLETED : ComponentStatus.PENDING;

            case "role_management":
                Map<String, Object> roleConfig = component.getConfig();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> managers = (List<Map<String, Object>>) roleConfig.getOrDefault("managers", new ArrayList<>());
                boolean allManagersAppointed = managers.stream().allMatch(managerConfig -> {
                    try {
                        String storeName = (String) managerConfig.get("store");
                        String managerUsername = (String) managerConfig.get("username");
                        UUID storeId = storeService.findStoreIdByName(storeName);
                        return storeRepository.isManager(storeId, managerUsername);
                    } catch (Exception e) {
                        return false;
                    }
                });

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> owners = (List<Map<String, Object>>) roleConfig.getOrDefault("owners", new ArrayList<>());
                boolean allOwnersAppointed = owners.stream().allMatch(ownerConfig -> {
                    try {
                        String storeName = (String) ownerConfig.get("store");
                        String ownerUsername = (String) ownerConfig.get("username");
                        UUID storeId = storeService.findStoreIdByName(storeName);
                        return storeRepository.isOwner(storeId, ownerUsername);
                    } catch (Exception e) {
                        return false;
                    }
                });

                return allManagersAppointed && allOwnersAppointed ? ComponentStatus.COMPLETED : ComponentStatus.PENDING;

            default:
                return ComponentStatus.PENDING;
        }
    }

    private Map<String, Object> gatherComponentDetails(ComponentConfig component) {
        Map<String, Object> details = new HashMap<>();

        switch (component.getId()) {
            case "admin_setup":
                Map<String, Object> adminConfig = component.getConfig();
                String adminUsername = (String) adminConfig.get("username");
                Optional<User> admin = userRepository.findByUsername(adminUsername);
                details.put("admin_exists", admin.isPresent());
                details.put("is_admin", admin.map(User::isAdmin).orElse(false));
                break;

            case "user_registration":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> users = (List<Map<String, Object>>) component.getConfig().get("users");
                List<String> existingUsers = new ArrayList<>();
                List<String> missingUsers = new ArrayList<>();

                for (Map<String, Object> userConfig : users) {
                    String username = (String) userConfig.get("username");
                    if (userRepository.findByUsername(username).isPresent()) {
                        existingUsers.add(username);
                    } else {
                        missingUsers.add(username);
                    }
                }
                details.put("existing_users", existingUsers);
                details.put("missing_users", missingUsers);
                break;

            case "user_login":
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> loginUsers = (List<Map<String, Object>>) component.getConfig().get("logins");
                List<String> loggedInUsers = new ArrayList<>();
                List<String> notLoggedInUsers = new ArrayList<>();

                for (Map<String, Object> loginConfig : loginUsers) {
                    String username = (String) loginConfig.get("username");
                    Optional<User> user = userRepository.findByUsername(username);
                    if (user.isPresent() && user.get().isLoggedIn()) {
                        loggedInUsers.add(username);
                    } else {
                        notLoggedInUsers.add(username);
                    }
                }
                details.put("logged_in_users", loggedInUsers);
                details.put("not_logged_in_users", notLoggedInUsers);
                break;

            case "role_management":
                Map<String, Object> roleConfig = component.getConfig();
                List<String> existingManagers = new ArrayList<>();
                List<String> existingOwners = new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> managers = (List<Map<String, Object>>) roleConfig.getOrDefault("managers", new ArrayList<>());
                for (Map<String, Object> managerConfig : managers) {
                    try {
                        String storeName = (String) managerConfig.get("store");
                        String managerUsername = (String) managerConfig.get("username");
                        UUID storeId = storeService.findStoreIdByName(storeName);
                        if (storeRepository.isManager(storeId, managerUsername)) {
                            existingManagers.add(managerUsername + "@" + storeName);
                        }
                    } catch (Exception e) {
                        // Ignore errors during status checking
                    }
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> owners = (List<Map<String, Object>>) roleConfig.getOrDefault("owners", new ArrayList<>());
                for (Map<String, Object> ownerConfig : owners) {
                    try {
                        String storeName = (String) ownerConfig.get("store");
                        String ownerUsername = (String) ownerConfig.get("username");
                        UUID storeId = storeService.findStoreIdByName(storeName);
                        if (storeRepository.isOwner(storeId, ownerUsername)) {
                            existingOwners.add(ownerUsername + "@" + storeName);
                        }
                    } catch (Exception e) {
                        // Ignore errors during status checking
                    }
                }

                details.put("existing_managers", existingManagers);
                details.put("existing_owners", existingOwners);
                break;
        }

        return details;
    }

    private void resetSystemState() {
        logger.warn("=== RESETTING SYSTEM STATE - ALL DATA WILL BE LOST ===");

        try {
            // Clear session tokens
            sessionTokens.clear();

            // Use the database cleaner instead of repository.clear()
            databaseCleaner.clearAllTables();
            logger.info("✓ Database cleared");

            // Clear state tracking
            stateRepository.clearAll();
            logger.info("✓ State tracking cleared");

            logger.info("✓ System state reset completed");

        } catch (Exception e) {
            logger.error("Failed to reset system state: {}", e.getMessage(), e);
            throw new RuntimeException("Reset failed", e);
        }
    }

    private void rollbackChanges(InitializationResult result) {
        logger.info("=== Rolling Back Changes ===");

        List<ComponentResult> components = new ArrayList<>(result.getComponentResults());
        Collections.reverse(components);

        for (ComponentResult componentResult : components) {
            if (componentResult.getStatus() == ComponentStatus.COMPLETED) {
                try {
                    rollbackComponent(componentResult);
                    logger.info("✓ Rolled back component: {}", componentResult.getComponentId());
                } catch (Exception e) {
                    logger.error("Failed to rollback component '{}': {}",
                            componentResult.getComponentId(), e.getMessage());
                }
            }
        }

        sessionTokens.clear();
        logger.info("✓ Rollback completed");
    }

    private void rollbackComponent(ComponentResult componentResult) {
        Map<String, String> rollbackActions = componentResult.getRollbackData();
        if (rollbackActions == null) return;

        for (Map.Entry<String, String> entry : rollbackActions.entrySet()) {
            String action = entry.getKey();
            String target = entry.getValue();

            try {
                switch (action) {
                    case "delete_user":
                        userRepository.delete(target);
                        break;
                    case "delete_store":
                        storeRepository.findByName(target).ifPresent(store ->
                                storeRepository.deleteById(store.getStoreId()));
                        break;
                    case "logout_user":
                        sessionTokens.remove(target);
                        break;
                }
            } catch (Exception e) {
                logger.warn("Failed to execute rollback action '{}' for '{}': {}",
                        action, target, e.getMessage());
            }
        }
    }

    private List<ComponentConfig> sortByDependencies(List<ComponentConfig> components) {
        List<ComponentConfig> sorted = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        List<String> dependencyOrder = Arrays.asList(
                "admin_setup",
                "user_registration",
                "user_login",
                "store_creation",
                "product_management",
                "role_management",
                "user_logout"
        );

        for (String componentId : dependencyOrder) {
            components.stream()
                    .filter(c -> c.getId().equals(componentId))
                    .filter(c -> !processed.contains(c.getId()))
                    .forEach(c -> {
                        sorted.add(c);
                        processed.add(c.getId());
                    });
        }

        components.stream()
                .filter(c -> !processed.contains(c.getId()))
                .forEach(sorted::add);

        return sorted;
    }

    /**
     * Helper method to populate session tokens for users who are already logged in
     * This handles the case where the app restarts but users are still logged in the database
     */
    private void populateSessionTokensForLoggedInUsers(List<Map<String, Object>> logins) {
        for (Map<String, Object> loginConfig : logins) {
            String username = (String) loginConfig.get("username");
            String password = (String) loginConfig.get("password");

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get().isLoggedIn() && !sessionTokens.containsKey(username)) {
                try {
                    // Force logout and login to get a fresh token
                    User user = userOpt.get();
                    user.setIsLoggedIn(false);
                    userRepository.update(user);

                    Response<String> response = userService.loginUser(username, password);
                    if (!response.isError()) {
                        String token = response.getData();
                        sessionTokens.put(username, token);
                        logger.info("✓ Restored session token for user '{}'", username);
                    } else {
                        logger.warn("Failed to restore session for user '{}': {}", username, response.getErrorMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Exception while restoring session for user '{}': {}", username, e.getMessage());
                }
            }
        }
    }
}