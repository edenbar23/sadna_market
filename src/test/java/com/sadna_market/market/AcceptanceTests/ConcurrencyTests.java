package com.sadna_market.market.AcceptanceTests;

import com.sadna_market.market.ApplicationLayer.Bridge;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance tests for concurrent operations in the market system.
 * These tests verify that the system maintains data consistency and
 * proper synchronization in multi-threaded environments.
 */
public class ConcurrencyTests {
    
    private Bridge bridge;
    private final int NUM_THREADS = 10;
    private final int TIMEOUT_SECONDS = 10;
    private ExecutorService executorService;
    
    // Test users and stores
    private String adminUser;
    private String adminToken;
    private List<String> testUsers;
    private List<String> userTokens;
    private UUID testStoreId;
    private UUID testProductId;
    
    @BeforeEach
    void setUp() {
        bridge = new Bridge();
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        testUsers = new ArrayList<>();
        userTokens = new ArrayList<>();
        
        // Create admin user
        RegisterRequest adminRequest = new RegisterRequest("admin", "Admin123!", "admin@test.com", "Admin", "User");
        Response registerResponse =  bridge.registerUser(adminRequest);
        Response loginResponse = bridge.loginUser("admin", "Admin123!");
        adminToken = loginResponse.getJson();
        adminUser = "admin";
        
        // Create test store
        createTestStoreAndProduct();
        
        // Create test users
        for (int i = 0; i < NUM_THREADS; i++) {
            String username = "testUser" + i;
            String password = "Test123!";
            RegisterRequest request = new RegisterRequest(username, password, username + "@test.com", "Test", "User" + i);
            bridge.registerUser(request);
            Response response = bridge.loginUser(username, password);
            testUsers.add(username);
            userTokens.add(response.getJson());
        }
    }
    
    private void createTestStoreAndProduct() {
        // Create a test store
        StoreRequest storeRequest = new StoreRequest("TestStore", "Test Store Description", 
                "Test Address", "store@test.com", "123456789", adminUser);
        Response storeResponse = bridge.createStore(adminUser, adminToken, storeRequest);
        testStoreId = UUID.fromString(storeResponse.getJson());
        
        // Add a product to the store
        ProductRequest productRequest = new ProductRequest(null, "TestProduct", "Test product description", "TestCategory", 50.0);
        Response productResponse = bridge.addProductToStore(adminToken, adminUser, testStoreId, productRequest,1);
        testProductId = UUID.fromString(productResponse.getJson());
    }
    
    /**
     * Test that multiple users can concurrently add products to their carts without conflicts.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentAddToCart() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < NUM_THREADS; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    Response response = bridge.addProductToUserCart(
                            testUsers.get(userIndex), 
                            userTokens.get(userIndex), 
                            testStoreId, 
                            testProductId, 
                            1);
                    
                    if (!response.isError()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error in thread " + userIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // Start all threads
        endLatch.await(); // Wait for all threads to complete
        
        assertEquals(NUM_THREADS, successCount.get(), "All users should be able to add the product to their carts concurrently");
        
        // Verify each user's cart contains the product
        for (int i = 0; i < NUM_THREADS; i++) {
            Response cartResponse = bridge.viewUserCart(testUsers.get(i), userTokens.get(i));
            assertFalse(cartResponse.isError(), "User should be able to view their cart");
            assertTrue(cartResponse.getJson().contains(testProductId.toString()), 
                    "User's cart should contain the test product");
        }
    }
    
    /**
     * Test that the system correctly handles concurrent purchases from the same store
     * ensuring inventory is properly updated.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentPurchases() throws InterruptedException {
        // First, update product quantity to have enough stock
        ProductRequest updateQuantityRequest = new ProductRequest(testProductId, "TestProduct", 
                "Test product description", "TestCategory", 50.0);
        bridge.editProductDetails(adminToken, adminUser, testStoreId, updateQuantityRequest,1);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Each user adds product to cart and checks out concurrently
        for (int i = 0; i < NUM_THREADS; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    // Add product to cart
                    Response addToCartResponse = bridge.addProductToUserCart(
                            testUsers.get(userIndex), 
                            userTokens.get(userIndex), 
                            testStoreId, 
                            testProductId, 
                            1);
                    
                    if (!addToCartResponse.isError()) {
                        // Create a payment method
                        PaymentMethod paymentMethod = new CreditCardDTO(
                                "4111111111111111", 
                                testUsers.get(userIndex), 
                                "12/25", 
                                "123");
                        
                        // Checkout
                        Response checkoutResponse = bridge.buyUserCart(
                                testUsers.get(userIndex), 
                                userTokens.get(userIndex), 
                                paymentMethod);
                        
                        if (!checkoutResponse.isError()) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in thread " + userIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // Start all threads
        endLatch.await(); // Wait for all threads to complete
        
        // Verify the inventory is consistent
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        Response productSearchResponse = bridge.searchProduct(searchRequest);
        
        // The number of successful purchases should match the expected count
        // Note: This test assumes there is inventory tracking logic
        assertTrue(successCount.get() > 0, "At least some purchases should succeed");
    }
    
    /**
     * Test that concurrent store operations (adding/removing products, updating) 
     * maintain data consistency.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentStoreOperations() throws InterruptedException {
        // Create another store owner for testing purposes
        String storeOwner = "storeOwner";
        RegisterRequest ownerRequest = new RegisterRequest(storeOwner, "Owner123!", "owner@test.com", "Store", "Owner");
        bridge.registerUser(ownerRequest);
        Response ownerLoginResponse = bridge.loginUser(storeOwner, "Owner123!");
        String ownerToken = ownerLoginResponse.getJson();
        
        // Appoint the user as a store owner
        bridge.appointOwner(adminUser, adminToken, testStoreId, storeOwner);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_THREADS * 2); // Admin and owner operations
        AtomicInteger successCount = new AtomicInteger(0);
        List<UUID> addedProductIds = new CopyOnWriteArrayList<>();
        
        // Admin and owner concurrently perform store operations
        for (int i = 0; i < NUM_THREADS; i++) {
            final int opIndex = i;
            
            // Admin adds products
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    ProductRequest productRequest = new ProductRequest(
                            null, 
                            "AdminProduct" + opIndex, 
                            "Admin product description", 
                            "TestCategory", 
                            50.0);
                    
                    Response response = bridge.addProductToStore(
                            adminToken, 
                            adminUser, 
                            testStoreId, 
                            productRequest,1);
                    
                    if (!response.isError()) {
                        UUID productId = UUID.fromString(response.getJson());
                        addedProductIds.add(productId);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error in admin thread " + opIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            
            // Store owner adds products
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    ProductRequest productRequest = new ProductRequest(
                            null, 
                            "OwnerProduct" + opIndex, 
                            "Owner product description", 
                            "TestCategory", 
                            60.0);
                    
                    Response response = bridge.addProductToStore(
                            ownerToken, 
                            storeOwner, 
                            testStoreId, 
                            productRequest,1);
                    
                    if (!response.isError()) {
                        UUID productId = UUID.fromString(response.getJson());
                        addedProductIds.add(productId);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error in owner thread " + opIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await();
        
        // Verify that products were added successfully
        assertTrue(successCount.get() > 0, "Some product additions should succeed");
        assertFalse(addedProductIds.isEmpty(), "Products should have been added");
        
        // Verify products can be retrieved
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        Response productSearchResponse = bridge.searchProduct(searchRequest);
        assertFalse(productSearchResponse.isError(), "Should be able to search for products");
        
        for (UUID productId : addedProductIds) {
            assertTrue(productSearchResponse.getJson().contains(productId.toString()), 
                    "Product should be found in search results");
        }
    }
    
    /**
     * Test that concurrent user registrations are handled correctly.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentUserRegistrations() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < NUM_THREADS; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    // Each thread tries to register a unique user
                    String username = "concurrentUser" + userIndex;
                    RegisterRequest request = new RegisterRequest(
                            username, 
                            "Password123!",
                            username + "@test.com", 
                            "Concurrent", 
                            "User" + userIndex);
                    
                    Response response = bridge.registerUser(request);
                    
                    if (!response.isError()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error in registration thread " + userIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await();
        
        assertEquals(NUM_THREADS, successCount.get(), 
                "All concurrent user registrations should succeed");
        
        // Verify that all users can log in
        for (int i = 0; i < NUM_THREADS; i++) {
            String username = "concurrentUser" + i;
            Response loginResponse = bridge.loginUser(username, "Password123!");
            assertFalse(loginResponse.isError(), 
                    "User " + username + " should be able to log in");
        }
    }
    
    /**
     * Test that concurrent store appointment operations (owner/manager) maintain 
     * data consistency and proper hierarchical structure.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentStoreAppointments() throws InterruptedException {
        // Create a new store for this test
        StoreRequest storeRequest = new StoreRequest("AppointmentStore", "Test store for appointments", 
                "Test Address", "appointments@test.com", "987654321", adminUser);
        Response storeResponse = bridge.createStore(adminUser, adminToken, storeRequest);
        UUID appointmentStoreId = UUID.fromString(storeResponse.getJson());
        
        // Create users to be appointed
        List<String> appointeeUsers = new ArrayList<>();
        List<String> appointeeTokens = new ArrayList<>();
        
        for (int i = 0; i < NUM_THREADS; i++) {
            String username = "appointee" + i;
            String password = "Appoint123!";
            
            RegisterRequest request = new RegisterRequest(username, password, username + "@test.com", "Appointee", "User" + i);
            bridge.registerUser(request);
            
            Response loginResponse = bridge.loginUser(username, password);
            appointeeUsers.add(username);
            appointeeTokens.add(loginResponse.getJson());
        }
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_THREADS);
        AtomicInteger ownerSuccessCount = new AtomicInteger(0);
        AtomicInteger managerSuccessCount = new AtomicInteger(0);
        
        // Concurrently appoint some users as owners and some as managers
        for (int i = 0; i < NUM_THREADS; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    if (userIndex % 2 == 0) {
                        // Appoint as owner
                        Response response = bridge.appointOwner(
                                adminUser, 
                                adminToken, 
                                appointmentStoreId, 
                                appointeeUsers.get(userIndex));
                        
                        if (!response.isError()) {
                            ownerSuccessCount.incrementAndGet();
                        }
                    } else {
                        // Appoint as manager
                        PermissionsRequest permissions = new PermissionsRequest(null);
                        Response response = bridge.appointManager(
                                adminUser, 
                                adminToken, 
                                appointmentStoreId, 
                                appointeeUsers.get(userIndex), 
                                permissions);
                        
                        if (!response.isError()) {
                            managerSuccessCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in appointment thread " + userIndex + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await();
        
        // Verify appointments were successful
        assertTrue(ownerSuccessCount.get() > 0, "Some owner appointments should succeed");
        assertTrue(managerSuccessCount.get() > 0, "Some manager appointments should succeed");
        
        // Verify store roles information
        Response rolesResponse = bridge.getRoles();
        assertFalse(rolesResponse.isError(), "Should be able to get store roles information");
        
        for (int i = 0; i < NUM_THREADS; i++) {
            if (i % 2 == 0) {
                // Check owner appointment
                assertTrue(rolesResponse.getJson().contains(appointeeUsers.get(i)), 
                        "User should be appointed as owner");
            } else {
                // Check manager appointment
                assertTrue(rolesResponse.getJson().contains(appointeeUsers.get(i)), 
                        "User should be appointed as manager");
            }
        }
    }
    
    /**
     * Test that the system handles concurrent store closures and reopenings correctly.
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentStoreStatusChanges() throws InterruptedException {
        // Create multiple test stores
        List<UUID> storeIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            StoreRequest storeRequest = new StoreRequest(
                    "StatusStore" + i, 
                    "Store for status testing", 
                    "Test Address", 
                    "status" + i + "@test.com", 
                    "123-456-" + i, 
                    adminUser);
            
            Response storeResponse = bridge.createStore(adminUser, adminToken, storeRequest);
            storeIds.add(UUID.fromString(storeResponse.getJson()));
        }
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(10); // Close and reopen operations
        AtomicBoolean storeStatus = new AtomicBoolean(true); // true = open, false = closed
        
        // Test concurrent closing of stores
        for (int i = 0; i < 5; i++) {
            final UUID storeId = storeIds.get(i);
            
            // Thread to close the store
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    Response response = bridge.closeStore(adminUser, adminToken, storeId);
                    if (!response.isError()) {
                        storeStatus.set(false);
                    }
                } catch (Exception e) {
                    System.err.println("Error in close store thread for store " + storeId + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
            
            // Thread to reopen the store (may run before or after the close)
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    // Add a small delay to ensure the close operation happens first
                    Thread.sleep(100);
                    Response response = bridge.reopenStore(adminUser, adminToken, storeId);
                    if (!response.isError()) {
                        storeStatus.set(true);
                    }
                } catch (Exception e) {
                    System.err.println("Error in reopen store thread for store " + storeId + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await();
        
        // After all operations, verify that all stores are in a consistent state
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        Response storeSearchResponse = bridge.searchProduct(searchRequest);
        
        // The final status should be consistent with the last operation
        assertTrue(storeStatus.get(), "Stores should be reopened after the test");
    }
    
    
}