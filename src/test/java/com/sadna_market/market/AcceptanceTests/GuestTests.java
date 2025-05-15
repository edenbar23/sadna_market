//package com.sadna_market.market.AcceptanceTests;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sadna_market.market.ApplicationLayer.*;
//import com.sadna_market.market.ApplicationLayer.Requests.*;
//import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
//import org.junit.jupiter.api.*;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.util.UUID;
//
//@SpringBootTest
//public class GuestTests {
//    private Bridge bridge = new Bridge();
//    ObjectMapper objectMapper = new ObjectMapper();
//    private static final Logger logger = LoggerFactory.getLogger(GuestTests.class);
//    // Test data
//    private UUID storeId;
//    private UUID productId;
//    private static final int PRODUCT_QUANTITY = 2;
//    private static final String STORE_NAME = "Test Store";
//    private static final String PRODUCT_NAME = "Test Product";
//    private static final String PRODUCT_CATEGORY = "Electronics";
//    private static final String PRODUCT_DESCRIPTION = "A test product for acceptance testing";
//    private static final double PRODUCT_PRICE = 99.99;
//    private CartRequest cartReq;
//    private String dummyUsername;
//    private String dummyToken;
//
//    @BeforeEach
//    void setup() {
//        // Initialize a fresh cart for each test
//        cartReq = new CartRequest();
//
//        // Create a "dummy" user who will create a store and product
//        dummyUsername = "storeowner";
//        String dummyPassword = "Password123!";
//
//        // Register the dummy user
//        RegisterRequest registerRequest = new RegisterRequest(
//                dummyUsername,
//                dummyPassword,
//                dummyUsername + "@example.com",
//                "Store",
//                "Owner"
//        );
//        Response registerResponse = bridge.registerUser(registerRequest);
//        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");
//
//        // Login the dummy user
//        Response loginResponse = bridge.loginUser(dummyUsername, dummyPassword);
//        Assertions.assertFalse(loginResponse.isError(), "User login should succeed");
//        dummyToken = loginResponse.getJson();
//
//        // Create a store
//        StoreRequest storeRequest = new StoreRequest(
//                STORE_NAME,
//                "A test store for guest testing",
//                "123 Test Street, Test City",
//                "guesttest@example.com",
//                "555-123-4567",
//                dummyUsername
//        );
//
//        Response createStoreResponse = bridge.createStore(
//                dummyUsername,
//                dummyToken,
//                storeRequest
//        );
//        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");
//
//        // Extract store ID from response or use a random UUID if not available
//        try {
//            storeId = UUID.fromString(createStoreResponse.getJson());
//        } catch (Exception e) {
//            logger.info("Store ID not found in response, generating a new one.");
//            storeId = UUID.randomUUID();
//        }
//
//        logger.info("Store ID: " + storeId);
//        // Add a product to the store
//        ProductRequest productRequest = new ProductRequest(
//                UUID.randomUUID(),
//                PRODUCT_NAME,
//                PRODUCT_CATEGORY,
//                PRODUCT_DESCRIPTION,
//                PRODUCT_PRICE
//        );
//
//        int productQuantity = 20; // Setting inventory quantity
//
//        Response addProductResponse = bridge.addProductToStore(
//                dummyToken,
//                dummyUsername,
//                storeId,
//                productRequest,
//                productQuantity
//        );
//        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");
//
//        // Use the product ID from the request
//        productId = productRequest.getProductId();
//    }
//
//    @AfterEach
//    void tearDown() {
//        // Clear the system state after each test
//        bridge.clear();
//    }
//
//    @Test
//    @DisplayName("Guest should be able to search for and find an existing product")
//    void searchExistingProductTest() {
//        // Create a search request that should match our test product
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        searchRequest.setName(PRODUCT_NAME);
//        searchRequest.setCategory(PRODUCT_CATEGORY);
//
//        // Execute the search
//        Response response = bridge.searchProduct(searchRequest);
//
//        // Verify response basics
//        Assertions.assertNotNull(response, "Search response should not be null");
//        Assertions.assertFalse(response.isError(), "Search for existing product should succeed");
//        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");
//
//    }
//
//    @Test
//    @DisplayName("Guest search for non-existent product should return empty results")
//    void searchNonExistentProductTest() {
//        // Create a search request with a name that doesn't exist
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        searchRequest.setName("NonExistentProduct");
//
//        // Search for the product
//        Response response = bridge.searchProduct(searchRequest);
//
//        // Verify response
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertFalse(response.isError(), "Response should not indicate an error even for no results");
//        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should be able to add a product to their cart")
//    void addProductToGuestCartTest() {
//        // Add product to guest cart
//        Response response = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
//
//        // Verify response
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertFalse(response.isError(), "Adding product to cart should succeed");
//        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should not be able to add a product with quantity exceeding store inventory")
//    void addProductWithExcessiveQuantityToGuestCartTest() {
//        // Try to add with excessive quantity
//        int excessiveQuantity = 100;
//        Response response = bridge.addProductToGuestCart(cartReq, storeId, productId, excessiveQuantity);
//
//        // Verify error response
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Adding excessive quantity should fail");
//        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should be able to update the quantity of a product in their cart")
//    void updateProductQuantityInGuestCartTest() {
//        // First add a product to the cart
//        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");
//
//        // Update the quantity
//        int newQuantity = PRODUCT_QUANTITY + 3;
//        Response updateResponse = bridge.updateGuestCart(cartReq, storeId, productId, newQuantity);
//
//        // Verify response
//        Assertions.assertNotNull(updateResponse, "Update response should not be null");
//        Assertions.assertFalse(updateResponse.isError(), "Updating cart should succeed");
//        Assertions.assertNotNull(updateResponse.getJson(), "Update response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should be able to remove a product from their cart")
//    void removeProductFromGuestCartTest() {
//        // First add a product to the cart
//        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");
//
//        // Remove the product
//        Response removeResponse = bridge.removeProductFromGuestCart(cartReq, storeId, productId);
//
//        // Verify response
//        Assertions.assertNotNull(removeResponse, "Remove response should not be null");
//        Assertions.assertFalse(removeResponse.isError(), "Removing product should succeed");
//        Assertions.assertNotNull(removeResponse.getJson(), "Remove response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should receive an error when trying to remove a product not in their cart")
//    void removeNonExistentProductFromGuestCartTest() {
//        // Try to remove a non-existent product
//        UUID nonExistentProductId = UUID.randomUUID();
//        Response response = bridge.removeProductFromGuestCart(cartReq, storeId, nonExistentProductId);
//
//        // Verify error response
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Removing non-existent product should fail");
//        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should be able to purchase items in their cart")
//    void buyGuestCartTest() {
//        // Add a product to the cart
//        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");
//
//        // Create credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//
//        // Purchase the cart
//        Response purchaseResponse = bridge.buyGuestCart(cartReq, creditCard);
//
//        // Verify response
//        Assertions.assertNotNull(purchaseResponse, "Purchase response should not be null");
//        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");
//        Assertions.assertNotNull(purchaseResponse.getJson(), "Purchase response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should receive an error when trying to purchase an empty cart")
//    void buyEmptyGuestCartTest() {
//        // Cart is empty by default
//
//        // Create credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//
//        // Try to purchase empty cart
//        Response purchaseResponse = bridge.buyGuestCart(cartReq, creditCard);
//
//        // Verify error response
//        Assertions.assertNotNull(purchaseResponse, "Purchase response should not be null");
//        Assertions.assertTrue(purchaseResponse.isError(), "Purchasing empty cart should fail");
//        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Error message should not be null");
//    }
//
//    @Test
//    @DisplayName("Guest should be able to register as a new user")
//    void registerUserTest() {
//        // Create a unique username
//        String testUsername = "testuser" + UUID.randomUUID().toString().substring(0, 8);
//
//        // Create registration request
//        RegisterRequest registerRequest = new RegisterRequest(
//                testUsername,
//                "Password123!",
//                testUsername + "@example.com",
//                "Test",
//                "User"
//        );
//
//        // Register the user
//        Response response = bridge.registerUser(registerRequest);
//
//        // Verify response
//        Assertions.assertNotNull(response, "Registration response should not be null");
//        Assertions.assertFalse(response.isError(), "Registration should succeed");
//        Assertions.assertNotNull(response.getJson(), "Registration response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("User should be able to login with valid credentials")
//    void loginUserTest() {
//        // Create a unique username
//        String testUsername = "loginuser" + UUID.randomUUID().toString().substring(0, 8);
//        String testPassword = "Password123!";
//
//        // Register the user first
//        RegisterRequest registerRequest = new RegisterRequest(
//                testUsername,
//                testPassword,
//                testUsername + "@example.com",
//                "Login",
//                "User"
//        );
//        Response registerResponse = bridge.registerUser(registerRequest);
//        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");
//
//        // Login with valid credentials
//        Response loginResponse = bridge.loginUser(testUsername, testPassword);
//
//        // Verify response
//        Assertions.assertNotNull(loginResponse, "Login response should not be null");
//        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");
//        Assertions.assertNotNull(loginResponse.getJson(), "Login response JSON should not be null");
//    }
//
//    @Test
//    @DisplayName("User should not be able to login with invalid credentials")
//    void loginUserWithInvalidCredentialsTest() {
//        // Try to login with credentials that don't exist
//        Response loginResponse = bridge.loginUser(
//                "nonexistentuser" + UUID.randomUUID().toString(),
//                "InvalidPassword123!"
//        );
//
//        // Verify error response
//        Assertions.assertNotNull(loginResponse, "Login response should not be null");
//        Assertions.assertTrue(loginResponse.isError(), "Login with invalid credentials should fail");
//        Assertions.assertNotNull(loginResponse.getErrorMessage(), "Error message should not be null");
//    }
//}