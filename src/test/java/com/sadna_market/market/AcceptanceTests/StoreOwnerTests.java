package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.DTOs.MessageDTO;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.Permission;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
public class StoreOwnerTests {
    @Autowired
    private Bridge bridge;
    ObjectMapper objectMapper = new ObjectMapper();

    // Test data that will be used across tests
    private UUID storeId;
    private static final String STORE_NAME = "Owner Test Store";
    private static final String STORE_DESCRIPTION = "A store for testing owner operations";
    private static final String STORE_ADDRESS = "456 Store Avenue, Test City";
    private static final String STORE_EMAIL = "owner-store@example.com";
    private static final String STORE_PHONE = "555-987-6543";

    // User credentials that will be set up before each test
    private String ownerUsername;
    private String ownerPassword;
    private String ownerToken;

    private String unauthorizedUsername;
    private String unauthorizedPassword;
    private String unauthorizedToken;

    private String managerUsername;
    private String managerPassword;
    private String managerToken;
    private PermissionsRequest managerPermissions;

    // Product data for testing
    private ProductRequest validProduct;
    private ProductRequest invalidProduct;
    private ProductRequest thirdTestProduct;
    private static final int PRODUCT_QUANTITY = 10;

    @BeforeEach
    void setUp() {
        // Create the three users required for testing
        setupUsers();

        // Create a store owned by the owner user
        setupStore();

        // Set up product data for testing
        setupProducts();
    }

    @AfterEach
    void tearDown() {
        // Clear the system state after each test
        bridge.clear();
    }

    private void setupUsers() {
        // Create owner user
        ownerUsername = "storeowner";
        ownerPassword = "Owner123!";
        RegisterRequest ownerRequest = new RegisterRequest(
                ownerUsername,
                ownerPassword,
                ownerUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response<String> ownerRegisterResponse = bridge.registerUser(ownerRequest);
        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");

        // Create unauthorized user
        unauthorizedUsername = "unauthorized";
        unauthorizedPassword = "Unauthorized123!";
        RegisterRequest unauthorizedRequest = new RegisterRequest(
                unauthorizedUsername,
                unauthorizedPassword,
                unauthorizedUsername + "@example.com",
                "Unauthorized",
                "User"
        );
        Response<String> unauthorizedRegisterResponse = bridge.registerUser(unauthorizedRequest);
        Assertions.assertFalse(unauthorizedRegisterResponse.isError(), "Unauthorized user registration should succeed");

        // Create manager user
        managerUsername = "manager";
        managerPassword = "Manager123!";
        RegisterRequest managerRequest = new RegisterRequest(
                managerUsername,
                managerPassword,
                managerUsername + "@example.com",
                "Store",
                "Manager"
        );
        Response<String> managerRegisterResponse = bridge.registerUser(managerRequest);
        Assertions.assertFalse(managerRegisterResponse.isError(), "Manager user registration should succeed");

        // Login all users to get their tokens
        Response<String> ownerLoginResponse = bridge.loginUser(ownerUsername, ownerPassword);
        Assertions.assertFalse(ownerLoginResponse.isError(), "Owner login should succeed");
        ownerToken = ownerLoginResponse.getData();

        Response<String> unauthorizedLoginResponse = bridge.loginUser(unauthorizedUsername, unauthorizedPassword);
        Assertions.assertFalse(unauthorizedLoginResponse.isError(), "Unauthorized user login should succeed");
        unauthorizedToken = unauthorizedLoginResponse.getData();

        Response<String> managerLoginResponse = bridge.loginUser(managerUsername, managerPassword);
        Assertions.assertFalse(managerLoginResponse.isError(), "Manager login should succeed");
        managerToken = managerLoginResponse.getData();

        // Create manager permissions
        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.VIEW_STORE_INFO);
        permissions.add(Permission.VIEW_PRODUCT_INFO);
        permissions.add(Permission.MANAGE_INVENTORY);
        permissions.add(Permission.ADD_PRODUCT);
        permissions.add(Permission.UPDATE_PRODUCT);
        permissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);
        managerPermissions = new PermissionsRequest(permissions);
    }

    private void setupStore() {
        StoreRequest newStore = new StoreRequest(
                STORE_NAME,
                STORE_DESCRIPTION,
                STORE_ADDRESS,
                STORE_EMAIL,
                STORE_PHONE,
                ownerUsername
        );

        Response<?> createStoreResponse = bridge.createStore(
                ownerUsername,
                ownerToken,
                newStore
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        // Extract the store ID from the response
        try {
            JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(createStoreResponse.getData()));
            storeId = UUID.fromString(jsonNode.get("storeId").asText());
        } catch (Exception e) {
            throw new AssertionError("Store ID extraction failed", e);
        }
    }

    private void setupProducts() {
        validProduct = new ProductRequest(
                null,   //product ID will be generated by the system
                "Test Product",
                "Electronics",
                "High-quality test product",
                99.99
        );

        invalidProduct = new ProductRequest(
                null,   //product ID will be generated by the system
                "Invalid Product",
                "Electronics",
                "Product with invalid price",
                -99.99  // Negative price makes this invalid
        );

        thirdTestProduct = new ProductRequest(
                null,   //product ID will be generated by the system
                "Third Test Product",
                "Home Goods",
                "Another test product",
                149.99
        );
    }

    @Test
    //@DisplayName("Store owner should be able to add a product to their store")
    void addProductToStoreTest() {
        Response<String> response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, validProduct, PRODUCT_QUANTITY);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Product addition should succeed");
        Assertions.assertNotNull(response.getData(), "Response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when adding a product with invalid data")
    void addInvalidProductToStoreTest() {
        Response<String> response = bridge.addProductToStore(ownerToken, ownerUsername, storeId, invalidProduct, PRODUCT_QUANTITY);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Response should indicate an error for invalid product data");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to add a product to the store")
    void unauthorizedProductAdditionTest() {
        Response<String> response = bridge.addProductToStore(
                unauthorizedToken,
                unauthorizedUsername,
                storeId,
                validProduct,
                PRODUCT_QUANTITY
        );

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Response should indicate an error for unauthorized user");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to edit product details")
    void editProductDetailsTest() {
        // First add a product to edit
        Response<String> addResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct,
                PRODUCT_QUANTITY
        );
        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");

        // Get the product ID from the response
        UUID productId = UUID.fromString(addResponse.getData());

        // Create modified product with same ID
        ProductRequest updatedProduct = new ProductRequest(
                productId,
                "Updated Product Name",
                "Home Electronics",
                "Updated high-quality description",
                129.99
        );

        // Edit the product
        Response<String> editResponse = bridge.editProductDetails(
                ownerToken,
                ownerUsername,
                storeId,
                updatedProduct,
                75  // New quantity
        );

        Assertions.assertNotNull(editResponse, "Edit response should not be null");
        Assertions.assertFalse(editResponse.isError(), "Product edit should succeed");
        Assertions.assertNotNull(editResponse.getData(), "Edit response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when trying to edit a product with invalid data")
    void invalidProductEditTest() {
        // First add a product to edit
        Response<String> addResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct,
                PRODUCT_QUANTITY
        );
        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");

        // Get the product ID
        UUID productId = UUID.fromString(addResponse.getData());

        // Create invalid product edit with negative price
        ProductRequest invalidEditProduct = new ProductRequest(
                productId,
                "Invalid Update",
                "Electronics",
                "Product with invalid price",
                -50.00
        );

        // Try to edit with invalid data
        Response<String> editResponse = bridge.editProductDetails(
                ownerToken,
                ownerUsername,
                storeId,
                invalidEditProduct,
                30
        );

        Assertions.assertNotNull(editResponse, "Response should not be null");
        Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for invalid product data");
        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to edit product details")
    void unauthorizedProductEditTest() {
        // First add a product to edit
        Response<String> addResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct,
                PRODUCT_QUANTITY
        );
        Assertions.assertFalse(addResponse.isError(), "Adding product for edit test should succeed");

        // Get the product ID
        UUID productId = UUID.fromString(addResponse.getData());

        // Create valid edit request
        ProductRequest validEditRequest = new ProductRequest(
                productId,
                "Unauthorized Edit Attempt",
                "Electronics",
                "Attempt to edit by unauthorized user",
                149.99
        );

        // Try to edit as unauthorized user
        Response<String> editResponse = bridge.editProductDetails(
                unauthorizedToken,
                unauthorizedUsername,
                storeId,
                validEditRequest,
                25
        );

        Assertions.assertNotNull(editResponse, "Response should not be null");
        Assertions.assertTrue(editResponse.isError(), "Response should indicate an error for unauthorized user");
        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to remove a product from their store")
    void removeProductFromStoreTest() {
        // First add a product to remove
        Response<String> addResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct,
                PRODUCT_QUANTITY
        );
        Assertions.assertFalse(addResponse.isError(), "Adding product for remove test should succeed");

        // Get the product ID
        UUID productId = UUID.fromString(addResponse.getData());
        thirdTestProduct.setProductId(productId);

        // Remove the product
        Response<String> removeResponse = bridge.removeProductFromStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct
        );

        Assertions.assertNotNull(removeResponse, "Remove response should not be null");
        Assertions.assertFalse(removeResponse.isError(), "Product removal should succeed");
        Assertions.assertNotNull(removeResponse.getData(), "Remove response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when removing a non-existent product")
    void removeNonExistentProductTest() {
        ProductRequest nonExistentProduct = new ProductRequest(
                UUID.randomUUID(),
                "Non-existent Product",
                "Imaginary",
                "This product doesn't exist",
                199.99
        );

        Response<String> removeResponse = bridge.removeProductFromStore(
                ownerToken,
                ownerUsername,
                storeId,
                nonExistentProduct
        );

        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for non-existent product");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to remove a product")
    void unauthorizedProductRemovalTest() {
        // First add a product to remove
        Response<String> addResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                thirdTestProduct,
                PRODUCT_QUANTITY
        );
        Assertions.assertFalse(addResponse.isError(), "Adding product for remove test should succeed");

        // Get the product ID
        UUID productId = UUID.fromString(addResponse.getData());
        thirdTestProduct.setProductId(productId);

        // Try to remove as unauthorized user
        Response<String> removeResponse = bridge.removeProductFromStore(
                unauthorizedToken,
                unauthorizedUsername,
                storeId,
                thirdTestProduct
        );

        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(), "Response should indicate an error for unauthorized user");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to appoint a store manager with specific permissions")
    void appointManagerTest() {
        Response<String> appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );

        Assertions.assertNotNull(appointResponse, "Appoint manager response should not be null");
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");
        Assertions.assertNotNull(appointResponse.getData(), "Appoint manager response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when appointing someone who is already a manager")
    void appointExistingManagerTest() {
        // First appoint the manager
        Response<String> firstAppointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );
        Assertions.assertFalse(firstAppointResponse.isError(), "First manager appointment should succeed");

        // Try to appoint the same manager again
        Response<String> secondAppointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );

        Assertions.assertNotNull(secondAppointResponse, "Response should not be null");
        Assertions.assertTrue(secondAppointResponse.isError(),
                "Response should indicate an error when appointing existing manager");
        Assertions.assertNotNull(secondAppointResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to appoint a manager")
    void unauthorizedManagerAppointmentTest() {
        Response<String> appointResponse = bridge.appointManager(
                unauthorizedUsername,
                unauthorizedToken,
                storeId,
                managerUsername,
                managerPermissions
        );

        Assertions.assertNotNull(appointResponse, "Response should not be null");
        Assertions.assertTrue(appointResponse.isError(),
                "Response should indicate an error for unauthorized appointment");
        Assertions.assertNotNull(appointResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to edit manager permissions")
    void editManagerPermissionsTest() {
        // First appoint the manager
        Response<String> appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");

        // Create updated permissions
        Set<Permission> updatedPermissions = new HashSet<>();
        updatedPermissions.add(Permission.VIEW_STORE_INFO);
        updatedPermissions.add(Permission.VIEW_PRODUCT_INFO);
        updatedPermissions.add(Permission.MANAGE_INVENTORY);
        updatedPermissions.add(Permission.ADD_PRODUCT);
        updatedPermissions.add(Permission.REMOVE_PRODUCT);
        updatedPermissions.add(Permission.UPDATE_PRODUCT);
        updatedPermissions.add(Permission.VIEW_STORE_PURCHASE_HISTORY);
        PermissionsRequest updatedPermissionsRequest = new PermissionsRequest(updatedPermissions);

        // Edit manager permissions
        Response<String> editPermissionsResponse = bridge.editManagerPermissions(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                updatedPermissionsRequest
        );

        Assertions.assertNotNull(editPermissionsResponse, "Edit permissions response should not be null");
        Assertions.assertFalse(editPermissionsResponse.isError(), "Updating manager permissions should succeed");
        Assertions.assertNotNull(editPermissionsResponse.getData(), "Edit permissions response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when editing permissions for a non-existent manager")
    void editNonExistentManagerPermissionsTest() {
        String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();

        Response<String> editResponse = bridge.editManagerPermissions(
                ownerUsername,
                ownerToken,
                storeId,
                nonExistentManager,
                managerPermissions
        );

        Assertions.assertNotNull(editResponse, "Response should not be null");
        Assertions.assertTrue(editResponse.isError(),
                "Response should indicate an error when editing non-existent manager permissions");
        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to edit manager permissions")
    void unauthorizedManagerPermissionEditTest() {
        // First appoint the manager
        Response<String> appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");

        // Try to edit permissions as unauthorized user
        Response<String> editResponse = bridge.editManagerPermissions(
                unauthorizedUsername,
                unauthorizedToken,
                storeId,
                managerUsername,
                managerPermissions
        );

        Assertions.assertNotNull(editResponse, "Response should not be null");
        Assertions.assertTrue(editResponse.isError(),
                "Response should indicate an error for unauthorized permission edit");
        Assertions.assertNotNull(editResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to remove a store manager")
    void removeManagerTest() {
        // First appoint the manager
        Response<String> appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");

        // Remove the manager
        Response<String> removeResponse = bridge.removeManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername
        );

        Assertions.assertNotNull(removeResponse, "Remove manager response should not be null");
        Assertions.assertFalse(removeResponse.isError(), "Manager removal should succeed");
        Assertions.assertNotNull(removeResponse.getData(), "Remove manager response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when removing a non-existent manager")
    void removeNonExistentManagerTest() {
        String nonExistentManager = "nonexistent" + UUID.randomUUID().toString();

        Response<String> removeResponse = bridge.removeManager(
                ownerUsername,
                ownerToken,
                storeId,
                nonExistentManager
        );

        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(),
                "Response should indicate an error when removing non-existent manager");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to remove a manager")
    void unauthorizedManagerRemovalTest() {
        // First appoint the manager
        Response<String> appointResponse = bridge.appointManager(
                ownerUsername,
                ownerToken,
                storeId,
                managerUsername,
                managerPermissions
        );
        Assertions.assertFalse(appointResponse.isError(), "Manager appointment should succeed");

        // Try to remove manager as unauthorized user
        Response<String> removeResponse = bridge.removeManager(
                unauthorizedUsername,
                unauthorizedToken,
                storeId,
                managerUsername
        );

        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(),
                "Response should indicate an error for unauthorized manager removal");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to close their store")
    void closeStoreTest() {
        Response<String> closeResponse = bridge.closeStore(
                ownerUsername,
                ownerToken,
                storeId
        );

        Assertions.assertNotNull(closeResponse, "Close store response should not be null");
        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");
        Assertions.assertNotNull(closeResponse.getData(), "Close store response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when closing a non-existent store")
    void closeNonExistentStoreTest() {
        UUID nonExistentStoreId = UUID.randomUUID();

        Response<String> closeResponse = bridge.closeStore(
                ownerUsername,
                ownerToken,
                nonExistentStoreId
        );

        Assertions.assertNotNull(closeResponse, "Response should not be null");
        Assertions.assertTrue(closeResponse.isError(),
                "Response should indicate an error when closing non-existent store");
        Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to close a store")
    void unauthorizedStoreClosureTest() {
        Response<String> closeResponse = bridge.closeStore(
                unauthorizedUsername,
                unauthorizedToken,
                storeId
        );

        Assertions.assertNotNull(closeResponse, "Response should not be null");
        Assertions.assertTrue(closeResponse.isError(),
                "Response should indicate an error for unauthorized store closure");
        Assertions.assertNotNull(closeResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Store owner should be able to reopen a closed store")
    void reopenStoreTest() {
        // First close the store
        Response<String> closeResponse = bridge.closeStore(
                ownerUsername,
                ownerToken,
                storeId
        );
        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");

        // Reopen the store
        Response<String> reopenResponse = bridge.reopenStore(
                ownerUsername,
                ownerToken,
                storeId
        );

        Assertions.assertNotNull(reopenResponse, "Reopen store response should not be null");
        Assertions.assertFalse(reopenResponse.isError(), "Store reopening should succeed");
        Assertions.assertNotNull(reopenResponse.getData(), "Reopen store response data should not be null");
    }

    @Test
    //@DisplayName("Store owner should receive an error when reopening a non-existent store")
    void reopenNonExistentStoreTest() {
        UUID nonExistentStoreId = UUID.randomUUID();

        Response<String> reopenResponse = bridge.reopenStore(
                ownerUsername,
                ownerToken,
                nonExistentStoreId
        );

        Assertions.assertNotNull(reopenResponse, "Response should not be null");
        Assertions.assertTrue(reopenResponse.isError(),
                "Response should indicate an error when reopening non-existent store");
        Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Unauthorized user should not be able to reopen a store")
    void unauthorizedStoreReopeningTest() {
        // First close the store
        Response<String> closeResponse = bridge.closeStore(
                ownerUsername,
                ownerToken,
                storeId
        );
        Assertions.assertFalse(closeResponse.isError(), "Store closure should succeed");

        // Try to reopen as unauthorized user
        Response<String> reopenResponse = bridge.reopenStore(
                unauthorizedUsername,
                unauthorizedToken,
                storeId
        );

        Assertions.assertNotNull(reopenResponse, "Response should not be null");
        Assertions.assertTrue(reopenResponse.isError(),
                "Response should indicate an error for unauthorized store reopening");
        Assertions.assertNotNull(reopenResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    //@DisplayName("Appointed store owner should be able to give up ownership")
    void giveUpOwnershipTest() {
        // Create a new owner to appoint
        String newOwnerUsername = "newowner";
        String newOwnerPassword = "NewOwner123!";
        RegisterRequest newOwnerRequest = new RegisterRequest(
                newOwnerUsername,
                newOwnerPassword,
                newOwnerUsername + "@example.com",
                "New",
                "Owner"
        );
        Response<String> registerResponse = bridge.registerUser(newOwnerRequest);
        Assertions.assertFalse(registerResponse.isError(), "New owner registration should succeed");

        // Login the new owner
        Response<String> loginResponse = bridge.loginUser(newOwnerUsername, newOwnerPassword);
        Assertions.assertFalse(loginResponse.isError(), "New owner login should succeed");
        String newOwnerToken = loginResponse.getData();

        // Appoint the new owner
        Response<String> appointResponse = bridge.appointOwner(
                ownerUsername,
                ownerToken,
                storeId,
                newOwnerUsername
        );
        Assertions.assertFalse(appointResponse.isError(), "Owner appointment should succeed");

        // New owner gives up ownership
        Response<String> giveUpResponse = bridge.giveUpOwnerShip(
                newOwnerUsername,
                newOwnerToken,
                storeId
        );

        Assertions.assertNotNull(giveUpResponse, "Give up ownership response should not be null");
        Assertions.assertFalse(giveUpResponse.isError(), "Giving up ownership should succeed");
        Assertions.assertNotNull(giveUpResponse.getData(), "Give up ownership response data should not be null");
    }

    @Test
    //@DisplayName("Non-owner should not be able to give up ownership")
    void nonOwnerGiveUpOwnershipTest() {
        Response<String> giveUpResponse = bridge.giveUpOwnerShip(
                unauthorizedUsername,
                unauthorizedToken,
                storeId
        );

        Assertions.assertNotNull(giveUpResponse, "Response should not be null");
        Assertions.assertTrue(giveUpResponse.isError(),
                "Response should indicate an error when non-owner tries to give up ownership");
        Assertions.assertNotNull(giveUpResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void successfulMessageReplyTest() {
        // First, create a message to reply to
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        // Send the message as unauthorized user (customer)
        Response<MessageDTO> sendResponse = bridge.sendMessage(
                unauthorizedUsername,
                unauthorizedToken,
                messageRequest
        );
        Assertions.assertFalse(sendResponse.isError(), "Message sending should succeed");

        // Get the message ID
        UUID messageId = sendResponse.getData().getMessageId();

        // Create a reply request
        MessageReplyRequest replyRequest = new MessageReplyRequest(
                messageId,
                "Thank you for your inquiry. How can I help you?"
        );

        // Reply to the message as store owner
        Response<String> replyResponse = bridge.replyToMessage(
                ownerUsername,
                ownerToken,
                replyRequest
        );

        // Verify response
        Assertions.assertNotNull(replyResponse, "Reply response should not be null");
        Assertions.assertFalse(replyResponse.isError(), "Message reply should succeed");
        Assertions.assertNotNull(replyResponse.getData(), "Reply response data should not be null");
    }

    @Test
    void unauthorizedUserMessageReplyTest() {
        // First, create a message to reply to
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        // Send the message as one unauthorized user
        Response<MessageDTO> sendResponse = bridge.sendMessage(
                unauthorizedUsername,
                unauthorizedToken,
                messageRequest
        );
        Assertions.assertFalse(sendResponse.isError(), "Message sending should succeed");

        // Get the message ID
        UUID messageId = sendResponse.getData().getMessageId();

        // Create a reply request
        MessageReplyRequest replyRequest = new MessageReplyRequest(
                messageId,
                "I'm trying to reply but I'm not the store owner."
        );

        // Try to reply to the message as another unauthorized user
        Response<String> replyResponse = bridge.replyToMessage(
                managerUsername,  // Manager isn't appointed to this store yet
                managerToken,
                replyRequest
        );

        // Verify response indicates an error
        Assertions.assertNotNull(replyResponse, "Response should not be null");
        Assertions.assertTrue(replyResponse.isError(),
                "Response should indicate an error for unauthorized message reply");
        Assertions.assertNotNull(replyResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void loggedOutUserMessageReplyTest() {
        // First, create a message to reply to
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        // Send the message as unauthorized user
        Response<MessageDTO> sendResponse = bridge.sendMessage(
                unauthorizedUsername,
                unauthorizedToken,
                messageRequest
        );
        Assertions.assertFalse(sendResponse.isError(), "Message sending should succeed");

        // Get the message ID
        UUID messageId = sendResponse.getData().getMessageId();

        // Log out the owner
        Response<String> logoutResponse = bridge.logout(ownerUsername, ownerToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Create a reply request
        MessageReplyRequest replyRequest = new MessageReplyRequest(
                messageId,
                "I'm trying to reply but I'm logged out."
        );

        // Try to reply to the message while logged out
        Response<String> replyResponse = bridge.replyToMessage(
                ownerUsername,
                ownerToken,  // This token is now invalid
                replyRequest
        );

        // Verify response indicates an error
        Assertions.assertNotNull(replyResponse, "Response should not be null");
        Assertions.assertTrue(replyResponse.isError(),
                "Response should indicate an error for logged out user");
        Assertions.assertNotNull(replyResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void successfulGetUserMessagesTest() {
        // First, send a message to create some data
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        Response<MessageDTO> sendResponse = bridge.sendMessage(
                unauthorizedUsername,
                unauthorizedToken,
                messageRequest
        );
        Assertions.assertFalse(sendResponse.isError(), "Message sending should succeed");

        // Get the user's messages
        Response<List<MessageDTO>> messagesResponse = bridge.getUserMessages(
                unauthorizedUsername,
                unauthorizedToken
        );

        // Verify response
        Assertions.assertNotNull(messagesResponse, "Get messages response should not be null");
        Assertions.assertFalse(messagesResponse.isError(), "Getting user messages should succeed");
        Assertions.assertNotNull(messagesResponse.getData(), "Messages response data should not be null");

        // Verify that the messages list contains at least the message we sent
        List<MessageDTO> messages = messagesResponse.getData();
        Assertions.assertFalse(messages.isEmpty(), "Messages list should not be empty");

        // Find our specific message
        boolean messageFound = false;
        for (MessageDTO message : messages) {
            if (message.getStoreId().equals(storeId) &&
                    message.getContent().equals("Hello, I have a question about your products.")) {
                messageFound = true;
                break;
            }
        }

        Assertions.assertTrue(messageFound, "The sent message should be found in the user's messages");
    }

    @Test
    void unauthorizedUserGetMessagesTest() {
        // Create another user who hasn't sent any messages
        String anotherUsername = "anotheruser";
        String anotherPassword = "Another123!";
        RegisterRequest anotherRequest = new RegisterRequest(
                anotherUsername,
                anotherPassword,
                anotherUsername + "@example.com",
                "Another",
                "User"
        );
        Response<String> registerResponse = bridge.registerUser(anotherRequest);
        Assertions.assertFalse(registerResponse.isError(), "Another user registration should succeed");

        // Login the new user
        Response<String> loginResponse = bridge.loginUser(anotherUsername, anotherPassword);
        Assertions.assertFalse(loginResponse.isError(), "Another user login should succeed");
        String anotherToken = loginResponse.getData();

        // Try to get messages for a different user
        Response<List<MessageDTO>> messagesResponse = bridge.getUserMessages(
                unauthorizedUsername,  // Trying to get messages for a different user
                anotherToken          // Using another user's token
        );

        // Verify response indicates an error
        Assertions.assertNotNull(messagesResponse, "Response should not be null");
        Assertions.assertTrue(messagesResponse.isError(),
                "Response should indicate an error for unauthorized access");
        Assertions.assertNotNull(messagesResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void loggedOutUserGetMessagesTest() {
        // First, send a message to create some data
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        Response<MessageDTO> sendResponse = bridge.sendMessage(
                unauthorizedUsername,
                unauthorizedToken,
                messageRequest
        );
        Assertions.assertFalse(sendResponse.isError(), "Message sending should succeed");

        // Log out the user
        Response<String> logoutResponse = bridge.logout(unauthorizedUsername, unauthorizedToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Try to get messages while logged out
        Response<List<MessageDTO>> messagesResponse = bridge.getUserMessages(
                unauthorizedUsername,
                unauthorizedToken  // This token is now invalid
        );

        // Verify response indicates an error
        Assertions.assertNotNull(messagesResponse, "Response should not be null");
        Assertions.assertTrue(messagesResponse.isError(),
                "Response should indicate an error for logged out user");
        Assertions.assertNotNull(messagesResponse.getErrorMessage(), "Error message should not be null");
    }
}