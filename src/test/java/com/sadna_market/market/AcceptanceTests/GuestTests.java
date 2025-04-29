package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.CartRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;

import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public class GuestTests {
    private Bridge bridge = new Bridge();
    ObjectMapper objectMapper = new ObjectMapper();
    // Test data
    private UUID storeId;
    private UUID productId;
    private static final int PRODUCT_QUANTITY = 2;
    private static final String STORE_NAME = "Test Store";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_CATEGORY = "Electronics";
    private static final String PRODUCT_DESCRIPTION = "A test product for acceptance testing";
    private static final double PRODUCT_PRICE = 99.99;
    private CartRequest cartReq = new CartRequest();

    @BeforeEach
    void setup() {

        // Create a "dummy" user who will create a store and product
        // This simulates an admin or registered user setting up the environment

        // 1. Register a dummy user
        String dummyUsername = "storeowner" + UUID.randomUUID().toString().substring(0, 8);
        String dummyPassword = "password123";
        RegisterRequest registerRequest = new RegisterRequest(
                dummyUsername,
                dummyPassword,
                dummyUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // 2. Login the dummy user to get a token
        Response loginResponse = bridge.loginUser(dummyUsername, dummyPassword);
        Assertions.assertFalse(loginResponse.isError(), "User login should succeed");

        // The token would normally be extracted from the JSON response
        // Since we're not parsing JSON, we'll assume the token is in the response
        String token = loginResponse.getJson(); // In a real scenario, you'd extract the token from JSON

        // 3. Create a store
        Response createStoreResponse = bridge.createStore(
                dummyUsername,
                token,
                null // Here you would typically pass a StoreRequest object, but for simplicity using null
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        // In a real scenario, you'd extract the storeId from the JSON response
        // For this example, we'll use a random UUID
        storeId = UUID.randomUUID();

        // 4. Add a product to the store
        Response addProductResponse = bridge.addProductToStore(
                token,
                dummyUsername,
                storeId,
                null // Here you would typically pass a ProductRequest object, but for simplicity using null
        );
        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");

        // In a real scenario, you'd extract the productId from the JSON response
        // For this example, we'll use a random UUID
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Guest should be able to search for a product by name and price")
    void searchProductTest() {
        // Create a search request specifying only name and price range
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName(PRODUCT_NAME);
        searchRequest.setMinPrice(PRODUCT_PRICE - 1); // Slightly below the product price
        searchRequest.setMaxPrice(PRODUCT_PRICE + 1); // Slightly above the product price

        // Search for the product
        Response response = bridge.searchProduct(searchRequest);

        // Assert - Verify response is valid
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error");
        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

        try {
            // Parse the JSON response to get the list of products
            // Assuming the response contains a JSON array of products
            List<Product> products = objectMapper.readValue(
                    response.getJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class)
            );

            // Print products for debugging
            System.out.println("Found products: " + products);

            // Verify that exactly one product was found
            Assertions.assertEquals(1, products.size(), "Search should return exactly one product");

            // Verify that the correct product was found
            Product foundProduct = products.get(0);
            Assertions.assertEquals(PRODUCT_NAME, foundProduct.getName(), "Product name should match search criteria");
            Assertions.assertEquals(PRODUCT_PRICE, foundProduct.getPrice(), "Product price should match");
            Assertions.assertEquals(productId, foundProduct.getProductId(), "Product ID should match the expected product");

        } catch (IOException e) {
            Assertions.fail("Failed to parse search results JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest search for non-existent product should return empty results")
    void searchNonExistentProductTest() {
        // Create a search request with a name that doesn't exist in the system
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName("NonExistentProduct");  // This name doesn't match any product in the system

        // Search for the product
        Response response = bridge.searchProduct(searchRequest);

        // Assert - Verify response is valid (a search with no results is not an error)
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error even for no results");
        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

        try {
            // Parse the JSON response to get the list of products
            List<Product> products = objectMapper.readValue(
                    response.getJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class)
            );

            // Print products for debugging
            System.out.println("Found products: " + products);

            // Verify that no products were found
            Assertions.assertTrue(products.isEmpty(), "Search for non-existent product should return empty list");

        } catch (IOException e) {
            Assertions.fail("Failed to parse search results JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest should be able to add a product to their cart")
    void addProductToGuestCartTest() {
        // Guest is recognized by creating a new cart
        // No need to login or signup

        // Add product to guest cart
        Response response = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);

        // Assert - Verify response is valid
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error");
        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

        // Verify that we can view the cart and it contains our product
        Response viewCartResponse = bridge.viewGuestCart(cartReq);
        Assertions.assertNotNull(viewCartResponse, "View cart response should not be null");
        Assertions.assertFalse(viewCartResponse.isError(), "View cart should not indicate an error");
        Assertions.assertNotNull(viewCartResponse.getJson(), "Cart JSON should not be null");

        // Parse cart JSON to verify product is in cart
        try {
            // Deserialize the cart JSON to a Map structure
            // This is a more generic approach that doesn't assume a specific JSON structure
            CartRequest cartJSON = objectMapper.readValue(viewCartResponse.getJson(), CartRequest.class);

            // Print the cart structure for debugging
            System.out.println("Cart structure: " + cartJSON);

            // Get the baskets map from the CartRequest
            Map<UUID, Map<UUID, Integer>> baskets = cartJSON.getBaskets();

            // Check if the store exists in the baskets
            Assertions.assertTrue(baskets.containsKey(storeId),
                    "The cart should contain the store ID: " + storeId);

            // Get the basket for our store
            Map<UUID, Integer> storeBasket = baskets.get(storeId);

            // Check if the product exists in the store's basket
            Assertions.assertTrue(storeBasket.containsKey(productId),
                    "The store basket should contain the product ID: " + productId);

            // Verify the quantity is correct
            Integer quantity = storeBasket.get(productId);
            Assertions.assertEquals(PRODUCT_QUANTITY, quantity,
                    "The quantity of the product should match what was added");

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest should not be able to add a product with quantity exceeding store inventory")
    void addProductWithExcessiveQuantityToGuestCartTest() {
        // Define an excessive quantity (more than what's available in store)
        int excessiveQuantity = 100; // Assuming the store doesn't have 100 units of this product

        // Try to add the product with excessive quantity to guest cart
        Response response = bridge.addProductToGuestCart(cartReq, storeId, productId, excessiveQuantity);

        // Assert - Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Response should indicate an error when adding excessive quantity");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");

        // Verify the error message contains relevant information
        String errorMessage = response.getErrorMessage();
        System.out.println("Error message: " + errorMessage);

        // Optionally verify specific error message content if you know it
        // Assertions.assertTrue(errorMessage.contains("quantity") && errorMessage.contains("exceed"),
        //                       "Error should mention insufficient quantity");

        // Verify that we can view the cart and it doesn't contain the product with excessive quantity
        Response viewCartResponse = bridge.viewGuestCart(cartReq);
        Assertions.assertNotNull(viewCartResponse, "View cart response should not be null");
        Assertions.assertFalse(viewCartResponse.isError(), "View cart should not indicate an error");
        Assertions.assertNotNull(viewCartResponse.getJson(), "Cart JSON should not be null");

        try {
            // Deserialize the cart JSON
            CartRequest cartJSON = objectMapper.readValue(viewCartResponse.getJson(), CartRequest.class);

            // Get the baskets map from the CartRequest
            Map<UUID, Map<UUID, Integer>> baskets = cartJSON.getBaskets();

            boolean hasExcessiveQuantity = false;

            // Check if the store exists in the baskets
            if (baskets.containsKey(storeId)) {
                Map<UUID, Integer> storeBasket = baskets.get(storeId);

                // Check if the product exists in the basket
                if (storeBasket.containsKey(productId)) {
                    // Verify the quantity is not the excessive amount
                    Integer quantity = storeBasket.get(productId);
                    hasExcessiveQuantity = (quantity != null && quantity >= excessiveQuantity);
                }
            }

            Assertions.assertFalse(hasExcessiveQuantity,
                    "The cart should not contain the product with excessive quantity");

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest should be able to update the quantity of a product in their cart")
    void updateProductQuantityInGuestCartTest() {
        // First add a product to the cart
        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Verify product was added successfully with initial quantity
        Response viewCartAfterAdd = bridge.viewGuestCart(cartReq);
        Assertions.assertFalse(viewCartAfterAdd.isError(), "Viewing cart after add should succeed");

        try {
            CartRequest cartAfterAdd = objectMapper.readValue(viewCartAfterAdd.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterAdd = cartAfterAdd.getBaskets();

            // Verify the product is in the cart with the initial quantity
            Assertions.assertTrue(
                    basketsAfterAdd.containsKey(storeId) &&
                            basketsAfterAdd.get(storeId).containsKey(productId),
                    "Product should be in cart before update"
            );

            int initialQuantity = basketsAfterAdd.get(storeId).get(productId);
            Assertions.assertEquals(PRODUCT_QUANTITY, initialQuantity, "Initial quantity should match what was added");

            // Define the new quantity for update
            int newQuantity = PRODUCT_QUANTITY + 3;  // Increase the quantity

            // Now update the product quantity in the cart
            Response updateResponse = bridge.updateGuestCart(cartReq,storeId, productId, newQuantity);

            // Assert - Verify update response is valid
            Assertions.assertNotNull(updateResponse, "Update response should not be null");
            Assertions.assertFalse(updateResponse.isError(), "Update response should not indicate an error");
            Assertions.assertNotNull(updateResponse.getJson(), "Update response JSON should not be null");

            // Verify that the product quantity has been updated
            Response viewCartAfterUpdate = bridge.viewGuestCart(cartReq);
            Assertions.assertFalse(viewCartAfterUpdate.isError(), "Viewing cart after update should succeed");

            CartRequest cartAfterUpdate = objectMapper.readValue(viewCartAfterUpdate.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterUpdate = cartAfterUpdate.getBaskets();

            // Verify the product is still in the cart and has the new quantity
            Assertions.assertTrue(
                    basketsAfterUpdate.containsKey(storeId) &&
                            basketsAfterUpdate.get(storeId).containsKey(productId),
                    "Product should still be in cart after update"
            );

            int updatedQuantity = basketsAfterUpdate.get(storeId).get(productId);
            Assertions.assertEquals(newQuantity, updatedQuantity,
                    "Product quantity should be updated to the new value");

            // Print debug information
            System.out.println("Cart before update: " + cartAfterAdd);
            System.out.println("Cart after update: " + cartAfterUpdate);

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest should be able to remove a product from their cart")
    void removeProductFromGuestCartTest() {
        // First add a product to the cart
        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Verify product was added successfully
        Response viewCartAfterAdd = bridge.viewGuestCart(cartReq);
        Assertions.assertFalse(viewCartAfterAdd.isError(), "Viewing cart after add should succeed");

        try {
            CartRequest cartAfterAdd = objectMapper.readValue(viewCartAfterAdd.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterAdd = cartAfterAdd.getBaskets();

            // Verify the product is in the cart before removal
            Assertions.assertTrue(
                    basketsAfterAdd.containsKey(storeId) &&
                            basketsAfterAdd.get(storeId).containsKey(productId),
                    "Product should be in cart before removal"
            );

            // Now remove the product from the cart
            Response removeResponse = bridge.removeProductFromGuestCart(cartReq,storeId, productId);

            // Assert - Verify removal response is valid
            Assertions.assertNotNull(removeResponse, "Remove response should not be null");
            Assertions.assertFalse(removeResponse.isError(), "Remove response should not indicate an error");
            Assertions.assertNotNull(removeResponse.getJson(), "Remove response JSON should not be null");

            // Verify that the product is no longer in the cart
            Response viewCartAfterRemove = bridge.viewGuestCart(cartReq);
            Assertions.assertFalse(viewCartAfterRemove.isError(), "Viewing cart after remove should succeed");

            CartRequest cartAfterRemove = objectMapper.readValue(viewCartAfterRemove.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterRemove = cartAfterRemove.getBaskets();

            boolean productStillInCart = false;
            if (basketsAfterRemove.containsKey(storeId)) {
                Map<UUID, Integer> storeBasket = basketsAfterRemove.get(storeId);
                productStillInCart = storeBasket.containsKey(productId);
            }

            Assertions.assertFalse(productStillInCart, "Product should not be in cart after removal");

            // Print debug information
            System.out.println("Cart after removal: " + cartAfterRemove);

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guests should receive an error when trying to remove a product not in their cart")
    void removeNonExistentProductFromUserCartTest() {
        // Create a UUID for a product that isn't in the cart
        UUID nonExistentProductId = UUID.randomUUID();

        // Try to remove a product that isn't in the cart
        Response response = bridge.removeProductFromGuestCart(cartReq,storeId, nonExistentProductId);
        // Assert - Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Response should indicate an error when removing non-existent product");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
        // Verify the error message contains relevant information
        String errorMessage = response.getErrorMessage();
        System.out.println("Error message: " + errorMessage);
        // Verify that the cart state remains unchanged
        Response viewCartResponse = bridge.viewGuestCart(cartReq);
        Assertions.assertNotNull(viewCartResponse, "View cart response should not be null");
        Assertions.assertFalse(viewCartResponse.isError(), "View cart should not indicate an error");
        try{
            // Parse cart JSON to verify state
            CartRequest cartJSON = objectMapper.readValue(viewCartResponse.getJson(), CartRequest.class);

            // Print the cart structure for debugging
            System.out.println("Cart after removal attempt: " + cartJSON);

            // Since we're removing a non-existent product, we don't expect any changes
            // But we can verify the cart is accessible and parseable
            Assertions.assertNotNull(cartJSON.getBaskets(), "Cart baskets should still be accessible");
        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }
    @Test
    @DisplayName("Guest should be able to purchase items in their cart")
    void buyGuestCartTest() {
        // First add a product to the cart to ensure there's something to purchase
        Response addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");
        PaymentProxy payment = new PaymentProxy();

        // Verify the product was added to the cart
        Response viewCartBeforePurchase = bridge.viewGuestCart(cartReq);
        Assertions.assertFalse(viewCartBeforePurchase.isError(), "Viewing cart before purchase should succeed");

        try {
            // Parse cart to verify it contains the product before purchase
            CartRequest cartBeforePurchase = objectMapper.readValue(viewCartBeforePurchase.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsBeforePurchase = cartBeforePurchase.getBaskets();

            Assertions.assertTrue(
                    basketsBeforePurchase.containsKey(storeId) &&
                            basketsBeforePurchase.get(storeId).containsKey(productId),
                    "Product should be in cart before purchase"
            );

            // Now purchase the items in the cart
            Response purchaseResponse = bridge.buyGuestCart(cartReq);

            // Assert - Verify purchase response is valid
            Assertions.assertNotNull(purchaseResponse, "Purchase response should not be null");
            Assertions.assertFalse(purchaseResponse.isError(), "Purchase should not indicate an error");
            Assertions.assertNotNull(purchaseResponse.getJson(), "Purchase response JSON should not be null");

            // Print purchase response for debugging
            System.out.println("Purchase response: " + purchaseResponse.getJson());

            // Verify the cart is now empty (or in a "purchased" state)
            Response viewCartAfterPurchase = bridge.viewGuestCart(cartReq);
            Assertions.assertFalse(viewCartAfterPurchase.isError(), "Viewing cart after purchase should succeed");

            CartRequest cartAfterPurchase = objectMapper.readValue(viewCartAfterPurchase.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterPurchase = cartAfterPurchase.getBaskets();

            // Check if cart is empty or cleared after purchase
            boolean cartIsEmpty = basketsAfterPurchase == null || basketsAfterPurchase.isEmpty();

            // Alternative check: if the implementation keeps the baskets but removes products
            boolean productsRemoved = true;
            if (!cartIsEmpty && basketsAfterPurchase.containsKey(storeId)) {
                Map<UUID, Integer> storeBasket = basketsAfterPurchase.get(storeId);
                productsRemoved = storeBasket == null || !storeBasket.containsKey(productId);
            }

            Assertions.assertTrue(cartIsEmpty || productsRemoved,
                    "Cart should be empty or products should be removed after purchase");

            // Print cart after purchase for debugging
            System.out.println("Cart after purchase: " + cartAfterPurchase);

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Guest should be able to register as a new user")
    void registerUserTest() {
        // Create a unique username to avoid conflicts with existing users
        String testUsername = "testuser" + UUID.randomUUID().toString().substring(0, 8);
        String testPassword = "Password123!";
        String testEmail = testUsername + "@example.com";
        String testFirstName = "Test";
        String testLastName = "User";

        // Create a RegisterRequest with the test data
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testEmail,
                testFirstName,
                testLastName
        );

        // Call the registration method
        Response response = bridge.registerUser(registerRequest);

        // Assert - Verify response is valid
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error");
        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

        // Print response for debugging
        System.out.println("Registration response: " + response.getJson());
    }

    @Test
    @DisplayName("User should be able to login with valid credentials")
    void loginUserTest() {
        // Create a unique username for testing
        String testUsername = "loginuser" + UUID.randomUUID().toString().substring(0, 8);
        String testPassword = "Password123!";
        String testEmail = testUsername + "@example.com";
        String testFirstName = "Login";
        String testLastName = "User";

        // First register the user so we have valid credentials to test
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testEmail,
                testFirstName,
                testLastName
        );

        Response registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Now test the login functionality
        Response loginResponse = bridge.loginUser(testUsername, testPassword);

        // Assert - Verify login response is valid
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed with valid credentials");
        Assertions.assertNotNull(loginResponse.getJson(), "Login response JSON should not be null");

        // Print response for debugging
        System.out.println("Login response: " + loginResponse.getJson());
    }

}