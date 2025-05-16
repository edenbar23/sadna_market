package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.DomainLayer.ShoppingBasket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CartUnitTest {

    private Cart cart;
    private UUID testStoreId;
    private UUID testProductId;
    private UUID anotherStoreId;
    private UUID anotherProductId;
    private int defaultQuantity;

    @BeforeEach
    @DisplayName("Set up test environment")
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");
        cart = new Cart();
        testStoreId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        anotherStoreId = UUID.randomUUID();
        anotherProductId = UUID.randomUUID();
        defaultQuantity = 5;
        System.out.println("Created new cart");
        System.out.println("Test store ID: " + testStoreId);
        System.out.println("Test product ID: " + testProductId);
        System.out.println("Another store ID: " + anotherStoreId);
        System.out.println("Another product ID: " + anotherProductId);
        System.out.println("Default quantity: " + defaultQuantity);
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    @DisplayName("Clean up test resources")
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        cart = null;
        System.out.println("Cart reference set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    @DisplayName("Empty cart constructor creates empty cart")
    void testConstructor_EmptyCart_CreatesEmptyCart() {
        System.out.println("TEST: Verifying empty cart constructor creates an empty cart");

        System.out.println("Expected: Cart should be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertTrue(cart.isEmpty(), "New cart should be empty");

        System.out.println("Expected: Shopping baskets map should be empty");
        System.out.println("Actual: Shopping baskets size = " + cart.getShoppingBaskets().size());
        assertEquals(0, cart.getShoppingBaskets().size(), "Shopping baskets map should be empty");

        System.out.println("✓ Empty cart constructor correctly creates an empty cart");
    }

    @Test
    @DisplayName("Constructor with shopping baskets creates cart with those baskets")
    void testConstructor_WithShoppingBaskets_CreatesCartWithBaskets() {
        System.out.println("TEST: Verifying constructor with shopping baskets creates a cart with those baskets");

        // Create a map of shopping baskets
        Map<UUID, Map<UUID, Integer>> shoppingBasketsMap = new HashMap<>();
        Map<UUID, Integer> productsMap = new HashMap<>();
        productsMap.put(testProductId, defaultQuantity);
        shoppingBasketsMap.put(testStoreId, productsMap);

        System.out.println("Creating cart with store ID: " + testStoreId + " and product ID: " + testProductId);
        Cart cartWithBaskets = new Cart(shoppingBasketsMap);

        System.out.println("Expected: Cart should not be empty");
        System.out.println("Actual: Cart is empty = " + cartWithBaskets.isEmpty());
        assertFalse(cartWithBaskets.isEmpty(), "Cart should not be empty");

        System.out.println("Expected: Shopping baskets size should be 1");
        System.out.println("Actual: Shopping baskets size = " + cartWithBaskets.getShoppingBaskets().size());
        assertEquals(1, cartWithBaskets.getShoppingBaskets().size(), "Cart should have one shopping basket");

        System.out.println("Expected: Cart should contain the provided store ID");
        System.out.println("Actual: Cart contains store ID = " + cartWithBaskets.getShoppingBaskets().containsKey(testStoreId));
        assertTrue(cartWithBaskets.getShoppingBaskets().containsKey(testStoreId), "Cart should contain the provided store ID");

        System.out.println("Expected: Total items count should be " + defaultQuantity);
        System.out.println("Actual: Total items count = " + cartWithBaskets.getTotalItems());
        assertEquals(defaultQuantity, cartWithBaskets.getTotalItems(), "Cart should have the correct total items count");

        System.out.println("✓ Constructor with shopping baskets correctly creates a cart with those baskets");
    }

    @Test
    @DisplayName("Adding new product to cart adds product correctly")
    void testAddToCart_NewProduct_AddsProductCorrectly() {
        System.out.println("TEST: Verifying adding a new product to cart");

        System.out.println("Adding product with ID: " + testProductId + " to store: " + testStoreId + " with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        System.out.println("Expected: Cart should not be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertFalse(cart.isEmpty(), "Cart should not be empty after adding a product");

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();

        System.out.println("Expected: Shopping baskets should contain the store");
        System.out.println("Actual: Shopping baskets contains store = " + baskets.containsKey(testStoreId));
        assertTrue(baskets.containsKey(testStoreId), "Shopping baskets should contain the store");

        ShoppingBasket basket = baskets.get(testStoreId);

        System.out.println("Expected: Basket should contain the product");
        System.out.println("Actual: Basket contains product = " + basket.containsProduct(testProductId));
        assertTrue(basket.containsProduct(testProductId), "Basket should contain the product");

        System.out.println("Expected: Product quantity should be " + defaultQuantity);
        System.out.println("Actual: Product quantity = " + basket.getProductQuantity(testProductId));
        assertEquals(defaultQuantity, basket.getProductQuantity(testProductId), "Product quantity should be correct");

        System.out.println("Expected: Total items count should be " + defaultQuantity);
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(defaultQuantity, cart.getTotalItems(), "Cart should have the correct total items count");

        System.out.println("✓ Adding new product to cart works correctly");
    }

    @Test
    @DisplayName("Adding existing product to cart increases quantity")
    void testAddToCart_ExistingProduct_IncreasesQuantity() {
        System.out.println("TEST: Verifying adding an existing product to cart increases its quantity");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int additionalQuantity = 3;
        System.out.println("Adding same product again with additional quantity: " + additionalQuantity);
        cart.addToCart(testStoreId, testProductId, additionalQuantity);

        int expectedTotalQuantity = defaultQuantity + additionalQuantity;
        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(testStoreId);

        System.out.println("Expected: Product quantity should be " + expectedTotalQuantity);
        System.out.println("Actual: Product quantity = " + basket.getProductQuantity(testProductId));
        assertEquals(expectedTotalQuantity, basket.getProductQuantity(testProductId), "Product quantity should be increased");

        System.out.println("Expected: Total items count should be " + expectedTotalQuantity);
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(expectedTotalQuantity, cart.getTotalItems(), "Cart should have the correct total items count");

        System.out.println("✓ Adding existing product to cart correctly increases its quantity");
    }

    @Test
    @DisplayName("Adding product with negative quantity throws IllegalArgumentException")
    void testAddToCart_NegativeQuantity_ThrowsException() {
        System.out.println("TEST: Verifying adding a product with negative quantity throws exception");

        int negativeQuantity = -1;
        System.out.println("Attempting to add product with negative quantity: " + negativeQuantity);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cart.addToCart(testStoreId, testProductId, negativeQuantity);
        });

        System.out.println("Expected: Exception message should contain 'Quantity must be positive'");
        System.out.println("Actual: Exception message = " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Quantity must be positive"), "Exception message should mention that quantity must be positive");

        System.out.println("✓ Adding product with negative quantity correctly throws exception");
    }

    @Test
    @DisplayName("Removing existing product from cart removes it correctly")
    void testRemoveFromCart_ExistingProduct_RemovesProduct() {
        System.out.println("TEST: Verifying removing an existing product from cart");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        System.out.println("Now removing the product");
        cart.removeFromCart(testStoreId, testProductId);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();

        System.out.println("Expected: Cart should be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertTrue(cart.isEmpty(), "Cart should be empty after removing the only product");

        System.out.println("Expected: Shopping baskets should not contain the store");
        System.out.println("Actual: Shopping baskets contains store = " + baskets.containsKey(testStoreId));
        assertFalse(baskets.containsKey(testStoreId), "Shopping baskets should not contain the store");

        System.out.println("Expected: Total items count should be 0");
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(0, cart.getTotalItems(), "Cart should have 0 total items");

        System.out.println("✓ Removing existing product from cart works correctly");
    }

    @Test
    @DisplayName("Removing non-existing product from cart has no effect")
    void testRemoveFromCart_NonExistingProduct_NoEffect() {
        System.out.println("TEST: Verifying removing a non-existing product from cart has no effect");

        System.out.println("First adding product with ID: " + testProductId);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        UUID nonExistentProductId = UUID.randomUUID();
        System.out.println("Now attempting to remove a non-existent product with ID: " + nonExistentProductId);
        cart.removeFromCart(testStoreId, nonExistentProductId);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(testStoreId);

        System.out.println("Expected: Cart should still contain the original product");
        System.out.println("Actual: Basket contains original product = " + basket.containsProduct(testProductId));
        assertTrue(basket.containsProduct(testProductId), "Cart should still contain the original product");

        System.out.println("Expected: Total items count should still be " + defaultQuantity);
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(defaultQuantity, cart.getTotalItems(), "Cart should still have the original total items count");

        System.out.println("✓ Removing non-existing product from cart correctly has no effect");
    }

    @Test
    @DisplayName("Changing product quantity increases quantity correctly")
    void testChangeProductQuantity_IncreaseQuantity_QuantityIncreased() {
        System.out.println("TEST: Verifying changing product quantity to increase it");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int newQuantity = 10;
        System.out.println("Now changing product quantity to: " + newQuantity);
        cart.changeProductQuantity(testStoreId, testProductId, newQuantity);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(testStoreId);

        System.out.println("Expected: Product quantity should be " + newQuantity);
        System.out.println("Actual: Product quantity = " + basket.getProductQuantity(testProductId));
        assertEquals(newQuantity, basket.getProductQuantity(testProductId), "Product quantity should be updated to the new value");

        System.out.println("Expected: Total items count should be " + newQuantity);
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(newQuantity, cart.getTotalItems(), "Cart should have the updated total items count");

        System.out.println("✓ Changing product quantity to increase it works correctly");
    }

    @Test
    @DisplayName("Changing product quantity decreases quantity correctly")
    void testChangeProductQuantity_DecreaseQuantity_QuantityDecreased() {
        System.out.println("TEST: Verifying changing product quantity to decrease it");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int newQuantity = 2;
        System.out.println("Now changing product quantity to: " + newQuantity);
        cart.changeProductQuantity(testStoreId, testProductId, newQuantity);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(testStoreId);

        System.out.println("Expected: Product quantity should be " + newQuantity);
        System.out.println("Actual: Product quantity = " + basket.getProductQuantity(testProductId));
        assertEquals(newQuantity, basket.getProductQuantity(testProductId), "Product quantity should be updated to the new value");

        System.out.println("Expected: Total items count should be " + newQuantity);
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(newQuantity, cart.getTotalItems(), "Cart should have the updated total items count");

        System.out.println("✓ Changing product quantity to decrease it works correctly");
    }

    @Test
    @DisplayName("Changing product quantity to zero removes product")
    void testChangeProductQuantity_ZeroQuantity_RemovesProduct() {
        System.out.println("TEST: Verifying changing product quantity to zero removes the product");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int newQuantity = 0;
        System.out.println("Now changing product quantity to: " + newQuantity);
        cart.changeProductQuantity(testStoreId, testProductId, newQuantity);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();

        System.out.println("Expected: Cart should be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertTrue(cart.isEmpty(), "Cart should be empty after setting product quantity to zero");

        System.out.println("Expected: Shopping baskets should not contain the store");
        System.out.println("Actual: Shopping baskets contains store = " + baskets.containsKey(testStoreId));
        assertFalse(baskets.containsKey(testStoreId), "Shopping baskets should not contain the store");

        System.out.println("Expected: Total items count should be 0");
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(0, cart.getTotalItems(), "Cart should have 0 total items");

        System.out.println("✓ Changing product quantity to zero correctly removes the product");
    }

    @Test
    @DisplayName("Changing product quantity to negative value removes product")
    void testChangeProductQuantity_NegativeQuantity_RemovesProduct() {
        System.out.println("TEST: Verifying changing product quantity to negative value removes the product");

        System.out.println("First adding product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int newQuantity = -1;
        System.out.println("Now changing product quantity to: " + newQuantity);
        cart.changeProductQuantity(testStoreId, testProductId, newQuantity);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();

        System.out.println("Expected: Cart should be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertTrue(cart.isEmpty(), "Cart should be empty after setting product quantity to negative value");

        System.out.println("Expected: Shopping baskets should not contain the store");
        System.out.println("Actual: Shopping baskets contains store = " + baskets.containsKey(testStoreId));
        assertFalse(baskets.containsKey(testStoreId), "Shopping baskets should not contain the store");

        System.out.println("Expected: Total items count should be 0");
        System.out.println("Actual: Total items count = " + cart.getTotalItems());
        assertEquals(0, cart.getTotalItems(), "Cart should have 0 total items");

        System.out.println("✓ Changing product quantity to negative value correctly removes the product");
    }

    @Test
    @DisplayName("getShoppingBaskets returns a defensive copy")
    void testGetShoppingBaskets_ReturnsDefensiveCopy() {
        System.out.println("TEST: Verifying getShoppingBaskets returns a defensive copy");

        System.out.println("First adding product to cart");
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();

        System.out.println("Now modifying the returned map by adding a new basket");
        baskets.put(anotherStoreId, new ShoppingBasket(anotherStoreId));

        Map<UUID, ShoppingBasket> basketsAfterModification = cart.getShoppingBaskets();

        System.out.println("Expected: Original cart should not be modified");
        System.out.println("Expected size: 1");
        System.out.println("Actual size: " + basketsAfterModification.size());
        assertEquals(1, basketsAfterModification.size(), "Original cart should not be modified");

        System.out.println("Expected: New basket should not be in the original cart");
        System.out.println("Actual: Cart contains the new basket = " + basketsAfterModification.containsKey(anotherStoreId));
        assertFalse(basketsAfterModification.containsKey(anotherStoreId), "New basket should not be in the original cart");

        System.out.println("✓ getShoppingBaskets correctly returns a defensive copy");
    }

    @Test
    @DisplayName("isEmpty returns true for empty cart")
    void testIsEmpty_EmptyCart_ReturnsTrue() {
        System.out.println("TEST: Verifying isEmpty returns true for empty cart");

        System.out.println("Expected: Cart should be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertTrue(cart.isEmpty(), "New cart should be empty");

        System.out.println("✓ isEmpty correctly returns true for empty cart");
    }

    @Test
    @DisplayName("isEmpty returns false for non-empty cart")
    void testIsEmpty_NonEmptyCart_ReturnsFalse() {
        System.out.println("TEST: Verifying isEmpty returns false for non-empty cart");

        System.out.println("Adding product to cart");
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        System.out.println("Expected: Cart should not be empty");
        System.out.println("Actual: Cart is empty = " + cart.isEmpty());
        assertFalse(cart.isEmpty(), "Cart with products should not be empty");

        System.out.println("✓ isEmpty correctly returns false for non-empty cart");
    }

    @Test
    @DisplayName("getTotalItems returns zero for empty cart")
    void testGetTotalItems_EmptyCart_ReturnsZero() {
        System.out.println("TEST: Verifying getTotalItems returns zero for empty cart");

        System.out.println("Expected: Total items should be 0");
        System.out.println("Actual: Total items = " + cart.getTotalItems());
        assertEquals(0, cart.getTotalItems(), "Empty cart should have 0 total items");

        System.out.println("✓ getTotalItems correctly returns zero for empty cart");
    }

    @Test
    @DisplayName("getTotalItems returns correct sum for multiple products")
    void testGetTotalItems_MultipleProducts_ReturnsTotalSum() {
        System.out.println("TEST: Verifying getTotalItems returns correct sum for multiple products");

        System.out.println("Adding first product with quantity: " + defaultQuantity);
        cart.addToCart(testStoreId, testProductId, defaultQuantity);

        int secondProductQuantity = 3;
        System.out.println("Adding second product with quantity: " + secondProductQuantity);
        cart.addToCart(testStoreId, anotherProductId, secondProductQuantity);

        int thirdProductQuantity = 2;
        System.out.println("Adding third product to a different store with quantity: " + thirdProductQuantity);
        cart.addToCart(anotherStoreId, testProductId, thirdProductQuantity);

        int expectedTotalItems = defaultQuantity + secondProductQuantity + thirdProductQuantity;

        System.out.println("Expected: Total items should be " + expectedTotalItems);
        System.out.println("Actual: Total items = " + cart.getTotalItems());
        assertEquals(expectedTotalItems, cart.getTotalItems(), "Cart should have the correct total items count");

        System.out.println("✓ getTotalItems correctly returns the total sum for multiple products");
    }
}