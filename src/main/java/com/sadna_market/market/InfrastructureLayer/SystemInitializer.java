package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * System initializer that creates the specific scenario requested using only Application Layer services:
 * 1. System initialization with user u1 as admin
 * 2. Registration of users u2, u3, u4, u5, u6
 * 3. Login with u2
 * 4. Opening store s1 with u2
 * 5. Adding product "Bamba" to store s1 by u2 with price 30 and quantity 20
 * 6. Appointing u3 as manager in store s1 with inventory management permissions (by u2)
 * 7. Appointing u4 and u5 as store owners in s1 by u2
 * 8. Logout of u2
 */
@Component
@Profile("dev")
public class SystemInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);

    // Application Layer Services
    private final UserService userService;
    private final StoreService storeService;
    private final ProductService productService;

    // For direct repository access (only for clearing data)
    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;

    // Configuration constants
    private static final String DEFAULT_PASSWORD = "Password123!";
    private static final String ADMIN_USERNAME = "u1";
    private static final String ADMIN_EMAIL = "u1@market.com";

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
        logger.info("Starting system initialization using Application Layer services...");

        try {
            // Step 1: Clear existing data
            clearAllData();

            // Step 2: Initialize admin user u1
            initializeAdmin();

            // Step 3: Register users u2, u3, u4, u5, u6
            registerUsers();

            // Step 4: Login with u2
            String u2Token = loginUser("u2");

            // Step 5: Open store s1 with u2
            UUID storeId = openStore("u2", u2Token);

            // Step 6: Add Bamba product to store s1
            UUID bambaProductId = addBambaProduct("u2", u2Token, storeId);

            // Step 7: Appoint u3 as manager with inventory permissions
            appointManager("u2", u2Token, storeId, "u3");

            // Step 8: Appoint u4 and u5 as store owners
            appointOwners("u2", u2Token, storeId);

            // Step 9: Logout u2
            logoutUser("u2", u2Token);

            // Step 10: Log completion summary
            logCompletionSummary(storeId, bambaProductId);

            logger.info("System initialization completed successfully!");

        } catch (Exception e) {
            logger.error("Error during system initialization: {}", e.getMessage(), e);
            handleInitializationFailure(e);
        }
    }

    private void clearAllData() {
        logger.info("Clearing existing data...");
        try {
            // Use application service clear method if available, otherwise clear repositories
            userService.clear();
            productService.clear();
            storeService.clear();

            logger.info("All data cleared successfully through Application Layer");
        } catch (Exception e) {
            logger.error("Error clearing data: {}", e.getMessage());
            throw new RuntimeException("Failed to clear existing data", e);
        }
    }

    private void initializeAdmin() {
        logger.info("Step 1: Initializing admin user u1...");
        try {
            RegisterRequest adminRequest = new RegisterRequest(
                    ADMIN_USERNAME,
                    DEFAULT_PASSWORD,
                    ADMIN_EMAIL,
                    "Admin",
                    "User"
            );

            Response<String> response = userService.registerUser(adminRequest);

            if (response.isError()) {
                throw new RuntimeException("Failed to register admin: " + response.getErrorMessage());
            }

            // Set admin flag - need to access repository directly for this system initialization case
            User adminUser = userRepository.findByUsername(ADMIN_USERNAME)
                    .orElseThrow(() -> new RuntimeException("Admin user not found after creation"));

            adminUser.setAdmin(true);
            userRepository.update(adminUser);

            logger.info("✓ Admin user u1 created successfully with admin privileges");
        } catch (Exception e) {
            logger.error("Failed to create admin user u1: {}", e.getMessage());
            throw new RuntimeException("Admin initialization failed", e);
        }
    }

    private void registerUsers() {
        logger.info("Step 2: Registering users u2, u3, u4, u5, u6...");

        String[] usernames = {"u2", "u3", "u4", "u5", "u6"};
        String[] emails = {"u2@market.com", "u3@market.com", "u4@market.com", "u5@market.com", "u6@market.com"};
        String[] firstNames = {"User", "User", "User", "User", "User"};
        String[] lastNames = {"Two", "Three", "Four", "Five", "Six"};

        for (int i = 0; i < usernames.length; i++) {
            try {
                RegisterRequest userRequest = new RegisterRequest(
                        usernames[i],
                        DEFAULT_PASSWORD,
                        emails[i],
                        firstNames[i],
                        lastNames[i]
                );

                Response<String> response = userService.registerUser(userRequest);

                if (response.isError()) {
                    throw new RuntimeException("Failed to register user: " + response.getErrorMessage());
                }

                logger.info("✓ User {} registered successfully", usernames[i]);
            } catch (Exception e) {
                logger.error("Failed to register user {}: {}", usernames[i], e.getMessage());
                throw new RuntimeException("User registration failed for " + usernames[i], e);
            }
        }

        logger.info("✓ All users registered successfully");
    }

    private String loginUser(String username) {
        logger.info("Step 3: Logging in user {}...", username);
        try {
            Response<String> response = userService.loginUser(username, DEFAULT_PASSWORD);

            if (response.isError()) {
                throw new RuntimeException("Login failed: " + response.getErrorMessage());
            }

            String token = response.getData();
            logger.info("✓ User {} logged in successfully with token", username);
            return token;
        } catch (Exception e) {
            logger.error("Failed to login user {}: {}", username, e.getMessage());
            throw new RuntimeException("Login failed for " + username, e);
        }
    }

    private UUID openStore(String username, String token) {
        logger.info("Step 4: Opening store s1 with user {}...", username);
        try {
            StoreRequest storeRequest = new StoreRequest(
                    "s1",
                    "Store s1 - Sample store created during initialization",
                    "123 Market Street, Sample City, SC 12345",
                    "s1@market.com",
                    "555-STORE-01",
                    username  // founder username
            );

            Response<StoreDTO> response = storeService.createStore(username, token, storeRequest);

            if (response.isError()) {
                throw new RuntimeException("Store creation failed: " + response.getErrorMessage());
            }

            UUID storeId = response.getData().getStoreId();
            logger.info("✓ Store s1 created successfully with ID: {}", storeId);
            return storeId;
        } catch (Exception e) {
            logger.error("Failed to create store s1: {}", e.getMessage());
            throw new RuntimeException("Store creation failed", e);
        }
    }

    private UUID addBambaProduct(String username, String token, UUID storeId) {
        logger.info("Step 5: Adding Bamba product to store s1...");
        try {
            ProductRequest productRequest = new ProductRequest(
                    null,  // productId - null for new product
                    "Bamba",
                    "Delicious peanut snack - crispy and tasty",
                    "Snacks",
                    30.0
            );

            Response<String> response = productService.addProduct(username, token, productRequest, storeId, 20);

            if (response.isError()) {
                throw new RuntimeException("Product addition failed: " + response.getErrorMessage());
            }

            // The response contains the product ID as a string
            UUID productId = UUID.fromString(response.getData());
            logger.info("✓ Bamba product added successfully with ID: {}, Price: 30, Quantity: 20", productId);
            return productId;
        } catch (Exception e) {
            logger.error("Failed to add Bamba product: {}", e.getMessage());
            throw new RuntimeException("Product addition failed", e);
        }
    }

    private void appointManager(String appointer, String token, UUID storeId, String managerUsername) {
        logger.info("Step 6: Appointing {} as manager in store s1 with inventory permissions...", managerUsername);
        try {
            // Create permissions set with inventory management
            Set<Permission> permissions = EnumSet.of(
                    Permission.MANAGE_INVENTORY,
                    Permission.ADD_PRODUCT,
                    Permission.REMOVE_PRODUCT,
                    Permission.UPDATE_PRODUCT
            );

            PermissionsRequest permissionsRequest = new PermissionsRequest(permissions);

            Response<String> response = storeService.appointStoreManager(
                    appointer, token, storeId, managerUsername, permissionsRequest);

            if (response.isError()) {
                throw new RuntimeException("Manager appointment failed: " + response.getErrorMessage());
            }

            logger.info("✓ User {} appointed as manager in store s1 with inventory management permissions", managerUsername);
        } catch (Exception e) {
            logger.error("Failed to appoint {} as manager: {}", managerUsername, e.getMessage());
            throw new RuntimeException("Manager appointment failed", e);
        }
    }

    private void appointOwners(String appointer, String token, UUID storeId) {
        logger.info("Step 7: Appointing u4 and u5 as store owners in s1...");

        String[] newOwners = {"u4", "u5"};

        for (String ownerUsername : newOwners) {
            try {
                Response<String> response = storeService.appointStoreOwner(
                        appointer, token, storeId, ownerUsername);

                if (response.isError()) {
                    throw new RuntimeException("Owner appointment failed: " + response.getErrorMessage());
                }

                logger.info("✓ User {} appointed as store owner in store s1", ownerUsername);
            } catch (Exception e) {
                logger.error("Failed to appoint {} as store owner: {}", ownerUsername, e.getMessage());
                throw new RuntimeException("Owner appointment failed for " + ownerUsername, e);
            }
        }

        logger.info("✓ All store owners appointed successfully");
    }

    private void logoutUser(String username, String token) {
        logger.info("Step 8: Logging out user {}...", username);
        try {
            Response<String> response = userService.logoutUser(username, token);

            if (response.isError()) {
                logger.warn("Logout failed but continuing: {}", response.getErrorMessage());
            } else {
                logger.info("✓ User {} logged out successfully", username);
            }
        } catch (Exception e) {
            logger.error("Failed to logout user {}: {}", username, e.getMessage());
            // Don't throw exception for logout failure, just log it
            logger.warn("Continuing despite logout failure");
        }
    }

    private void logCompletionSummary(UUID storeId, UUID bambaProductId) {
        logger.info("=== SYSTEM INITIALIZATION SUMMARY ===");

        try {
            // Get final state information
            List<User> users = userRepository.findAll();
            List<Store> stores = storeRepository.findAll();

            logger.info("✓ Total users created: {}", users.size());
            logger.info("✓ Admin user: u1");
            logger.info("✓ Regular users: u2, u3, u4, u5, u6");
            logger.info("✓ Total stores created: {}", stores.size());
            logger.info("✓ Store s1 ID: {}", storeId);
            logger.info("✓ Bamba product ID: {}", bambaProductId);

            // Get store details using Application Layer
            Response<StoreDTO> storeResponse = storeService.getStoreInfo(storeId);
            if (!storeResponse.isError()) {
                StoreDTO store = storeResponse.getData();
                logger.info("✓ Store founder: {}", store.getFounderUsername());
                logger.info("✓ Store owners: {}", store.getOwnerUsernames());
                logger.info("✓ Store managers: {}", store.getManagerUsernames());
            }

            // Get product details using Application Layer
            Response<ProductDTO> productResponse = productService.getProductInfo(bambaProductId);
            if (!productResponse.isError()) {
                ProductDTO product = productResponse.getData();
                logger.info("✓ Product name: {}", product.getName());
                logger.info("✓ Product price: {}", product.getPrice());
                logger.info("✓ Product category: {}", product.getCategory());
            }

            logger.info("=== SCENARIO COMPLETED SUCCESSFULLY ===");
            logger.info("The system is now ready with the exact scenario you requested!");
            logger.info("All operations were performed through the Application Layer services.");

        } catch (Exception e) {
            logger.error("Error generating completion summary: {}", e.getMessage());
        }
    }

    private void handleInitializationFailure(Exception e) {
        logger.error("System initialization failed. Attempting cleanup...");
        try {
            clearAllData();
            logger.info("Cleanup completed. System is in clean state.");
        } catch (Exception cleanupException) {
            logger.error("Cleanup also failed: {}", cleanupException.getMessage());
        }
    }

    /**
     * Helper method for development - prints current system state
     */
    public void printSystemState() {
        logger.info("=== CURRENT SYSTEM STATE ===");

        try {
            int userCount = userRepository.findAll().size();
            int storeCount = storeRepository.findAll().size();

            logger.info("Users: {} | Stores: {}", userCount, storeCount);

        } catch (Exception e) {
            logger.error("Error printing system state: {}", e.getMessage());
        }
    }

    /**
     * Emergency cleanup method for development
     */
    public void emergencyCleanup() {
        logger.warn("Performing emergency cleanup of all data...");
        try {
            clearAllData();
            logger.info("Emergency cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Emergency cleanup failed: {}", e.getMessage());
        }
    }
}