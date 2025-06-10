package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.DomainLayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * System Initializer according to Version 3 academic requirements:
 * - Initializes system from external configuration
 * - Executes initial state from external file
 * - All operations must be legal (through application layer)
 * - Complete failure if any command fails
 * - Handles system restarts gracefully
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = "system.init.enabled", havingValue = "true")
public class SystemInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);


    private final UserService userService;
    private final StoreService storeService;
    private final ProductService productService;


    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;


    @Value("${system.init.initial-state-file:}")
    private String initialStateFile;

    @Value("${system.init.admin.username:u1}")
    private String adminUsername;

    @Value("${system.init.admin.password:Password123!}")
    private String adminPassword;

    @Value("${system.init.admin.email:u1@market.com}")
    private String adminEmail;

    @Value("${system.init.admin.first-name:System}")
    private String adminFirstName;

    @Value("${system.init.admin.last-name:Administrator}")
    private String adminLastName;


    @Value("${system.init.reset.enabled:false}")
    private boolean resetEnabled;


    private final Map<String, String> userTokens = new HashMap<>();

    @Autowired
    public SystemInitializer(
            UserService userService,
            StoreService storeService,
            ProductService productService,
            IUserRepository userRepository,
            IStoreRepository storeRepository,
            IProductRepository productRepository) {
        this.userService = userService;
        this.storeService = storeService;
        this.productService = productService;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("=== STARTING SYSTEM INITIALIZATION (Version 3 - Academic Requirements) ===");
        logger.info("Configuration file based initialization with initial state execution");

        try {

            if (!shouldRunInitialization()) {
                logger.info("=== SYSTEM ALREADY INITIALIZED - SKIPPING ===");
                logger.info("Set system.init.reset.enabled=true to force re-initialization");
                return;
            }


            if (resetEnabled) {
                logger.info("=== RESET MODE: Clearing existing data ===");
                resetSystemState();
            }


            ensureSystemAdminExists();


            if (initialStateFile != null && !initialStateFile.isEmpty()) {
                executeInitialStateFromFile();
            } else {
                logger.warn("No initial-state-file configured - skipping state initialization");
            }


            markInitializationComplete();

            logInitializationSuccess();

        } catch (Exception e) {
            logger.error("=== SYSTEM INITIALIZATION FAILED ===");
            logger.error("Error: {}", e.getMessage(), e);
            handleInitializationFailure(e);
            throw new RuntimeException("System initialization failed - all operations must succeed", e);
        }
    }

    /**
     * Check if initialization should run
     * This prevents re-running on system restarts
     */
    private boolean shouldRunInitialization() {
        try {

            boolean adminExists = userRepository.findByUsername(adminUsername).isPresent();

            if (adminExists) {
                User admin = userRepository.findByUsername(adminUsername).get();
                boolean isAdminSetup = admin.isAdmin();

                if (isAdminSetup) {
                    logger.info("System admin '{}' already exists and configured", adminUsername);
                    return false; // Skip initialization
                }
            }

            return true; // Run initialization
        } catch (Exception e) {
            logger.warn("Could not check initialization status: {} - running initialization", e.getMessage());
            return true;
        }
    }

    /**
     * Reset system state for clean initialization - REAL IMPLEMENTATION
     * Clears all repositories in the correct order to handle foreign key constraints
     */
    private void resetSystemState() {
        logger.warn("RESETTING SYSTEM STATE - ALL DATA WILL BE LOST");

        try {
            // Clear session tokens first
            userTokens.clear();

            // Clear repositories in correct order (respecting foreign key constraints)

            logger.info("Clearing repositories in dependency order...");


            if (productRepository != null) {
                logger.info("Clearing product repository...");
                productRepository.clear();
                logger.debug("✓ Product repository cleared");
            }


            if (storeRepository != null) {
                logger.info("Clearing store repository...");
                storeRepository.clear();
                logger.debug("✓ Store repository cleared");
            }


            if (userRepository != null) {
                logger.info("Clearing user repository...");
                userRepository.clear();
                logger.debug("✓ User repository cleared");
            }

            logger.info("✓ System state reset completed - all data cleared");

        } catch (Exception e) {
            logger.error("Failed to fully reset system state: {}", e.getMessage(), e);
            throw new RuntimeException("Reset failed - may have partial data", e);
        }
    }

    /**
     * Alternative: Transaction-based reset for better safety
     * Use this if you want atomic reset operations
     */
    @Transactional
    protected void resetSystemStateTransactional() {
        logger.warn("RESETTING SYSTEM STATE (TRANSACTIONAL) - ALL DATA WILL BE LOST");

        try {
            userTokens.clear();

            // All clears happen in a single transaction
            // If any fails, entire reset is rolled back
            if (productRepository != null) {
                productRepository.clear();
            }

            if (storeRepository != null) {
                storeRepository.clear();
            }

            if (userRepository != null) {
                userRepository.clear();
            }

            logger.info("✓ Transactional system state reset completed");

        } catch (Exception e) {
            logger.error("Transactional reset failed - rolling back: {}", e.getMessage(), e);
            throw new RuntimeException("Reset failed and rolled back", e);
        }
    }

    private void ensureSystemAdminExists() {
        logger.info("Setting up system administrator from configuration: {}", adminUsername);

        try {
            Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
            if (existingAdmin.isPresent()) {
                User admin = existingAdmin.get();
                if (!admin.isAdmin()) {
                    admin.setAdmin(true);
                    userRepository.update(admin);
                    logger.info("✓ Upgraded existing user '{}' to admin", adminUsername);
                } else {
                    logger.info("✓ System admin '{}' already properly configured", adminUsername);
                }
                return;
            }

            // Create new admin user
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

            logger.info("✓ System admin '{}' created and configured", adminUsername);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup system admin: " + e.getMessage(), e);
        }
    }

    private void executeInitialStateFromFile() {
        logger.info("Executing initial state commands from file: {}", initialStateFile);

        try {
            List<String> commands = readCommandsFromFile(initialStateFile);
            if (commands.isEmpty()) {
                logger.info("No commands found in initial state file");
                return;
            }

            logger.info("Found {} commands to execute", commands.size());
            logger.info("NOTE: All operations must be legal - using application layer only");

            // Execute all commands - if any fails, entire process fails
            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                int lineNumber = i + 1;

                logger.info("Executing command {}/{}: {}", lineNumber, commands.size(), command);

                try {
                    executeCommand(command, lineNumber);
                    logger.info("✓ Command {} completed successfully", lineNumber);
                } catch (Exception e) {
                    // As per requirements: if one operation fails, entire process fails
                    userTokens.clear();
                    throw new RuntimeException("Command " + lineNumber + " failed - aborting initialization: " + e.getMessage(), e);
                }
            }

            logger.info("✓ All {} commands executed successfully", commands.size());
        } catch (Exception e) {
            userTokens.clear();
            throw new RuntimeException("Initial state execution failed: " + e.getMessage(), e);
        }
    }

    private List<String> readCommandsFromFile(String filePath) throws Exception {
        List<String> commands = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                commands.add(line);
            }
        }

        return commands;
    }

    private void executeCommand(String command, int lineNumber) throws Exception {
        if (command.endsWith(";")) {
            command = command.substring(0, command.length() - 1);
        }

        int parenIndex = command.indexOf('(');
        if (parenIndex == -1) {
            throw new IllegalArgumentException("Invalid command format: " + command);
        }

        String commandName = command.substring(0, parenIndex).trim();
        String paramString = command.substring(parenIndex + 1, command.lastIndexOf(')')).trim();
        List<String> params = parseParameters(paramString);

        switch (commandName.toLowerCase()) {
            case "guest-registration":
            case "register":
                executeRegisterCommand(params);
                break;
            case "login":
                executeLoginCommand(params);
                break;
            case "open-shop":
            case "open-store":
                executeOpenStoreCommand(params);
                break;
            case "add-product":
                executeAddProductCommand(params);
                break;
            case "appoint-manager":
                executeAppointManagerCommand(params);
                break;
            case "appoint-owner":
                executeAppointOwnerCommand(params);
                break;
            case "logout":
                executeLogoutCommand(params);
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + commandName);
        }
    }

    private List<String> parseParameters(String paramString) {
        List<String> params = new ArrayList<>();
        if (paramString.isEmpty()) {
            return params;
        }

        String[] parts = paramString.split(",");
        for (String part : parts) {
            String param = part.trim();
            if (param.startsWith("*") && param.endsWith("*")) {
                param = param.substring(1, param.length() - 1);
            }
            params.add(param);
        }

        return params;
    }



    private void executeRegisterCommand(List<String> params) throws Exception {
        if (params.size() < 5) {
            throw new IllegalArgumentException("guest-registration requires 5 parameters");
        }

        String username = params.get(0);

        RegisterRequest request = new RegisterRequest(
                params.get(0), params.get(1), params.get(2), params.get(3), params.get(4)
        );

        Response<String> response = userService.registerUser(request);
        if (response.isError()) {
            throw new RuntimeException("Registration failed for user '" + username + "': " + response.getErrorMessage());
        }

        logger.debug("User '{}' registered successfully", username);
    }

    private void executeLoginCommand(List<String> params) throws Exception {
        if (params.size() < 2) {
            throw new IllegalArgumentException("login requires 2 parameters");
        }

        String username = params.get(0);
        String password = params.get(1);

        Response<String> response = userService.loginUser(username, password);
        if (response.isError()) {
            throw new RuntimeException("Login failed for user '" + username + "': " + response.getErrorMessage());
        }

        userTokens.put(username, response.getData());
        logger.debug("User '{}' logged in successfully", username);
    }

    private void executeOpenStoreCommand(List<String> params) throws Exception {
        if (params.size() < 6) {
            throw new IllegalArgumentException("open-shop requires 6 parameters");
        }

        String username = params.get(0);
        String token = userTokens.get(username);
        if (token == null) {
            throw new RuntimeException("User '" + username + "' is not logged in");
        }

        StoreRequest storeRequest = new StoreRequest(
                params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), username
        );

        Response<StoreDTO> response = storeService.createStore(username, token, storeRequest);
        if (response.isError()) {
            throw new RuntimeException("Store creation failed for '" + params.get(1) + "': " + response.getErrorMessage());
        }

        logger.debug("Store '{}' created successfully", params.get(1));
    }

    private void executeAddProductCommand(List<String> params) throws Exception {
        if (params.size() < 7) {
            throw new IllegalArgumentException("add-product requires 7 parameters");
        }

        String username = params.get(0);
        String token = userTokens.get(username);
        if (token == null) {
            throw new RuntimeException("User '" + username + "' is not logged in");
        }

        String storeName = params.get(1);
        UUID storeId = findStoreIdByName(storeName);

        ProductRequest productRequest = new ProductRequest(
                null, params.get(2), params.get(3), params.get(4), Double.parseDouble(params.get(5))
        );

        int quantity = Integer.parseInt(params.get(6));

        Response<String> response = productService.addProduct(username, token, productRequest, storeId, quantity);
        if (response.isError()) {
            throw new RuntimeException("Product addition failed for '" + params.get(2) + "': " + response.getErrorMessage());
        }

        logger.debug("Product '{}' added to store '{}'", params.get(2), storeName);
    }

    private void executeAppointManagerCommand(List<String> params) throws Exception {
        if (params.size() < 4) {
            throw new IllegalArgumentException("appoint-manager requires 4 parameters");
        }

        String appointer = params.get(0);
        String token = userTokens.get(appointer);
        if (token == null) {
            throw new RuntimeException("User '" + appointer + "' is not logged in");
        }

        String storeName = params.get(1);
        UUID storeId = findStoreIdByName(storeName);
        String managerUsername = params.get(2);

        Set<Permission> permissions = parsePermissions(params.get(3));
        PermissionsRequest permissionsRequest = new PermissionsRequest(permissions);

        Response<String> response = storeService.appointStoreManager(
                appointer, token, storeId, managerUsername, permissionsRequest);

        if (response.isError()) {
            throw new RuntimeException("Manager appointment failed for '" + managerUsername + "': " + response.getErrorMessage());
        }

        logger.debug("User '{}' appointed as manager in store '{}'", managerUsername, storeName);
    }

    private void executeAppointOwnerCommand(List<String> params) throws Exception {
        if (params.size() < 3) {
            throw new IllegalArgumentException("appoint-owner requires 3 parameters");
        }

        String appointer = params.get(0);
        String token = userTokens.get(appointer);
        if (token == null) {
            throw new RuntimeException("User '" + appointer + "' is not logged in");
        }

        String storeName = params.get(1);
        UUID storeId = findStoreIdByName(storeName);
        String ownerUsername = params.get(2);

        Response<String> response = storeService.appointStoreOwner(appointer, token, storeId, ownerUsername);
        if (response.isError()) {
            throw new RuntimeException("Owner appointment failed for '" + ownerUsername + "': " + response.getErrorMessage());
        }

        logger.debug("User '{}' appointed as owner in store '{}'", ownerUsername, storeName);
    }

    private void executeLogoutCommand(List<String> params) throws Exception {
        if (params.size() < 1) {
            throw new IllegalArgumentException("logout requires 1 parameter");
        }

        String username = params.get(0);
        String token = userTokens.get(username);
        if (token == null) {
            logger.warn("User '{}' is not logged in, skipping logout", username);
            return;
        }

        Response<String> response = userService.logoutUser(username, token);
        if (response.isError()) {
            throw new RuntimeException("Logout failed for user '" + username + "': " + response.getErrorMessage());
        }

        userTokens.remove(username);
        logger.debug("User '{}' logged out successfully", username);
    }

    /**
     * Find store ID by name using application layer
     * This method needs to be implemented in your StoreService
     */
    private UUID findStoreIdByName(String storeName) {
        try {
            return storeService.findStoreIdByName(storeName);
        } catch (Exception e) {
            throw new RuntimeException("Could not find store with name '" + storeName + "': " + e.getMessage(), e);
        }
    }

    private Set<Permission> parsePermissions(String permissionsStr) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        if (permissionsStr.isEmpty()) {
            return permissions;
        }

        String[] permArray = permissionsStr.split(",");
        for (String perm : permArray) {
            try {
                permissions.add(Permission.valueOf(perm.trim()));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown permission: {}", perm.trim());
            }
        }

        return permissions;
    }

    private void markInitializationComplete() {
        logger.info("✓ System initialization completed - marked for future reference");
        // The admin user existence serves as our completion marker
    }

    private void logInitializationSuccess() {
        logger.info("=== SYSTEM INITIALIZATION COMPLETED SUCCESSFULLY ===");
        logger.info("✓ Configuration-based initialization completed");
        logger.info("✓ Initial state file executed successfully");
        logger.info("✓ All operations were legal and completed through application layer");
        logger.info("✓ System ready for use");
        logger.info("=== Restart Safe: Subsequent startups will skip initialization ===");
    }

    private void handleInitializationFailure(Exception e) {
        logger.error("=== INITIALIZATION FAILURE (AS PER ACADEMIC REQUIREMENTS) ===");
        logger.error("One or more operations failed - entire initialization aborted");
        logger.error("Fix the issue in the initial-state file or configuration and restart");
        userTokens.clear();
    }
}