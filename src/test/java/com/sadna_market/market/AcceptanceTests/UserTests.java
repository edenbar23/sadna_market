package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.CartRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;
import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
import com.sadna_market.market.DomainLayer.Product.Product;

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
public class UserTests {
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

    // User credentials
    private String testUsername;
    private String testPassword;
    private String testToken;  // JWT token for the test user

    @BeforeEach
    void setup() {
        // Create a regular user for testing
        testUsername = "testuser" + UUID.randomUUID().toString().substring(0, 8);
        testPassword = "password123";
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testUsername + "@example.com",
                "Test",
                "User"
        );
        Response registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "Test user registration should succeed");

        // Login the test user to get a JWT token
        Response loginResponse = bridge.loginUser(testUsername, testPassword);
        Assertions.assertFalse(loginResponse.isError(), "Test user login should succeed");

        // Extract the JWT token from the response
        testToken = loginResponse.getJson();
        Assertions.assertNotNull(testToken, "JWT token should not be null");
        Assertions.assertFalse(testToken.isEmpty(), "JWT token should not be empty");
        System.out.println("Test user JWT token: " + testToken);

        // Create a store owner user for setting up test environment
        String ownerUsername = "storeowner" + UUID.randomUUID().toString().substring(0, 8);
        String ownerPassword = "password123";
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
        String ownerToken = ownerLoginResponse.getJson();
        Assertions.assertNotNull(ownerToken, "Owner JWT token should not be null");
        Assertions.assertFalse(ownerToken.isEmpty(), "Owner JWT token should not be empty");
        System.out.println("Store owner JWT token: " + ownerToken);

        // Create a store
        Response createStoreResponse = bridge.createStore(
                ownerUsername,
                ownerToken,
                null // Here you would typically pass a StoreRequest object, but for simplicity using null
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        // In a real scenario, you'd extract the storeId from the JSON response
        storeId = UUID.randomUUID();

        // Add a product to the store
        Response addProductResponse = bridge.addProductToStore(
                ownerToken,
                ownerUsername,
                storeId,
                null // Here you would typically pass a ProductRequest object, but for simplicity using null
        );
        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");

        // In a real scenario, you'd extract the productId from the JSON response
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("User should be able to add a product to their cart")
    void addProductToUserCartTest() {
        // Add product to user cart using the user's credentials and token
        Response response = bridge.addProductToUserCart(testUsername, testToken,storeId, productId, PRODUCT_QUANTITY);

        // Assert - Verify response is valid
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error");
        Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

        // Verify that we can view the cart and it contains our product
        Response viewCartResponse = bridge.viewUserCart(testUsername, testToken);
        Assertions.assertNotNull(viewCartResponse, "View cart response should not be null");
        Assertions.assertFalse(viewCartResponse.isError(), "View cart should not indicate an error");
        Assertions.assertNotNull(viewCartResponse.getJson(), "Cart JSON should not be null");

        // Parse cart JSON to verify product is in cart
        try {
            // Deserialize the cart JSON to a CartRequest structure
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
    @DisplayName("User should not be able to add a product with quantity exceeding store inventory")
    void addProductWithExcessiveQuantityToUserCartTest() {
        // Define an excessive quantity (more than what's available in store)
        int excessiveQuantity = 100; // Assuming the store doesn't have 100 units of this product

        // Try to add the product with excessive quantity to user cart
        Response response = bridge.addProductToUserCart(testUsername, testToken,storeId, productId, excessiveQuantity);

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
        Response viewCartResponse = bridge.viewUserCart(testUsername, testToken);
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
    @DisplayName("User should be able to update the quantity of a product in their cart")
    void updateProductQuantityInUserCartTest() {
        // First add a product to the user's cart
        Response addResponse = bridge.addProductToUserCart(testUsername, testToken,storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Verify product was added successfully with initial quantity
        Response viewCartAfterAdd = bridge.viewUserCart(testUsername, testToken);
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
            Response updateResponse = bridge.updateUserCart(testUsername, testToken, storeId, productId, newQuantity);

            // Assert - Verify update response is valid
            Assertions.assertNotNull(updateResponse, "Update response should not be null");
            Assertions.assertFalse(updateResponse.isError(), "Update response should not indicate an error");
            Assertions.assertNotNull(updateResponse.getJson(), "Update response JSON should not be null");

            // Verify that the product quantity has been updated
            Response viewCartAfterUpdate = bridge.viewUserCart(testUsername, testToken);
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
    @DisplayName("User should be able to remove a product from their cart")
    void removeProductFromUserCartTest() {
        // First add a product to the user's cart
        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Verify product was added successfully
        Response viewCartAfterAdd = bridge.viewUserCart(testUsername, testToken);
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
            Response removeResponse = bridge.removeProductFromUserCart(testUsername, testToken, storeId, productId);

            // Assert - Verify removal response is valid
            Assertions.assertNotNull(removeResponse, "Remove response should not be null");
            Assertions.assertFalse(removeResponse.isError(), "Remove response should not indicate an error");
            Assertions.assertNotNull(removeResponse.getJson(), "Remove response JSON should not be null");

            // Verify that the product is no longer in the cart
            Response viewCartAfterRemove = bridge.viewUserCart(testUsername, testToken);
            Assertions.assertFalse(viewCartAfterRemove.isError(), "Viewing cart after remove should succeed");

            CartRequest cartAfterRemove = objectMapper.readValue(viewCartAfterRemove.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterRemove = cartAfterRemove.getBaskets();

            boolean productStillInCart = false;
            if (basketsAfterRemove != null && basketsAfterRemove.containsKey(storeId)) {
                Map<UUID, Integer> storeBasket = basketsAfterRemove.get(storeId);
                if (storeBasket != null) {
                    productStillInCart = storeBasket.containsKey(productId);
                }
            }

            Assertions.assertFalse(productStillInCart, "Product should not be in cart after removal");

            // Print debug information
            System.out.println("Cart before removal: " + cartAfterAdd);
            System.out.println("Cart after removal: " + cartAfterRemove);

        } catch (IOException e) {
            Assertions.fail("Failed to parse cart JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("User should receive an error when trying to remove a product not in their cart")
    void removeNonExistentProductFromUserCartTest() {
        // Create a UUID for a product that isn't in the cart
        UUID nonExistentProductId = UUID.randomUUID();

        // Try to remove a product that isn't in the cart
        Response response = bridge.removeProductFromUserCart(testUsername, testToken, storeId, nonExistentProductId);

        // Assert - Verify response indicates an error
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Response should indicate an error when removing non-existent product");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");

        // Verify the error message contains relevant information
        String errorMessage = response.getErrorMessage();
        System.out.println("Error message: " + errorMessage);

        // Optionally verify specific error message content if you know it
        // Assertions.assertTrue(errorMessage.contains("product") && errorMessage.contains("not found"),
        //                      "Error should mention product not found in cart");

        // Verify that the cart state remains unchanged
        Response viewCartResponse = bridge.viewUserCart(testUsername, testToken);
        Assertions.assertNotNull(viewCartResponse, "View cart response should not be null");
        Assertions.assertFalse(viewCartResponse.isError(), "View cart should not indicate an error");

        try {
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
    @DisplayName("User should be able to successfully buy their cart")
    void buyUserCartTest() {
        // First, add a product to the user's cart
        Response addResponse = bridge.addProductToUserCart(testUsername, testToken, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Verify product was added successfully
        Response viewCartBeforeBuy = bridge.viewUserCart(testUsername, testToken);
        Assertions.assertFalse(viewCartBeforeBuy.isError(), "Viewing cart before buy should succeed");

        try {
            // Try to buy the cart
            Response buyResponse = bridge.buyUserCart(testUsername, testToken);

            // Assert buy response
            Assertions.assertNotNull(buyResponse, "Buy cart response should not be null");
            Assertions.assertFalse(buyResponse.isError(), "Buy cart should succeed");
            Assertions.assertNotNull(buyResponse.getJson(), "Buy cart response JSON should not be null");

            // Verify the cart is now empty after successful purchase
            Response viewCartAfterBuy = bridge.viewUserCart(testUsername, testToken);
            Assertions.assertFalse(viewCartAfterBuy.isError(), "Viewing cart after buy should succeed");

            CartRequest cartAfterBuy = objectMapper.readValue(viewCartAfterBuy.getJson(), CartRequest.class);
            Map<UUID, Map<UUID, Integer>> basketsAfterBuy = cartAfterBuy.getBaskets();

            // Check if cart is empty or cleared after purchase
            boolean cartIsEmpty = basketsAfterBuy == null || basketsAfterBuy.isEmpty();

            // Alternative check: if the implementation keeps the baskets but removes products
            boolean productsRemoved = true;
            if (!cartIsEmpty && basketsAfterBuy.containsKey(storeId)) {
                Map<UUID, Integer> storeBasket = basketsAfterBuy.get(storeId);
                productsRemoved = storeBasket == null || !storeBasket.containsKey(productId);
            }

            Assertions.assertTrue(cartIsEmpty || productsRemoved,
                    "Cart should be empty or products should be removed after purchase");

            // Print cart after purchase for debugging
            System.out.println("Cart after purchase: " + viewCartAfterBuy);


        } catch (IOException e) {
            Assertions.fail("Failed to process cart or order data: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("User should be able to create a new store")
    void createStoreTest() {
        // Create a StoreRequest object for the new store using the correct constructor
        StoreRequest newStore = new StoreRequest(
                "User's Test Store",                // storeName
                "A store created for testing",      // description
                "123 Test Street, Test City",       // address
                "teststore@example.com",            // email
                "555-123-4567",                     // phoneNumber
                testUsername                        // founderUsername
        );
        try {

            // Call the createStore method from the bridge
            Response response = bridge.createStore(testUsername, testToken, newStore);

            // Assert - Verify response is valid
            Assertions.assertNotNull(response, "Response should not be null");
            Assertions.assertFalse(response.isError(), "Store creation should succeed");
            Assertions.assertNotNull(response.getJson(), "Response JSON should not be null");

            // Print the success message for debugging
            System.out.println("Store creation response: " + response.getJson());
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }


}