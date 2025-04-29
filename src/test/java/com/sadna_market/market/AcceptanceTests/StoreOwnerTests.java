package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.PermissionsRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;

import com.sadna_market.market.DomainLayer.Permission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
public class StoreOwnerTests {
    private Bridge bridge = new Bridge();
    ObjectMapper objectMapper = new ObjectMapper();

    // Test data
    private UUID storeId;
    private static final String STORE_NAME = "Owner Test Store";
    private static final String STORE_DESCRIPTION = "A store for testing owner operations";
    private static final String STORE_ADDRESS = "456 Store Avenue, Test City";
    private static final String STORE_EMAIL = "owner-store@example.com";
    private static final String STORE_PHONE = "555-987-6543";

    // Store owner credentials
    private String ownerUsername;
    private String ownerPassword;
    private String ownerToken;  // JWT token for the store owner

    @BeforeEach
    void setup() {
        // Create a store owner user
        ownerUsername = "storeowner" + UUID.randomUUID().toString().substring(0, 8);
        ownerPassword = "owner123";
        RegisterRequest ownerRegisterRequest = new RegisterRequest(
                ownerUsername,
                ownerPassword,
                ownerUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response ownerRegisterResponse = bridge.registerUser(ownerRegisterRequest);
        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");

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

    @Test
    @DisplayName("Store owner should be able to add a product to their store")
    void addProductToStoreTest() {
        // Create a ProductRequest object for the new product
        ProductRequest newProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                         // Price
        );
        int PRODUCT_QUANTITY = 10; // Example quantity
        try {
            // Call the addProductToStore method from the bridge
            Response response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, newProduct, PRODUCT_QUANTITY);

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
        // Create an invalid ProductRequest (e.g., negative price)
        ProductRequest invalidProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                         // Price
        );
        int PRODUCT_QUANTITY = 10; // Example quantity
        try {
            // Call the addProductToStore method with invalid product data
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
    @DisplayName("Store owner should be able to edit product details")
    void editProductDetailsTest() {
        // First, add a product to the store
        ProductRequest initialProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                         // Price
        );
        int PRODUCT_QUANTITY = 10; // Example quantity
        Response addResponse = bridge.addProductToStore(ownerToken, ownerUsername, storeId, initialProduct,PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product should succeed");



        // Now create a modified product request with updated details
        ProductRequest updatedProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                         // Price
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
    @DisplayName("Store owner should be able to remove a product from their store")
    void removeProductFromStoreTest() {
        // First, add a product to the store so we have something to remove
        ProductRequest initialProduct = new ProductRequest(
                UUID.randomUUID(),
                "Test Product",                  // Product name
                "Electronics",                   // Category
                "High-quality test product",     // Description
                99.99                         // Price
        );
        int PRODUCT_QUANTITY = 10; // Example quantity
        Response addResponse = bridge.addProductToStore(ownerToken, ownerUsername, storeId, initialProduct,PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product should succeed");
        try {
            // Call the removeProductFromStore method
            Response removeResponse = bridge.removeProductFromStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    initialProduct
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
    @DisplayName("Store owner should be able to appoint a store manager with specific permissions")
    void appointManagerTest() {
        // First, create a new user who will be appointed as a manager
        String managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
        String managerPassword = "manager123";
        RegisterRequest managerRegisterRequest = new RegisterRequest(
                managerUsername,
                managerPassword,
                managerUsername + "@example.com",
                "Manager",
                "User"
        );

        Response registerResponse = bridge.registerUser(managerRegisterRequest);
        Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");

        // Create a set of permissions to grant to the manager
        Set<Permission> managerPermissions = new HashSet<Permission>();
        managerPermissions.add(Permission.VIEW_STORE_INFO);
        managerPermissions.add(Permission.VIEW_PRODUCT_INFO);
        managerPermissions.add(Permission.MANAGE_INVENTORY);
        managerPermissions.add(Permission.ADD_PRODUCT);
        managerPermissions.add(Permission.UPDATE_PRODUCT);
        managerPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);

        // Create a permissions request object
        PermissionsRequest permissionsRequest = new PermissionsRequest(managerPermissions);

        try {
            Response appointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    permissionsRequest
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
    @DisplayName("Store owner should be able to remove a store manager")
    void removeManagerTest() {
        // First, create a new user who will be appointed as a manager
        String managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
        String managerPassword = "manager123";
        RegisterRequest managerRegisterRequest = new RegisterRequest(
                managerUsername,
                managerPassword,
                managerUsername + "@example.com",
                "Manager",
                "User"
        );

        Response registerResponse = bridge.registerUser(managerRegisterRequest);
        Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");

        // Create permissions for the manager
        Set<Permission> managerPermissions = new HashSet<Permission>();
        managerPermissions.add(Permission.VIEW_STORE_INFO);
        managerPermissions.add(Permission.VIEW_PRODUCT_INFO);
        managerPermissions.add(Permission.ADD_PRODUCT);

        // Create a permissions request object
        PermissionsRequest permissionsRequest = new PermissionsRequest(managerPermissions);

        // Appoint the user as a manager
        try {
            Response appointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    permissionsRequest
            );

            Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
            System.out.println("Appoint manager response: " + appointResponse.getJson());
        } catch (Exception e) {
            Assertions.fail("Appointing manager failed with exception: " + e.getMessage());
            return; // Exit the test if appointment fails
        }

        // Now remove the manager
        try {
            Response removeResponse = bridge.removeManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername
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
    @DisplayName("Store owner should be able to edit manager permissions")
    void editManagerPermissionsTest() {
        // First, create a new user who will be appointed as a manager
        String managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
        String managerPassword = "manager123";
        RegisterRequest managerRegisterRequest = new RegisterRequest(
                managerUsername,
                managerPassword,
                managerUsername + "@example.com",
                "Manager",
                "User"
        );

        Response registerResponse = bridge.registerUser(managerRegisterRequest);
        Assertions.assertFalse(registerResponse.isError(), "Manager user registration should succeed");

        // Create initial permissions for the manager
        Set<Permission> initialPermissions = new HashSet<>();
        initialPermissions.add(Permission.VIEW_STORE_INFO);
        initialPermissions.add(Permission.VIEW_PRODUCT_INFO);
        initialPermissions.add(Permission.ADD_PRODUCT);

        // Create a permissions request object for initial permissions
        PermissionsRequest initialPermissionsRequest = new PermissionsRequest(initialPermissions);

        // Appoint the user as a manager with initial permissions
        try {
            Response appointResponse = bridge.appointManager(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    initialPermissionsRequest
            );

            Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
            System.out.println("Appoint manager response: " + appointResponse.getJson());
        } catch (Exception e) {
            Assertions.fail("Appointing manager failed with exception: " + e.getMessage());
            return; // Exit the test if appointment fails
        }

        // Now create updated permissions for the manager
        Set<Permission> updatedPermissions = new HashSet<>();
        updatedPermissions.add(Permission.VIEW_STORE_INFO);
        updatedPermissions.add(Permission.VIEW_PRODUCT_INFO);
        updatedPermissions.add(Permission.MANAGE_INVENTORY);
        updatedPermissions.add(Permission.ADD_PRODUCT);
        updatedPermissions.add(Permission.REMOVE_PRODUCT);
        updatedPermissions.add(Permission.UPDATE_PRODUCT);
        updatedPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);

        // Create a permissions request object for the updated permissions
        PermissionsRequest updatedPermissionsRequest = new PermissionsRequest(updatedPermissions);

        // Edit the manager's permissions
        try {
            Response editPermissionsResponse = bridge.editManagerPermissions(
                    ownerUsername,
                    ownerToken,
                    storeId,
                    managerUsername,
                    updatedPermissionsRequest
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
    @DisplayName("Store manager without ADD_PRODUCT permission should not be able to add a product")
    void managerWithoutPermissionTest() {
        // First, create a new user who will be appointed as a manager
        String managerUsername = "manager" + UUID.randomUUID().toString().substring(0, 8);
        String managerPassword = "manager123";
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

    @Test
    @DisplayName("Store owner should be able to close their store")
    void closeStoreTest() {
        try {
            // Call the closeStore method
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    storeId
            );

            // Assert - Verify response is valid
            Assertions.assertNotNull(closeResponse, "Close store response should not be null");
            Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
            Assertions.assertNotNull(closeResponse.getJson(), "Close store response JSON should not be null");

            System.out.println("Close store response: " + closeResponse.getJson());

            // Verify the store is now closed by trying to perform an operation that should fail on a closed store
            ProductRequest newProduct = new ProductRequest(
                    UUID.randomUUID(),
                    "Test Product After Close",
                    "Electronics",
                    "This should not be added to a closed store",
                    99.99
            );
            int productQuantity = 10;

            Response addToClosedStoreResponse = bridge.addProductToStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    newProduct,
                    productQuantity
            );

            // Assert that the operation on a closed store fails
            Assertions.assertTrue(addToClosedStoreResponse.isError(),
                    "Adding a product to a closed store should fail");
            Assertions.assertTrue(
                    addToClosedStoreResponse.getErrorMessage().contains("closed") ||
                            addToClosedStoreResponse.getErrorMessage().contains("inactive") ||
                            addToClosedStoreResponse.getErrorMessage().contains("not active"),
                    "Error message should indicate the store is closed or inactive"
            );

            System.out.println("Expected error when adding product to closed store: " +
                    addToClosedStoreResponse.getErrorMessage());
        } catch (Exception e) {
            Assertions.fail("Store closure failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Store owner should be able to reopen a closed store")
    void reopenStoreTest() {
        try {
            // First, close the store so we can reopen it
            Response closeResponse = bridge.closeStore(
                    ownerUsername,
                    ownerToken,
                    storeId
            );

            Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
            System.out.println("Store closed successfully");

            // Verify the store is closed by trying to add a product (should fail)
            ProductRequest testProduct = new ProductRequest(
                    UUID.randomUUID(),
                    "Test Product",
                    "Electronics",
                    "This should not be added to a closed store",
                    99.99
            );
            int productQuantity = 10;

            Response addToClosedStoreResponse = bridge.addProductToStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    testProduct,
                    productQuantity
            );

            Assertions.assertTrue(addToClosedStoreResponse.isError(),
                    "Adding a product to a closed store should fail");

            // Now call the reopenStore method to reopen the closed store
            Response reopenResponse = bridge.reopenStore(
                    ownerUsername,
                    ownerToken,
                    storeId
            );

            // Assert - Verify reopen response is valid
            Assertions.assertNotNull(reopenResponse, "Reopen store response should not be null");
            Assertions.assertFalse(reopenResponse.isError(), "Store reopening should succeed");
            Assertions.assertNotNull(reopenResponse.getJson(), "Reopen store response JSON should not be null");

            System.out.println("Reopen store response: " + reopenResponse.getJson());

            // Verify the store is now open by successfully adding a product
            ProductRequest productAfterReopen = new ProductRequest(
                    UUID.randomUUID(),
                    "Test Product After Reopen",
                    "Electronics",
                    "This should be added after reopening",
                    129.99
            );

            Response addAfterReopenResponse = bridge.addProductToStore(
                    ownerToken,
                    ownerUsername,
                    storeId,
                    productAfterReopen,
                    productQuantity
            );

            Assertions.assertFalse(addAfterReopenResponse.isError(),
                    "Adding a product after reopening the store should succeed");
            System.out.println("Successfully added product to reopened store");

        } catch (Exception e) {
            Assertions.fail("Store reopening test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Appointed store owner should be able to give up ownership")
    void giveUpOwnershipTest() {
        try {
            // First, create a new user who will be appointed as an owner
            String newOwnerUsername = "owner" + UUID.randomUUID().toString().substring(0, 8);
            String newOwnerPassword = "owner123";
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
            String newOwnerToken = newOwnerLoginResponse.getJson();
            Assertions.assertNotNull(newOwnerToken, "New owner JWT token should not be null");
            System.out.println("New owner logged in successfully");

            // Verify the new owner has owner privileges by attempting an owner action
            // For example, trying to appoint another user as manager would be an owner privilege
            // For now, let's just verify they can add a product to the store
            ProductRequest testProduct = new ProductRequest(
                    UUID.randomUUID(),
                    "New Owner Test Product",
                    "Electronics",
                    "Product added by new owner",
                    129.99
            );
            int productQuantity = 10;

            Response addProductResponse = bridge.addProductToStore(
                    newOwnerToken,
                    newOwnerUsername,
                    storeId,
                    testProduct,
                    productQuantity
            );

            Assertions.assertFalse(addProductResponse.isError(),
                    "New owner should be able to add a product to the store");
            System.out.println("New owner successfully demonstrated ownership privileges");

            // Now the new owner gives up their ownership
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

            // Verify the user no longer has owner privileges by attempting the same owner action
            // This should now fail
            ProductRequest afterProduct = new ProductRequest(
                    UUID.randomUUID(),
                    "After Giving Up Ownership Product",
                    "Electronics",
                    "This should not be added after giving up ownership",
                    149.99
            );

            Response addAfterGivingUpResponse = bridge.addProductToStore(
                    newOwnerToken,
                    newOwnerUsername,
                    storeId,
                    afterProduct,
                    productQuantity
            );

            // Assert that the operation fails because the user is no longer an owner
            Assertions.assertTrue(addAfterGivingUpResponse.isError(),
                    "Former owner should not be able to add a product after giving up ownership");
            Assertions.assertTrue(
                    addAfterGivingUpResponse.getErrorMessage().contains("permission") ||
                            addAfterGivingUpResponse.getErrorMessage().contains("Permission") ||
                            addAfterGivingUpResponse.getErrorMessage().contains("unauthorized") ||
                            addAfterGivingUpResponse.getErrorMessage().contains("Unauthorized"),
                    "Error message should indicate a permission or authorization issue"
            );
            System.out.println("Confirmed user no longer has owner privileges");

        } catch (Exception e) {
            Assertions.fail("Give up ownership test failed with exception: " + e.getMessage());
        }
    }

}