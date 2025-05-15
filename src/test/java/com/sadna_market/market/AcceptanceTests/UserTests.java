//package com.sadna_market.market.AcceptanceTests;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sadna_market.market.ApplicationLayer.*;
//import com.sadna_market.market.ApplicationLayer.Requests.*;
//import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
//import org.junit.jupiter.api.*;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.UUID;
//import java.util.logging.Logger;
//
//@SpringBootTest
//public class UserTests {
//    private Bridge bridge = new Bridge();
//    ObjectMapper objectMapper = new ObjectMapper();
//    private Logger logger = Logger.getLogger(UserTests.class.getName());
//    // Test data
//    private UUID storeId;
//    private UUID productId;
//    private static final int PRODUCT_QUANTITY = 2;
//    private static final String STORE_NAME = "Test Store";
//    private static final String PRODUCT_NAME = "Test Product";
//    private static final String PRODUCT_CATEGORY = "Electronics";
//    private static final String PRODUCT_DESCRIPTION = "A test product for acceptance testing";
//    private static final double PRODUCT_PRICE = 99.99;
//    private static final int STORE_PRODUCT_QUANTITY = 10; // Total quantity available in store
//
//    // User credentials
//    private String testUsername;
//    private String testPassword;
//    private String testToken;  // JWT token for the test user
//
//    // Store owner credentials
//    private String ownerUsername;
//    private String ownerPassword;
//    private String ownerToken;
//
//    @BeforeEach
//    void setup() {
//        // Create a regular user for testing
//        testUsername = "testuser";
//        testPassword = "Password123!";
//        RegisterRequest registerRequest = new RegisterRequest(
//                testUsername,
//                testPassword,
//                testUsername + "@example.com",
//                "Test",
//                "User"
//        );
//        Response registerResponse = bridge.registerUser(registerRequest);
//        Assertions.assertFalse(registerResponse.isError(), "Test user registration should succeed");
//
//        // Login the test user to get a JWT token
//        Response loginResponse = bridge.loginUser(testUsername, testPassword);
//        Assertions.assertFalse(loginResponse.isError(), "Test user login should succeed");
//
//        // Extract the JWT token from the response
//        testToken = loginResponse.getJson();
//        Assertions.assertNotNull(testToken, "JWT token should not be null");
//        Assertions.assertFalse(testToken.isEmpty(), "JWT token should not be empty");
//
//        // Create a store owner user for setting up test environment
//        ownerUsername = "storeowner";
//        ownerPassword = "Password123!";
//        RegisterRequest ownerRegisterRequest = new RegisterRequest(
//                ownerUsername,
//                ownerPassword,
//                ownerUsername + "@example.com",
//                "Store",
//                "Owner"
//        );
//        Response ownerRegisterResponse = bridge.registerUser(ownerRegisterRequest);
//        Assertions.assertFalse(ownerRegisterResponse.isError(), "Store owner registration should succeed");
//
//        // Login the store owner to get a JWT token
//        Response ownerLoginResponse = bridge.loginUser(ownerUsername, ownerPassword);
//        Assertions.assertFalse(ownerLoginResponse.isError(), "Store owner login should succeed");
//
//        // Extract the store owner's JWT token
//        ownerToken = ownerLoginResponse.getJson();
//        Assertions.assertNotNull(ownerToken, "Owner JWT token should not be null");
//        Assertions.assertFalse(ownerToken.isEmpty(), "Owner JWT token should not be empty");
//
//        // Create a store
//        StoreRequest storeRequest = new StoreRequest(
//                STORE_NAME,
//                "A test store for acceptance testing",
//                "123 Test Street, Test City",
//                "teststore@example.com",
//                "555-123-4567",
//                ownerUsername
//        );
//
//        Response createStoreResponse = bridge.createStore(
//                ownerUsername,
//                ownerToken,
//                storeRequest
//        );
//        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");
//
//        // Extract store ID from response
//        try {
//            storeId = UUID.fromString(createStoreResponse.getJson());
//
//        } catch (Exception e) {
//            // If parsing fails, use a random UUID for testing
//            storeId = UUID.randomUUID();
//        }
//
//        // Create a ProductRequest object for adding a product
//        ProductRequest productRequest = new ProductRequest(
//                UUID.randomUUID(),  // Generate a random UUID for the new product
//                PRODUCT_NAME,
//                PRODUCT_CATEGORY,
//                PRODUCT_DESCRIPTION,
//                PRODUCT_PRICE
//        );
//
//        // Add a product to the store with the correct parameters
//        Response addProductResponse = bridge.addProductToStore(
//                ownerToken,
//                ownerUsername,
//                storeId,
//                productRequest,
//                STORE_PRODUCT_QUANTITY  // Adding items to inventory
//        );
//        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");
//
//        // Use the UUID we generated for the product
//        productId = productRequest.getProductId();
//    }
//
//    @AfterEach
//    void tearDown() {
//        // Clear system state after each test
//        bridge.clear();
//    }
//
//    // CART ADDITION TESTS
//
//    @Test
//    @DisplayName("Valid product addition to user cart")
//    void validProductAdditionToCartTest() {
//        // Add product to cart with valid quantity
//        Response response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//
//        // Verify response
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertFalse(response.isError(), "Adding valid product to cart should succeed");
//        Assertions.assertNotNull(response.getJson(), "Response should contain cart data");
//
//        // Optionally verify cart contents through viewUserCart
//        Response viewCartResponse = bridge.viewUserCart(testUsername, testToken);
//        Assertions.assertFalse(viewCartResponse.isError(), "Viewing cart should succeed");
//
//    }
//
//    @Test
//    @DisplayName("Invalid product addition to user cart - negative quantity")
//    void negativeQuantityProductAdditionTest() {
//        // Try to add product with negative quantity
//        int negativeQuantity = -1;
//        Response response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, negativeQuantity);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Adding product with negative quantity should fail");
//        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid product addition to user cart - quantity exceeds inventory")
//    void excessiveQuantityProductAdditionTest() {
//        // Try to add more products than available in inventory
//        int excessiveQuantity = STORE_PRODUCT_QUANTITY + 5; // 5 more than available
//        Response response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, excessiveQuantity);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Adding more products than available should fail");
//        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid product addition to user cart - logged out user")
//    void loggedOutUserProductAdditionTest() {
//        // First log out the user
//        Response logoutResponse = bridge.logout(testUsername, testToken);
//        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
//
//        // Try to add product after logout
//        Response response = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(response, "Response should not be null");
//        Assertions.assertTrue(response.isError(), "Adding product when logged out should fail");
//        Assertions.assertNotNull(response.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    // CART UPDATE TESTS
//
//    @Test
//    @DisplayName("Valid update of product quantity in user cart")
//    void validCartUpdateTest() {
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Update the quantity
//        int newQuantity = 4; // Different from initial quantity
//        Response updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, productId, newQuantity);
//
//        // Verify response
//        Assertions.assertNotNull(updateResponse, "Response should not be null");
//        Assertions.assertFalse(updateResponse.isError(), "Updating cart with valid quantity should succeed");
//        Assertions.assertNotNull(updateResponse.getJson(), "Response should contain updated cart data");
//
//    }
//
//    @Test
//    @DisplayName("Invalid update of product quantity in user cart - negative quantity")
//    void negativeQuantityCartUpdateTest() {
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Try to update with negative quantity
//        int negativeQuantity = -3;
//        Response updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, productId, negativeQuantity);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(updateResponse, "Response should not be null");
//        Assertions.assertTrue(updateResponse.isError(), "Updating cart with negative quantity should fail");
//        Assertions.assertNotNull(updateResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid update of product quantity in user cart - product not in cart")
//    void nonExistentProductCartUpdateTest() {
//        // Generate a random product ID that doesn't exist in the cart
//        UUID nonExistentProductId = UUID.randomUUID();
//
//        // Try to update a product that isn't in the cart
//        Response updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, nonExistentProductId, 5);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(updateResponse, "Response should not be null");
//        Assertions.assertTrue(updateResponse.isError(), "Updating non-existent product should fail");
//        Assertions.assertNotNull(updateResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    // PRODUCT REMOVAL TESTS
//
//    @Test
//    @DisplayName("Valid removal of product from user cart")
//    void validProductRemovalTest() {
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Remove the product
//        Response removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, productId);
//
//        // Verify response
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertFalse(removeResponse.isError(), "Removing existing product should succeed");
//        Assertions.assertNotNull(removeResponse.getJson(), "Response should contain updated cart data");
//
//    }
//
//    @Test
//    @DisplayName("Invalid removal of product from user cart - product not in cart")
//    void nonExistentProductRemovalTest() {
//        // Generate a random product ID that doesn't exist in the cart
//        UUID nonExistentProductId = UUID.randomUUID();
//
//        // Try to remove a product that isn't in the cart
//        Response removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, nonExistentProductId);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(), "Removing non-existent product should fail");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid removal of product from user cart - logged out user")
//    void loggedOutUserProductRemovalTest() {
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Log out the user
//        Response logoutResponse = bridge.logout(testUsername, testToken);
//        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
//
//        // Try to remove product after logout
//        Response removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, productId);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(removeResponse, "Response should not be null");
//        Assertions.assertTrue(removeResponse.isError(), "Removing product when logged out should fail");
//        Assertions.assertNotNull(removeResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    // CART PURCHASE TESTS
//
//    @Test
//    @DisplayName("Valid purchase of user cart")
//    void validCartPurchaseTest() {
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Purchase the cart
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//
//        // Verify response
//        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
//        Assertions.assertFalse(purchaseResponse.isError(), "Cart purchase should succeed");
//        Assertions.assertNotNull(purchaseResponse.getJson(), "Response should contain purchase confirmation");
//
//    }
//
//    @Test
//    @DisplayName("Invalid purchase of user cart - empty cart")
//    void emptyCartPurchaseTest() {
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//
//        // Cart is empty by default since we haven't added anything
//
//        // Try to purchase the empty cart
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
//        Assertions.assertTrue(purchaseResponse.isError(), "Purchasing empty cart should fail");
//        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid purchase of user cart - invalid credit card")
//    void invalidCreditCardPurchaseTest() {
//        // Create an invalid credit card number
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "15/25",
//                "123"
//        );
//
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Try to purchase with invalid credit card
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
//        Assertions.assertTrue(purchaseResponse.isError(), "Purchase with invalid credit card should fail");
//        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Invalid purchase of user cart - logged out user")
//    void loggedOutUserPurchaseTest() {
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "15/25",
//                "123"
//        );
//
//        // First add product to cart
//        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addResponse.isError(), "Initial product addition should succeed");
//
//        // Log out the user
//        Response logoutResponse = bridge.logout(testUsername, testToken);
//        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
//
//        // Try to purchase after logout
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//
//        // Verify response indicates an error
//        Assertions.assertNotNull(purchaseResponse, "Response should not be null");
//        Assertions.assertTrue(purchaseResponse.isError(), "Purchase when logged out should fail");
//        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    // Add these tests to your UserTests class
//
//    @Test
//    @DisplayName("User searches for a product that exists in store")
//    void searchExistingProductTest() {
//        // Create a search request for the product we added in setup
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        searchRequest.setName(PRODUCT_NAME);
//        searchRequest.setCategory(PRODUCT_CATEGORY);
//
//        // Search for the product
//        Response searchResponse = bridge.searchProduct(searchRequest);
//
//        // Verify the response
//        Assertions.assertNotNull(searchResponse, "Search response should not be null");
//        Assertions.assertFalse(searchResponse.isError(), "Search for existing product should succeed");
//        Assertions.assertNotNull(searchResponse.getJson(), "Response should contain search results");
//
//        // Parse and verify the search results contain our product
//        String searchResultsJson = searchResponse.getJson();
//        Assertions.assertFalse(searchResultsJson.isEmpty(), "Search results should not be empty");
//
//        // Optional: Parse JSON and verify product details
//        try {
//            // This assumes the search results are returned as a JSON array of products
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode resultsArray = mapper.readTree(searchResultsJson);
//
//            // Check if array is not empty
//            Assertions.assertTrue(resultsArray.isArray() && resultsArray.size() > 0,
//                    "Search results should contain at least one product");
//
//            // Try to find our product in the results
//            boolean productFound = false;
//            for (JsonNode product : resultsArray) {
//                // Check if this is our product by matching ID and/or name
//                if (product.has("productId") &&
//                        product.get("productId").asText().equals(productId.toString())) {
//                    productFound = true;
//
//                    // Verify product details
//                    Assertions.assertEquals(PRODUCT_NAME, product.get("name").asText(),
//                            "Product name should match");
//                    Assertions.assertEquals(PRODUCT_CATEGORY, product.get("category").asText(),
//                            "Product category should match");
//                    Assertions.assertEquals(PRODUCT_PRICE, product.get("price").asDouble(),
//                            "Product price should match");
//
//                    break;
//                }
//            }
//
//            Assertions.assertTrue(productFound, "The specific product should be found in search results");
//
//        } catch (Exception e) {
//            // If JSON parsing fails, we'll just check that the product name is in the results
//            Assertions.assertTrue(searchResultsJson.contains(PRODUCT_NAME),
//                    "Search results should contain the product name");
//            Assertions.assertTrue(searchResultsJson.contains(productId.toString()),
//                    "Search results should contain the product ID");
//        }
//    }
//
//    @Test
//    @DisplayName("User searches for a product that doesn't exist in store")
//    void searchNonExistingProductTest() {
//        // Create a search request for a product that doesn't exist
//        String nonExistingProductName = "NonExistingProduct" + UUID.randomUUID().toString();
//        String nonExistingCategory = "NonExistingCategory";
//
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        searchRequest.setName(nonExistingProductName);
//        searchRequest.setCategory(nonExistingCategory);
//
//        // Search for the non-existing product
//        Response searchResponse = bridge.searchProduct(searchRequest);
//
//        // Verify the response - the search itself should succeed but return empty results
//        Assertions.assertNotNull(searchResponse, "Search response should not be null");
//        Assertions.assertFalse(searchResponse.isError(), "Search operation should succeed even for non-existing products");
//        Assertions.assertNotNull(searchResponse.getJson(), "Response should contain search results (empty)");
//
//        // Parse and verify the search results are empty or indicate no matches
//        String searchResultsJson = searchResponse.getJson();
//
//        try {
//            // This assumes the search results are returned as a JSON array of products
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode resultsArray = mapper.readTree(searchResultsJson);
//
//            // Check if array is empty
//            Assertions.assertTrue(resultsArray.isArray() && resultsArray.size() == 0,
//                    "Search results should be an empty array");
//
//        } catch (Exception e) {
//            // If JSON parsing fails, we'll just check that the response indicates empty results
//            Assertions.assertTrue(
//                    searchResultsJson.isEmpty() ||
//                            searchResultsJson.equals("[]") ||
//                            searchResultsJson.contains("no results") ||
//                            searchResultsJson.contains("not found"),
//                    "Search results should indicate no matches were found"
//            );
//        }
//    }
//
//    @Test
//    @DisplayName("User searches with partial product name that exists")
//    void searchPartialProductNameTest() {
//        // Assume our product name is something like "Test Product"
//        // We'll search for just "Test" to test partial matching
//        String partialName = PRODUCT_NAME.substring(0, 4); // Get first few characters
//
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        searchRequest.setName(partialName);
//        // Leave category blank to broaden the search
//
//        // Search with partial name
//        Response searchResponse = bridge.searchProduct(searchRequest);
//
//        // Verify the response
//        Assertions.assertNotNull(searchResponse, "Search response should not be null");
//        Assertions.assertFalse(searchResponse.isError(), "Search with partial name should succeed");
//        Assertions.assertNotNull(searchResponse.getJson(), "Response should contain search results");
//
//        // Verify results contain our product
//        String searchResultsJson = searchResponse.getJson();
//        Assertions.assertFalse(searchResultsJson.isEmpty(), "Search results should not be empty");
//
//        // Simple check that our product appears in results
//        Assertions.assertTrue(
//                searchResultsJson.contains(PRODUCT_NAME) ||
//                        searchResultsJson.contains(productId.toString()),
//                "Partial name search should find our product"
//        );
//    }
//
//    @Test
//    @DisplayName("User searches with price range filter")
//    void searchWithPriceRangeTest() {
//        // Create a search request with a price range that includes our product
//        ProductSearchRequest searchRequest = new ProductSearchRequest();
//        // Set price range around our product price
//        searchRequest.setMinPrice(PRODUCT_PRICE - 10);
//        searchRequest.setMaxPrice(PRODUCT_PRICE + 10);
//
//        // Search with price range
//        Response searchResponse = bridge.searchProduct(searchRequest);
//
//        // Verify the response
//        Assertions.assertNotNull(searchResponse, "Search response should not be null");
//        Assertions.assertFalse(searchResponse.isError(), "Search with price range should succeed");
//        Assertions.assertNotNull(searchResponse.getJson(), "Response should contain search results");
//
//        // Verify results contain our product
//        String searchResultsJson = searchResponse.getJson();
//        Assertions.assertFalse(searchResultsJson.isEmpty(), "Search results should not be empty");
//
//        // Simple check that our product appears in results
//        Assertions.assertTrue(
//                searchResultsJson.contains(PRODUCT_NAME) ||
//                        searchResultsJson.contains(productId.toString()),
//                "Price range search should find our product"
//        );
//
//        // Now search with a price range that excludes our product
//        ProductSearchRequest exclusionSearchRequest = new ProductSearchRequest();
//        exclusionSearchRequest.setMinPrice(PRODUCT_PRICE + 100); // Way above our product price
//        exclusionSearchRequest.setMaxPrice(PRODUCT_PRICE + 200);
//
//        Response exclusionResponse = bridge.searchProduct(exclusionSearchRequest);
//
//        // Verify this response doesn't include our product
//        Assertions.assertNotNull(exclusionResponse, "Exclusion search response should not be null");
//        Assertions.assertFalse(exclusionResponse.isError(), "Exclusion search should succeed");
//
//        String exclusionResultsJson = exclusionResponse.getJson();
//
//        // The results should not contain our product
//        Assertions.assertFalse(
//                exclusionResultsJson.contains(PRODUCT_NAME) ||
//                        exclusionResultsJson.contains(productId.toString()),
//                "Exclusion price range search should not find our product"
//        );
//    }
//
//
//    @Test
//    @DisplayName("User successfully rates an existing product")
//    void successfulProductRatingTest() {
//        // First add the product to cart and purchase it
//        Response addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");
//
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");
//
//        // Create a rating request
//        int rating = 4; // 4 out of 5 stars
//        ProductRateRequest rateRequest = new ProductRateRequest(
//                UUID.fromString(testUsername), // Using username as UUID for simplicity; adapt if your implementation is different
//                productId,
//                rating
//        );
//
//        // Submit the rating
//        Response rateResponse = bridge.rateProduct(testToken, rateRequest);
//
//        // Verify the response
//        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
//        Assertions.assertFalse(rateResponse.isError(), "Rating an existing product should succeed");
//        Assertions.assertNotNull(rateResponse.getJson(), "Response should contain rating confirmation");
//
//    }
//
//    @Test
//    @DisplayName("User fails to rate a non-existent product")
//    void rateNonExistentProductTest() {
//        // Generate a random UUID for a non-existent product
//        UUID nonExistentProductId = UUID.randomUUID();
//
//        // Create a rating request for a non-existent product
//        int rating = 5;
//        ProductRateRequest rateRequest = new ProductRateRequest(
//                UUID.fromString(testUsername), // Using username as UUID for simplicity; adapt if needed
//                nonExistentProductId,
//                rating
//        );
//
//        // Attempt to submit the rating
//        Response rateResponse = bridge.rateProduct(testToken, rateRequest);
//
//        // Verify the response indicates an error
//        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
//        Assertions.assertTrue(rateResponse.isError(), "Rating a non-existent product should fail");
//        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("Logged out user fails to rate a product")
//    void loggedOutUserRatingTest() {
//        // First, log out the user
//        Response logoutResponse = bridge.logout(testUsername, testToken);
//        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
//
//        // Create a rating request
//        int rating = 3;
//        ProductRateRequest rateRequest = new ProductRateRequest(
//                UUID.fromString(testUsername), // Using username as UUID for simplicity; adapt if needed
//                productId,
//                rating
//        );
//
//        // Attempt to submit the rating while logged out
//        Response rateResponse = bridge.rateProduct(testToken, rateRequest);
//
//        // Verify the response indicates an error
//        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
//        Assertions.assertTrue(rateResponse.isError(), "Rating while logged out should fail");
//        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//    @Test
//    @DisplayName("User tries to rate a product with an invalid rating value")
//    void invalidRatingValueTest() {
//        // Create a rating request with an invalid rating value (outside 1-5 range)
//        int invalidRating = 10; // Assuming valid ratings are 1-5
//        ProductRateRequest rateRequest = new ProductRateRequest(
//                UUID.fromString(testUsername), // Using username as UUID for simplicity; adapt if needed
//                productId,
//                invalidRating
//        );
//
//        // Attempt to submit the invalid rating
//        Response rateResponse = bridge.rateProduct(testToken, rateRequest);
//
//        // Verify the response indicates an error
//        Assertions.assertNotNull(rateResponse, "Rating response should not be null");
//        Assertions.assertTrue(rateResponse.isError(), "Rating with invalid value should fail");
//        Assertions.assertNotNull(rateResponse.getErrorMessage(), "Response should contain error message");
//    }
//    @Test
//    @DisplayName("Logged in user successfully retrieves order history")
//    void loggedInUserOrderHistoryTest() {
//        // First, add a product to cart and make a purchase to have order history
//        Response addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");
//
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );             Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");
//
//        // Now retrieve order history
//        Response historyResponse = bridge.getOrdersHistory(testUsername, testToken);
//
//        // Verify the response
//        Assertions.assertNotNull(historyResponse, "Order history response should not be null");
//        Assertions.assertFalse(historyResponse.isError(), "Retrieving order history as logged in user should succeed");
//        Assertions.assertNotNull(historyResponse.getJson(), "Response should contain order history data");
//
//        // Verify that the order history is not empty and contains our order
//        String historyJson = historyResponse.getJson();
//        Assertions.assertFalse(historyJson.isEmpty(), "Order history should not be empty");
//
//    }
//
//    @Test
//    @DisplayName("Logged out user fails to retrieve order history")
//    void loggedOutUserOrderHistoryTest() {
//        // First, add a product to cart and make a purchase to have order history
//        Response addToCartResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
//        Assertions.assertFalse(addToCartResponse.isError(), "Adding product to cart should succeed");
//
//        // Create a credit card for purchase
//        CreditCardDTO creditCard = new CreditCardDTO(
//                "John Doe",
//                "4111111111111111",
//                "12/25",
//                "123"
//        );
//        Response purchaseResponse = bridge.buyUserCart(testUsername, testToken, creditCard);
//        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");
//
//        // Log out the user
//        Response logoutResponse = bridge.logout(testUsername, testToken);
//        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
//
//        // Attempt to retrieve order history while logged out
//        Response historyResponse = bridge.getOrdersHistory(testUsername, testToken);
//
//        // Verify the response indicates an error
//        Assertions.assertNotNull(historyResponse, "Order history response should not be null");
//        Assertions.assertTrue(historyResponse.isError(), "Retrieving order history while logged out should fail");
//        Assertions.assertNotNull(historyResponse.getErrorMessage(), "Response should contain error message");
//
//    }
//
//
//}