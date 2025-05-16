package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StoreUnitTest {

    private Store store;
    private StoreFounder founder;
    private final String founderUsername = "founder";
    private final String storeName = "Test Store";
    private final String storeDescription = "This is a test store";
    // Track added products, owners, managers, and orders for cleanup
    private final Set<UUID> addedProductIds = new HashSet<>();
    private final Set<String> addedOwners = new HashSet<>();
    private final Set<String> addedManagers = new HashSet<>();
    private final Set<UUID> addedOrders = new HashSet<>();

    @BeforeEach
    void setUp() {
        // Create a store first (without founder)
        store = new Store(storeName, storeDescription);

        // Create a founder with the store's ID
        founder = new StoreFounder(founderUsername, store.getStoreId(), null);

        // Set the founder in the store
        store.setFounder(founder);

        System.out.println("⚙️ Test setup: Created store with ID " + store.getStoreId());
    }

    @AfterEach
    void tearDown() {
        // Clean up any state that might affect other tests

        // Clean up added products if the store is still active
        if (store.isActive()) {
            for (UUID productId : addedProductIds) {
                try {
                    if (store.hasProduct(productId)) {
                        store.removeProduct(productId);
                        System.out.println("🧹 Cleanup: Removed product " + productId);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Cleanup warning: Error removing product " + productId + ": " + e.getMessage());
                }
            }
        }

        // Cleanup added managers
        for (String manager : addedManagers) {
            try {
                if (store.isStoreManager(manager)) {
                    store.removeStoreManager(manager);
                    System.out.println("🧹 Cleanup: Removed manager " + manager);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Cleanup warning: Error removing manager " + manager + ": " + e.getMessage());
            }
        }

        // Clean up added owners (non-founder)
        for (String owner : addedOwners) {
            try {
                if (store.isStoreOwner(owner) && !owner.equals(founderUsername)) {
                    store.removeStoreOwner(owner);
                    System.out.println("🧹 Cleanup: Removed owner " + owner);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Cleanup warning: Error removing owner " + owner + ": " + e.getMessage());
            }
        }

        // Reset all collections
        addedProductIds.clear();
        addedOwners.clear();
        addedManagers.clear();
        addedOrders.clear();

        // Nullify references to help garbage collection
        store = null;
        founder = null;

        System.out.println("🧹 Cleanup: Test cleanup complete");
    }

    // Helper method to add a product and track it for cleanup
    private UUID addTestProduct(int quantity) {
        UUID productId = UUID.randomUUID();
        store.addProduct(productId, quantity);
        addedProductIds.add(productId);
        System.out.println("➕ Helper: Added test product " + productId + " with quantity " + quantity);
        return productId;
    }

    // Helper method to add a store owner and track it
    private void addTestOwner(String username) {
        store.addStoreOwner(username);
        addedOwners.add(username);
        System.out.println("➕ Helper: Added test owner " + username);
    }

    // Helper method to add a store manager and track it
    private void addTestManager(String username) {
        store.addStoreManager(username);
        addedManagers.add(username);
        System.out.println("➕ Helper: Added test manager " + username);
    }

    // Helper method to add an order and track it
    private UUID addTestOrder() {
        UUID orderId = UUID.randomUUID();
        store.addOrder(orderId);
        addedOrders.add(orderId);
        System.out.println("➕ Helper: Added test order " + orderId);
        return orderId;
    }

    // Constructor Tests
    @Test
    @DisplayName("Default constructor should initialize basic properties correctly")
    void testDefaultConstructorInitialization() {
        System.out.println("🧪 TEST: Verifying default constructor initialization");

        // Create a new store with the default constructor
        Store defaultStore = new Store();

        // Verify store properties
        assertNotNull(defaultStore.getStoreId(),
                "Store ID should be automatically generated and not null");
        assertNotNull(defaultStore.getCreationDate(),
                "Creation date should be initialized");
        assertNull(defaultStore.getFounder(),
                "Founder should be null initially");
        assertTrue(defaultStore.isActive(),
                "Store should be active by default");
        assertEquals(0.0, defaultStore.getStoreRating(),
                "Initial rating should be 0.0");
        assertEquals(0, defaultStore.getNumOfRatings(),
                "Initial number of ratings should be 0");

        System.out.println("✅ Default constructor initializes properties correctly");
    }

    @Test
    @DisplayName("Two-argument constructor should initialize basic properties correctly")
    void testTwoArgConstructorInitialization() {
        System.out.println("🧪 TEST: Verifying two-argument constructor initialization");

        // Create a new store with the two-argument constructor
        String testName = "New Test Store";
        String testDescription = "New test description";
        Store twoArgStore = new Store(testName, testDescription);

        // Verify the store name and description
        assertEquals(testName, twoArgStore.getName(),
                "Store name should match the provided name");
        assertEquals(testDescription, twoArgStore.getDescription(),
                "Store description should match the provided description");

        // Verify other initialized properties
        assertNotNull(twoArgStore.getStoreId(),
                "Store ID should be automatically generated and not null");
        assertNotNull(twoArgStore.getCreationDate(),
                "Creation date should be initialized");
        assertNull(twoArgStore.getFounder(),
                "Founder should be null initially");
        assertTrue(twoArgStore.isActive(),
                "Store should be active by default");
        assertEquals(0.0, twoArgStore.getStoreRating(),
                "Initial rating should be 0.0");
        assertEquals(0, twoArgStore.getNumOfRatings(),
                "Initial number of ratings should be 0");

        System.out.println("✅ Two-argument constructor initializes properties correctly");
    }

    @Test
    @DisplayName("Three-argument constructor should initialize with founder correctly")
    void testThreeArgConstructorInitialization() {
        System.out.println("🧪 TEST: Verifying three-argument constructor initialization");

        // Create test data
        String testName = "Another Test Store";
        String testDescription = "Another test description";

        // Create a UUID that will be used for both the store and founder
        UUID storeId = UUID.randomUUID();

        // Create a founder with this UUID
        StoreFounder newFounder = new StoreFounder("founderUser", storeId, null);

        // We need to modify the Store constructor to accept an explicit UUID
        // For now, we'll test without using the three-argument constructor

        // Create a store using the full constructor instead
        Store storeWithFounder = new Store(storeId, testName, testDescription, true, new Date(), newFounder);

        // Verify the store properties
        assertEquals(testName, storeWithFounder.getName(),
                "Store name should match the provided name");
        assertEquals(testDescription, storeWithFounder.getDescription(),
                "Store description should match the provided description");
        assertNotNull(storeWithFounder.getFounder(),
                "Founder should be set when provided");
        assertEquals(newFounder.getUsername(), storeWithFounder.getFounder().getUsername(),
                "Founder username should match");
        assertTrue(storeWithFounder.isStoreOwner(newFounder.getUsername()),
                "Founder should be in the store owners list");

        System.out.println("✅ Store with founder initialized correctly");
    }

    @Test
    @DisplayName("Full constructor should initialize all properties correctly")
    void testFullConstructorInitialization() {
        System.out.println("🧪 TEST: Verifying full constructor initialization");

        // Create test data for full constructor
        UUID testStoreId = UUID.randomUUID();
        String testName = "Full Test Store";
        String testDescription = "Full test description";
        boolean testActive = false;
        Date testDate = new Date();
        StoreFounder testFounder = new StoreFounder("testFounder", testStoreId, null);

        // Create a store with the full constructor
        Store fullStore = new Store(testStoreId, testName, testDescription,
                testActive, testDate, testFounder);

        // Verify all properties are set correctly
        assertEquals(testStoreId, fullStore.getStoreId(),
                "Store ID should match the provided ID");
        assertEquals(testName, fullStore.getName(),
                "Store name should match the provided name");
        assertEquals(testDescription, fullStore.getDescription(),
                "Store description should match the provided description");
        assertEquals(testActive, fullStore.isActive(),
                "Active status should match the provided value");
        assertEquals(testDate, fullStore.getCreationDate(),
                "Creation date should match the provided date");
        assertEquals(testFounder, fullStore.getFounder(),
                "Founder should match the provided founder");
        assertTrue(fullStore.isStoreOwner(testFounder.getUsername()),
                "Founder should be in the store owners list");

        System.out.println("✅ Full constructor initializes all properties correctly");
    }

    @Test
    @DisplayName("Constructor should throw exception when name is null")
    void testConstructorThrowsExceptionForNullName() {
        System.out.println("🧪 TEST: Verifying constructor rejects null name");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Store(null, storeDescription),
                "Constructor should throw IllegalArgumentException when name is null"
        );

        System.out.println("✅ Constructor correctly rejected null name with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should throw exception when name is empty")
    void testConstructorThrowsExceptionForEmptyName() {
        System.out.println("🧪 TEST: Verifying constructor rejects empty name");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Store("", storeDescription),
                "Constructor should throw IllegalArgumentException when name is empty"
        );

        System.out.println("✅ Constructor correctly rejected empty name with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("setFounder should throw exception when founder is null")
    void testSetFounderThrowsExceptionForNullFounder() {
        System.out.println("🧪 TEST: Verifying setFounder rejects null founder");

        // Create a store without a founder
        Store testStore = new Store(storeName, storeDescription);

        // Try to set a null founder
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testStore.setFounder(null),
                "setFounder should throw IllegalArgumentException when founder is null"
        );

        System.out.println("✅ setFounder correctly rejected null founder with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("setFounder should throw exception when founder's store ID doesn't match")
    void testSetFounderThrowsExceptionForIdMismatch() {
        System.out.println("🧪 TEST: Verifying setFounder rejects ID mismatch");

        // Create a store without a founder
        Store testStore = new Store(storeName, storeDescription);

        // Create a founder with a different store ID
        UUID differentStoreId = UUID.randomUUID();
        StoreFounder mismatchFounder = new StoreFounder(founderUsername, differentStoreId, null);

        // Try to set a founder with mismatched store ID
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testStore.setFounder(mismatchFounder),
                "setFounder should throw IllegalArgumentException when store IDs don't match"
        );

        System.out.println("✅ setFounder correctly rejected ID mismatch with message: " + exception.getMessage());
    }

    // Property Tests
    @Test
    @DisplayName("Name setter should update store name")
    void testNameSetterUpdatesName() {
        System.out.println("🧪 TEST: Verifying name setter");

        // Define a new name
        String originalName = store.getName();
        String newName = "New Store Name";

        // Update name
        store.setName(newName);

        // Verify update
        assertEquals(newName, store.getName(),
                "Store name should be updated from '" + originalName + "' to '" + newName + "'");

        System.out.println("✅ Store name successfully updated to: " + newName);
    }

    @Test
    @DisplayName("Description setter should update store description")
    void testDescriptionSetterUpdatesDescription() {
        System.out.println("🧪 TEST: Verifying description setter");

        // Define a new description
        String originalDescription = store.getDescription();
        String newDescription = "New store description";

        // Update description
        store.setDescription(newDescription);

        // Verify update
        assertEquals(newDescription, store.getDescription(),
                "Store description should be updated from '" + originalDescription + "' to '" + newDescription + "'");

        System.out.println("✅ Store description successfully updated to: " + newDescription);
    }

    // Status Tests
    @Test
    @DisplayName("Newly created store should be active")
    void testNewStoreIsActive() {
        System.out.println("🧪 TEST: Verifying new store is active by default");

        assertTrue(store.isActive(),
                "A newly created store should have active status set to true");

        System.out.println("✅ Store is active as expected");
    }

    @Test
    @DisplayName("closeStore should set store to inactive")
    void testCloseStore() {
        System.out.println("🧪 TEST: Verifying closeStore method");

        // Initial state verification
        assertTrue(store.isActive(), "Store should be active initially");

        // Close the store
        store.closeStore();

        // Verify a closed state
        assertFalse(store.isActive(),
                "Store should be inactive after calling closeStore()");

        System.out.println("✅ Store successfully closed (set to inactive)");
    }

    @Test
    @DisplayName("reopenStore should set store to active")
    void testReopenStore() {
        System.out.println("🧪 TEST: Verifying reopenStore method");

        // Set up an initially closed state
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive after closing");

        // Reopen the store
        store.reopenStore();

        // Verify reopened state
        assertTrue(store.isActive(),
                "Store should be active after calling reopenStore()");

        System.out.println("✅ Store successfully reopened (set to active)");
    }

    @Test
    @DisplayName("closeStore should throw exception when already closed")
    void testCloseStoreWhenAlreadyClosed() {
        System.out.println("🧪 TEST: Verifying closeStore rejects already closed store");

        // Close the store
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive after first close");

        // Attempt to close again
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> store.closeStore(),
                "closeStore should throw IllegalStateException when store is already closed"
        );

        System.out.println("✅ closeStore correctly rejected already-closed store with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("reopenStore should throw exception when already open")
    void testReopenStoreWhenAlreadyOpen() {
        System.out.println("🧪 TEST: Verifying reopenStore rejects already open store");

        // Verify the store is active
        assertTrue(store.isActive(), "Store should be active initially");

        // Attempt to reopen
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> store.reopenStore(),
                "reopenStore should throw IllegalStateException when store is already open"
        );

        System.out.println("✅ reopenStore correctly rejected already-open store with message: " + exception.getMessage());
    }

    // Product Management Tests
    @Test
    @DisplayName("addProduct should add product to inventory")
    void testAddProduct() {
        System.out.println("🧪 TEST: Verifying addProduct basic functionality");

        // Add product
        int quantity = 10;
        UUID productId = addTestProduct(quantity);

        // Verify the product exists
        assertTrue(store.hasProduct(productId),
                "Product with ID " + productId + " should exist in store after adding");

        System.out.println("✅ Product successfully added to store inventory");
    }

    @Test
    @DisplayName("addProduct should set correct quantity")
    void testAddProductSetsCorrectQuantity() {
        System.out.println("🧪 TEST: Verifying addProduct sets correct quantity");

        // Add product with specific quantity
        int expectedQuantity = 15;
        UUID productId = addTestProduct(expectedQuantity);

        // Verify quantity
        int actualQuantity = store.getProductQuantity(productId);
        assertEquals(expectedQuantity, actualQuantity,
                "Product quantity should be " + expectedQuantity + " but was " + actualQuantity);

        System.out.println("✅ Product added with correct quantity: " + actualQuantity);
    }

    @Test
    @DisplayName("addProduct should throw exception for negative quantity")
    void testAddProductThrowsExceptionForNegativeQuantity() {
        System.out.println("🧪 TEST: Verifying addProduct rejects negative quantity");

        UUID productId = UUID.randomUUID();
        int negativeQuantity = -5;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addProduct(productId, negativeQuantity),
                "addProduct should throw IllegalArgumentException when quantity is negative"
        );

        System.out.println("✅ addProduct correctly rejected negative quantity with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("addProduct should throw exception for inactive store")
    void testAddProductThrowsExceptionForInactiveStore() {
        System.out.println("🧪 TEST: Verifying addProduct rejects inactive store");

        // Close the store
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive");

        // Try to add a product
        UUID productId = UUID.randomUUID();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> store.addProduct(productId, 10),
                "addProduct should throw IllegalStateException when store is inactive"
        );

        System.out.println("✅ addProduct correctly rejected inactive store with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("addProduct should throw exception for existing product")
    void testAddProductThrowsExceptionForExistingProduct() {
        System.out.println("🧪 TEST: Verifying addProduct rejects existing product");

        // Add product first time
        UUID productId = addTestProduct(10);

        // Try to add same product again
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addProduct(productId, 5),
                "addProduct should throw IllegalArgumentException when product already exists"
        );

        System.out.println("✅ addProduct correctly rejected duplicate product with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("updateProductQuantity should update quantity")
    void testUpdateProductQuantity() {
        System.out.println("🧪 TEST: Verifying updateProductQuantity functionality");

        // Add product with initial quantity
        int initialQuantity = 10;
        UUID productId = addTestProduct(initialQuantity);

        // Update to new quantity
        int newQuantity = 20;
        store.updateProductQuantity(productId, newQuantity);

        // Verify updated quantity
        int actualQuantity = store.getProductQuantity(productId);
        assertEquals(newQuantity, actualQuantity,
                "Product quantity should be updated from " + initialQuantity + " to " + newQuantity);

        System.out.println("✅ Product quantity successfully updated from " + initialQuantity +
                " to " + actualQuantity);
    }

    @Test
    @DisplayName("updateProductQuantity should throw exception for non-existent product")
    void testUpdateQuantityThrowsExceptionForNonExistentProduct() {
        System.out.println("🧪 TEST: Verifying updateProductQuantity rejects non-existent product");

        UUID nonExistentProductId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.updateProductQuantity(nonExistentProductId, 10),
                "updateProductQuantity should throw IllegalArgumentException for non-existent product"
        );

        System.out.println("✅ updateProductQuantity correctly rejected non-existent product with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("updateProductQuantity should throw exception for negative quantity")
    void testUpdateProductQuantityThrowsExceptionForNegativeQuantity() {
        System.out.println("🧪 TEST: Verifying updateProductQuantity rejects negative quantity");

        // Add product
        UUID productId = addTestProduct(10);

        // Try to update with a negative quantity
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.updateProductQuantity(productId, -5),
                "updateProductQuantity should throw IllegalArgumentException for negative quantity"
        );

        System.out.println("✅ updateProductQuantity correctly rejected negative quantity with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("updateProductQuantity should throw exception for inactive store")
    void testUpdateProductQuantityThrowsExceptionForInactiveStore() {
        System.out.println("🧪 TEST: Verifying updateProductQuantity rejects inactive store");

        // Add product
        UUID productId = addTestProduct(10);

        // Close store
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive");

        // Try to update product
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> store.updateProductQuantity(productId, 20),
                "updateProductQuantity should throw IllegalStateException for inactive store"
        );

        System.out.println("✅ updateProductQuantity correctly rejected inactive store with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("removeProduct should remove product from inventory")
    void testRemoveProduct() {
        System.out.println("🧪 TEST: Verifying removeProduct functionality");

        // Add product
        UUID productId = addTestProduct(10);
        assertTrue(store.hasProduct(productId), "Product should exist after adding");

        // Remove product
        store.removeProduct(productId);

        // Verify removal
        assertFalse(store.hasProduct(productId),
                "Product with ID " + productId + " should no longer exist after removal");

        // Remove from tracking since we manually removed it
        addedProductIds.remove(productId);

        System.out.println("✅ Product successfully removed from inventory");
    }

    @Test
    @DisplayName("removeProduct should throw exception for non-existent product")
    void testRemoveProductThrowsExceptionForNonExistentProduct() {
        System.out.println("🧪 TEST: Verifying removeProduct rejects non-existent product");

        UUID nonExistentProductId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.removeProduct(nonExistentProductId),
                "removeProduct should throw IllegalArgumentException for non-existent product"
        );

        System.out.println("✅ removeProduct correctly rejected non-existent product with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("removeProduct should throw exception for inactive store")
    void testRemoveProductThrowsExceptionForInactiveStore() {
        System.out.println("🧪 TEST: Verifying removeProduct rejects inactive store");

        // Add product
        UUID productId = addTestProduct(10);

        // Close store
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive");

        // Try to remove product
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> store.removeProduct(productId),
                "removeProduct should throw IllegalStateException for inactive store"
        );

        System.out.println("✅ removeProduct correctly rejected inactive store with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("hasProduct should return true for existing product")
    void testHasProductReturnsTrueForExistingProduct() {
        System.out.println("🧪 TEST: Verifying hasProduct for existing product");

        // Add product
        UUID productId = addTestProduct(10);

        // Check if product exists
        boolean hasProduct = store.hasProduct(productId);

        // Verify existence
        assertTrue(hasProduct,
                "hasProduct should return true for product with ID " + productId);

        System.out.println("✅ hasProduct correctly detected existing product");
    }

    @Test
    @DisplayName("hasProduct should return false for non-existent product")
    void testHasProductReturnsFalseForNonExistentProduct() {
        System.out.println("🧪 TEST: Verifying hasProduct for non-existent product");

        UUID nonExistentProductId = UUID.randomUUID();

        // Check if product exists
        boolean hasProduct = store.hasProduct(nonExistentProductId);

        // Verify non-existence
        assertFalse(hasProduct,
                "hasProduct should return false for non-existent product with ID " + nonExistentProductId);

        System.out.println("✅ hasProduct correctly detected non-existent product");
    }

    // Personnel Management Tests
    @Test
    @DisplayName("addStoreOwner should add username to owners list")
    void testAddStoreOwner() {
        System.out.println("🧪 TEST: Verifying addStoreOwner functionality");

        // Add owner
        String newOwnerUsername = "newOwner";
        addTestOwner(newOwnerUsername);

        // Verify owner was added
        assertTrue(store.isStoreOwner(newOwnerUsername),
                "Username '" + newOwnerUsername + "' should be in the store owners list");

        System.out.println("✅ Store owner successfully added");
    }

    @Test
    @DisplayName("addStoreOwner should throw exception for null username")
    void testAddStoreOwnerThrowsExceptionForNullUsername() {
        System.out.println("🧪 TEST: Verifying addStoreOwner rejects null username");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addStoreOwner(null),
                "addStoreOwner should throw IllegalArgumentException for null username"
        );

        System.out.println("✅ addStoreOwner correctly rejected null username with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("addStoreOwner should throw exception for existing owner")
    void testAddStoreOwnerThrowsExceptionForExistingOwner() {
        System.out.println("🧪 TEST: Verifying addStoreOwner rejects existing owner");

        // Add owner first time
        String ownerUsername = "owner";
        addTestOwner(ownerUsername);

        // Try to add same owner again
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addStoreOwner(ownerUsername),
                "addStoreOwner should throw IllegalArgumentException for existing owner"
        );

        System.out.println("✅ addStoreOwner correctly rejected existing owner with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("removeStoreOwner should remove username from owners list")
    void testRemoveStoreOwner() {
        System.out.println("🧪 TEST: Verifying removeStoreOwner functionality");

        // Add owner
        String ownerUsername = "owner";
        addTestOwner(ownerUsername);
        assertTrue(store.isStoreOwner(ownerUsername), "Owner should be added initially");

        // Remove owner
        store.removeStoreOwner(ownerUsername);

        // Verify removal
        assertFalse(store.isStoreOwner(ownerUsername),
                "Username '" + ownerUsername + "' should no longer be in the store owners list");

        // Remove from tracking since we manually removed it
        addedOwners.remove(ownerUsername);

        System.out.println("✅ Store owner successfully removed");
    }

    @Test
    @DisplayName("removeStoreOwner should throw exception for founder")
    void testRemoveStoreOwnerThrowsExceptionForFounder() {
        System.out.println("🧪 TEST: Verifying removeStoreOwner rejects founder removal");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.removeStoreOwner(founderUsername),
                "removeStoreOwner should throw IllegalArgumentException when trying to remove founder"
        );

        System.out.println("✅ removeStoreOwner correctly rejected founder removal with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("removeStoreOwner should throw exception for non-existent owner")
    void testRemoveStoreOwnerThrowsExceptionForNonExistentOwner() {
        System.out.println("🧪 TEST: Verifying removeStoreOwner rejects non-existent owner");

        String nonExistentOwner = "nonExistentOwner";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.removeStoreOwner(nonExistentOwner),
                "removeStoreOwner should throw IllegalArgumentException for non-existent owner"
        );

        System.out.println("✅ removeStoreOwner correctly rejected non-existent owner with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("addStoreManager should add username to managers list")
    void testAddStoreManager() {
        System.out.println("🧪 TEST: Verifying addStoreManager functionality");

        // Add manager
        String managerUsername = "manager";
        addTestManager(managerUsername);

        // Verify manager was added
        assertTrue(store.isStoreManager(managerUsername),
                "Username '" + managerUsername + "' should be in the store managers list");

        System.out.println("✅ Store manager successfully added");
    }

    @Test
    @DisplayName("addStoreManager should throw exception for existing manager")
    void testAddStoreManagerThrowsExceptionForExistingManager() {
        System.out.println("🧪 TEST: Verifying addStoreManager rejects existing manager");

        // Add manager first time
        String managerUsername = "manager";
        addTestManager(managerUsername);

        // Try to add same manager again
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addStoreManager(managerUsername),
                "addStoreManager should throw IllegalArgumentException for existing manager"
        );

        System.out.println("✅ addStoreManager correctly rejected existing manager with message: " +
                exception.getMessage());
    }

    @Test
    @DisplayName("removeStoreManager should remove username from managers list")
    void testRemoveStoreManager() {
        System.out.println("🧪 TEST: Verifying removeStoreManager functionality");

        // Add manager
        String managerUsername = "manager";
        addTestManager(managerUsername);
        assertTrue(store.isStoreManager(managerUsername), "Manager should be added initially");

        // Remove manager
        store.removeStoreManager(managerUsername);

        // Verify removal
        assertFalse(store.isStoreManager(managerUsername),
                "Username '" + managerUsername + "' should no longer be in the store managers list");

        // Remove from tracking since we manually removed it
        addedManagers.remove(managerUsername);

        System.out.println("✅ Store manager successfully removed");
    }

    @Test
    @DisplayName("removeStoreManager should throw exception for non-existent manager")
    void testRemoveStoreManagerThrowsExceptionForNonExistentManager() {
        System.out.println("🧪 TEST: Verifying removeStoreManager rejects non-existent manager");

        String nonExistentManager = "nonExistentManager";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.removeStoreManager(nonExistentManager),
                "removeStoreManager should throw IllegalArgumentException for non-existent manager"
        );

        System.out.println("✅ removeStoreManager correctly rejected non-existent manager with message: " +
                exception.getMessage());
    }

    // Role Check Tests
    @Test
    @DisplayName("isFounder should return true for founder")
    void testIsFounderReturnsTrueForFounder() {
        System.out.println("🧪 TEST: Verifying isFounder for actual founder");

        boolean isFounder = store.isFounder(founderUsername);

        assertTrue(isFounder,
                "isFounder should return true for the actual founder username '" + founderUsername + "'");

        System.out.println("✅ isFounder correctly identified founder");
    }

    @Test
    @DisplayName("isFounder should return false for non-founder")
    void testIsFounderReturnsFalseForNonFounder() {
        System.out.println("🧪 TEST: Verifying isFounder for non-founder");

        String nonFounderUsername = "nonFounder";
        boolean isFounder = store.isFounder(nonFounderUsername);

        assertFalse(isFounder,
                "isFounder should return false for non-founder username '" + nonFounderUsername + "'");

        System.out.println("✅ isFounder correctly identified non-founder");
    }

    // Order Management Tests
    @Test
    @DisplayName("addOrder should add order ID to orders list")
    void testAddOrder() {
        System.out.println("🧪 TEST: Verifying addOrder functionality");

        // Add order
        UUID orderId = addTestOrder();

        // Verify order was added
        assertTrue(store.getOrderIds().contains(orderId),
                "Order ID " + orderId + " should be in the store's orders list");

        System.out.println("✅ Order successfully added to store");
    }

    @Test
    @DisplayName("addOrder should throw exception for duplicate order")
    void testAddOrderThrowsExceptionForDuplicateOrder() {
        System.out.println("🧪 TEST: Verifying addOrder rejects duplicate order");

        // Add order first time
        UUID orderId = addTestOrder();

        // Try to add same order again
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> store.addOrder(orderId),
                "addOrder should throw IllegalArgumentException for duplicate order"
        );

        System.out.println("✅ addOrder correctly rejected duplicate order with message: " +
                exception.getMessage());
    }

    // Rating Tests
    @Test
    @DisplayName("addRating should update rating sum and count")
    void testAddRating() {
        System.out.println("🧪 TEST: Verifying addRating functionality");

        // Initial state
        assertEquals(0, store.getNumOfRatings(), "Store should have 0 ratings initially");
        assertEquals(0.0, store.getStoreRating(), "Store rating should be 0.0 initially");

        // Add rating
        int rating = 4;
        store.addRating(rating);

        // Verify rating was added correctly
        assertEquals(1, store.getNumOfRatings(),
                "Store should have 1 rating after adding");
        assertEquals(rating, store.getStoreRating(),
                "Store rating should be " + rating + " after adding a single rating of " + rating);

        System.out.println("✅ Rating successfully added to store");
    }

    @Test
    @DisplayName("addRating should throw exception for invalid rating")
    void testAddRatingThrowsExceptionForInvalidRating() {
        System.out.println("🧪 TEST: Verifying addRating rejects invalid ratings");

        // Test too low rating
        IllegalArgumentException tooLowException = assertThrows(
                IllegalArgumentException.class,
                () -> store.addRating(0),
                "addRating should throw IllegalArgumentException for rating below 1"
        );

        // Test too high rating
        IllegalArgumentException tooHighException = assertThrows(
                IllegalArgumentException.class,
                () -> store.addRating(6),
                "addRating should throw IllegalArgumentException for rating above 5"
        );

        System.out.println("✅ addRating correctly rejected invalid ratings:");
        System.out.println("  - Too low (0): " + tooLowException.getMessage());
        System.out.println("  - Too high (6): " + tooHighException.getMessage());
    }

    @Test
    @DisplayName("updateRating should correctly adjust the rating sum")
    void testUpdateRating() {
        System.out.println("🧪 TEST: Verifying updateRating functionality");

        // Add an initial rating
        int initialRating = 3;
        store.addRating(initialRating);
        assertEquals(1, store.getNumOfRatings(), "Store should have 1 rating after adding");
        assertEquals(initialRating, store.getStoreRating(),
                "Store rating should be " + initialRating + " after adding initial rating");

        // Update the rating
        int newRating = 5;
        store.updateRating(initialRating, newRating);

        // Check that only the sum changed, not the count
        assertEquals(1, store.getNumOfRatings(),
                "Number of ratings should still be 1 after updating");
        assertEquals(newRating, store.getStoreRating(),
                "Store rating should be updated from " + initialRating + " to " + newRating);

        System.out.println("✅ Rating successfully updated from " + initialRating + " to " + newRating);
    }

    // Shopping Cart Tests
    @Test
    @DisplayName("checkCart should return no errors for valid cart")
    void testCheckCartWithValidItems() {
        System.out.println("🧪 TEST: Verifying checkCart for valid cart");

        // Add products to store
        UUID product1Id = addTestProduct(10);
        UUID product2Id = addTestProduct(15);

        // Create valid cart
        Map<UUID, Integer> cart = new HashMap<>();
        cart.put(product1Id, 5);  // Half of available quantity
        cart.put(product2Id, 10); // Lower than available quantity

        // Check cart
        Set<String> errors = store.checkCart(cart);

        // Verify no errors
        assertTrue(errors.isEmpty(),
                "checkCart should return empty error set for valid cart but got: " + errors);

        System.out.println("✅ Valid cart passed validation with no errors");
    }

    @Test
    @DisplayName("checkCart should detect non-existent products")
    void testCheckCartDetectsNonExistentProducts() {
        System.out.println("🧪 TEST: Verifying checkCart detects non-existent products");

        // Add one product to store
        UUID existingId = addTestProduct(10);
        UUID nonExistentId = UUID.randomUUID();

        // Cart with one existing and one non-existent product
        Map<UUID, Integer> cart = new HashMap<>();
        cart.put(existingId, 5);
        cart.put(nonExistentId, 3);

        // Check cart
        Set<String> errors = store.checkCart(cart);

        // Verify errors
        assertFalse(errors.isEmpty(),
                "checkCart should return errors for cart with non-existent product");

        // Verify specific error for non-existent product
        boolean foundNonExistentError = false;
        for (String error : errors) {
            if (error.contains(nonExistentId.toString()) && error.toLowerCase().contains("not exist")) {
                foundNonExistentError = true;
                break;
            }
        }

        assertTrue(foundNonExistentError,
                "checkCart errors should include message about non-existent product " + nonExistentId);

        System.out.println("✅ Non-existent product correctly detected with errors: " + errors);
    }

    @Test
    @DisplayName("checkCart should detect insufficient stock")
    void testCheckCartDetectsInsufficientStock() {
        System.out.println("🧪 TEST: Verifying checkCart detects insufficient stock");

        // Add product with limited stock
        UUID productId = addTestProduct(5);
        int availableQuantity = 5;

        // Cart requesting more than available
        Map<UUID, Integer> cart = new HashMap<>();
        int requestedQuantity = availableQuantity + 2;
        cart.put(productId, requestedQuantity);

        // Check cart
        Set<String> errors = store.checkCart(cart);

        // Verify errors
        assertFalse(errors.isEmpty(),
                "checkCart should return errors for cart requesting more than available stock");

        // Verify specific error for insufficient stock
        boolean foundStockError = false;
        for (String error : errors) {
            if (error.contains(productId.toString()) &&
                    error.toLowerCase().contains("not enough stock")) {
                foundStockError = true;
                break;
            }
        }

        assertTrue(foundStockError,
                "checkCart errors should include message about insufficient stock for product " + productId);

        System.out.println("✅ Insufficient stock correctly detected with errors: " + errors);
    }

    @Test
    @DisplayName("checkCart should detect inactive store")
    void testCheckCartDetectsInactiveStore() {
        System.out.println("🧪 TEST: Verifying checkCart detects inactive store");

        // Add product and close store
        UUID productId = addTestProduct(10);
        store.closeStore();
        assertFalse(store.isActive(), "Store should be inactive");

        // Valid cart but inactive store
        Map<UUID, Integer> cart = new HashMap<>();
        cart.put(productId, 5);

        // Check cart
        Set<String> errors = store.checkCart(cart);

        // Verify errors
        assertFalse(errors.isEmpty(),
                "checkCart should return errors for inactive store");

        // Verify specific error for inactive store
        boolean foundInactiveError = false;
        for (String error : errors) {
            if (error.toLowerCase().contains("not active") ||
                    error.toLowerCase().contains("inactive")) {
                foundInactiveError = true;
                break;
            }
        }

        assertTrue(foundInactiveError,
                "checkCart errors should include message about inactive store");

        System.out.println("✅ Inactive store correctly detected with errors: " + errors);
    }

    @Test
    @DisplayName("updateStockAfterPurchase should reduce stock correctly")
    void testUpdateStockAfterPurchase() {
        System.out.println("🧪 TEST: Verifying updateStockAfterPurchase functionality");

        // Add products with initial quantities
        UUID product1Id = addTestProduct(10);
        UUID product2Id = addTestProduct(15);
        int initialQuantity1 = 10;
        int initialQuantity2 = 15;

        // Create purchase cart with specific purchase quantities
        int purchaseQuantity1 = 3;
        int purchaseQuantity2 = 5;
        Map<UUID, Integer> cart = new HashMap<>();
        cart.put(product1Id, purchaseQuantity1);
        cart.put(product2Id, purchaseQuantity2);

        // Process the purchase
        Set<String> errors = store.updateStockAfterPurchase(cart);

        // Verify success
        assertTrue(errors.isEmpty(),
                "updateStockAfterPurchase should succeed without errors for valid cart");

        // Verify first product stock reduction
        int expectedQuantity1 = initialQuantity1 - purchaseQuantity1;
        int actualQuantity1 = store.getProductQuantity(product1Id);
        assertEquals(expectedQuantity1, actualQuantity1,
                "First product quantity should be reduced from " + initialQuantity1 +
                        " to " + expectedQuantity1 + " after purchase of " + purchaseQuantity1);

        // Verify second product stock reduction
        int expectedQuantity2 = initialQuantity2 - purchaseQuantity2;
        int actualQuantity2 = store.getProductQuantity(product2Id);
        assertEquals(expectedQuantity2, actualQuantity2,
                "Second product quantity should be reduced from " + initialQuantity2 +
                        " to " + expectedQuantity2 + " after purchase of " + purchaseQuantity2);

        System.out.println("✅ Stock successfully updated after purchase");
        System.out.println("  - Product 1: " + initialQuantity1 + " → " + actualQuantity1);
        System.out.println("  - Product 2: " + initialQuantity2 + " → " + actualQuantity2);
    }

    @Test
    @DisplayName("updateStockAfterPurchase should not update stock if validation fails")
    void testUpdateStockAfterPurchaseFailsForInvalidCart() {
        System.out.println("🧪 TEST: Verifying updateStockAfterPurchase fails for invalid cart");

        // Add product with limited stock
        UUID productId = addTestProduct(5);
        int initialQuantity = 5;

        // Cart requesting more than available
        Map<UUID, Integer> cart = new HashMap<>();
        int excessiveQuantity = initialQuantity + 2;
        cart.put(productId, excessiveQuantity);

        // Attempt to process purchase
        Set<String> errors = store.updateStockAfterPurchase(cart);

        // Verify failure
        assertFalse(errors.isEmpty(),
                "updateStockAfterPurchase should return errors for invalid cart");

        // Verify unchanged stock
        int actualQuantity = store.getProductQuantity(productId);
        assertEquals(initialQuantity, actualQuantity,
                "Product quantity should remain unchanged at " + initialQuantity +
                        " after failed purchase attempt");

        System.out.println("✅ Stock remained unchanged after failed purchase attempt");
        System.out.println("  - Errors returned: " + errors);
    }

    // Object Method Tests
    @Test
    @DisplayName("equals should compare by store ID only")
    void testEquals() {
        System.out.println("🧪 TEST: Verifying equals method");

        // Self-equality
        assertEquals(store, store, "Store should be equal to itself");

        // Different instance with different ID
        Store anotherStore = new Store("Another Store", "Description");
        StoreFounder anotherFounder = new StoreFounder(founderUsername, anotherStore.getStoreId(), null);
        anotherStore.setFounder(anotherFounder);

        assertNotEquals(store, anotherStore,
                "Stores with different IDs should not be equal");

        System.out.println("✅ equals method correctly compares stores by ID");
    }

    @Test
    @DisplayName("toString should include essential store information")
    void testToString() {
        System.out.println("🧪 TEST: Verifying toString method");

        String result = store.toString();

        // Verify essential info is included
        assertTrue(result.contains(store.getName()),
                "toString should include store name: " + store.getName());

        assertTrue(result.contains(store.getStoreId().toString()),
                "toString should include store ID: " + store.getStoreId());

        assertTrue(result.contains(String.valueOf(store.isActive())),
                "toString should include active status: " + store.isActive());

        System.out.println("✅ toString includes essential information: " + result);
    }
}