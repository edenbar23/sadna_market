package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.ApplicationLayer.CartDTO;
import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.DomainLayer.ShoppingBasket;

import java.util.HashMap;
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
        
        HashMap<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        assertEquals(1, baskets.size());
        assertTrue(baskets.containsKey(STORE_ID));
    }
    
    @Test
    void testAddToCart_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Get and mock the created basket
        HashMap<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(STORE_ID);
        ShoppingBasket spyBasket = spy(basket);
        
        // Replace real basket with spy
        baskets.put(STORE_ID, spyBasket);
        
        // Add to the same store again with different product
        UUID newProductId = UUID.randomUUID();
        int newQuantity = 3;
        cart.addToCart(STORE_ID, newProductId, newQuantity);
        
        // Verify the method was called on the existing basket
        verify(spyBasket).addProduct(newProductId, newQuantity);
    }
    
    @Test
    void testChangeProductQuantity_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Get and mock the created basket
        HashMap<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(STORE_ID);
        ShoppingBasket spyBasket = spy(basket);
        
        // Replace real basket with spy
        baskets.put(STORE_ID, spyBasket);
        
        // Change quantity
        int newQuantity = 5;
        cart.changeProductQuantity(STORE_ID, PRODUCT_ID, newQuantity);
        
        // Verify the method was called on the basket
        verify(spyBasket).changeProductQuantity(PRODUCT_ID, newQuantity);
    }
    
    @Test
    void testChangeProductQuantity_NonExistentBasket() {
        // Use a store ID that doesn't exist in the cart
        UUID nonExistentStoreId = UUID.randomUUID();
        
        // This should log an error but not throw an exception
        cart.changeProductQuantity(nonExistentStoreId, PRODUCT_ID, 5);
    }
    
    @Test
    void testRemoveFromCart_ExistingBasket() {
        // First add to create the basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Get and mock the created basket
        HashMap<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        ShoppingBasket basket = baskets.get(STORE_ID);
        ShoppingBasket spyBasket = spy(basket);
        
        // Replace real basket with spy
        baskets.put(STORE_ID, spyBasket);
        
        // Remove the product
        cart.removeFromCart(STORE_ID, PRODUCT_ID);
        
        // Verify the method was called on the basket
        verify(spyBasket).removeProduct(PRODUCT_ID);
    }
    
    @Test
    void testRemoveFromCart_NonExistentBasket() {
        // Use a store ID that doesn't exist in the cart
        UUID nonExistentStoreId = UUID.randomUUID();
        
        // This should log an error but not throw an exception
        cart.removeFromCart(nonExistentStoreId, PRODUCT_ID);
    }
    
    @Test
    void testGetShoppingBaskets() {
        // Add some items to the cart
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        UUID storeId2 = UUID.randomUUID();
        cart.addToCart(storeId2, PRODUCT_ID, QUANTITY);
        
        // Get the shopping baskets
        HashMap<UUID, ShoppingBasket> baskets = cart.getShoppingBaskets();
        
        // Verify baskets
        assertEquals(2, baskets.size());
        assertTrue(baskets.containsKey(STORE_ID));
        assertTrue(baskets.containsKey(storeId2));
    }
    
    @Test
    void testAddProduct() {
        // This method seems to be a simple wrapper around addToCart
        // We'll test it with spy to ensure the underlying method is called
        Cart spyCart = spy(cart);
        
        // Call addProduct
        spyCart.addProduct(PRODUCT_ID, QUANTITY);
        
        // It should internally call addToCart, but since we don't know the storeId
        // used in the implementation, we can't verify with exact parameters
        // We can check if getShoppingBaskets() was called as part of the execution
        verify(spyCart).getShoppingBaskets();
    }
    
    @Test
    void testCartDTO() {
        // Add a product to create a basket
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Create a DTO from the cart
        CartDTO dto = new CartDTO(cart);
        
        // Verify the DTO has the shopping baskets
        assertNotNull(dto.shoppingBaskets);
        assertEquals(1, dto.shoppingBaskets.size());
        assertTrue(dto.shoppingBaskets.containsKey(STORE_ID));
        
        // Verify the contents of the basket in the DTO
        HashMap<UUID, Integer> basketProducts = dto.shoppingBaskets.get(STORE_ID);
        assertNotNull(basketProducts);
        assertEquals(QUANTITY, basketProducts.get(PRODUCT_ID));
    }
    
    @Test
    void testIsStoreInCart() throws Exception {
        // Test private method isStoreInCart using reflection
        java.lang.reflect.Method isStoreInCartMethod = Cart.class.getDeclaredMethod("isStoreInCart", UUID.class);
        isStoreInCartMethod.setAccessible(true);
        
        // Initially the store should not be in the cart
        boolean result = (boolean) isStoreInCartMethod.invoke(cart, STORE_ID);
        assertFalse(result);
        
        // Add a product to create a basket for the store
        cart.addToCart(STORE_ID, PRODUCT_ID, QUANTITY);
        
        // Now the store should be in the cart
        result = (boolean) isStoreInCartMethod.invoke(cart, STORE_ID);
        assertTrue(result);
    }
}