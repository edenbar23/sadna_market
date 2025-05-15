//package com.sadna_market.market.AcceptanceTests;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sadna_market.market.ApplicationLayer.*;
//import com.sadna_market.market.ApplicationLayer.Requests.PermissionsRequest;
//import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
//import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
//import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
//import com.sadna_market.market.DomainLayer.Permission;
//import org.junit.jupiter.api.*;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//@SpringBootTest
//public class StoreOwnerTests {
//    private final static Bridge bridge = new Bridge();
//    ObjectMapper objectMapper = new ObjectMapper();
//
//    // Test data that will be used across tests
//    private UUID storeId;
//    private static final String STORE_NAME = "Owner Test Store";
//    private static final String STORE_DESCRIPTION = "A store for testing owner operations";
//    private static final String STORE_ADDRESS = "456 Store Avenue, Test City";
//    private static final String STORE_EMAIL = "owner-store@example.com";
//    private static final String STORE_PHONE = "555-987-6543";
//
//    // User credentials that will be set up before each test
//    private String ownerUsername;
//    private String ownerPassword;
//    private String ownerToken;
//
//    private String unauthorizedUsername;
//    private String unauthorizedPassword;
//    private String unauthorizedToken;
//
//    private String managerUsername;
//    private String managerPassword;
//    private String managerToken;
//    private PermissionsRequest managerPermissions;
//
//    // Product data for testing
//    private ProductRequest validProduct;
//    private ProductRequest invalidProduct;
//    private ProductRequest thirdTestProduct;
//    private static final int PRODUCT_QUANTITY = 10;
//
//    @BeforeEach
//    void setUp() {
//        // Create the three users required for testing
//        setupUsers();
//
//        // Create a store owned by the owner user
//        setupStore();
//
//        // Set up product data for testing
//        //setupProducts();
//    }
//
//    @AfterEach
//    void tearDown() {
//        // Clear the system state after each test
//        bridge.clear();
//    }
//
//    private void setupUsers() {
//        // Create owner user
//        ownerUsername = "storeowner_";
//        ownerPassword = "Owner123!";
//        RegisterRequest ownerRequest = new RegisterRequest(
//                ownerUsername,
//                ownerPassword,
//                ownerUsername + "@example.com",
//                "Store",
//                "Owner"
//        );
//        Response ownerRegisterResponse = bridge.registerUser(ownerRequest);
//        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");
//
//        // Create unauthorized user
//        unauthorizedUsername = "unauthorized_";
//        unauthorizedPassword = "Unauthorized123!";
//        RegisterRequest unauthorizedRequest = new RegisterRequest(
//                unauthorizedUsername,
//                unauthorizedPassword,
//                unauthorizedUsername + "@example.com",
//                "Unauthorized",
//                "User"
//        );
//        Response unauthorizedRegisterResponse = bridge.registerUser(unauthorizedRequest);
//        Assertions.assertFalse(unauthorizedRegisterResponse.isError(), "Unauthorized user registration should succeed");
//
//        // Create manager user
//        managerUsername = "manager_";
//        managerPassword = "Manager123!";
//        RegisterRequest managerRequest = new RegisterRequest(
//                managerUsername,
//                managerPassword,
//                managerUsername + "@example.com",
//                "Store",
//                "Manager"
//        );
//        Response managerRegisterResponse = bridge.registerUser(managerRequest);
//        Assertions.assertFalse(managerRegisterResponse.isError(), "Manager user registration should succeed");
//
//        // Login all users to get their tokens
//        Response ownerLoginResponse = bridge.loginUser(ownerUsername, ownerPassword);
//        Assertions.assertFalse(ownerLoginResponse.isError(), "Owner login should succeed");
//        ownerToken = ownerLoginResponse.getJson();
//
//        Response unauthorizedLoginResponse = bridge.loginUser(unauthorizedUsername, unauthorizedPassword);
//        Assertions.assertFalse(unauthorizedLoginResponse.isError(), "Unauthorized user login should succeed");
//        unauthorizedToken = unauthorizedLoginResponse.getJson();
//
//        Response managerLoginResponse = bridge.loginUser(managerUsername, managerPassword);
//        Assertions.assertFalse(managerLoginResponse.isError(), "Manager login should succeed");
//        managerToken = managerLoginResponse.getJson();
//
//        // Create manager permissions
//        Set<Permission> permissions = new HashSet<>();
//        permissions.add(Permission.VIEW_STORE_INFO);
//        permissions.add(Permission.VIEW_PRODUCT_INFO);
//        permissions.add(Permission.MANAGE_INVENTORY);
//        permissions.add(Permission.ADD_PRODUCT);
//        permissions.add(Permission.UPDATE_PRODUCT);
//        permissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);
//        managerPermissions = new PermissionsRequest(permissions);
//    }
//
//    private void setupStore() {
//        StoreRequest newStore = new StoreRequest(
//                STORE_NAME,
//                STORE_DESCRIPTION,
//                STORE_ADDRESS,
//                STORE_EMAIL,
//                STORE_PHONE,
//                ownerUsername
//        );
//
//        Response createStoreResponse = bridge.createStore(
//                ownerUsername,
//                ownerToken,
//                newStore
//        );
//        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");
//
//        // Extract the store ID from the response
//        try {
//            storeId = UUID.fromString(createStoreResponse.getJson());
//        } catch (Exception e) {
//            storeId = UUID.randomUUID();
//            System.out.println("Using generated store ID for testing: " + storeId);
//        }
//    }
//
//    private void setupProducts() {
//        validProduct = new ProductRequest(
//                UUID.randomUUID(),
//                "Test Product",
//                "Electronics",
//                "High-quality test product",
//                99.99
//        );
//
//        invalidProduct = new ProductRequest(
//                UUID.randomUUID(),
//                "Invalid Product",
//                "Electronics",
//                "Product with invalid price",
//                -99.99  // Negative price makes this invalid
//        );
//
//        thirdTestProduct = new ProductRequest(
//                UUID.randomUUID(),
//                "Third Test Product",
//                "Home Goods",
//                "Another test product",
//                149.99
//        );
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to add a product to their store")
//    void addProductToStoreTest() {
//        Response response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, validProduct, PRODUCT_QUANTITY);
//
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertFalse(response.isError(), "Product addition should succeed");
//        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when adding a product with invalid data")
//    void addInvalidProductToStoreTest() {
//        Response response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, invalidProduct, PRODUCT_QUANTITY);
//
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Response should indicate an error for invalid product data");
//        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to add a product to the store")
//    void unauthorizedProductAdditionTest() {
//        Response response = bridge.addProductToStore(
//                unauthorizedToken,
//                unauthorizedUsername,
//                storeId,
//                validProduct,
//                PRODUCT_QUANTITY
//        );
//
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Response should indicate an error for unauthorized user");
//        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to edit product details")
//    void editProductDetailsTest() {
//        // First add a product to edit
//        Response addResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct,
//                PRODUCT_QUANTITY
//        );
//        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");
//
//        // Create modified product with same ID
//        ProductRequest updatedProduct = new ProductRequest(
//                thirdTestProduct.getProductId(),
//                "Updated Product Name",
//                "Home Electronics",
//                "Updated high-quality description",
//                129.99
//        );
//
//        // Edit the product
//        Response editResponse = bridge.editProductDetails(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                updatedProduct,
//                75  // New quantity
//        );
//
//        Assertions.assertNotNull(editResponse, "Edit response should not be null");
//        Assertions.assertFalse(editResponse.isError(), "Product edit should succeed");
//        Assertions.assertNotNull(editResponse.getJson(), "Edit response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when trying to edit a product with invalid data")
//    void invalidProductEditTest() {
//        // First add a product to edit
//        Response addResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct,
//                PRODUCT_QUANTITY
//        );
//        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");
//
//        // Create invalid product edit with negative price
//        ProductRequest invalidEditProduct = new ProductRequest(
//                thirdTestProduct.getProductId(),
//                "Invalid Update",
//                "Electronics",
//                "Product with invalid price",
//                -50.00
//        );
//
//        // Try to edit with invalid data
//        Response editResponse = bridge.editProductDetails(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                invalidEditProduct,
//                30
//        );
//
//        Assertions.assertNotNull(editResponse, "Response should not be null");
//        Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for invalid product data");
//        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to edit product details")
//    void unauthorizedProductEditTest() {
//        // First add a product to edit
//        Response addResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct,
//                PRODUCT_QUANTITY
//        );
//        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");
//
//        // Create valid edit request
//        ProductRequest validEditRequest = new ProductRequest(
//                thirdTestProduct.getProductId(),
//                "Unauthorized Edit Attempt",
//                "Electronics",
//                "Attempt to edit by unauthorized user",
//                149.99
//        );
//
//        // Try to edit as unauthorized user
//        Response editResponse = bridge.editProductDetails(
//                unauthorizedToken,
//                unauthorizedUsername,
//                storeId,
//                validEditRequest,
//                25
//        );
//
//        Assertions.assertNotNull(editResponse, "Response should not be null");
//        Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for unauthorized user");
//        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to remove a product from their store")
//    void removeProductFromStoreTest() {
//        // First add a product to remove
//        Response addResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct,
//                PRODUCT_QUANTITY
//        );
//        Assertions.assertFalse(addResponse.isError(), "Adding product for remove test should succeed");
//
//        // Remove the product
//        Response removeResponse = bridge.removeProductFromStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct
//        );
//
//        Assertions.assertNotNull(removeResponse, "Remove response should not be null");
//        Assertions.assertFalse(removeResponse.isError(), "Product removal should succeed");
//        Assertions.assertNotNull(removeResponse.getJson(), "Remove response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when removing a non-existent product")
//    void removeNonExistentProductTest() {
//        ProductRequest nonExistentProduct = new ProductRequest(
//                UUID.randomUUID(),
//                "Non-existent Product",
//                "Imaginary",
//                "This product doesn't exist",
//                199.99
//        );
//
//        Response removeResponse = bridge.removeProductFromStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                nonExistentProduct
//        );
//
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for non-existent product");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to remove a product")
//    void unauthorizedProductRemovalTest() {
//        // First add a product to remove
//        Response addResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                thirdTestProduct,
//                PRODUCT_QUANTITY
//        );
//        Assertions.assertFalse(addResponse.isError(), "Adding product for remove test should succeed");
//
//        // Try to remove as unauthorized user
//        Response removeResponse = bridge.removeProductFromStore(
//                unauthorizedToken,
//                unauthorizedUsername,
//                storeId,
//                thirdTestProduct
//        );
//
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for unauthorized user");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to appoint a store manager with specific permissions")
//    void appointManagerTest() {
//        Response appointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//
//        Assertions.assertNotNull(appointResponse, "Appoint manager response should not be null");
//        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
//        Assertions.assertNotNull(appointResponse.getJson(), "Appoint manager response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when appointing someone who is already a manager")
//    void appointExistingManagerTest() {
//        // First appoint the manager
//        Response firstAppointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//        Assertions.assertFalse(firstAppointResponse.isError(), "First manager appointment should succeed");
//
//        // Try to appoint the same manager again
//        Response secondAppointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//
//        Assertions.assertNotNull(secondAppointResponse, "Response should not be null");
//        Assertions.assertTrue(secondAppointResponse.isError(),
//                "Response should indicate an error when appointing existing manager");
//        Assertions.assertNotNull(secondAppointResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to appoint a manager")
//    void unauthorizedManagerAppointmentTest() {
//        Response appointResponse = bridge.appointManager(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//
//        Assertions.assertNotNull(appointResponse, "Response should not be null");
//        Assertions.assertTrue(appointResponse.isError(),
//                "Response should indicate an error for unauthorized appointment");
//        Assertions.assertNotNull(appointResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to edit manager permissions")
//    void editManagerPermissionsTest() {
//        // First appoint the manager
//        Response appointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
//
//        // Create updated permissions
//        Set<Permission> updatedPermissions = new HashSet<>();
//        updatedPermissions.add(Permission.VIEW_STORE_INFO);
//        updatedPermissions.add(Permission.VIEW_PRODUCT_INFO);
//        updatedPermissions.add(Permission.MANAGE_INVENTORY);
//        updatedPermissions.add(Permission.ADD_PRODUCT);
//        updatedPermissions.add(Permission.REMOVE_PRODUCT);
//        updatedPermissions.add(Permission.UPDATE_PRODUCT);
//        updatedPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);
//        PermissionsRequest updatedPermissionsRequest = new PermissionsRequest(updatedPermissions);
//
//        // Edit manager permissions
//        Response editPermissionsResponse = bridge.editManagerPermissions(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                updatedPermissionsRequest
//        );
//
//        Assertions.assertNotNull(editPermissionsResponse, "Edit permissions response should not be null");
//        Assertions.assertFalse(editPermissionsResponse.isError(), "Updating manager permissions should succeed");
//        Assertions.assertNotNull(editPermissionsResponse.getJson(), "Edit permissions response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when editing permissions for a non-existent manager")
//    void editNonExistentManagerPermissionsTest() {
//        String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();
//
//        Response editResponse = bridge.editManagerPermissions(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                nonExistentManager,
//                managerPermissions
//        );
//
//        Assertions.assertNotNull(editResponse, "Response should not be null");
//        Assertions.assertTrue(editResponse.isError(),
//                "Response should indicate an error when editing non-existent manager permissions");
//        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to edit manager permissions")
//    void unauthorizedManagerPermissionEditTest() {
//        // First appoint the manager
//        Response appointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
//
//        // Try to edit permissions as unauthorized user
//        Response editResponse = bridge.editManagerPermissions(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//
//        Assertions.assertNotNull(editResponse, "Response should not be null");
//        Assertions.assertTrue(editResponse.isError(),
//                "Response should indicate an error for unauthorized permission edit");
//        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to remove a store manager")
//    void removeManagerTest() {
//        // First appoint the manager
//        Response appointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
//
//        // Remove the manager
//        Response removeResponse = bridge.removeManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername
//        );
//
//        Assertions.assertNotNull(removeResponse, "Remove manager response should not be null");
//        Assertions.assertFalse(removeResponse.isError(), "Manager removal should succeed");
//        Assertions.assertNotNull(removeResponse.getJson(), "Remove manager response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when removing a non-existent manager")
//    void removeNonExistentManagerTest() {
//        String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();
//
//        Response removeResponse = bridge.removeManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                nonExistentManager
//        );
//
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(),
//                "Response should indicate an error when removing non-existent manager");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to remove a manager")
//    void unauthorizedManagerRemovalTest() {
//        // First appoint the manager
//        Response appointResponse = bridge.appointManager(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                managerUsername,
//                managerPermissions
//        );
//        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
//
//        // Try to remove manager as unauthorized user
//        Response removeResponse = bridge.removeManager(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId,
//                managerUsername
//        );
//
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(),
//                "Response should indicate an error for unauthorized manager removal");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to close their store")
//    void closeStoreTest() {
//        Response closeResponse = bridge.closeStore(
//                ownerUsername,
//                ownerToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(closeResponse, "Close store response should not be null");
//        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
//        Assertions.assertNotNull(closeResponse.getJson(), "Close store response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when closing a non-existent store")
//    void closeNonExistentStoreTest() {
//        UUID nonExistentStoreId = UUID.randomUUID();
//
//        Response closeResponse = bridge.closeStore(
//                ownerUsername,
//                ownerToken,
//                nonExistentStoreId
//        );
//
//        Assertions.assertNotNull(closeResponse, "Response should not be null");
//        Assertions.assertTrue(closeResponse.isError(),
//                "Response should indicate an error when closing non-existent store");
//        Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to close a store")
//    void unauthorizedStoreClosureTest() {
//        Response closeResponse = bridge.closeStore(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(closeResponse, "Response should not be null");
//        Assertions.assertTrue(closeResponse.isError(),
//                "Response should indicate an error for unauthorized store closure");
//        Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should be able to reopen a closed store")
//    void reopenStoreTest() {
//        // First close the store
//        Response closeResponse = bridge.closeStore(
//                ownerUsername,
//                ownerToken,
//                storeId
//        );
//        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
//
//        // Reopen the store
//        Response reopenResponse = bridge.reopenStore(
//                ownerUsername,
//                ownerToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(reopenResponse, "Reopen store response should not be null");
//        Assertions.assertFalse(reopenResponse.isError(), "Store reopening should succeed");
//        Assertions.assertNotNull(reopenResponse.getJson(), "Reopen store response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Store owner should receive an error when reopening a non-existent store")
//    void reopenNonExistentStoreTest() {
//        UUID nonExistentStoreId = UUID.randomUUID();
//
//        Response reopenResponse = bridge.reopenStore(
//                ownerUsername,
//                ownerToken,
//                nonExistentStoreId
//        );
//
//        Assertions.assertNotNull(reopenResponse, "Response should not be null");
//        Assertions.assertTrue(reopenResponse.isError(),
//                "Response should indicate an error when reopening non-existent store");
//        Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Unauthorized user should not be able to reopen a store")
//    void unauthorizedStoreReopeningTest() {
//        // First close the store
//        Response closeResponse = bridge.closeStore(
//                ownerUsername,
//                ownerToken,
//                storeId
//        );
//        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
//
//        // Try to reopen as unauthorized user
//        Response reopenResponse = bridge.reopenStore(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(reopenResponse, "Response should not be null");
//        Assertions.assertTrue(reopenResponse.isError(),
//                "Response should indicate an error for unauthorized store reopening");
//        Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Appointed store owner should be able to give up ownership")
//    void giveUpOwnershipTest() {
//        // Create a new owner to appoint
//        String newOwnerUsername = "newowner_" + System.currentTimeMillis();
//        String newOwnerPassword = "NewOwner123!";
//        RegisterRequest newOwnerRequest = new RegisterRequest(
//                newOwnerUsername,
//                newOwnerPassword,
//                newOwnerUsername + "@example.com",
//                "New",
//                "Owner"
//        );
//        Response registerResponse = bridge.registerUser(newOwnerRequest);
//        Assertions.assertFalse(registerResponse.isError(), "New owner registration should succeed");
//
//        // Login the new owner
//        Response loginResponse = bridge.loginUser(newOwnerUsername, newOwnerPassword);
//        Assertions.assertFalse(loginResponse.isError(), "New owner login should succeed");
//        String newOwnerToken = loginResponse.getJson();
//
//        // Appoint the new owner
//        Response appointResponse = bridge.appointOwner(
//                ownerUsername,
//                ownerToken,
//                storeId,
//                newOwnerUsername
//        );
//        Assertions.assertFalse(appointResponse.isError(), "Owner appointment should succeed");
//
//        // New owner gives up ownership
//        Response giveUpResponse = bridge.giveUpOwnerShip(
//                newOwnerUsername,
//                newOwnerToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(giveUpResponse, "Give up ownership response should not be null");
//        Assertions.assertFalse(giveUpResponse.isError(), "Giving up ownership should succeed");
//        Assertions.assertNotNull(giveUpResponse.getJson(), "Give up ownership response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Non-owner should not be able to give up ownership")
//    void nonOwnerGiveUpOwnershipTest() {
//        Response giveUpResponse = bridge.giveUpOwnerShip(
//                unauthorizedUsername,
//                unauthorizedToken,
//                storeId
//        );
//
//        Assertions.assertNotNull(giveUpResponse, "Response should not be null");
//        Assertions.assertTrue(giveUpResponse.isError(),
//                "Response should indicate an error when non-owner tries to give up ownership");
//        Assertions.assertNotNull(giveUpResponse.getErrorMessage(), "Error message should not be null");
//    }
//}