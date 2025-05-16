package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryStoreRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStoreRepositoryUnitTest {

    private InMemoryStoreRepository storeRepository;
    private UUID testStoreId;
    private String testStoreName;
    private String testFounder;
    private Store testStore;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");
        storeRepository = new InMemoryStoreRepository();
        testFounder = "testFounder";
        testStoreName = "TestStore";

        // Create a test store
        testStoreId = storeRepository.createStore(testFounder, testStoreName, "Test Address", "test@example.com", "123456789");
        System.out.println("Created test store with ID: " + testStoreId);
        System.out.println("Store name: " + testStoreName);
        System.out.println("Store founder: " + testFounder);

        // Get the store for use in tests
        Optional<Store> storeOpt = storeRepository.findById(testStoreId);
        assertTrue(storeOpt.isPresent(), "Test store should be found");
        testStore = storeOpt.get();
        System.out.println("Retrieved test store successfully");
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        storeRepository.clear();
        testStore = null;
        System.out.println("Store repository cleared");
        System.out.println("Test store reference set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    // CRUD Operations Tests

    @Test
    void testFindById_ExistingStore_ReturnsStore() {
        System.out.println("TEST: Verifying findById with existing store");

        System.out.println("Looking for store with ID: " + testStoreId);
        Optional<Store> result = storeRepository.findById(testStoreId);

        System.out.println("Expected: Store should be present");
        System.out.println("Actual: Store is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Store should be found");

        System.out.println("Expected store ID: " + testStoreId);
        System.out.println("Actual store ID: " + result.get().getStoreId());
        assertEquals(testStoreId, result.get().getStoreId(), "Store ID should match");

        System.out.println("Expected store name: " + testStoreName);
        System.out.println("Actual store name: " + result.get().getName());
        assertEquals(testStoreName, result.get().getName(), "Store name should match");

        System.out.println("✓ findById correctly returns the store");
    }

    @Test
    void testFindById_NonExistingStore_ReturnsEmpty() {
        System.out.println("TEST: Verifying findById with non-existing store");

        UUID nonExistingId = UUID.randomUUID();
        System.out.println("Looking for non-existing store with ID: " + nonExistingId);
        Optional<Store> result = storeRepository.findById(nonExistingId);

        System.out.println("Expected: Store should not be present");
        System.out.println("Actual: Store is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Store should not be found");

        System.out.println("✓ findById correctly returns empty for non-existing store");
    }

    @Test
    void testFindByName_ExistingStore_ReturnsStore() {
        System.out.println("TEST: Verifying findByName with existing store");

        System.out.println("Looking for store with name: " + testStoreName);
        Optional<Store> result = storeRepository.findByName(testStoreName);

        System.out.println("Expected: Store should be present");
        System.out.println("Actual: Store is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Store should be found");

        System.out.println("Expected store ID: " + testStoreId);
        System.out.println("Actual store ID: " + result.get().getStoreId());
        assertEquals(testStoreId, result.get().getStoreId(), "Store ID should match");

        System.out.println("Expected store name: " + testStoreName);
        System.out.println("Actual store name: " + result.get().getName());
        assertEquals(testStoreName, result.get().getName(), "Store name should match");

        System.out.println("✓ findByName correctly returns the store");
    }

    @Test
    void testFindByName_NonExistingStore_ReturnsEmpty() {
        System.out.println("TEST: Verifying findByName with non-existing store");

        String nonExistingName = "NonExistingStore";
        System.out.println("Looking for non-existing store with name: " + nonExistingName);
        Optional<Store> result = storeRepository.findByName(nonExistingName);

        System.out.println("Expected: Store should not be present");
        System.out.println("Actual: Store is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Store should not be found");

        System.out.println("✓ findByName correctly returns empty for non-existing store");
    }

    @Test
    void testFindAll_ReturnsAllStores() {
        System.out.println("TEST: Verifying findAll returns all stores");

        // Create another store
        String anotherStoreName = "AnotherStore";
        UUID anotherStoreId = storeRepository.createStore("anotherFounder", anotherStoreName,
                "Another Address", "another@example.com", "987654321");
        System.out.println("Created another test store with ID: " + anotherStoreId);
        System.out.println("Created another test store with name: " + anotherStoreName);

        List<Store> stores = storeRepository.findAll();

        System.out.println("Expected number of stores: 2");
        System.out.println("Actual number of stores: " + stores.size());
        assertEquals(2, stores.size(), "Should return 2 stores");

        boolean containsFirstStore = stores.stream().anyMatch(s -> s.getStoreId().equals(testStoreId));
        System.out.println("Expected to contain first store: true");
        System.out.println("Actually contains first store: " + containsFirstStore);
        assertTrue(containsFirstStore, "Should contain first store");

        boolean containsSecondStore = stores.stream().anyMatch(s -> s.getStoreId().equals(anotherStoreId));
        System.out.println("Expected to contain second store: true");
        System.out.println("Actually contains second store: " + containsSecondStore);
        assertTrue(containsSecondStore, "Should contain second store");

        System.out.println("✓ findAll correctly returns all stores");
    }

    @Test
    void testDeleteById_ExistingStore_RemovesStore() {
        System.out.println("TEST: Verifying deleteById removes an existing store");

        System.out.println("Deleting store with ID: " + testStoreId);
        storeRepository.deleteById(testStoreId);

        Optional<Store> result = storeRepository.findById(testStoreId);
        System.out.println("Expected: Store should not be present after deletion");
        System.out.println("Actual: Store is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Store should be deleted");

        System.out.println("✓ deleteById correctly removes the store");
    }

    @Test
    void testExists_ExistingStore_ReturnsTrue() {
        System.out.println("TEST: Verifying exists returns true for existing store");

        System.out.println("Checking if store with ID exists: " + testStoreId);
        boolean exists = storeRepository.exists(testStoreId);

        System.out.println("Expected: true");
        System.out.println("Actual: " + exists);
        assertTrue(exists, "Store should exist");

        System.out.println("✓ exists correctly returns true for existing store");
    }

    @Test
    void testExists_NonExistingStore_ReturnsFalse() {
        System.out.println("TEST: Verifying exists returns false for non-existing store");

        UUID nonExistingId = UUID.randomUUID();
        System.out.println("Checking if non-existing store with ID exists: " + nonExistingId);
        boolean exists = storeRepository.exists(nonExistingId);

        System.out.println("Expected: false");
        System.out.println("Actual: " + exists);
        assertFalse(exists, "Store should not exist");

        System.out.println("✓ exists correctly returns false for non-existing store");
    }

    @Test
    void testSave_NewStore_StoresSuccessfully() {
        System.out.println("TEST: Verifying save stores a new store successfully");

        // Create a new store object to save
        UUID newStoreId = UUID.randomUUID();
        String newStoreName = "NewStore";
        StoreFounder newFounder = new StoreFounder("newFounder", newStoreId, null);

        System.out.println("Creating new store with ID: " + newStoreId);
        System.out.println("Creating new store with name: " + newStoreName);
        Store newStore = new Store(newStoreId, newStoreName, "Test Description", true, new Date(), newFounder);

        Store savedStore = storeRepository.save(newStore);
        System.out.println("Expected: saved store should match provided store");
        assertEquals(newStore, savedStore, "Saved store should be the same");

        Optional<Store> foundStore = storeRepository.findById(newStoreId);
        System.out.println("Expected: Store should be found after saving");
        System.out.println("Actual: Store is present = " + foundStore.isPresent());
        assertTrue(foundStore.isPresent(), "Store should be found");

        System.out.println("Expected store ID: " + newStoreId);
        System.out.println("Actual store ID: " + foundStore.get().getStoreId());
        assertEquals(newStoreId, foundStore.get().getStoreId(), "Store ID should match");

        System.out.println("Expected store name: " + newStoreName);
        System.out.println("Actual store name: " + foundStore.get().getName());
        assertEquals(newStoreName, foundStore.get().getName(), "Store name should match");

        System.out.println("✓ save correctly stores a new store");
    }

    // Store Status Tests

    @Test
    void testUpdateStoreStatus_Open_StoreIsActive() {
        System.out.println("TEST: Verifying updateStoreStatus sets store to active");

        // First close the store
        System.out.println("First closing the store with ID: " + testStoreId);
        storeRepository.updateStoreStatus(testStoreId, false);

        Optional<Store> closedStore = storeRepository.findById(testStoreId);
        System.out.println("Expected: Store should be closed");
        System.out.println("Actual: Store is active = " + closedStore.get().isActive());
        assertFalse(closedStore.get().isActive(), "Store should be closed");

        // Then open it
        System.out.println("Now reopening the store");
        storeRepository.updateStoreStatus(testStoreId, true);

        Optional<Store> reopenedStore = storeRepository.findById(testStoreId);
        System.out.println("Expected: Store should be open");
        System.out.println("Actual: Store is active = " + reopenedStore.get().isActive());
        assertTrue(reopenedStore.get().isActive(), "Store should be open");

        System.out.println("✓ updateStoreStatus correctly sets store to active");
    }

    @Test
    void testUpdateStoreStatus_Close_StoreIsInactive() {
        System.out.println("TEST: Verifying updateStoreStatus sets store to inactive");

        System.out.println("Closing store with ID: " + testStoreId);
        storeRepository.updateStoreStatus(testStoreId, false);

        Optional<Store> result = storeRepository.findById(testStoreId);
        System.out.println("Expected: Store should be closed");
        System.out.println("Actual: Store is active = " + result.get().isActive());
        assertFalse(result.get().isActive(), "Store should be closed");

        System.out.println("✓ updateStoreStatus correctly sets store to inactive");
    }

    // Store Personnel Management Tests

    @Test
    void testAddOwner_NewOwner_OwnerAdded() {
        System.out.println("TEST: Verifying addOwner adds a new owner");

        String newOwner = "newOwner";
        System.out.println("Adding new owner: " + newOwner + " to store: " + testStoreId);
        storeRepository.addOwner(testStoreId, newOwner);

        Set<String> owners = storeRepository.getStoreOwners(testStoreId);
        System.out.println("Expected: Owners set should contain new owner");
        System.out.println("Actual: Owners set contains new owner = " + owners.contains(newOwner));
        assertTrue(owners.contains(newOwner), "New owner should be added");

        System.out.println("✓ addOwner correctly adds a new owner");
    }

    @Test
    void testRemoveOwner_ExistingOwner_OwnerRemoved() {
        System.out.println("TEST: Verifying removeOwner removes an existing owner");

        String newOwner = "newOwner";
        System.out.println("First adding new owner: " + newOwner);
        storeRepository.addOwner(testStoreId, newOwner);

        Set<String> ownersBeforeRemoval = storeRepository.getStoreOwners(testStoreId);
        System.out.println("Owners before removal: " + ownersBeforeRemoval);
        System.out.println("Contains owner before removal: " + ownersBeforeRemoval.contains(newOwner));

        System.out.println("Now removing owner: " + newOwner);
        storeRepository.removeOwner(testStoreId, newOwner);

        Set<String> owners = storeRepository.getStoreOwners(testStoreId);
        System.out.println("Expected: Owners set should not contain removed owner");
        System.out.println("Actual: Owners set contains removed owner = " + owners.contains(newOwner));
        assertFalse(owners.contains(newOwner), "Owner should be removed");

        System.out.println("✓ removeOwner correctly removes an existing owner");
    }

    @Test
    void testAddManager_NewManager_ManagerAdded() {
        System.out.println("TEST: Verifying addManager adds a new manager");

        String newManager = "newManager";
        System.out.println("Adding new manager: " + newManager + " to store: " + testStoreId);
        storeRepository.addManager(testStoreId, newManager);

        Set<String> managers = storeRepository.getStoreManagers(testStoreId);
        System.out.println("Expected: Managers set should contain new manager");
        System.out.println("Actual: Managers set contains new manager = " + managers.contains(newManager));
        assertTrue(managers.contains(newManager), "New manager should be added");

        System.out.println("✓ addManager correctly adds a new manager");
    }

    @Test
    void testRemoveManager_ExistingManager_ManagerRemoved() {
        System.out.println("TEST: Verifying removeManager removes an existing manager");

        String newManager = "newManager";
        System.out.println("First adding new manager: " + newManager);
        storeRepository.addManager(testStoreId, newManager);

        Set<String> managersBeforeRemoval = storeRepository.getStoreManagers(testStoreId);
        System.out.println("Managers before removal: " + managersBeforeRemoval);
        System.out.println("Contains manager before removal: " + managersBeforeRemoval.contains(newManager));

        System.out.println("Now removing manager: " + newManager);
        storeRepository.removeManager(testStoreId, newManager);

        Set<String> managers = storeRepository.getStoreManagers(testStoreId);
        System.out.println("Expected: Managers set should not contain removed manager");
        System.out.println("Actual: Managers set contains removed manager = " + managers.contains(newManager));
        assertFalse(managers.contains(newManager), "Manager should be removed");

        System.out.println("✓ removeManager correctly removes an existing manager");
    }

    @Test
    void testIsOwner_ExistingOwner_ReturnsTrue() {
        System.out.println("TEST: Verifying isOwner returns true for existing owner");

        System.out.println("Checking if user is an owner: " + testFounder);
        boolean isOwner = storeRepository.isOwner(testStoreId, testFounder);

        System.out.println("Expected: true (founder should be an owner)");
        System.out.println("Actual: " + isOwner);
        assertTrue(isOwner, "Founder should be an owner");

        System.out.println("✓ isOwner correctly returns true for existing owner");
    }

    @Test
    void testIsOwner_NonExistingOwner_ReturnsFalse() {
        System.out.println("TEST: Verifying isOwner returns false for non-existing owner");

        String nonExistingOwner = "nonExistingOwner";
        System.out.println("Checking if non-existing user is an owner: " + nonExistingOwner);
        boolean isOwner = storeRepository.isOwner(testStoreId, nonExistingOwner);

        System.out.println("Expected: false");
        System.out.println("Actual: " + isOwner);
        assertFalse(isOwner, "Non-existing user should not be an owner");

        System.out.println("✓ isOwner correctly returns false for non-existing owner");
    }

    @Test
    void testIsManager_ExistingManager_ReturnsTrue() {
        System.out.println("TEST: Verifying isManager returns true for existing manager");

        String newManager = "newManager";
        System.out.println("First adding new manager: " + newManager);
        storeRepository.addManager(testStoreId, newManager);

        System.out.println("Checking if user is a manager: " + newManager);
        boolean isManager = storeRepository.isManager(testStoreId, newManager);

        System.out.println("Expected: true");
        System.out.println("Actual: " + isManager);
        assertTrue(isManager, "Added user should be a manager");

        System.out.println("✓ isManager correctly returns true for existing manager");
    }

    @Test
    void testIsManager_NonExistingManager_ReturnsFalse() {
        System.out.println("TEST: Verifying isManager returns false for non-existing manager");

        String nonExistingManager = "nonExistingManager";
        System.out.println("Checking if non-existing user is a manager: " + nonExistingManager);
        boolean isManager = storeRepository.isManager(testStoreId, nonExistingManager);

        System.out.println("Expected: false");
        System.out.println("Actual: " + isManager);
        assertFalse(isManager, "Non-existing user should not be a manager");

        System.out.println("✓ isManager correctly returns false for non-existing manager");
    }

    @Test
    void testGetStoreOwners_ReturnsAllOwners() {
        System.out.println("TEST: Verifying getStoreOwners returns all owners");

        String newOwner = "newOwner";
        System.out.println("Adding new owner: " + newOwner);
        storeRepository.addOwner(testStoreId, newOwner);

        Set<String> owners = storeRepository.getStoreOwners(testStoreId);

        System.out.println("Expected number of owners: 2");
        System.out.println("Actual number of owners: " + owners.size());
        assertEquals(2, owners.size(), "Should have 2 owners");

        System.out.println("Expected to contain founder: " + testFounder);
        System.out.println("Actually contains founder: " + owners.contains(testFounder));
        assertTrue(owners.contains(testFounder), "Should contain founder");

        System.out.println("Expected to contain new owner: " + newOwner);
        System.out.println("Actually contains new owner: " + owners.contains(newOwner));
        assertTrue(owners.contains(newOwner), "Should contain new owner");

        System.out.println("✓ getStoreOwners correctly returns all owners");
    }

    @Test
    void testGetStoreFounder_ReturnsFounder() {
        System.out.println("TEST: Verifying getStoreFounder returns the correct founder");

        System.out.println("Getting founder for store: " + testStoreId);
        String founder = storeRepository.getStoreFounder(testStoreId);

        System.out.println("Expected founder: " + testFounder);
        System.out.println("Actual founder: " + founder);
        assertEquals(testFounder, founder, "Founder should match");

        System.out.println("✓ getStoreFounder correctly returns the founder");
    }

    // Inventory Management Tests

    @Test
    void testAddProduct_NewProduct_ProductAdded() {
        System.out.println("TEST: Verifying addProduct adds a new product");

        UUID productId = UUID.randomUUID();
        int quantity = 10;

        System.out.println("Adding product with ID: " + productId + " and quantity: " + quantity);
        storeRepository.addProduct(testStoreId, productId, quantity);

        boolean hasProduct = storeRepository.hasProduct(testStoreId, productId);
        System.out.println("Expected: Store has product = true");
        System.out.println("Actual: Store has product = " + hasProduct);
        assertTrue(hasProduct, "Product should be added");

        int storedQuantity = storeRepository.getProductQuantity(testStoreId, productId);
        System.out.println("Expected quantity: " + quantity);
        System.out.println("Actual quantity: " + storedQuantity);
        assertEquals(quantity, storedQuantity, "Product quantity should match");

        System.out.println("✓ addProduct correctly adds a new product");
    }

    @Test
    void testRemoveProduct_ExistingProduct_ProductRemoved() {
        System.out.println("TEST: Verifying removeProduct removes an existing product");

        UUID productId = UUID.randomUUID();
        System.out.println("First adding product with ID: " + productId);
        storeRepository.addProduct(testStoreId, productId, 10);

        boolean hasProductBeforeRemoval = storeRepository.hasProduct(testStoreId, productId);
        System.out.println("Store has product before removal: " + hasProductBeforeRemoval);

        System.out.println("Now removing product with ID: " + productId);
        storeRepository.removeProduct(testStoreId, productId);

        boolean hasProduct = storeRepository.hasProduct(testStoreId, productId);
        System.out.println("Expected: Store has product after removal = false");
        System.out.println("Actual: Store has product after removal = " + hasProduct);
        assertFalse(hasProduct, "Product should be removed");

        System.out.println("✓ removeProduct correctly removes an existing product");
    }

    @Test
    void testHasProductInStock_SufficientQuantity_ReturnsTrue() {
        System.out.println("TEST: Verifying hasProductInStock returns true with sufficient quantity");

        UUID productId = UUID.randomUUID();
        int quantity = 10;
        System.out.println("Adding product with ID: " + productId + " and quantity: " + quantity);
        storeRepository.addProduct(testStoreId, productId, quantity);

        int requestedQuantity = 5;
        System.out.println("Checking if store has " + requestedQuantity + " items of product " + productId + " in stock");
        boolean inStock = storeRepository.hasProductInStock(testStoreId, productId, requestedQuantity);

        System.out.println("Expected: true");
        System.out.println("Actual: " + inStock);
        assertTrue(inStock, "Product should be in stock");

        System.out.println("✓ hasProductInStock correctly returns true with sufficient quantity");
    }

    @Test
    void testHasProductInStock_InsufficientQuantity_ReturnsFalse() {
        System.out.println("TEST: Verifying hasProductInStock returns false with insufficient quantity");

        UUID productId = UUID.randomUUID();
        int quantity = 10;
        System.out.println("Adding product with ID: " + productId + " and quantity: " + quantity);
        storeRepository.addProduct(testStoreId, productId, quantity);

        int requestedQuantity = 15;
        System.out.println("Checking if store has " + requestedQuantity + " items of product " + productId + " in stock");
        boolean inStock = storeRepository.hasProductInStock(testStoreId, productId, requestedQuantity);

        System.out.println("Expected: false");
        System.out.println("Actual: " + inStock);
        assertFalse(inStock, "Product should not be in stock in required quantity");

        System.out.println("✓ hasProductInStock correctly returns false with insufficient quantity");
    }

    @Test
    void testUpdateProductQuantity_ExistingProduct_QuantityUpdated() {
        System.out.println("TEST: Verifying updateProductQuantity updates quantity correctly");

        UUID productId = UUID.randomUUID();
        int initialQuantity = 10;
        System.out.println("Adding product with ID: " + productId + " and initial quantity: " + initialQuantity);
        storeRepository.addProduct(testStoreId, productId, initialQuantity);

        int newQuantity = 20;
        System.out.println("Updating product quantity to: " + newQuantity);
        storeRepository.updateProductQuantity(testStoreId, productId, newQuantity);

        int quantity = storeRepository.getProductQuantity(testStoreId, productId);
        System.out.println("Expected quantity after update: " + newQuantity);
        System.out.println("Actual quantity after update: " + quantity);
        assertEquals(newQuantity, quantity, "Quantity should be updated");

        System.out.println("✓ updateProductQuantity correctly updates product quantity");
    }

    @Test
    void testGetAllProductsInStore_ReturnsAllProducts() {
        System.out.println("TEST: Verifying getAllProductsInStore returns all products");

        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();
        int quantity1 = 10;
        int quantity2 = 20;

        System.out.println("Adding product 1 with ID: " + product1 + " and quantity: " + quantity1);
        storeRepository.addProduct(testStoreId, product1, quantity1);

        System.out.println("Adding product 2 with ID: " + product2 + " and quantity: " + quantity2);
        storeRepository.addProduct(testStoreId, product2, quantity2);

        Map<UUID, Integer> products = storeRepository.getAllProductsInStore(testStoreId);

        System.out.println("Expected number of products: 2");
        System.out.println("Actual number of products: " + products.size());
        assertEquals(2, products.size(), "Should have 2 products");

        System.out.println("Expected to contain product 1: true");
        System.out.println("Actually contains product 1: " + products.containsKey(product1));
        assertTrue(products.containsKey(product1), "Should contain first product");

        System.out.println("Expected to contain product 2: true");
        System.out.println("Actually contains product 2: " + products.containsKey(product2));
        assertTrue(products.containsKey(product2), "Should contain second product");

        System.out.println("Expected quantity for product 1: " + quantity1);
        System.out.println("Actual quantity for product 1: " + products.get(product1));
        assertEquals(quantity1, (int)products.get(product1), "First product quantity should match");

        System.out.println("Expected quantity for product 2: " + quantity2);
        System.out.println("Actual quantity for product 2: " + products.get(product2));
        assertEquals(quantity2, (int)products.get(product2), "Second product quantity should match");

        System.out.println("✓ getAllProductsInStore correctly returns all products");
    }

    // Order Management Tests

    @Test
    void testAddOrderIdToStore_NewOrder_OrderAdded() {
        System.out.println("TEST: Verifying addOrderIdToStore adds a new order");

        UUID orderId = UUID.randomUUID();
        System.out.println("Adding order with ID: " + orderId + " to store: " + testStoreId);
        storeRepository.addOrderIdToStore(testStoreId, orderId);

        List<UUID> orders = storeRepository.getStoreOrdersIds(testStoreId);
        System.out.println("Expected: Orders list should contain the order");
        System.out.println("Actual: Orders list contains the order = " + orders.contains(orderId));
        assertTrue(orders.contains(orderId), "Order should be added");

        System.out.println("✓ addOrderIdToStore correctly adds a new order");
    }

    @Test
    void testGetStoreOrdersIds_ReturnsAllOrders() {
        System.out.println("TEST: Verifying getStoreOrdersIds returns all orders");

        UUID order1 = UUID.randomUUID();
        UUID order2 = UUID.randomUUID();

        System.out.println("Adding order 1 with ID: " + order1);
        storeRepository.addOrderIdToStore(testStoreId, order1);

        System.out.println("Adding order 2 with ID: " + order2);
        storeRepository.addOrderIdToStore(testStoreId, order2);

        List<UUID> orders = storeRepository.getStoreOrdersIds(testStoreId);

        System.out.println("Expected number of orders: 2");
        System.out.println("Actual number of orders: " + orders.size());
        assertEquals(2, orders.size(), "Should have 2 orders");

        System.out.println("Expected to contain order 1: true");
        System.out.println("Actually contains order 1: " + orders.contains(order1));
        assertTrue(orders.contains(order1), "Should contain first order");

        System.out.println("Expected to contain order 2: true");
        System.out.println("Actually contains order 2: " + orders.contains(order2));
        assertTrue(orders.contains(order2), "Should contain second order");

        System.out.println("✓ getStoreOrdersIds correctly returns all orders");
    }
}