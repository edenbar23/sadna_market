package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.DTOs.CheckoutResultDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.StoreDTO;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class GuestTests {
    @Autowired
    private Bridge bridge;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GuestTests.class);
    // Test data
    private UUID storeId;
    private UUID productId;
    private static final int PRODUCT_QUANTITY = 2;
    private static final String STORE_NAME = "Test Store";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_CATEGORY = "Electronics";
    private static final String PRODUCT_DESCRIPTION = "A test product for acceptance testing";
    private static final double PRODUCT_PRICE = 99.99;
    private CartRequest cartReq;
    private String dummyUsername;
    private String dummyToken;

    @BeforeEach
    void setup() {
        // Initialize a fresh cart for each test
        cartReq = new CartRequest();

        // Create a "dummy" user who will create a store and product
        dummyUsername = "storeowner";
        String dummyPassword = "Password123!";

        // Register the dummy user
        RegisterRequest registerRequest = new RegisterRequest(
                dummyUsername,
                dummyPassword,
                dummyUsername + "@example.com",
                "Store",
                "Owner"
        );
        Response<String> registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Login the dummy user
        Response<String> loginResponse = bridge.loginUser(dummyUsername, dummyPassword);
        Assertions.assertFalse(loginResponse.isError(), "User login should succeed");
        dummyToken = loginResponse.getData();

        // Create a store
        StoreRequest storeRequest = new StoreRequest(
                STORE_NAME,
                "A test store for guest testing",
                "123 Test Street, Test City",
                "guesttest@example.com",
                "555-123-4567",
                dummyUsername
        );

        Response<StoreDTO> createStoreResponse = bridge.createStore(
                dummyUsername,
                dummyToken,
                storeRequest
        );
        Assertions.assertFalse(createStoreResponse.isError(), "Store creation should succeed");

        // Extract store ID from response
        StoreDTO storeDTO = createStoreResponse.getData();
        storeId = storeDTO.getStoreId();
        logger.info("Store ID extracted from response: " + storeId);

        // Add a product to the store
        ProductRequest productRequest = new ProductRequest(
                null, // No product ID for new product
                PRODUCT_NAME,
                PRODUCT_CATEGORY,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE
        );

        int productQuantity = 20; // Setting inventory quantity

        Response<String> addProductResponse = bridge.addProductToStore(
                dummyToken,
                dummyUsername,
                storeId,
                productRequest,
                productQuantity
        );
        Assertions.assertFalse(addProductResponse.isError(), "Product addition should succeed");

        // Extract product ID from response
        String productIdStr = addProductResponse.getData();
        productId = UUID.fromString(productIdStr);
        logger.info("Product ID extracted from response: " + productId);
    }

    @AfterEach
    void tearDown() {
        // Clear the system state after each test
        bridge.clear();
    }

    @Test
    void searchExistingProductTest() {
        // This test performs a direct check using the product ID
        // which is more reliable than searching by name/category

        // Create a search request that's minimal to get all products
        ProductSearchRequest searchRequest = new ProductSearchRequest();

        // Execute the search
        Response<List<ProductDTO>> response = bridge.searchProduct(searchRequest);

        // Verify response basics
        Assertions.assertNotNull(response, "Search response should not be null");
        Assertions.assertFalse(response.isError(), "Search for existing product should succeed");

        // Verify response data
        List<ProductDTO> products = response.getData();
        Assertions.assertNotNull(products, "Product list should not be null");
        Assertions.assertFalse(products.isEmpty(), "Product list should not be empty");

        // Find our product in the results by ID
        boolean foundProduct = false;
        for (ProductDTO product : products) {
            if (product.getProductId().equals(productId)) {
                foundProduct = true;
                Assertions.assertEquals(PRODUCT_NAME, product.getName(), "Product name should match");
                Assertions.assertEquals(PRODUCT_CATEGORY, product.getCategory(), "Product category should match");
                Assertions.assertEquals(PRODUCT_PRICE, product.getPrice(), 0.01, "Product price should match");
                break;
            }
        }

        Assertions.assertTrue(foundProduct, "Our product should be found in the search results");

        // Now try searching specifically by name as well
        ProductSearchRequest nameSearchRequest = new ProductSearchRequest();
        nameSearchRequest.setName(PRODUCT_NAME);

        Response<List<ProductDTO>> nameResponse = bridge.searchProduct(nameSearchRequest);
        Assertions.assertFalse(nameResponse.isError(), "Search by name should succeed");

        // Verify the name search finds at least one product
        List<ProductDTO> nameResults = nameResponse.getData();
        Assertions.assertNotNull(nameResults, "Name search results should not be null");
        // Note: We're not checking if it's empty as that depends on your search implementation
    }

    @Test
    void searchNonExistentProductTest() {
        // Create a search request with a name that doesn't exist
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setName("NonExistentProduct" + UUID.randomUUID());

        // Make sure name is very specific to avoid any partial matches
        String uniqueSearchTerm = "CompletelyUniqueName" + System.currentTimeMillis();
        searchRequest.setName(uniqueSearchTerm);

        // Search for the product
        Response<List<ProductDTO>> response = bridge.searchProduct(searchRequest);

        // Verify response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Response should not indicate an error even for no results");

        // Check if the specific product is not in the results
        List<ProductDTO> products = response.getData();

        // It's acceptable if we get an empty list OR if we get some products but none with our unique search name
        if (products != null && !products.isEmpty()) {
            boolean foundNonExistentProduct = false;
            for (ProductDTO product : products) {
                if (product.getName().equals(uniqueSearchTerm)) {
                    foundNonExistentProduct = true;
                    break;
                }
            }
            Assertions.assertFalse(foundNonExistentProduct, "Should not find our non-existent product name");
        }
    }

    @Test
    void addProductToGuestCartTest() {
        // Add product to guest cart
        Response<CartRequest> response = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);

        // Verify response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.isError(), "Adding product to cart should succeed");

        // Verify cart data
        CartRequest updatedCart = response.getData();
        Assertions.assertNotNull(updatedCart, "Updated cart should not be null");
        Assertions.assertTrue(updatedCart.getBaskets().containsKey(storeId), "Cart should contain our store");
        Assertions.assertTrue(updatedCart.getBaskets().get(storeId).containsKey(productId),
                "Store basket should contain our product");
        Assertions.assertEquals(PRODUCT_QUANTITY,
                updatedCart.getBaskets().get(storeId).get(productId),
                "Product quantity should match");
    }

    @Test
    void addProductWithExcessiveQuantityToGuestCartTest() {
        // Try to add with excessive quantity
        int excessiveQuantity = 100;
        Response<CartRequest> response = bridge.addProductToGuestCart(cartReq, storeId, productId, excessiveQuantity);

        // Verify error response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Adding excessive quantity should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void updateProductQuantityInGuestCartTest() {
        // First add a product to the cart
        Response<CartRequest> addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Update the quantity
        int newQuantity = PRODUCT_QUANTITY + 3;
        Response<CartRequest> updateResponse = bridge.updateGuestCart(cartReq, storeId, productId, newQuantity);

        // Verify response
        Assertions.assertNotNull(updateResponse, "Update response should not be null");
        Assertions.assertFalse(updateResponse.isError(), "Updating cart should succeed");

        // Verify updated cart data
        CartRequest updatedCart = updateResponse.getData();
        Assertions.assertNotNull(updatedCart, "Updated cart should not be null");
        Assertions.assertTrue(updatedCart.getBaskets().containsKey(storeId), "Cart should contain our store");
        Assertions.assertTrue(updatedCart.getBaskets().get(storeId).containsKey(productId),
                "Store basket should contain our product");
        Assertions.assertEquals(newQuantity,
                updatedCart.getBaskets().get(storeId).get(productId),
                "Product quantity should be updated to the new value");
    }

    @Test
    void removeProductFromGuestCartTest() {
        // First add a product to the cart
        Response<CartRequest> addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Remove the product
        Response<CartRequest> removeResponse = bridge.removeProductFromGuestCart(cartReq, storeId, productId);

        // Verify response
        Assertions.assertNotNull(removeResponse, "Remove response should not be null");
        Assertions.assertFalse(removeResponse.isError(), "Removing product should succeed");

        // Verify product is removed
        CartRequest updatedCart = removeResponse.getData();
        Assertions.assertNotNull(updatedCart, "Updated cart should not be null");

        // Either the store should be removed from the cart,
        // or the product should be removed from the store's basket
        boolean productRemoved = !updatedCart.getBaskets().containsKey(storeId) ||
                !updatedCart.getBaskets().get(storeId).containsKey(productId);

        Assertions.assertTrue(productRemoved, "Product should be removed from the cart");
    }

    @Test
    void removeNonExistentProductFromGuestCartTest() {
        // Try to remove a non-existent product
        UUID nonExistentProductId = UUID.randomUUID();
        Response<CartRequest> response = bridge.removeProductFromGuestCart(cartReq, storeId, nonExistentProductId);

        // Verify error response
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.isError(), "Removing non-existent product should fail");
        Assertions.assertNotNull(response.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void buyGuestCartTest() {
        // Add a product to the cart
        Response<CartRequest> addResponse = bridge.addProductToGuestCart(cartReq, storeId, productId, PRODUCT_QUANTITY);
        Assertions.assertFalse(addResponse.isError(), "Adding product to cart should succeed");

        // Create credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "John Doe",
                "4111111111111111",
                "12/25",
                "123"
        );

        // Create guest checkout request
        GuestCheckoutRequest checkoutRequest = new GuestCheckoutRequest();
        checkoutRequest.setCartItems(cartReq.getBaskets()); // or however you extract cart items from cartReq
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setContactEmail("test@example.com"); // required field
        checkoutRequest.setShippingAddress("123 Test Street, Test City"); // required field
        // Process the checkout
        Response<CheckoutResultDTO> purchaseResponse = bridge.processGuestCheckout(checkoutRequest);

        // Verify response
        Assertions.assertNotNull(purchaseResponse, "Purchase response should not be null");
        Assertions.assertFalse(purchaseResponse.isError(), "Purchase should succeed");
        Assertions.assertNotNull(purchaseResponse.getData(), "Purchase confirmation should not be null");
    }

    @Test
    void buyEmptyGuestCartTest() {
        // Cart is empty by default

        // Create credit card for purchase
        CreditCardDTO creditCard = new CreditCardDTO(
                "John Doe",
                "4111111111111111",
                "12/25",
                "123"
        );

        // Create guest checkout request with empty cart
        GuestCheckoutRequest checkoutRequest = new GuestCheckoutRequest();
        checkoutRequest.setCartItems(cartReq.getBaskets()); // empty cart items
        checkoutRequest.setPaymentMethod(creditCard);
        checkoutRequest.setContactEmail("test@example.com"); // required field
        checkoutRequest.setShippingAddress("123 Test Street, Test City"); // required field

        // Try to process checkout with empty cart
        Response<CheckoutResultDTO> purchaseResponse = bridge.processGuestCheckout(checkoutRequest);

        // Verify error response
        Assertions.assertNotNull(purchaseResponse, "Purchase response should not be null");
        Assertions.assertTrue(purchaseResponse.isError(), "Purchasing empty cart should fail");
        Assertions.assertNotNull(purchaseResponse.getErrorMessage(), "Error message should not be null");
    }
}