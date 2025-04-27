package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreTest {

    private Store store;
    private UUID storeId;
    private String storeName;
    private String storeDescription;
    
    @Mock
    private StoreFounder mockFounder;
    
    private final String FOUNDER_USERNAME = "founder";
    private final String OWNER_USERNAME = "owner";
    private final String MANAGER_USERNAME = "manager";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeId = UUID.randomUUID();
        storeName = "Test Store";
        storeDescription = "This is a test store";
        
        when(mockFounder.getUsername()).thenReturn(FOUNDER_USERNAME);
        
        store = new Store(storeName, storeDescription, mockFounder);
    }

    @Test
    void testConstructor() {
        assertNotNull(store.getStoreId());
        assertEquals(storeName, store.getName());
        assertEquals(storeDescription, store.getDescription());
        assertEquals(mockFounder, store.getFounder());
        assertTrue(store.isActive());
        assertNotNull(store.getCreationDate());
        
        // Founder should be added as an owner
        assertTrue(store.isStoreOwner(FOUNDER_USERNAME));
    }

    @Test
    void testAddAndRemoveProduct() {
        UUID productId = UUID.randomUUID();
        int quantity = 10;
        
        assertFalse(store.hasProduct(productId));
        
        store.addProduct(productId, quantity);
        
        assertTrue(store.hasProduct(productId));
        assertEquals(quantity, store.getProductQuantity(productId));
        
        store.removeProduct(productId);
        
        assertFalse(store.hasProduct(productId));
    }

    @Test
    void testAddProductWithNegativeQuantity() {
        UUID productId = UUID.randomUUID();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.addProduct(productId, -1);
        });
        
        String expectedMessage = "Product quantity cannot be negative";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testAddDuplicateProduct() {
        UUID productId = UUID.randomUUID();
        
        store.addProduct(productId, 5);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.addProduct(productId, 10);
        });
        
        String expectedMessage = "Product already exists in store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateProductQuantity() {
        UUID productId = UUID.randomUUID();
        int initialQuantity = 5;
        int newQuantity = 10;
        
        store.addProduct(productId, initialQuantity);
        assertEquals(initialQuantity, store.getProductQuantity(productId));
        
        store.updateProductQuantity(productId, newQuantity);
        assertEquals(newQuantity, store.getProductQuantity(productId));
    }

    @Test
    void testUpdateNonExistentProduct() {
        UUID productId = UUID.randomUUID();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.updateProductQuantity(productId, 10);
        });
        
        String expectedMessage = "Product does not exist in store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemoveNonExistentProduct() {
        UUID productId = UUID.randomUUID();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.removeProduct(productId);
        });
        
        String expectedMessage = "Product does not exist in store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCloseAndReopenStore() {
        assertTrue(store.isActive());
        
        store.closeStore();
        assertFalse(store.isActive());
        
        store.reopenStore();
        assertTrue(store.isActive());
    }

    @Test
    void testCloseAlreadyClosedStore() {
        store.closeStore();
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            store.closeStore();
        });
        
        String expectedMessage = "Store is already closed";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testReopenAlreadyOpenStore() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            store.reopenStore();
        });
        
        String expectedMessage = "Store is already open";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testAddAndRemoveOwner() {
        assertFalse(store.isStoreOwner(OWNER_USERNAME));
        
        store.addStoreOwner(OWNER_USERNAME);
        assertTrue(store.isStoreOwner(OWNER_USERNAME));
        
        store.removeStoreOwner(OWNER_USERNAME);
        assertFalse(store.isStoreOwner(OWNER_USERNAME));
    }

    @Test
    void testAddDuplicateOwner() {
        store.addStoreOwner(OWNER_USERNAME);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.addStoreOwner(OWNER_USERNAME);
        });
        
        String expectedMessage = "User is already an owner of this store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemoveFounder() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.removeStoreOwner(FOUNDER_USERNAME);
        });
        
        String expectedMessage = "Cannot remove the founder of the store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemoveNonExistentOwner() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.removeStoreOwner("nonexistentowner");
        });
        
        String expectedMessage = "User is not an owner of this store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testAddAndRemoveManager() {
        assertFalse(store.isStoreManager(MANAGER_USERNAME));
        
        store.addStoreManager(MANAGER_USERNAME);
        assertTrue(store.isStoreManager(MANAGER_USERNAME));
        
        store.removeStoreManager(MANAGER_USERNAME);
        assertFalse(store.isStoreManager(MANAGER_USERNAME));
    }

    @Test
    void testAddDuplicateManager() {
        store.addStoreManager(MANAGER_USERNAME);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.addStoreManager(MANAGER_USERNAME);
        });
        
        String expectedMessage = "User is already a manager of this store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemoveNonExistentManager() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.removeStoreManager("nonexistentmanager");
        });
        
        String expectedMessage = "User is not a manager of this store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testAddOrder() {
        UUID orderId = UUID.randomUUID();
        
        store.addOrder(orderId);
        
        assertTrue(store.getOrderIds().contains(orderId));
    }

    @Test
    void testAddDuplicateOrder() {
        UUID orderId = UUID.randomUUID();
        
        store.addOrder(orderId);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            store.addOrder(orderId);
        });
        
        String expectedMessage = "Order already exists in store";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCheckCartWithValidItems() {
        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();
        
        store.addProduct(product1, 10);
        store.addProduct(product2, 5);
        
        Map<UUID, Integer> items = new HashMap<>();
        items.put(product1, 5);
        items.put(product2, 3);
        
        Set<String> errors = store.checkCart(items);
        
        assertTrue(errors.isEmpty());
    }

    @Test
    void testCheckCartWithInsufficientQuantity() {
        UUID product1 = UUID.randomUUID();
        
        store.addProduct(product1, 5);
        
        Map<UUID, Integer> items = new HashMap<>();
        items.put(product1, 10);
        
        Set<String> errors = store.checkCart(items);
        
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }

    @Test
    void testCheckCartWithNonExistentProduct() {
        UUID product1 = UUID.randomUUID();
        
        Map<UUID, Integer> items = new HashMap<>();
        items.put(product1, 5);
        
        Set<String> errors = store.checkCart(items);
        
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }

    @Test
    void testUpdateStockAfterPurchase() {
        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();
        
        int initialQuantity1 = 10;
        int initialQuantity2 = 5;
        
        store.addProduct(product1, initialQuantity1);
        store.addProduct(product2, initialQuantity2);
        
        Map<UUID, Integer> items = new HashMap<>();
        int purchaseQuantity1 = 3;
        int purchaseQuantity2 = 2;
        items.put(product1, purchaseQuantity1);
        items.put(product2, purchaseQuantity2);
        
        Set<String> errors = store.updateStockAfterPurchase(items);
        
        assertTrue(errors.isEmpty());
        assertEquals(initialQuantity1 - purchaseQuantity1, store.getProductQuantity(product1));
        assertEquals(initialQuantity2 - purchaseQuantity2, store.getProductQuantity(product2));
    }
}