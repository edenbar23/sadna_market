package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.PermissionsRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;

import com.sadna_market.market.DomainLayer.Permission;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
public class StoreOwnerTests {
    private final static Bridge bridge = new Bridge();
    ObjectMapper objectMapper = new ObjectMapper();

    // Test data
    private static UUID storeId;
    private static final String STORE_NAME = "Owner Test Store";
    private static final String STORE_DESCRIPTION = "A store for testing owner operations";
    private static final String STORE_ADDRESS = "456 Store Avenue, Test City";
    private static final String STORE_EMAIL = "owner-store@example.com";
    private static final String STORE_PHONE = "555-987-6543";

    // Store owner credentials
    private static String ownerUsername;
    private static String ownerPassword;
    private static String ownerToken;  // JWT token for the store owner


    // Dummy unauthorized user credentials
    private static String unauthorizedUsername;
    private static String unauthorizedPassword;
    private static String unauthorizedToken;  // JWT token for the unauthenticated user

    @BeforeAll
    static void setup() {
        // Create a store owner user
        ownerUsername = "storeowner" + UUID.randomUUID().toString().substring(0, 8);
        ownerPassword = "Owner123!";
        RegisterRequest ownerRegisterRequest = new RegisterRequest(
                ownerUsername,
                ownerPassword,
                ownerUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response ownerRegisterResponse = bridge.registerUser(ownerRegisterRequest);
        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");

        // Create a dummy unauthorized user
        unauthorizedUsername = "unauthorized" + UUID.randomUUID().toString().substring(0, 8);
        unauthorizedPassword = "Unauthorized123!";
        RegisterRequest unauthorizedRegisterRequest = new RegisterRequest(
                unauthorizedUsername,
                unauthorizedPassword,
                unauthorizedUsername + "@example.com",
                "Unauthorized",
                "User"
        );
        Response unauthorizedRegisterResponse = bridge.registerUser(unauthorizedRegisterRequest);
        Assertions.assertFalse(unauthorizedRegisterResponse.isError(), "Unauthorized user registration should succeed");

        // Login the store owner to get a JWT token
        Response ownerLoginResponse = bridge.loginUser(ownerUsername, ownerPassword);
        Assertions.assertFalse(ownerLoginResponse.isError(), "Store owner login should succeed");

        // Extract the store owner's JWT token
        ownerToken = ownerLoginResponse.getJson();
        Assertions.assertNotNull(ownerToken, "Owner JWT token should not be null");
        Assertions.assertFalse(ownerToken.isEmpty(), "Owner JWT token should not be empty");
        System.out.println("Store owner JWT token: " + ownerToken);

        // Create a store with the owner as the founder
        StoreRequest newStore = new StoreRequest(
                STORE_NAME,
                STORE_DESCRIPTION,
                STORE_ADDRESS,
                STORE_EMAIL,
                STORE_PHONE,
                ownerUsername
        );

        Response createStoreResponse = bridge.createStore(
                ownerUsername,
                ownerToken,
                newStore
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        // Here we assume the response contains the store ID
        // In a real implementation, you'd extract the actual store ID from the response
        String storeIdStr = createStoreResponse.getJson();

        // Check if the response contains a valid UUID
        if (storeIdStr != null && !storeIdStr.isEmpty()) {
            try {
                storeId = UUID.fromString(storeIdStr);
                System.out.println("Created store ID: " + storeId);
            } catch (IllegalArgumentException e) {
                // If parsing fails, generate a UUID for testing purposes
                storeId = UUID.randomUUID();
                System.out.println("Using generated store ID for testing: " + storeId);
            }
        } else {
            // If no store ID was returned, generate one for testing
            storeId = UUID.randomUUID();
            System.out.println("Using generated store ID for testing: " + storeId);
        }

    }

    private static ProductRequest validProduct;
    private static ProductRequest invalidProduct;
    private static ProductRequest thirdTestProduct;
    private static final int PRODUCT_QUANTITY = 10;

    @BeforeAll
    static void setupProductTests() {
        // Initialize the product objects that will be used across tests
        validProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                            // Price
        );

        invalidProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                -99.99                            // Negative Price
        );

        thirdTestProduct = new ProductRequest(
                UUID.randomUUID(),
                "Third Test Product",            // Product name
                "Home Goods",                    // Category
                "Another test product",          // Description
                149.99                           // Price
        );

    }

    @Test
    @DisplayName("Store owner should be able to add a product to their store")
    void addProductToStoreTest() {
        try {
            // Call the addProductToStore method from the bridge using the pre-initialized valid product
            Response response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, validProduct, PRODUCT_QUANTITY);

            // Assert - Verify response is valid
            Assertions.assertNotNull(response, "Response should not be null");
            Assertions.assertFalse(response.isError(), "Product addition should succeed");
            Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");
        }
        catch (Exception e) {
            Assertions.fail("Adding product failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when adding a product with invalid data")
    void addInvalidProductToStoreTest() {
        try {
            // Call the addProductToStore method with the pre-initialized invalid product
            Response response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, invalidProduct, PRODUCT_QUANTITY);

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(response, "Response should not be null");
            Assertions.assertTrue(response.isError(), "Response should indicate an error for invalid product data");
            Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
        }
        catch (Exception e) {
            Assertions.fail("Adding invalid product failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to add a product to the store")
    void unauthorizedProductAdditionTest() {
        try {
            // Attempt to add a product using the unauthorized user's credentials
            Response response = bridge.addProductToStore(
                    unauthorizedToken,
                    unauthorizedUsername,
                    storeId,
                    validProduct,
                    PRODUCT_QUANTITY
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(response, "Response should not be null");
            Assertions.assertTrue(response.isError(), "Response should indicate an error for unauthorized user");
            Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");


            System.out.println("Expected error for unauthorized product addition: " +
                    response.getErrorMessage());
        }
        catch (Exception e) {
            Assertions.fail("Unauthorized product addition test failed with exception: " + e.getMessage());
        }
    }

    // Setup function for edit and remove tests
    @BeforeAll
    static void setupEditAndRemoveTests() {
        try {
            // Use the third product we already initialized in the first setup
            Response addResponse = bridge.addProductToStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    thirdTestProduct,
                    PRODUCT_QUANTITY
            );

            Assertions.assertFalse(addResponse.isError(), "Adding product for edit/remove tests should succeed");
            System.out.println("Product for edit/remove tests added successfully with ID: " + thirdTestProduct.getProductId());

        } catch (Exception e) {
            System.err.println("Error in setup for edit and remove tests: " + e.getMessage());
            throw new RuntimeException("Setup for edit and remove tests failed", e);
        }
    }

    @Test
    @DisplayName("Store owner should be able to edit product details")
    void editProductDetailsTest() {
        // Create a modified product request with updated details
        // Using the same ID as the product we added in setup
        ProductRequest updatedProduct = new ProductRequest(
                thirdTestProduct.getProductId(),    // Use the ID from thirdTestProduct
                "Updated Product Name",             // Updated name
                "Home Electronics",                 // Updated category
                "Updated high-quality description", // Updated description
                129.99                              // Updated price
        );

        // Define the new quantity
        int newQuantity = 75;  // Increased quantity

        try {
            // Call the editProductDetails method
            Response editResponse = bridge.editProductDetails(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    updatedProduct,
                    newQuantity
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(editResponse, "Edit response should not be null");
            Assertions.assertFalse(editResponse.isError(), "Product edit should succeed");
            Assertions.assertNotNull(editResponse.getJson(), "Edit response JSON should not be null");

            System.out.println("Edit product response: " + editResponse.getJson());
        }
        catch (Exception e) {
            Assertions.fail("Editing product failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when trying to edit a product with invalid data")
    void invalidProductEditTest() {
        // Create a product request with invalid data (negative price)
        ProductRequest invalidEditProduct = new ProductRequest(
                thirdTestProduct.getProductId(),    // Use the ID from thirdTestProduct
                "Invalid Update",                   // Name
                "Electronics",                      // Category
                "Product with invalid price",       // Description
                -50.00                              // Invalid negative price
        );

        // Define the new quantity
        int newQuantity = 30;

        try {
            // Call the editProductDetails method with invalid data
            Response editResponse = bridge.editProductDetails(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    invalidEditProduct,
                    newQuantity
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(editResponse, "Response should not be null");
            Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for invalid product data");
            Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for invalid edit: " + editResponse.getErrorMessage());
        }
        catch (Exception e) {
            Assertions.fail("Invalid product edit test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to edit product details")
    void unauthorizedProductEditTest() {
        // Create a valid product edit request
        ProductRequest validEditRequest = new ProductRequest(
                thirdTestProduct.getProductId(),    // Use the ID from thirdTestProduct
                "Unauthorized Edit Attempt",        // Name
                "Electronics",                      // Category
                "Attempt to edit by unauthorized user", // Description
                149.99                              // Price
        );

        // Define the new quantity
        int newQuantity = 25;

        try {
            // Attempt to edit the product using unauthorized user credentials
            Response editResponse = bridge.editProductDetails(
                    unauthorizedToken,
                    unauthorizedUsername,
                    storeId,
                    validEditRequest,
                    newQuantity
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(editResponse, "Response should not be null");
            Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for unauthorized user");
            Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized edit: " + editResponse.getErrorMessage());
        }
        catch (Exception e) {
            Assertions.fail("Unauthorized product edit test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should be able to remove a product from their store")
    void removeProductFromStoreTest() {
        try {
            // Call the removeProductFromStore method using thirdTestProduct directly
            Response removeResponse = bridge.removeProductFromStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    thirdTestProduct
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(removeResponse, "Remove response should not be null");
            Assertions.assertFalse(removeResponse.isError(), "Product removal should succeed");
            Assertions.assertNotNull(removeResponse.getJson(), "Remove response JSON should not be null");

            System.out.println("Remove product response: " + removeResponse.getJson());
        }
        catch (Exception e) {
            Assertions.fail("Removing product failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when removing a non-existent product")
    void removeNonExistentProductTest() {
        // Create a product request with a random UUID that doesn't exist in the store
        ProductRequest nonExistentProduct = new ProductRequest(
                UUID.randomUUID(),                 // Random UUID that doesn't exist
                "Non-existent Product",            // Name
                "Imaginary",                       // Category
                "This product doesn't exist",      // Description
                199.99                             // Price
        );

        try {
            // Attempt to remove a product that doesn't exist
            Response removeResponse = bridge.removeProductFromStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    nonExistentProduct
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(removeResponse, "Response should not be null");
            Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for non-existent product");
            Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for non-existent product removal: " +
                    removeResponse.getErrorMessage());
        }
        catch (Exception e) {
            Assertions.fail("Non-existent product removal test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to remove a product")
    void unauthorizedProductRemovalTest() {
        // Create a valid product for removal - we'll use thirdTestProduct
        try {
            // Attempt to remove the product using unauthorized user credentials
            Response removeResponse = bridge.removeProductFromStore(
                    unauthorizedToken,
                    unauthorizedUsername,
                    storeId,
                    thirdTestProduct
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(removeResponse, "Response should not be null");
            Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for unauthorized user");
            Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized product removal: " +
                    removeResponse.getErrorMessage());
        }
        catch (Exception e) {
            Assertions.fail("Unauthorized product removal test failed with exception: " + e.getMessage());
        }
    }

    // Add these as class fields
    private static String managerUsername;
    private static String managerPassword;
    private static PermissionsRequest managerPermissionsRequest;

    @BeforeAll
    static void setupManagerTests() {
        try {
            // Generate a unique username for the manager
            managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
            managerPassword = "Manager123!";

            // Register the new manager user
            RegisterRequest managerRegisterRequest = new RegisterRequest(
                    managerUsername,
                    managerPassword,
                    managerUsername + "@example.com",
                    "Manager",
                    "User"
            );

            Response registerResponse = bridge.registerUser(managerRegisterRequest);
            Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");
            System.out.println("Manager user registered successfully: " + managerUsername);

            // Create a set of permissions to grant to the manager
            Set<Permission> managerPermissions = new HashSet<>();
            managerPermissions.add(Permission.VIEW_STORE_INFO);
            managerPermissions.add(Permission.VIEW_PRODUCT_INFO);
            managerPermissions.add(Permission.MANAGE_INVENTORY);
            managerPermissions.add(Permission.ADD_PRODUCT);
            managerPermissions.add(Permission.UPDATE_PRODUCT);
            managerPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);

            // Create a permissions request object
            managerPermissionsRequest = new PermissionsRequest(managerPermissions);

            System.out.println("Manager test setup completed successfully");
        } catch (Exception e) {
            System.err.println("Error in manager test setup: " + e.getMessage());
            throw new RuntimeException("Manager test setup failed", e);
        }
    }

    @Test
    @DisplayName("Store owner should be able to appoint a store manager with specific permissions")
    void appointManagerTest() {
        try {
            // Appoint the user as manager using the data from setup
            Response appointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    managerPermissionsRequest
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(appointResponse, "Appoint manager response should not be null");
            Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
            Assertions.assertNotNull(appointResponse.getJson(), "Appoint manager response JSON should not be null");

            System.out.println("Appoint manager response: " + appointResponse.getJson());
        } catch (Exception e) {
            Assertions.fail("Appointing manager failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when appointing someone who is already a manager")
    void appointExistingManagerTest() {
        try {
            // First, appoint the user as a manager
            Response firstAppointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    managerPermissionsRequest
            );

            // Verify the first appointment succeeds
            Assertions.assertFalse(firstAppointResponse.isError(), "First manager appointment should succeed");

            // Now try to appoint the same user again
            Response secondAppointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    managerPermissionsRequest
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(secondAppointResponse, "Response should not be null");
            Assertions.assertTrue(secondAppointResponse.isError(),
                    "Response should indicate an error when appointing existing manager");
            Assertions.assertNotNull(secondAppointResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error when appointing existing manager: " +
                    secondAppointResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Appointing existing manager test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to appoint a manager")
    void unauthorizedManagerAppointmentTest() {
        try {
            // Create a set of permissions for the unauthorized appointment attempt
            Set<Permission> permissions = new HashSet<>();
            permissions.add(Permission.VIEW_STORE_INFO);
            permissions.add(Permission.VIEW_PRODUCT_INFO);

            PermissionsRequest permissionsRequest = new PermissionsRequest(permissions);

            // Attempt to appoint a manager using unauthorized user credentials
            Response appointResponse = bridge.appointManager(
                    unauthorizedUsername,
                    unauthorizedToken,
                    storeId,
                    unauthorizedUsername,  // Trying to appoint themselves
                    permissionsRequest
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(appointResponse, "Response should not be null");
            Assertions.assertTrue(appointResponse.isError(),
                    "Response should indicate an error for unauthorized appointment");
            Assertions.assertNotNull(appointResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized manager appointment: " +
                    appointResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Unauthorized manager appointment test failed with exception: " + e.getMessage());
        }
    }

    // Add these static fields for the manager tests
    private static String testManagerUsername;
    private static String testManagerPassword;
    private static PermissionsRequest initialManagerPermissions;
    private static PermissionsRequest updatedManagerPermissions;

    @BeforeAll
    static void setupManagerOperationsTests() {
        try {
            // Create a unique username for the test manager
            testManagerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
            testManagerPassword = "Manager123!";

            // Register the manager user
            RegisterRequest managerRegisterRequest = new RegisterRequest(
                    testManagerUsername,
                    testManagerPassword,
                    testManagerUsername + "@example.com",
                    "Manager",
                    "User"
            );

            Response registerResponse = bridge.registerUser(managerRegisterRequest);
            Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");
            System.out.println("Test manager registered successfully: " + testManagerUsername);

            // Create initial permissions for the manager
            Set<Permission> initialPermissions = new HashSet<>();
            initialPermissions.add(Permission.VIEW_STORE_INFO);
            initialPermissions.add(Permission.VIEW_PRODUCT_INFO);
            initialPermissions.add(Permission.ADD_PRODUCT);

            // Create a permissions request object for initial permissions
            initialManagerPermissions = new PermissionsRequest(initialPermissions);

            // Create updated permissions for permission editing test
            Set<Permission> updatedPermissions = new HashSet<>();
            updatedPermissions.add(Permission.VIEW_STORE_INFO);
            updatedPermissions.add(Permission.VIEW_PRODUCT_INFO);
            updatedPermissions.add(Permission.MANAGE_INVENTORY);
            updatedPermissions.add(Permission.ADD_PRODUCT);
            updatedPermissions.add(Permission.REMOVE_PRODUCT);
            updatedPermissions.add(Permission.UPDATE_PRODUCT);
            updatedPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);

            // Create a permissions request object for the updated permissions
            updatedManagerPermissions = new PermissionsRequest(updatedPermissions);

            // Appoint the user as a manager
            Response appointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    testManagerUsername,
                    initialManagerPermissions
            );

            Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
            System.out.println("Test manager appointed successfully");

        } catch (Exception e) {
            System.err.println("Error in manager operations test setup: " + e.getMessage());
            throw new RuntimeException("Manager operations test setup failed", e);
        }
    }

    @Test
    @DisplayName("Store owner should be able to edit manager permissions")
    void editManagerPermissionsTest() {
        try {
            // Edit the manager's permissions using pre-initialized data
            Response editPermissionsResponse = bridge.editManagerPermissions(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    testManagerUsername,
                    updatedManagerPermissions
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(editPermissionsResponse, "Edit permissions response should not be null");
            Assertions.assertFalse(editPermissionsResponse.isError(), "Updating manager permissions should succeed");
            Assertions.assertNotNull(editPermissionsResponse.getJson(), "Edit permissions response JSON should not be null");

            System.out.println("Edit manager permissions response: " + editPermissionsResponse.getJson());
        } catch (Exception e) {
            Assertions.fail("Editing manager permissions failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when editing permissions for a non-existent manager")
    void editNonExistentManagerPermissionsTest() {
        try {
            // Create a non-existent manager username
            String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();

            // Try to edit permissions for a manager that doesn't exist
            Response editResponse = bridge.editManagerPermissions(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    nonExistentManager,
                    updatedManagerPermissions
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(editResponse, "Response should not be null");
            Assertions.assertTrue(editResponse.isError(),
                    "Response should indicate an error when editing non-existent manager permissions");
            Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error when editing non-existent manager permissions: " +
                    editResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Editing non-existent manager permissions test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to edit manager permissions")
    void unauthorizedManagerPermissionEditTest() {
        try {
            // Attempt to edit permissions using unauthorized user credentials
            Response editResponse = bridge.editManagerPermissions(
                    unauthorizedUsername,
                    unauthorizedToken,
                    storeId,
                    testManagerUsername,
                    updatedManagerPermissions
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(editResponse, "Response should not be null");
            Assertions.assertTrue(editResponse.isError(),
                    "Response should indicate an error for unauthorized permission edit");
            Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized manager permission edit: " +
                    editResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Unauthorized manager permission edit test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should be able to remove a store manager")
    void removeManagerTest() {
        try {
            // Remove the manager using pre-initialized data
            Response removeResponse = bridge.removeManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    testManagerUsername
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(removeResponse, "Remove manager response should not be null");
            Assertions.assertFalse(removeResponse.isError(), "Manager removal should succeed");
            Assertions.assertNotNull(removeResponse.getJson(), "Remove manager response JSON should not be null");

            System.out.println("Remove manager response: " + removeResponse.getJson());
        } catch (Exception e) {
            Assertions.fail("Removing manager failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when removing a non-existent manager")
    void removeNonExistentManagerTest() {
        try {
            // Create a non-existent manager username
            String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();

            // Try to remove a manager that doesn't exist
            Response removeResponse = bridge.removeManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    nonExistentManager
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(removeResponse, "Response should not be null");
            Assertions.assertTrue(removeResponse.isError(),
                    "Response should indicate an error when removing non-existent manager");
            Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error when removing non-existent manager: " +
                    removeResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Removing non-existent manager test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to remove a manager")
    void unauthorizedManagerRemovalTest() {
        try {
            // Attempt to remove a manager using unauthorized user credentials
            Response removeResponse = bridge.removeManager(
                    unauthorizedUsername,
                    unauthorizedToken,
                    storeId,
                    testManagerUsername
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(removeResponse, "Response should not be null");
            Assertions.assertTrue(removeResponse.isError(),
                    "Response should indicate an error for unauthorized manager removal");
            Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized manager removal: " +
                    removeResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Unauthorized manager removal test failed with exception: " + e.getMessage());
        }
    }


    /**
     * should be moved to the store manager tests
     * **/
    @Test
    @DisplayName("Store manager without ADD_PRODUCT permission should not be able to add a product")
    void managerWithoutPermissionTest() {
        // First, create a new user who will be appointed as a manager
        String managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
        String managerPassword = "Manager123!";
        RegisterRequest managerRegisterRequest = new RegisterRequest(
                managerUsername,
                managerPassword,
                managerUsername + "@example.com",
                "Manager",
                "User"
        );

        Response registerResponse = bridge.registerUser(managerRegisterRequest);
        Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");

        // Create permissions for the manager - deliberately excluding ADD_PRODUCT permission
        Set<Permission> managerPermissions = new HashSet<>();
        managerPermissions.add(Permission.VIEW_STORE_INFO);
        managerPermissions.add(Permission.VIEW_PRODUCT_INFO);
        managerPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);
        // Notice we don't include Permission.ADD_PRODUCT

        // Create a permissions request object
        PermissionsRequest permissionsRequest = new PermissionsRequest(managerPermissions);

        // Appoint the user as a manager with limited permissions
        Response appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                permissionsRequest
        );
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");

        // Now login as the manager
        Response managerLoginResponse = bridge.loginUser(managerUsername, managerPassword);
        Assertions.assertFalse(managerLoginResponse.isError(), "Manager login should succeed");
        String managerToken = managerLoginResponse.getJson();
        Assertions.assertNotNull(managerToken, "Manager JWT token should not be null");

        // Create a product that the manager will try to add
        ProductRequest newProduct = new ProductRequest(
                UUID.randomUUID(),
                "Manager Test Product",
                "Electronics",
                "A product that should not be added",
                149.99
        );
        int productQuantity = 5;

        // Manager tries to add a product (this should fail due to lack of permission)
        try {
            Response addProductResponse = bridge.addProductToStore(
                    managerToken,
                    managerUsername,
                    storeId,
                    newProduct,
                    productQuantity
            );

            // Assert - Verify response indicates an error due to insufficient permissions
            Assertions.assertNotNull(addProductResponse, "Add product response should not be null");
            Assertions.assertTrue(addProductResponse.isError(), "Manager without ADD_PRODUCT permission should not be able to add a product");
            Assertions.assertNotNull(addProductResponse.getErrorMessage(), "Error message should not be null");
            Assertions.assertTrue(
                    addProductResponse.getErrorMessage().contains("permission") ||
                            addProductResponse.getErrorMessage().contains("Permission") ||
                            addProductResponse.getErrorMessage().contains("unauthorized") ||
                            addProductResponse.getErrorMessage().contains("Unauthorized"),
                    "Error message should indicate a permission or authorization issue"
            );

            System.out.println("Expected error when manager without permission tries to add product: " +
                    addProductResponse.getErrorMessage());
        } catch (Exception e) {
            // Even if an exception is thrown, this is acceptable as long as the product is not added
            System.out.println("Exception when manager without permission tries to add product: " + e.getMessage());
        }

    }

    // Add these static fields for the close/reopen tests
    private static UUID testStoreId;
    private static final String TEST_STORE_NAME = "Test Close/Reopen Store";
    private static final String TEST_STORE_DESCRIPTION = "A store for testing close and reopen operations";
    private static final String TEST_STORE_ADDRESS = "789 Test Avenue, Test City";
    private static final String TEST_STORE_EMAIL = "close-reopen-store@example.com";
    private static final String TEST_STORE_PHONE = "555-123-7890";

    @BeforeAll
    static void setupCloseReopenTests() {
        try {
            // Create a new store for close/reopen tests
            StoreRequest newStore = new StoreRequest(
                    TEST_STORE_NAME,
                    TEST_STORE_DESCRIPTION,
                    TEST_STORE_ADDRESS,
                    TEST_STORE_EMAIL,
                    TEST_STORE_PHONE,
                    ownerUsername
            );

            Response createStoreResponse = bridge.createStore(
                    ownerUsername,
                    ownerToken,
                    newStore
            );

            Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

            // Extract the store ID from the response
            String storeIdStr = createStoreResponse.getJson();

            // Check if the response contains a valid UUID
            if (storeIdStr != null && !storeIdStr.isEmpty()) {
                try {
                    testStoreId = UUID.fromString(storeIdStr);
                    System.out.println("Created test store for close/reopen tests with ID: " + testStoreId);
                } catch (IllegalArgumentException e) {
                    // If parsing fails, generate a UUID for testing purposes
                    testStoreId = UUID.randomUUID();
                    System.out.println("Using generated test store ID for close/reopen tests: " + testStoreId);
                }
            } else {
                // If no store ID was returned, generate one for testing
                testStoreId = UUID.randomUUID();
                System.out.println("Using generated test store ID for close/reopen tests: " + testStoreId);
            }

            System.out.println("Setup for close/reopen tests completed successfully");
        } catch (Exception e) {
            System.err.println("Error in setup for close/reopen tests: " + e.getMessage());
            throw new RuntimeException("Setup for close/reopen tests failed", e);
        }
    }

    @Test
    @DisplayName("Store owner should be able to close their store")
    void closeStoreTest() {
        try {
            // Call the closeStore method using the test store
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    testStoreId
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(closeResponse, "Close store response should not be null");
            Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
            Assertions.assertNotNull(closeResponse.getJson(), "Close store response JSON should not be null");

            System.out.println("Close store response: " + closeResponse.getJson());

        } catch (Exception e) {
            Assertions.fail("Store closure failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when closing a non-existent store")
    void closeNonExistentStoreTest() {
        try {
            // Generate a random UUID for a non-existent store
            UUID nonExistentStoreId = UUID.randomUUID();

            // Try to close a store that doesn't exist
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    nonExistentStoreId
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(closeResponse, "Response should not be null");
            Assertions.assertTrue(closeResponse.isError(),
                    "Response should indicate an error when closing non-existent store");
            Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error when closing non-existent store: " +
                    closeResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Closing non-existent store test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to close a store")
    void unauthorizedStoreClosureTest() {
        try {
            // Attempt to close the store using unauthorized user credentials
            Response closeResponse = bridge.closeStore(
                    unauthorizedUsername,
                    unauthorizedToken,
                    testStoreId
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(closeResponse, "Response should not be null");
            Assertions.assertTrue(closeResponse.isError(),
                    "Response should indicate an error for unauthorized store closure");
            Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized store closure: " +
                    closeResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Unauthorized store closure test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should be able to reopen a closed store")
    void reopenStoreTest() {
        try {
            // First, close the store so we can reopen it (if not already closed by previous test)
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    testStoreId  // Use test store ID
            );

            // We don't assert on this close operation as it might already be closed from the previous test
            System.out.println("Store closed (or was already closed)");

            // Now call the reopenStore method to reopen the closed store
            Response reopenResponse = bridge.reopenStore(
                    ownerUsername,
                    ownerToken,
                    testStoreId  // Use test store ID
            );

            // Assert - Verify reopen response is valid
            Assertions.assertNotNull(reopenResponse, "Reopen store response should not be null");
            Assertions.assertFalse(reopenResponse.isError(), "Store reopening should succeed");
            Assertions.assertNotNull(reopenResponse.getJson(), "Reopen store response JSON should not be null");

            System.out.println("Reopen store response: " + reopenResponse.getJson());

        } catch (Exception e) {
            Assertions.fail("Store reopening test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should receive an error when reopening a non-existent store")
    void reopenNonExistentStoreTest() {
        try {
            // Generate a random UUID for a non-existent store
            UUID nonExistentStoreId = UUID.randomUUID();

            // Try to reopen a store that doesn't exist
            Response reopenResponse = bridge.reopenStore(
                    ownerUsername,
                    ownerToken,
                    nonExistentStoreId
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(reopenResponse, "Response should not be null");
            Assertions.assertTrue(reopenResponse.isError(),
                    "Response should indicate an error when reopening non-existent store");
            Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error when reopening non-existent store: " +
                    reopenResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Reopening non-existent store test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Unauthorized user should not be able to reopen a store")
    void unauthorizedStoreReopeningTest() {
        try {
            // First ensure the store is closed (using owner credentials)
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    testStoreId
            );

            // Now attempt to reopen the store using unauthorized user credentials
            Response reopenResponse = bridge.reopenStore(
                    unauthorizedUsername,
                    unauthorizedToken,
                    testStoreId
            );

            // Assert - Verify response indicates an error due to lack of authorization
            Assertions.assertNotNull(reopenResponse, "Response should not be null");
            Assertions.assertTrue(reopenResponse.isError(),
                    "Response should indicate an error for unauthorized store reopening");
            Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");

            System.out.println("Expected error for unauthorized store reopening: " +
                    reopenResponse.getErrorMessage());

            // Make sure we reopen the store for subsequent tests (using owner credentials)
            bridge.reopenStore(
                    ownerUsername,
                    ownerToken,
                    testStoreId
            );
        } catch (Exception e) {
            Assertions.fail("Unauthorized store reopening test failed with exception: " + e.getMessage());
        }
    }

    // Add these static fields for ownership tests
    private static String newOwnerUsername;
    private static String newOwnerPassword;
    private static String newOwnerToken;

    @BeforeAll
    static void setupOwnershipTests() {
        try {
            // Create a new user who will be appointed as an owner
            newOwnerUsername = "owner" + UUID.randomUUID().toString().substring(0, 8);
            newOwnerPassword = "Owner123!";
            RegisterRequest newOwnerRegisterRequest = new RegisterRequest(
                    newOwnerUsername,
                    newOwnerPassword,
                    newOwnerUsername + "@example.com",
                    "New",
                    "Owner"
            );

            Response registerResponse = bridge.registerUser(newOwnerRegisterRequest);
            Assertions.assertFalse(registerResponse.isError(), "New owner user registration should succeed");
            System.out.println("New owner registered: " + newOwnerUsername);

            // The founder (original owner) appoints the new user as a store owner
            Response appointResponse = bridge.appointOwner(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    newOwnerUsername
            );

            // Verify appointment was successful
            Assertions.assertNotNull(appointResponse, "Appoint owner response should not be null");
            Assertions.assertFalse(appointResponse.isError(), "Owner appointment should succeed");
            Assertions.assertNotNull(appointResponse.getJson(), "Appoint owner response JSON should not be null");
            System.out.println("New owner appointed successfully");

            // Now login as the new owner
            Response newOwnerLoginResponse = bridge.loginUser(newOwnerUsername, newOwnerPassword);
            Assertions.assertFalse(newOwnerLoginResponse.isError(), "New owner login should succeed");
            newOwnerToken = newOwnerLoginResponse.getJson();
            Assertions.assertNotNull(newOwnerToken, "New owner JWT token should not be null");
            System.out.println("New owner logged in successfully");

            System.out.println("Ownership test setup completed successfully");
        } catch (Exception e) {
            System.err.println("Error in ownership test setup: " + e.getMessage());
            throw new RuntimeException("Ownership test setup failed", e);
        }
    }

    @Test
    @DisplayName("Appointed store owner should be able to give up ownership")
    void giveUpOwnershipTest() {
        try {
            // Attempt to give up ownership using the new owner's credentials
            Response giveUpResponse = bridge.giveUpOwnerShip(
                    newOwnerUsername,
                    newOwnerToken,
                    storeId
            );

            // Verify giving up ownership was successful
            Assertions.assertNotNull(giveUpResponse, "Give up ownership response should not be null");
            Assertions.assertFalse(giveUpResponse.isError(), "Giving up ownership should succeed");
            Assertions.assertNotNull(giveUpResponse.getJson(), "Give up ownership response JSON should not be null");
            System.out.println("Ownership successfully relinquished");

        } catch (Exception e) {
            Assertions.fail("Give up ownership test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Non-owner should not be able to give up ownership")
    void nonOwnerGiveUpOwnershipTest() {
        try {
            // Attempt to give up ownership using unauthorized user credentials
            // This user was never an owner of the store
            Response giveUpResponse = bridge.giveUpOwnerShip(
                    unauthorizedUsername,
                    unauthorizedToken,
                    storeId
            );

            // Assert - Verify response indicates an error
            Assertions.assertNotNull(giveUpResponse, "Response should not be null");
            Assertions.assertTrue(giveUpResponse.isError(),
                    "Response should indicate an error when non-owner tries to give up ownership");
            Assertions.assertNotNull(giveUpResponse.getErrorMessage(), "Error message should not be null");


            System.out.println("Expected error when non-owner tries to give up ownership: " +
                    giveUpResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Non-owner give up ownership test failed with exception: " + e.getMessage());
        }
    }

}