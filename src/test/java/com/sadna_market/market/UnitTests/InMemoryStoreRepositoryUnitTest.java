package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryStoreRepositoryUnitTest {

    private IStoreRepository repository;
    private UUID storeId;
    private String storeName;
    private String founderUsername;
     private RepositoryConfiguration RC = new RepositoryConfiguration();
    
    @Mock
    private StoreFounder mockFounder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
       // repository = new InMemoryStoreRepository();
       repository=RC.storeRepository();
        // Set up test data
        storeName = "Test Store";
        founderUsername = "testFounder";
        String address = "123 Test Street";
        String email = "test@example.com";
        String phoneNumber = "123-456-7890";
        
        // Set up mock founder
        when(mockFounder.getUsername()).thenReturn(founderUsername);
        
        // Create a store
        storeId = repository.createStore(founderUsername, storeName, address, email, phoneNumber);
    }

    @Test
    void testCreateStore() {
        // Verify the store was created
        assertTrue(repository.exists(storeId));
        
        // Get the store and verify details
        Optional<Store> storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        
        Store store = storeOpt.get();
        assertEquals(storeName, store.getName());
        assertEquals(founderUsername, store.getFounder().getUsername());
        assertTrue(store.isActive());
    }

    @Test
    void testFindById() {
        // Find existing store
        Optional<Store> storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        assertEquals(storeName, storeOpt.get().getName());
        
        // Find non-existent store
        UUID nonExistentId = UUID.randomUUID();
        Optional<Store> nonExistentStore = repository.findById(nonExistentId);
        assertFalse(nonExistentStore.isPresent());
    }

    @Test
    void testFindByName() {
        // Find existing store
        Optional<Store> storeOpt = repository.findByName(storeName);
        assertTrue(storeOpt.isPresent());
        assertEquals(storeId, storeOpt.get().getStoreId());
        
        // Find non-existent store
        String nonExistentName = "Non-existent Store";
        Optional<Store> nonExistentStore = repository.findByName(nonExistentName);
        assertFalse(nonExistentStore.isPresent());
    }

    @Test
    void testFindAll() {
        // Initially should have one store
        List<Store> allStores = repository.findAll();
        assertEquals(1, allStores.size());
        assertEquals(storeId, allStores.get(0).getStoreId());
        
        // Add another store
        String anotherName = "Another Store";
        repository.createStore(founderUsername, anotherName, "Address", "email@example.com", "987-654-3210");
        
        // Now should have two stores
        allStores = repository.findAll();
        assertEquals(2, allStores.size());
    }

    @Test
    void testDeleteById() {
        // Store exists initially
        assertTrue(repository.exists(storeId));
        
        // Delete the store
        repository.deleteById(storeId);
        
        // Verify it's gone
        assertFalse(repository.exists(storeId));
    }

    @Test
    void testExists() {
        // Check existing store
        assertTrue(repository.exists(storeId));
        
        // Check non-existent store
        UUID nonExistentId = UUID.randomUUID();
        assertFalse(repository.exists(nonExistentId));
    }

    @Test
    void testSave() {
        // Get the existing store
        Optional<Store> storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        Store store = storeOpt.get();
        
        // Modify and save
        String newDescription = "Updated Description";
        store.setDescription(newDescription);
        repository.save(store);
        
        // Verify the update
        Optional<Store> updatedStoreOpt = repository.findById(storeId);
        assertTrue(updatedStoreOpt.isPresent());
        assertEquals(newDescription, updatedStoreOpt.get().getDescription());
    }

    @Test
    void testUpdateStoreStatus() {
        // Initially active
        Optional<Store> storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        assertTrue(storeOpt.get().isActive());
        
        // Update to inactive
        repository.updateStoreStatus(storeId, false);
        
        // Verify
        storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        assertFalse(storeOpt.get().isActive());
        
        // Update back to active
        repository.updateStoreStatus(storeId, true);
        
        // Verify
        storeOpt = repository.findById(storeId);
        assertTrue(storeOpt.isPresent());
        assertTrue(storeOpt.get().isActive());
    }

    @Test
    void testUpdateStoreStatusNonExistentStore() {
        // Update status of non-existent store - should not throw exception
        UUID nonExistentId = UUID.randomUUID();
        repository.updateStoreStatus(nonExistentId, false);
    }

    @Test
    void testAddAndRemoveOwner() {
        // Add an owner
        String ownerUsername = "testOwner";
        repository.addOwner(storeId, ownerUsername);
        
        // Verify owner was added
        assertTrue(repository.isOwner(storeId, ownerUsername));
        
        // Remove the owner
        repository.removeOwner(storeId, ownerUsername);
        
        // Verify owner was removed
        assertFalse(repository.isOwner(storeId, ownerUsername));
    }

    @Test
    void testAddAndRemoveManager() {
        // Add a manager
        String managerUsername = "testManager";
        repository.addManager(storeId, managerUsername);
        
        // Verify manager was added
        assertTrue(repository.isManager(storeId, managerUsername));
        
        // Remove the manager
        repository.removeManager(storeId, managerUsername);
        
        // Verify manager was removed
        assertFalse(repository.isManager(storeId, managerUsername));
    }

    @Test
    void testIsOwnerAndGetStoreOwners() {
        // Initially, only the founder should be an owner
        assertTrue(repository.isOwner(storeId, founderUsername));
        
        // Add another owner
        String ownerUsername = "testOwner";
        repository.addOwner(storeId, ownerUsername);
        
        // Verify both are owners
        assertTrue(repository.isOwner(storeId, founderUsername));
        assertTrue(repository.isOwner(storeId, ownerUsername));
        
        // Get all owners
        Set<String> owners = repository.getStoreOwners(storeId);
        assertEquals(2, owners.size());
        assertTrue(owners.contains(founderUsername));
        assertTrue(owners.contains(ownerUsername));
    }

    @Test
    void testIsManagerAndGetStoreManagers() {
        // Initially, there should be no managers
        String managerUsername = "testManager";
        assertFalse(repository.isManager(storeId, managerUsername));
        
        // Add a manager
        repository.addManager(storeId, managerUsername);
        
        // Verify manager was added
        assertTrue(repository.isManager(storeId, managerUsername));
        
        // Get all managers
        Set<String> managers = repository.getStoreManagers(storeId);
        assertEquals(1, managers.size());
        assertTrue(managers.contains(managerUsername));
    }

    @Test
    void testGetStoreFounder() {
        // Verify founder
        String founder = repository.getStoreFounder(storeId);
        assertEquals(founderUsername, founder);
        
        // Non-existent store
        UUID nonExistentId = UUID.randomUUID();
        assertNull(repository.getStoreFounder(nonExistentId));
    }

    @Test
    void testProductManagement() {
        // Add a product
        UUID productId = UUID.randomUUID();
        int quantity = 10;
        repository.addProduct(storeId, productId, quantity);
        
        // Verify product was added
        assertTrue(repository.hasProduct(storeId, productId));
        assertEquals(quantity, repository.getProductQuantity(storeId, productId));
        
        // Update quantity
        int newQuantity = 5;
        repository.updateProductQuantity(storeId, productId, newQuantity);
        
        // Verify quantity was updated
        assertEquals(newQuantity, repository.getProductQuantity(storeId, productId));
        
        // Check sufficient stock
        assertTrue(repository.hasProductInStock(storeId, productId, 3));
        
        // Check insufficient stock
        assertFalse(repository.hasProductInStock(storeId, productId, 6));
        
        // Get all products
        Map<UUID, Integer> products = repository.getAllProductsInStore(storeId);
        assertEquals(1, products.size());
        assertTrue(products.containsKey(productId));
        assertEquals(newQuantity, products.get(productId));
        
        // Remove the product
        repository.removeProduct(storeId, productId);
        
        // Verify product was removed
        assertFalse(repository.hasProduct(storeId, productId));
    }

    @Test
    void testOrderManagement() {
        // Add an order
        UUID orderId = UUID.randomUUID();
        repository.addOrderIdToStore(storeId, orderId);
        
        // Get orders
        List<UUID> orders = repository.getStoreOrdersIds(storeId);
        assertEquals(1, orders.size());
        assertEquals(orderId, orders.get(0));
    }

    @Test
    void testProductManagementNonExistentStore() {
        UUID nonExistentId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        // These should not throw exceptions
        repository.addProduct(nonExistentId, productId, 10);
        repository.updateProductQuantity(nonExistentId, productId, 5);
        repository.removeProduct(nonExistentId, productId);
        
        // These should return expected default values
        assertFalse(repository.hasProduct(nonExistentId, productId));
        assertFalse(repository.hasProductInStock(nonExistentId, productId, 1));
        assertTrue(repository.getAllProductsInStore(nonExistentId).isEmpty());
    }

    @Test
    void testOrderManagementNonExistentStore() {
        UUID nonExistentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        
        // These should not throw exceptions
        repository.addOrderIdToStore(nonExistentId, orderId);
        
        // These should return expected default values
        assertTrue(repository.getStoreOrdersIds(nonExistentId).isEmpty());
    }

    @Test
    void testFindByProductId() {
        // Add a product
        UUID productId = UUID.randomUUID();
        repository.addProduct(storeId, productId, 10);
        
        // Find stores with this product
        Set<Store> stores = repository.findByProductId(productId);
        assertEquals(1, stores.size());
        assertEquals(storeId, stores.iterator().next().getStoreId());
        
        // Find stores with non-existent product
        UUID nonExistentProductId = UUID.randomUUID();
        stores = repository.findByProductId(nonExistentProductId);
        assertTrue(stores.isEmpty());
    }

    @Test
    void testFindByProductCategory() {
        // This is a limited implementation in InMemoryStoreRepository
        String category = "Test Category";
        Set<Store> stores = repository.findByProductCategory(category);
        assertTrue(stores.isEmpty());
    }

    @Test
    void testGetFilteredProductIds() {
        // This is a limited implementation in InMemoryStoreRepository
        String namePattern = "Test";
        String category = "Test Category";
        Double maxPrice = 100.0;
        Double minRating = 4.0;
        
        Set<UUID> productIds = repository.getFilteredProductIds(storeId, namePattern, category, maxPrice, minRating);
        assertTrue(productIds.isEmpty());
    }
}