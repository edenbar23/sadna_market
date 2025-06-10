package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.DummyExternalAPIConfig;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
import com.sadna_market.market.InfrastructureLayer.Payment.MockExternalPaymentAPI;
import com.sadna_market.market.InfrastructureLayer.Supply.PickupDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")  // This activates the test profile
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class UserTests {
    @MockBean
    private MockExternalPaymentAPI mockExternalPaymentAPI;
    @Autowired
    private Bridge bridge;
    ObjectMapper objectMapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(UserTests.class.getName());
    // Test data
    private UUID storeId;
    private UUID productId;
    private static final int PRODUCT_QUANTITY = 2;
    private static final String STORE_NAME = "Test Store";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_CATEGORY = "Electronics";
    private static final String PRODUCT_DESCRIPTION = "A test product for acceptance testing";
    private static final double PRODUCT_PRICE = 99.99;
    private static final int STORE_PRODUCT_QUANTITY = 10; // Total quantity available in store

    // User credentials
    private String testUsername;
    private String testPassword;
    private String testToken;  // JWT token for the test user

    // Store owner credentials
    private String ownerUsername;
    private String ownerPassword;
    private String ownerToken;

    @BeforeEach
    void setup() {
        // Create a regular user for testing
        testUsername = "testuser";
        testPassword = "Password123!";
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testUsername + "@example.com",
                "Test",
                "User"
        );
        Response<String> registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "Test user registration should succeed");

        // Login the test user to get a JWT token
        Response<String> loginResponse = bridge.loginUser(testUsername, testPassword);
        Assertions.assertFalse(loginResponse.isError(), "Test user login should succeed");

        // Extract the JWT token from the response
        testToken = loginResponse.getData();
        Assertions.assertNotNull(testToken, "JWT token should not be null");
        Assertions.assertFalse(testToken.isEmpty(), "JWT token should not be empty");

        // Create a store owner user for setting up test environment
        ownerUsername = "storeowner";
        ownerPassword = "Password123!";
        RegisterRequest ownerRegisterRequest = new RegisterRequest(
                ownerUsername,
                ownerPassword,
                ownerUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response<String> ownerRegisterResponse = bridge.registerUser(ownerRegisterRequest);
        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");

        // Login the store owner to get a JWT token
        Response<String> ownerLoginResponse = bridge.loginUser(ownerUsername, ownerPassword);
        Assertions.assertFalse(ownerLoginResponse.isError(), "Store owner login should succeed");

        // Extract the store owner's JWT token
        ownerToken = ownerLoginResponse.getData();
        Assertions.assertNotNull(ownerToken, "Owner JWT token should not be null");
        Assertions.assertFalse(ownerToken.isEmpty(), "Owner JWT token should not be empty");

        // Create a store
        StoreRequest storeRequest = new StoreRequest(
                STORE_NAME,
                "A test store for acceptance testing",
                "123 Test Street, Test City",
                "teststore@example.com",
                "555-123-4567",
                ownerUsername
        );

        Response<?> createStoreResponse = bridge.createStore(
                ownerUsername,
                ownerToken,
                storeRequest
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        try {
            JsonNode rootNode = objectMapper.readTree(objectMapper.writeValueAsString(createStoreResponse.getData()));
            String storeIdStr = rootNode.get("storeId").asText();
            storeId = UUID.fromString(storeIdStr);
            logger.info("Store ID extracted from response: " + storeId);
        } catch (Exception e) {
            logger.info("Failed to extract store ID: " + e.getMessage());
            logger.info("Response was: " + createStoreResponse.getData());
            throw new AssertionError("Store ID extraction failed", e);
        }
        // Create a ProductRequest object for adding a product
        ProductRequest productRequest = new ProductRequest(
                null, // No product ID for new product
                PRODUCT_NAME,
                PRODUCT_CATEGORY,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE
        );

        // Add a product to the store with the correct parameters
        Response<String> addProductResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                productRequest,
                STORE_PRODUCT_QUANTITY  // Adding items to inventory
        );
        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");

        // Extract product ID from response
        try {
            // The response is just the UUID string
            String uuidString = addProductResponse.getData();
            productId = UUID.fromString(uuidString);
            logger.info("Product ID extracted from response: " + productId);
        } catch (Exception e) {
            logger.error("Failed to extract product ID from response: " + e.getMessage());
            logger.info("Response was: " + addProductResponse.getData());
            throw new AssertionError("Product ID extraction failed", e);
        }
    }

    @BeforeEach
    void setupTestAPI() {
        mockExternalPaymentAPI.setSimulateFailure(false);
        mockExternalPaymentAPI.setSimulateUnavailable(false);
    }

    @AfterEach
    void tearDown() {
        // Clear system state after each test
        bridge.clear();
    }

    // CART ADDITION TESTS

    @Test
        //@DisplayName("Valid product addition to user cart")
    void validProductAdditionToCartTest() {
        // Add product to cart with valid quantity
        Response<String> response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);

        // Verify response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Adding valid product to cart should succeed");
        Assertions.assertNotNull(response.getData(), "Response should contain cart data");

        // Optionally verify cart contents through viewUserCart
        Response<CartDTO> viewCartResponse = bridge.viewUserCart(testUsername, testToken);
        Assertions.assertFalse(viewCartResponse.isError(), "Viewing cart should succeed");
    }

    @Test
        //@DisplayName("Invalid product addition to user cart - negative quantity")
    void negativeQuantityProductAdditionTest() {
        // Try to add product with negative quantity
        int negativeQuantity = -1;
        Response<String> response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, negativeQuantity);

        // Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Adding product with negative quantity should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid product addition to user cart - quantity exceeds inventory")
    void excessiveQuantityProductAdditionTest() {
        // Try to add more products than available in inventory
        int excessiveQuantity = STORE_PRODUCT_QUANTITY + 5; // 5 more than available
        Response<String> response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, excessiveQuantity);

        // Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Adding more products than available should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid product addition to user cart - logged out user")
    void loggedOutUserProductAdditionTest() {
        // First log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Try to add product after logout
        Response<String> response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);

        // Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Adding product when logged out should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
    }

    // CART UPDATE TESTS

    @Test
        //@DisplayName("Valid update of product quantity in user cart")
    void validCartUpdateTest() {
        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Update the quantity
        int newQuantity = 4; // Different from initial quantity
        Response<CartDTO> updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, productId, newQuantity);

        // Verify response
        Assertions.assertNotNull(updateResponse, "Response should not be null");
        Assertions.assertFalse(updateResponse.isError(), "Updating cart with valid quantity should succeed");
        Assertions.assertNotNull(updateResponse.getData(), "Response should contain updated cart data");
    }

    @Test
        //@DisplayName("Invalid update of product quantity in user cart - negative quantity")
    void negativeQuantityCartUpdateTest() {
        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Try to update with negative quantity
        int negativeQuantity = -3;
        Response<CartDTO> updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, productId, negativeQuantity);

        // Verify response indicates an error
        Assertions.assertNotNull(updateResponse, "Response should not be null");
        Assertions.assertTrue(updateResponse.isError(), "Updating cart with negative quantity should fail");
        Assertions.assertNotNull(updateResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid update of product quantity in user cart - product not in cart")
    void nonExistentProductCartUpdateTest() {
        // Generate a random product ID that doesn't exist in the cart
        UUID nonExistentProductId = UUID.randomUUID();

        // Try to update a product that isn't in the cart
        Response<CartDTO> updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, nonExistentProductId, 5);

        // Verify response indicates an error
        Assertions.assertNotNull(updateResponse, "Response should not be null");
        Assertions.assertTrue(updateResponse.isError(), "Updating non-existent product should fail");
        Assertions.assertNotNull(updateResponse.getErrorMessage(), "Response should contain error message");
    }

    // PRODUCT REMOVAL TESTS

    @Test
        //@DisplayName("Valid removal of product from user cart")
    void validProductRemovalTest() {
        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Remove the product
        Response<CartDTO> removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, productId);

        // Verify response
        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertFalse(removeResponse.isError(), "Removing existing product should succeed");
        Assertions.assertNotNull(removeResponse.getData(), "Response should contain updated cart data");
    }

    @Test
        //@DisplayName("Invalid removal of product from user cart - product not in cart")
    void nonExistentProductRemovalTest() {
        // Generate a random product ID that doesn't exist in the cart
        UUID nonExistentProductId = UUID.randomUUID();

        // Try to remove a product that isn't in the cart
        Response<CartDTO> removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, nonExistentProductId);

        // Verify response indicates an error
        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(), "Removing non-existent product should fail");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid removal of product from user cart - logged out user")
    void loggedOutUserProductRemovalTest() {
        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Try to remove product after logout
        Response<CartDTO> removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, productId);

        // Verify response indicates an error
        Assertions.assertNotNull(removeResponse, "Response should not be null");
        Assertions.assertTrue(removeResponse.isError(), "Removing product when logged out should fail");
        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Response should contain error message");
    }

    // CART PURCHASE TESTS

        // External Payment System
    @Test
    void testFailsWhenPaymentSystemDown() throws ExternalAPIException {
        Mockito.when(mockExternalPaymentAPI.sendCreditCardPayment(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyDouble()
        )).thenReturn(-1);
    
        // Add to cart first
        bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
    
        CreditCardDTO creditCard = new CreditCardDTO("4111111111111111", "John Doe", "12/25", "123");
        PickupDTO pickup = new PickupDTO("Test Store Location", "Pickup123");
    
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickup);
    
        Response<CheckoutResultDTO> response = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);
    
        Assertions.assertTrue(response.isError(), "Expected failure due to payment system down");
        Assertions.assertTrue(response.getErrorMessage().toLowerCase().contains("external"));
    }
        
    @Test
    void testFailsWhenPaymentServiceThrowsException() throws Exception {
        // Simulate a network or processing exception
        Mockito.when(mockExternalPaymentAPI.sendCreditCardPayment(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyDouble()
        )).thenThrow(new ExternalAPIException("Payment system down"));

        CheckoutRequest checkoutRequest = new CheckoutRequest();

        Response<CheckoutResultDTO> result = bridge.processUserCheckout(
                testUsername, testToken, checkoutRequest)
        ;

        Assertions.assertTrue(result.isError());
    }

    @Test
        //@DisplayName("Valid purchase of user cart")
    void validCartPurchaseTest() {
        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");

        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);


        // Verify response
        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
        Assertions.assertFalse(purchaseResponse.isError(), "Cart purchase should succeed");
        Assertions.assertNotNull(purchaseResponse.getData(), "Response should contain purchase confirmation");
    }

    @Test
        //@DisplayName("Invalid purchase of user cart - empty cart")
    void emptyCartPurchaseTest() {
        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);


        // Verify response indicates an error
        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
        Assertions.assertTrue(purchaseResponse.isError(), "Purchasing empty cart should fail");
        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid purchase of user cart - invalid credit card")
    void invalidCreditCardPurchaseTest() {
        // Create an invalid credit card number
        CreditCardDTO creditCard = new CreditCardDTO(
                "John Doe",
                "4111111111111111",
                "15/25",
                "123"
        );

        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);


        // Verify response indicates an error
        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
        Assertions.assertTrue(purchaseResponse.isError(), "Purchase with invalid credit card should fail");
        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Invalid purchase of user cart - logged out user")
    void loggedOutUserPurchaseTest() {
        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "John Doe",
                "4111111111111111",
                "12/25",
                "123"
        );

        // First add product to cart
        Response<String> addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");

        // Log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);


        // Verify response indicates an error
        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
        Assertions.assertTrue(purchaseResponse.isError(), "Purchase when logged out should fail");
        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
    }

    // SEARCH TESTS

    @Test
        //@DisplayName("User searches for a product that exists in store")
    void searchExistingProductTest() {
        // Create a search request for the product we added in setup
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName(PRODUCT_NAME);
        searchRequest.setCategory(PRODUCT_CATEGORY);

        // Search for the product
        Response<List<ProductDTO>> searchResponse = bridge.searchProduct(searchRequest);

        // Verify the response
        Assertions.assertNotNull(searchResponse, "Search response should not be null");
        Assertions.assertFalse(searchResponse.isError(), "Search for existing product should succeed");
        Assertions.assertNotNull(searchResponse.getData(), "Response should contain search results");

        // Verify the search results contain our product
        List<ProductDTO> searchResults = searchResponse.getData();
        Assertions.assertFalse(searchResults.isEmpty(), "Search results should not be empty");

        // Try to find our product in the results
        boolean productFound = false;
        for (ProductDTO product : searchResults) {
            if (product.getProductId().equals(productId)) {
                productFound = true;

                // Verify product details
                Assertions.assertEquals(PRODUCT_NAME, product.getName(), "Product name should match");
                Assertions.assertEquals(PRODUCT_CATEGORY, product.getCategory(), "Product category should match");
                Assertions.assertEquals(PRODUCT_PRICE, product.getPrice(), "Product price should match");
                break;
            }
        }

        Assertions.assertTrue(productFound, "The specific product should be found in search results");
    }

    @Test
        //@DisplayName("User searches for a product that doesn't exist in store")
    void searchNonExistingProductTest() {
        // Create a search request for a product that doesn't exist
        String nonExistingProductName = "NonExistingProduct" + UUID.randomUUID().toString();
        String nonExistingCategory = "NonExistingCategory";

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName(nonExistingProductName);
        searchRequest.setCategory(nonExistingCategory);

        // Search for the non-existing product
        Response<List<ProductDTO>> searchResponse = bridge.searchProduct(searchRequest);

        // Verify the response - the search itself should succeed but return empty results
        Assertions.assertNotNull(searchResponse, "Search response should not be null");
        Assertions.assertFalse(searchResponse.isError(), "Search operation should succeed even for non-existing products");
        Assertions.assertNotNull(searchResponse.getData(), "Response should contain search results (empty)");

        // The results should be empty
        List<ProductDTO> searchResults = searchResponse.getData();
        Assertions.assertTrue(searchResults.isEmpty(), "Search results should be an empty array");
    }

    @Test
        //@DisplayName("User searches with partial product name that exists")
    void searchPartialProductNameTest() {
        // Assume our product name is something like "Test Product"
        // We'll search for just "Test" to test partial matching
        String partialName = PRODUCT_NAME.substring(0, 4); // Get first few characters

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName(partialName);
        // Leave category blank to broaden the search

        // Search with partial name
        Response<List<ProductDTO>> searchResponse = bridge.searchProduct(searchRequest);

        // Verify the response
        Assertions.assertNotNull(searchResponse, "Search response should not be null");
        Assertions.assertFalse(searchResponse.isError(), "Search with partial name should succeed");
        Assertions.assertNotNull(searchResponse.getData(), "Response should contain search results");

        // Verify results contain our product
        List<ProductDTO> searchResults = searchResponse.getData();
        Assertions.assertFalse(searchResults.isEmpty(), "Search results should not be empty");

        // Check if our product is in the results
        boolean productFound = false;
        for (ProductDTO product : searchResults) {
            if (product.getProductId().equals(productId)) {
                productFound = true;
                break;
            }
        }

        Assertions.assertTrue(productFound, "Partial name search should find our product");
    }

    @Test
        //@DisplayName("User searches with price range filter")
    void searchWithPriceRangeTest() {
        // Create a search request with a price range that includes our product
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        // Set price range around our product price
        searchRequest.setMinPrice(PRODUCT_PRICE - 10);
        searchRequest.setMaxPrice(PRODUCT_PRICE + 10);

        // Search with price range
        Response<List<ProductDTO>> searchResponse = bridge.searchProduct(searchRequest);

        // Verify the response
        Assertions.assertNotNull(searchResponse, "Search response should not be null");
        Assertions.assertFalse(searchResponse.isError(), "Search with price range should succeed");
        Assertions.assertNotNull(searchResponse.getData(), "Response should contain search results");

        // Verify results contain our product
        List<ProductDTO> searchResults = searchResponse.getData();
        Assertions.assertFalse(searchResults.isEmpty(), "Search results should not be empty");

        // Check if our product is in the results
        boolean productFound = false;
        for (ProductDTO product : searchResults) {
            if (product.getProductId().equals(productId)) {
                productFound = true;
                break;
            }
        }

        Assertions.assertTrue(productFound, "Price range search should find our product");

        // Now search with a price range that excludes our product
        ProductSearchRequest exclusionSearchRequest = new ProductSearchRequest();
        exclusionSearchRequest.setMinPrice(PRODUCT_PRICE + 100); // Way above our product price
        exclusionSearchRequest.setMaxPrice(PRODUCT_PRICE + 200);

        Response<List<ProductDTO>> exclusionResponse = bridge.searchProduct(exclusionSearchRequest);

        // Verify this response doesn't include our product
        Assertions.assertNotNull(exclusionResponse, "Exclusion search response should not be null");
        Assertions.assertFalse(exclusionResponse.isError(), "Exclusion search should succeed");

        List<ProductDTO> exclusionResults = exclusionResponse.getData();

        // Verify our product is not in the results
        boolean productNotFound = true;
        for (ProductDTO product : exclusionResults) {
            if (product.getProductId().equals(productId)) {
                productNotFound = false;
                break;
            }
        }

        Assertions.assertTrue(productNotFound, "Exclusion price range search should not find our product");
    }

    // RATING TESTS

    @Test
        //@DisplayName("User successfully rates an existing product")
    void successfulProductRatingTest() {
        // First add the product to cart and purchase it
        Response<String> addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");

        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);

        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");

        // Create a rating request
        int rating = 4; // 4 out of 5 stars
        ProductRateRequest rateRequest = new ProductRateRequest(
                productId,
                testUsername,
                storeId,
                rating
        );

        // Submit the rating
        Response<ProductRatingDTO> rateResponse = bridge.rateProduct(testToken, rateRequest);

        // Verify the response
        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
        Assertions.assertFalse(rateResponse.isError(), "Rating an existing product should succeed");
        Assertions.assertNotNull(rateResponse.getData(), "Response should contain rating confirmation");
    }

    @Test
        //@DisplayName("User fails to rate a non-existent product")
    void rateNonExistentProductTest() {
        // Generate a random UUID for a non-existent product
        UUID nonExistentProductId = UUID.randomUUID();

        // Create a rating request for a non-existent product
        int rating = 5;
        ProductRateRequest rateRequest = new ProductRateRequest(
                nonExistentProductId,
                testUsername,
                storeId,
                rating
        );

        // Attempt to submit the rating
        Response<ProductRatingDTO> rateResponse = bridge.rateProduct(testToken, rateRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
        Assertions.assertTrue(rateResponse.isError(), "Rating a non-existent product should fail");
        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Logged out user fails to rate a product")
    void loggedOutUserRatingTest() {
        // First, log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Create a rating request
        int rating = 3;
        ProductRateRequest rateRequest = new ProductRateRequest(
                productId,
                testUsername,
                storeId,
                rating
        );

        // Attempt to submit the rating while logged out
        Response<ProductRatingDTO> rateResponse = bridge.rateProduct(testToken, rateRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
        Assertions.assertTrue(rateResponse.isError(), "Rating while logged out should fail");
        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("User tries to rate a product with an invalid rating value")
    void invalidRatingValueTest() {
        // Create a rating request with an invalid rating value (outside 1-5 range)
        int invalidRating = 10; // Assuming valid ratings are 1-5
        ProductRateRequest rateRequest = new ProductRateRequest(
                productId,
                testUsername,
                storeId,
                invalidRating
        );

        // Attempt to submit the invalid rating
        Response<ProductRatingDTO> rateResponse = bridge.rateProduct(testToken, rateRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
        Assertions.assertTrue(rateResponse.isError(), "Rating with invalid value should fail");
        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
        //@DisplayName("Logged in user successfully retrieves order history")
    void loggedInUserOrderHistoryTest() {
        // First, add a product to cart and make a purchase to have order history
        Response<String> addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");

        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");


        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);

        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");

        // Now retrieve order history
        Response<List<UUID>> historyResponse = bridge.getOrdersHistory(testUsername, testToken);

        // Verify the response
        Assertions.assertNotNull(historyResponse, "Order history response should not be null");
        Assertions.assertFalse(historyResponse.isError(), "Retrieving order history as logged in user should succeed");
        Assertions.assertNotNull(historyResponse.getData(), "Response should contain order history data");

        // Verify that the order history is not empty and contains our order
        List<UUID> orderHistory = historyResponse.getData();
        Assertions.assertFalse(orderHistory.isEmpty(), "Order history should not be empty");
    }

    @Test
        //@DisplayName("Logged out user fails to retrieve order history")
    void loggedOutUserOrderHistoryTest() {
        // First, add a product to cart and make a purchase to have order history
        Response<String> addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");

        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");


        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);

        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);

        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");

        // Log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Attempt to retrieve order history while logged out
        Response<List<UUID>> historyResponse = bridge.getOrdersHistory(testUsername, testToken);

        // Verify the response indicates an error
        Assertions.assertNotNull(historyResponse, "Order history response should not be null");
        Assertions.assertTrue(historyResponse.isError(), "Retrieving order history while logged out should fail");
        Assertions.assertNotNull(historyResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
    void successfulMessageSendTest() {
        // Create a message request
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "Hello, I have a question about your products."
        );

        // Send the message
        Response<MessageDTO> response = bridge.sendMessage(testUsername, testToken, messageRequest);

        // Verify the response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Message sending should succeed");
        Assertions.assertNotNull(response.getData(), "Response should contain message data");

    }

    @Test
    void sendMessageToNonExistingStoreTest() {
        // Generate a random UUID for a non-existent store
        UUID nonExistentStoreId = UUID.randomUUID();

        // Create a message request to a non-existent store
        MessageRequest messageRequest = new MessageRequest(
                nonExistentStoreId,
                "This message is going to a non-existent store."
        );

        // Attempt to send the message
        Response<MessageDTO> response = bridge.sendMessage(testUsername, testToken, messageRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Sending message to non-existent store should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
    }

    @Test
    void loggedOutUserMessageSendTest() {
        // First, log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Create a message request
        MessageRequest messageRequest = new MessageRequest(
                storeId,
                "This message is being sent while logged out."
        );

        // Attempt to send the message while logged out
        Response<MessageDTO> response = bridge.sendMessage(testUsername, testToken, messageRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Sending message while logged out should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");

    }

    @Test
    void successfulProductReviewTest() {
        // First add the product to cart and purchase it to establish a purchase history
        Response<String> addToCartResponse = bridge.addProductToUserCart(
                testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");

        // Create a credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
        PickupDTO pickupMethod = new PickupDTO("Test Store Location", "Pickup123");

        // Create checkout request
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setShippingAddress("123 Test Street, Test City");
        checkoutRequest.setSupplyMethod(pickupMethod);


        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processUserCheckout(testUsername, testToken, checkoutRequest);

        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");

        // Create a product review request
        String reviewText = "This product exceeded my expectations. Great value for money!";

        // Using UUID.randomUUID() for userId as it's not clear what this should be
        ProductReviewRequest reviewRequest = new ProductReviewRequest(
                testUsername,
                productId,
                storeId,
                reviewText
        );

        // Submit the review
        Response<ProductReviewDTO> reviewResponse = bridge.reviewProduct(testUsername, testToken, reviewRequest);

        // Verify the response
        Assertions.assertNotNull(reviewResponse, "Review response should not be null");
        Assertions.assertFalse(reviewResponse.isError(), "Reviewing an existing product should succeed");
        Assertions.assertNotNull(reviewResponse.getData(), "Response should contain review data");

    }

    @Test
    void reviewNonPurchasedProductTest() {
        // Create a review request for a product the user hasn't purchased
        ProductReviewRequest reviewRequest = new ProductReviewRequest(
                testUsername,
                productId,
                storeId,
                "This is a review for a product I haven't purchased."
        );

        // Attempt to submit the review
        Response<ProductReviewDTO> reviewResponse = bridge.reviewProduct(testUsername, testToken, reviewRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(reviewResponse, "Review response should not be null");
        Assertions.assertTrue(reviewResponse.isError(), "Reviewing a non-purchased product should fail");
        Assertions.assertNotNull(reviewResponse.getErrorMessage(), "Response should contain error message");

    }

    @Test
    void reviewNonExistentProductTest() {
        // Generate a random UUID for a non-existent product
        UUID nonExistentProductId = UUID.randomUUID();

        // Create a review request for a non-existent product
        ProductReviewRequest reviewRequest = new ProductReviewRequest(
                testUsername, // userId
                nonExistentProductId,
                storeId,
                "This review is for a product that doesn't exist."
        );

        // Attempt to submit the review
        Response<ProductReviewDTO> reviewResponse = bridge.reviewProduct(testUsername, testToken, reviewRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(reviewResponse, "Review response should not be null");
        Assertions.assertTrue(reviewResponse.isError(), "Reviewing a non-existent product should fail");
        Assertions.assertNotNull(reviewResponse.getErrorMessage(), "Response should contain error message");
    }

    @Test
    void loggedOutUserReviewTest() {
        // First, log out the user
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");

        // Create a review request
        ProductReviewRequest reviewRequest = new ProductReviewRequest(
                testUsername, // userId
                productId,
                storeId,
                "This review is being submitted while logged out."
        );

        // Attempt to submit the review while logged out
        Response<ProductReviewDTO> reviewResponse = bridge.reviewProduct(testUsername, testToken, reviewRequest);

        // Verify the response indicates an error
        Assertions.assertNotNull(reviewResponse, "Review response should not be null");
        Assertions.assertTrue(reviewResponse.isError(), "Reviewing while logged out should fail");
        Assertions.assertNotNull(reviewResponse.getErrorMessage(), "Response should contain error message");
    }
}