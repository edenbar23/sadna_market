package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryOrderRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryOrderRepository Unit Tests")
public class InMemoryOrderRepositoryUnitTest {

    private InMemoryOrderRepository orderRepository;
    private UUID testOrderId;
    private UUID testStoreId;
    private String testUserName;
    private UUID testDeliveryId;
    private Map<UUID, Integer> testProducts;
    private double testTotalPrice;
    private double testFinalPrice;
    private LocalDateTime testOrderDate;
    private OrderStatus testOrderStatus;
    private UUID testPaymentId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");
        orderRepository = new InMemoryOrderRepository();
        testStoreId = UUID.randomUUID();
        testUserName = "testUser";
        testProducts = new HashMap<>();

        // Add some test products
        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();
        testProducts.put(product1, 2);
        testProducts.put(product2, 3);

        testTotalPrice = 100.0;
        testFinalPrice = 90.0; // With discount
        testOrderDate = LocalDateTime.now();
        testOrderStatus = OrderStatus.PENDING;
        testPaymentId = UUID.randomUUID();

        // Create a test order
        testOrderId = orderRepository.createOrder(
                testStoreId,
                testUserName,
                testProducts,
                testTotalPrice,
                testFinalPrice,
                testOrderDate,
                testOrderStatus,
                testPaymentId
        );

        System.out.println("Created test order with ID: " + testOrderId);
        System.out.println("Store ID: " + testStoreId);
        System.out.println("User name: " + testUserName);

        // Get the order for use in tests
        Optional<Order> orderOpt = orderRepository.findById(testOrderId);
        assertTrue(orderOpt.isPresent(), "Test order should be found");
        testOrder = orderOpt.get();
        System.out.println("Retrieved test order successfully");
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        orderRepository.clear();
        testOrder = null;
        System.out.println("Order repository cleared");
        System.out.println("Test order reference set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    // CRUD Operation Tests

    @Test
    @DisplayName("findById should return an order when it exists")
    void testFindById_ExistingOrder_ReturnsOrder() {
        System.out.println("TEST: Verifying findById with existing order");

        System.out.println("Looking for order with ID: " + testOrderId);
        Optional<Order> result = orderRepository.findById(testOrderId);

        System.out.println("Expected: Order should be present");
        System.out.println("Actual: Order is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Order should be found");

        System.out.println("Expected order ID: " + testOrderId);
        System.out.println("Actual order ID: " + result.get().getOrderId());
        assertEquals(testOrderId, result.get().getOrderId(), "Order ID should match");

        System.out.println("Expected store ID: " + testStoreId);
        System.out.println("Actual store ID: " + result.get().getStoreId());
        assertEquals(testStoreId, result.get().getStoreId(), "Store ID should match");

        System.out.println("Expected username: " + testUserName);
        System.out.println("Actual username: " + result.get().getUserName());
        assertEquals(testUserName, result.get().getUserName(), "Username should match");

        System.out.println("✓ findById correctly returns the order");
    }

    @Test
    @DisplayName("findById should return empty when order does not exist")
    void testFindById_NonExistingOrder_ReturnsEmpty() {
        System.out.println("TEST: Verifying findById with non-existing order");

        UUID nonExistingId = UUID.randomUUID();
        System.out.println("Looking for non-existing order with ID: " + nonExistingId);
        Optional<Order> result = orderRepository.findById(nonExistingId);

        System.out.println("Expected: Order should not be present");
        System.out.println("Actual: Order is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Order should not be found");

        System.out.println("✓ findById correctly returns empty for non-existing order");
    }

    @Test
    @DisplayName("findAll should return all orders")
    void testFindAll_ReturnsAllOrders() {
        System.out.println("TEST: Verifying findAll returns all orders");

        // Create another order
        UUID anotherStoreId = UUID.randomUUID();
        String anotherUserName = "anotherUser";
        Map<UUID, Integer> anotherProducts = new HashMap<>();
        anotherProducts.put(UUID.randomUUID(), 1);

        UUID anotherOrderId = orderRepository.createOrder(
                anotherStoreId,
                anotherUserName,
                anotherProducts,
                50.0,
                45.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID()
        );
        System.out.println("Created another test order with ID: " + anotherOrderId);

        List<Order> orders = orderRepository.findAll();

        System.out.println("Expected number of orders: 2");
        System.out.println("Actual number of orders: " + orders.size());
        assertEquals(2, orders.size(), "Should return 2 orders");

        boolean containsFirstOrder = orders.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected to contain first order: true");
        System.out.println("Actually contains first order: " + containsFirstOrder);
        assertTrue(containsFirstOrder, "Should contain first order");

        boolean containsSecondOrder = orders.stream().anyMatch(o -> o.getOrderId().equals(anotherOrderId));
        System.out.println("Expected to contain second order: true");
        System.out.println("Actually contains second order: " + containsSecondOrder);
        assertTrue(containsSecondOrder, "Should contain second order");

        System.out.println("✓ findAll correctly returns all orders");
    }

    @Test
    @DisplayName("deleteById should remove an existing order")
    void testDeleteById_ExistingOrder_RemovesOrder() {
        System.out.println("TEST: Verifying deleteById removes an existing order");

        System.out.println("Deleting order with ID: " + testOrderId);
        orderRepository.deleteById(testOrderId);

        Optional<Order> result = orderRepository.findById(testOrderId);
        System.out.println("Expected: Order should not be present after deletion");
        System.out.println("Actual: Order is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Order should be deleted");

        System.out.println("✓ deleteById correctly removes the order");
    }

    @Test
    @DisplayName("exists should return true for existing order")
    void testExists_ExistingOrder_ReturnsTrue() {
        System.out.println("TEST: Verifying exists returns true for existing order");

        System.out.println("Checking if order with ID exists: " + testOrderId);
        boolean exists = orderRepository.exists(testOrderId);

        System.out.println("Expected: true");
        System.out.println("Actual: " + exists);
        assertTrue(exists, "Order should exist");

        System.out.println("✓ exists correctly returns true for existing order");
    }

    @Test
    @DisplayName("exists should return false for non-existing order")
    void testExists_NonExistingOrder_ReturnsFalse() {
        System.out.println("TEST: Verifying exists returns false for non-existing order");

        UUID nonExistingId = UUID.randomUUID();
        System.out.println("Checking if non-existing order with ID exists: " + nonExistingId);
        boolean exists = orderRepository.exists(nonExistingId);

        System.out.println("Expected: false");
        System.out.println("Actual: " + exists);
        assertFalse(exists, "Order should not exist");

        System.out.println("✓ exists correctly returns false for non-existing order");
    }

    @Test
    @DisplayName("save should store an order successfully")
    void testSave_NewOrder_StoresSuccessfully() {
        System.out.println("TEST: Verifying save stores a new order successfully");

        // Create a new order to save
        UUID newOrderId = UUID.randomUUID();
        Order newOrder = new Order(
                newOrderId,
                UUID.randomUUID(),
                "newUser",
                new HashMap<>(),
                75.0,
                70.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID(),
                null
        );

        System.out.println("Saving new order with ID: " + newOrderId);
        Order savedOrder = orderRepository.save(newOrder);

        System.out.println("Expected: saved order should match provided order");
        assertEquals(newOrder.getOrderId(), savedOrder.getOrderId(), "Saved order ID should match");

        Optional<Order> foundOrder = orderRepository.findById(newOrderId);
        System.out.println("Expected: Order should be found after saving");
        System.out.println("Actual: Order is present = " + foundOrder.isPresent());
        assertTrue(foundOrder.isPresent(), "Order should be found");

        System.out.println("Expected order ID: " + newOrderId);
        System.out.println("Actual order ID: " + foundOrder.get().getOrderId());
        assertEquals(newOrderId, foundOrder.get().getOrderId(), "Order ID should match");

        System.out.println("✓ save correctly stores a new order");
    }

    // Order Creation and Management Tests

    @Test
    @DisplayName("createOrder should create a new order with given parameters")
    void testCreateOrder_ValidParameters_CreatesOrder() {
        System.out.println("TEST: Verifying createOrder creates a new order with valid parameters");

        UUID storeId = UUID.randomUUID();
        String userName = "newUser";
        Map<UUID, Integer> products = new HashMap<>();
        products.put(UUID.randomUUID(), 1);
        double totalPrice = 200.0;
        double finalPrice = 180.0;
        LocalDateTime orderDate = LocalDateTime.now();
        OrderStatus status = OrderStatus.PENDING;
        UUID paymentId = UUID.randomUUID();

        System.out.println("Creating new order for user: " + userName + " in store: " + storeId);
        UUID orderId = orderRepository.createOrder(
                storeId, userName, products, totalPrice, finalPrice, orderDate, status, paymentId
        );

        System.out.println("Created order with ID: " + orderId);

        Optional<Order> createdOrder = orderRepository.findById(orderId);
        System.out.println("Expected: Order should exist");
        System.out.println("Actual: Order exists = " + createdOrder.isPresent());
        assertTrue(createdOrder.isPresent(), "Created order should exist");

        Order order = createdOrder.get();
        System.out.println("Verifying order properties:");

        System.out.println("Expected store ID: " + storeId);
        System.out.println("Actual store ID: " + order.getStoreId());
        assertEquals(storeId, order.getStoreId(), "Store ID should match");

        System.out.println("Expected username: " + userName);
        System.out.println("Actual username: " + order.getUserName());
        assertEquals(userName, order.getUserName(), "Username should match");

        System.out.println("Expected total price: " + totalPrice);
        System.out.println("Actual total price: " + order.getTotalPrice());
        assertEquals(totalPrice, order.getTotalPrice(), "Total price should match");

        System.out.println("Expected final price: " + finalPrice);
        System.out.println("Actual final price: " + order.getFinalPrice());
        assertEquals(finalPrice, order.getFinalPrice(), "Final price should match");

        System.out.println("Expected status: " + status);
        System.out.println("Actual status: " + order.getStatus());
        assertEquals(status, order.getStatus(), "Status should match");

        System.out.println("Expected payment ID: " + paymentId);
        System.out.println("Actual payment ID: " + order.getPaymentId());
        assertEquals(paymentId, order.getPaymentId(), "Payment ID should match");

        System.out.println("✓ createOrder correctly creates a new order");
    }

    @Test
    @DisplayName("updateOrderStatus should update the status of an existing order")
    void testUpdateOrderStatus_ExistingOrder_StatusUpdated() {
        System.out.println("TEST: Verifying updateOrderStatus updates status of existing order");

        OrderStatus newStatus = OrderStatus.PAID;
        System.out.println("Updating order status from " + testOrderStatus + " to " + newStatus);
        boolean updated = orderRepository.updateOrderStatus(testOrderId, newStatus);

        System.out.println("Expected update result: true");
        System.out.println("Actual update result: " + updated);
        assertTrue(updated, "Status update should succeed");

        Optional<Order> updatedOrder = orderRepository.findById(testOrderId);
        assertTrue(updatedOrder.isPresent(), "Order should exist");

        System.out.println("Expected status after update: " + newStatus);
        System.out.println("Actual status after update: " + updatedOrder.get().getStatus());
        assertEquals(newStatus, updatedOrder.get().getStatus(), "Status should be updated");

        System.out.println("✓ updateOrderStatus correctly updates order status");
    }

    @Test
    @DisplayName("setDeliveryId should set the delivery ID for an existing order")
    void testSetDeliveryId_ExistingOrder_DeliveryIdSet() {
        System.out.println("TEST: Verifying setDeliveryId sets delivery ID for existing order");

        // Save the order to repository
        orderRepository.save(testOrder);

        // First update the order status to PAID (required before setting delivery ID)
        boolean statusUpdated = orderRepository.updateOrderStatus(testOrder.getOrderId(), OrderStatus.PAID);
        assertTrue(statusUpdated, "Order status update to PAID should succeed");

        // Create a delivery ID if not already initialized
        if (testOrderId == null) {
            testOrderId = UUID.randomUUID();
        }

        // Now try to set the delivery ID
        System.out.println("Setting delivery ID: " + testOrderId + " for order: " + testOrder.getOrderId());
        boolean updated = orderRepository.setDeliveryId(testOrder.getOrderId(), testOrderId);

        System.out.println("Expected update result: true");
        System.out.println("Actual update result: " + updated);

        // Verify that the update was successful
        assertTrue(updated, "Delivery ID update should succeed");

        // Also verify the delivery ID was set correctly
        Optional<Order> updatedOrder = orderRepository.findById(testOrder.getOrderId());
        assertTrue(updatedOrder.isPresent(), "Updated order should exist");
        assertEquals(testOrderId, updatedOrder.get().getDeliveryId(), "Delivery ID should match");
    }

    // Query Method Tests

    @Test
    @DisplayName("findByStoreId should return all orders for a store")
    void testFindByStoreId_ExistingStore_ReturnsOrders() {
        System.out.println("TEST: Verifying findByStoreId returns orders for a store");

        // Create another order for the same store
        UUID anotherOrderId = orderRepository.createOrder(
                testStoreId,
                "anotherUser",
                testProducts,
                75.0,
                70.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID()
        );
        System.out.println("Created another order with ID: " + anotherOrderId + " for the same store");

        // Create an order for a different store
        UUID differentStoreId = UUID.randomUUID();
        UUID differentOrderId = orderRepository.createOrder(
                differentStoreId,
                "differentUser",
                testProducts,
                60.0,
                55.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID()
        );
        System.out.println("Created an order with ID: " + differentOrderId + " for a different store");

        System.out.println("Looking for orders in store with ID: " + testStoreId);
        List<Order> storeOrders = orderRepository.findByStoreId(testStoreId);

        System.out.println("Expected number of orders for store: 2");
        System.out.println("Actual number of orders for store: " + storeOrders.size());
        assertEquals(2, storeOrders.size(), "Should find 2 orders for the store");

        boolean containsTestOrder = storeOrders.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order");

        boolean containsAnotherOrder = storeOrders.stream().anyMatch(o -> o.getOrderId().equals(anotherOrderId));
        System.out.println("Expected: Contains another order = true");
        System.out.println("Actual: Contains another order = " + containsAnotherOrder);
        assertTrue(containsAnotherOrder, "Should contain another order for same store");

        System.out.println("Looking for orders in store with ID: " + differentStoreId);
        List<Order> differentStoreOrders = orderRepository.findByStoreId(differentStoreId);
        System.out.println("Expected number of orders for different store: 1");
        System.out.println("Actual number of orders for different store: " + differentStoreOrders.size());
        assertEquals(1, differentStoreOrders.size(), "Should find 1 order for the different store");

        System.out.println("✓ findByStoreId correctly returns orders for a store");
    }

    @Test
    @DisplayName("findByUserName should return all orders for a user")
    void testFindByUserName_ExistingUser_ReturnsOrders() {
        System.out.println("TEST: Verifying findByUserName returns orders for a user");

        // Create another order for the same user
        UUID anotherOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                testUserName,
                testProducts,
                120.0,
                110.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID()

        );
        System.out.println("Created another order with ID: " + anotherOrderId + " for the same user");

        // Create an order for a different user
        String differentUserName = "differentUser";
        UUID differentOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                differentUserName,
                testProducts,
                85.0,
                80.0,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                UUID.randomUUID()
        );
        System.out.println("Created an order with ID: " + differentOrderId + " for a different user");

        System.out.println("Looking for orders for user: " + testUserName);
        List<Order> userOrders = orderRepository.findByUserName(testUserName);

        System.out.println("Expected number of orders for user: 2");
        System.out.println("Actual number of orders for user: " + userOrders.size());
        assertEquals(2, userOrders.size(), "Should find 2 orders for the user");

        boolean containsTestOrder = userOrders.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order");

        boolean containsAnotherOrder = userOrders.stream().anyMatch(o -> o.getOrderId().equals(anotherOrderId));
        System.out.println("Expected: Contains another order = true");
        System.out.println("Actual: Contains another order = " + containsAnotherOrder);
        assertTrue(containsAnotherOrder, "Should contain another order for same user");

        System.out.println("Looking for orders for user: " + differentUserName);
        List<Order> differentUserOrders = orderRepository.findByUserName(differentUserName);
        System.out.println("Expected number of orders for different user: 1");
        System.out.println("Actual number of orders for different user: " + differentUserOrders.size());
        assertEquals(1, differentUserOrders.size(), "Should find 1 order for the different user");

        System.out.println("✓ findByUserName correctly returns orders for a user");
    }

    @Test
    @DisplayName("findByStatus should return all orders with specified status")
    void testFindByStatus_ExistingStatus_ReturnsOrders() {
        System.out.println("TEST: Verifying findByStatus returns orders with specified status");

        // Create another order with the same status
        UUID anotherOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                "anotherUser",
                testProducts,
                150.0,
                140.0,
                LocalDateTime.now(),
                testOrderStatus,
                UUID.randomUUID()
        );
        System.out.println("Created another order with ID: " + anotherOrderId + " with status: " + testOrderStatus);

        // Create an order with a different status
        OrderStatus differentStatus = OrderStatus.SHIPPED;
        UUID differentOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                "differentUser",
                testProducts,
                95.0,
                90.0,
                LocalDateTime.now(),
                differentStatus,
                UUID.randomUUID()
        );
        System.out.println("Created an order with ID: " + differentOrderId + " with status: " + differentStatus);

        System.out.println("Looking for orders with status: " + testOrderStatus);
        List<Order> statusOrders = orderRepository.findByStatus(testOrderStatus);

        System.out.println("Expected number of orders with status: 2");
        System.out.println("Actual number of orders with status: " + statusOrders.size());
        assertEquals(2, statusOrders.size(), "Should find 2 orders with the status");

        boolean containsTestOrder = statusOrders.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order");

        boolean containsAnotherOrder = statusOrders.stream().anyMatch(o -> o.getOrderId().equals(anotherOrderId));
        System.out.println("Expected: Contains another order = true");
        System.out.println("Actual: Contains another order = " + containsAnotherOrder);
        assertTrue(containsAnotherOrder, "Should contain another order with same status");

        System.out.println("Looking for orders with status: " + differentStatus);
        List<Order> differentStatusOrders = orderRepository.findByStatus(differentStatus);
        System.out.println("Expected number of orders with different status: 1");
        System.out.println("Actual number of orders with different status: " + differentStatusOrders.size());
        assertEquals(1, differentStatusOrders.size(), "Should find 1 order with the different status");

        System.out.println("✓ findByStatus correctly returns orders with specified status");
    }

    @Test
    @DisplayName("findByDateRange should return all orders within specified date range")
    void testFindByDateRange_DateRange_ReturnsOrdersInRange() {
        System.out.println("TEST: Verifying findByDateRange returns orders within specified date range");

        // Create an order with a past date
        LocalDateTime pastDate = LocalDateTime.now().minusDays(2);
        UUID pastOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                "pastUser",
                testProducts,
                50.0,
                45.0,
                pastDate,
                OrderStatus.PAID,
                UUID.randomUUID()
        );
        System.out.println("Created an order with ID: " + pastOrderId + " with date: " + pastDate);

        // Create an order with a future date
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        UUID futureOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                "futureUser",
                testProducts,
                60.0,
                55.0,
                futureDate,
                OrderStatus.PENDING,
                UUID.randomUUID()
        );
        System.out.println("Created an order with ID: " + futureOrderId + " with date: " + futureDate);

        // Set up the date range to include today but exclude the past and future dates
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        System.out.println("Looking for orders between: " + startDate + " and " + endDate);

        List<Order> dateRangeOrders = orderRepository.findByDateRange(startDate, endDate);

        System.out.println("Expected number of orders in date range: 1");
        System.out.println("Actual number of orders in date range: " + dateRangeOrders.size());
        assertEquals(1, dateRangeOrders.size(), "Should find 1 order in the date range");

        boolean containsTestOrder = dateRangeOrders.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order in date range");

        // Test a wider date range that includes all orders
        LocalDateTime wideStartDate = LocalDateTime.now().minusDays(3);
        LocalDateTime wideEndDate = LocalDateTime.now().plusDays(3);
        System.out.println("Looking for orders in wider range between: " + wideStartDate + " and " + wideEndDate);

        List<Order> allDateRangeOrders = orderRepository.findByDateRange(wideStartDate, wideEndDate);
        System.out.println("Expected number of orders in wider date range: 3");
        System.out.println("Actual number of orders in wider date range: " + allDateRangeOrders.size());
        assertEquals(3, allDateRangeOrders.size(), "Should find all 3 orders in the wider date range");

        System.out.println("✓ findByDateRange correctly returns orders within specified date range");
    }

    @Test
    @DisplayName("getUserPurchaseHistory should return purchase history for a user")
    void testGetUserPurchaseHistory_ExistingUser_ReturnsPurchaseHistory() {
        System.out.println("TEST: Verifying getUserPurchaseHistory returns purchase history for a user");

        // Create another order for the same user with a different date
        LocalDateTime olderDate = LocalDateTime.now().minusDays(1);
        UUID olderOrderId = orderRepository.createOrder(
                UUID.randomUUID(),
                testUserName,
                testProducts,
                80.0,
                75.0,
                olderDate,
                OrderStatus.COMPLETED,
                UUID.randomUUID()
        );
        System.out.println("Created an older order with ID: " + olderOrderId + " for the same user with date: " + olderDate);

        System.out.println("Getting purchase history for user: " + testUserName);
        List<Order> purchaseHistory = orderRepository.getUserPurchaseHistory(testUserName);

        System.out.println("Expected number of orders in purchase history: 2");
        System.out.println("Actual number of orders in purchase history: " + purchaseHistory.size());
        assertEquals(2, purchaseHistory.size(), "Should find 2 orders in the purchase history");

        // Check if orders are sorted by date (newest first)
        System.out.println("Expected: Orders sorted by date (newest first)");
        assertTrue(purchaseHistory.get(0).getOrderDate().isAfter(purchaseHistory.get(1).getOrderDate()) ||
                        purchaseHistory.get(0).getOrderDate().isEqual(purchaseHistory.get(1).getOrderDate()),
                "Orders should be sorted by date (newest first)");

        System.out.println("First order date: " + purchaseHistory.get(0).getOrderDate());
        System.out.println("Second order date: " + purchaseHistory.get(1).getOrderDate());

        boolean containsTestOrder = purchaseHistory.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order in purchase history");

        boolean containsOlderOrder = purchaseHistory.stream().anyMatch(o -> o.getOrderId().equals(olderOrderId));
        System.out.println("Expected: Contains older order = true");
        System.out.println("Actual: Contains older order = " + containsOlderOrder);
        assertTrue(containsOlderOrder, "Should contain older order in purchase history");

        System.out.println("✓ getUserPurchaseHistory correctly returns purchase history for a user");
    }

    @Test
    @DisplayName("getStorePurchaseHistory should return purchase history for a store")
    void testGetStorePurchaseHistory_ExistingStore_ReturnsPurchaseHistory() {
        System.out.println("TEST: Verifying getStorePurchaseHistory returns purchase history for a store");

        // Create another order for the same store with a different date
        LocalDateTime olderDate = LocalDateTime.now().minusDays(1);
        UUID olderOrderId = orderRepository.createOrder(
                testStoreId,
                "anotherUser",
                testProducts,
                70.0,
                65.0,
                olderDate,
                OrderStatus.COMPLETED,
                UUID.randomUUID()
        );
        System.out.println("Created an older order with ID: " + olderOrderId + " for the same store with date: " + olderDate);

        System.out.println("Getting purchase history for store: " + testStoreId);
        List<Order> purchaseHistory = orderRepository.getStorePurchaseHistory(testStoreId);

        System.out.println("Expected number of orders in purchase history: 2");
        System.out.println("Actual number of orders in purchase history: " + purchaseHistory.size());
        assertEquals(2, purchaseHistory.size(), "Should find 2 orders in the purchase history");

        // Check if orders are sorted by date (newest first)
        System.out.println("Expected: Orders sorted by date (newest first)");
        assertTrue(purchaseHistory.get(0).getOrderDate().isAfter(purchaseHistory.get(1).getOrderDate()) ||
                        purchaseHistory.get(0).getOrderDate().isEqual(purchaseHistory.get(1).getOrderDate()),
                "Orders should be sorted by date (newest first)");

        System.out.println("First order date: " + purchaseHistory.get(0).getOrderDate());
        System.out.println("Second order date: " + purchaseHistory.get(1).getOrderDate());

        boolean containsTestOrder = purchaseHistory.stream().anyMatch(o -> o.getOrderId().equals(testOrderId));
        System.out.println("Expected: Contains test order = true");
        System.out.println("Actual: Contains test order = " + containsTestOrder);
        assertTrue(containsTestOrder, "Should contain test order in purchase history");

        boolean containsOlderOrder = purchaseHistory.stream().anyMatch(o -> o.getOrderId().equals(olderOrderId));
        System.out.println("Expected: Contains older order = true");
        System.out.println("Actual: Contains older order = " + containsOlderOrder);
        assertTrue(containsOlderOrder, "Should contain older order in purchase history");

        System.out.println("✓ getStorePurchaseHistory correctly returns purchase history for a store");
    }

    @Test
    @DisplayName("clear should remove all orders")
    void testClear_RemovesAllOrders() {
        System.out.println("TEST: Verifying clear removes all orders");

        // Check that we have at least one order before clearing
        List<Order> ordersBefore = orderRepository.findAll();
        System.out.println("Number of orders before clearing: " + ordersBefore.size());
        assertTrue(ordersBefore.size() > 0, "Should have at least one order before clearing");

        System.out.println("Clearing order repository");
        orderRepository.clear();

        List<Order> ordersAfter = orderRepository.findAll();
        System.out.println("Expected number of orders after clearing: 0");
        System.out.println("Actual number of orders after clearing: " + ordersAfter.size());
        assertEquals(0, ordersAfter.size(), "Should have no orders after clearing");

        System.out.println("✓ clear correctly removes all orders");
    }
}