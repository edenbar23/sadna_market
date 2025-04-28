package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.DomainLayer.ShoppingBasket;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartTest {

    private Cart cart;
    
    @Mock
    private ShoppingBasket mockBasket;
    
    private final UUID STORE_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID = UUID.randomUUID();
    private final int QUANTITY = 2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cart = new Cart();
    }
    
    @Test
    void testConstructor() {
        assertNotNull(cart);
        assertTrue(cart.getShoppingBaskets().isEmpty());
    }
    
    @Test
    void testAddToCart_NewBasket() {
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        assertEquals(1, baskets.size());
        assertTrue(baskets.containsKey(STORE_ID));
    }
    
    @Test
    void testAddToCart_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Add to the same store again with different product
        UUID newProductId = UUID.randomUUID();
        int newQuantity = 3;
        cart.addToCart(STORE_ID, newProductId, newQuantity);
        
        // Verify the basket exists and check total items
        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        assertEquals(1, baskets.size());
        assertTrue(baskets.containsKey(STORE_ID));
        // The total items should be the sum of both products
        assertEquals(QUANTITY + newQuantity, cart.getTotalItems());
    }
    
    @Test
    void testAddToCart_NegativeQuantity() {
        // Should throw IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cart.addToCart(STORE_ID, PRODUCT_ID, -1);
        });
        
        String expectedMessage = "Quantity must be positive";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void testChangeProductQuantity_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Change quantity
        int newQuantity = 5;
        cart.changeProductQuantity(STORE_ID, PRODUCT_ID, newQuantity);
        
        // Verify the quantity was updated by checking total items
        assertEquals(newQuantity, cart.getTotalItems());
    }
    
    @Test
    void testChangeProductQuantity_ToZero() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Change quantity to zero should remove the product
        cart.changeProductQuantity(STORE_ID, PRODUCT_ID, 0);
        
        // Verify the basket is empty
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testChangeProductQuantity_NonExistentBasket() {
        // Use a store ID that doesn't exist in the cart
        UUID nonExistentStoreId = UUID.randomUUID();
        
        // This should not throw an exception
        cart.changeProductQuantity(nonExistentStoreId, PRODUCT_ID, 5);
        
        // Verify cart is still empty
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testRemoveFromCart_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Remove the product
        cart.removeFromCart(STORE_ID, PRODUCT_ID);
        
        // Verify the product was removed
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testRemoveFromCart_NonExistentBasket() {
        // Use a store ID that doesn't exist in the cart
        UUID nonExistentStoreId = UUID.randomUUID();
        
        // This should not throw an exception
        cart.removeFromCart(nonExistentStoreId, PRODUCT_ID);
        
        // Verify cart is still empty
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testGetShoppingBaskets() {
        // Add some items to the cart
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        UUID storeId2 = UUID.randomUUID();
        cart.addToCart(storeId2, PRODUCT_ID, QUANTITY);
        
        // Get the shopping baskets
        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        
        // Verify baskets
        assertEquals(2, baskets.size());
        assertTrue(baskets.containsKey(STORE_ID));
        assertTrue(baskets.containsKey(storeId2));
    }
    
    @Test
    void testIsEmpty() {
        // Initially empty
        assertTrue(cart.isEmpty());
        
        // Add an item
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Now not empty
        assertFalse(cart.isEmpty());
        
        // Remove the item
        cart.removeFromCart(STORE_ID, PRODUCT_ID);
        
        // Should be empty again
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testGetTotalItems() {
        // Initially zero
        assertEquals(0, cart.getTotalItems());
        
        // Add some items
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        UUID productId2 = UUID.randomUUID();
        int quantity2 = 3;
        cart.addToCart(STORE_ID, productId2, quantity2);
        
        // Total should be the sum
        assertEquals(QUANTITY + quantity2, cart.getTotalItems());
    }
    
    @Test
    void testChainedMethods() {
        // Test that the methods can be chained
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY)
            .changeProductQuantity(STORE_ID, PRODUCT_ID, QUANTITY + 1)
            .removeFromCart(STORE_ID, PRODUCT_ID);
            
        // Verify cart is empty after the chain
        assertTrue(cart.isEmpty());
    }
    
    @Test
    void testDefensiveCopy() {
        // Add an item to cart
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Get the map
        Map<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        
        // Try to modify the map directly
        baskets.clear();
        
        // Verify the original cart is unchanged
        assertFalse(cart.isEmpty());
        assertEquals(QUANTITY, cart.getTotalItems());
    }
}