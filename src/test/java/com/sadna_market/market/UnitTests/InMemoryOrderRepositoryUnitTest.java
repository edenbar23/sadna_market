//
//package com.sadna_market.market.UnitTests;
//
//import com.sadna_market.market.DomainLayer.IOrderRepository;
//import com.sadna_market.market.DomainLayer.Order;
//import com.sadna_market.market.DomainLayer.OrderStatus;
//import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class InMemoryOrderRepositoryUnitTest {
//
//    private IOrderRepository repository;
//    private Order testOrder;
//    private UUID storeId= UUID.randomUUID();
//    private String userName;
//    private HashMap<UUID, Integer> products;
//    private LocalDateTime orderDate;
//    private UUID paymentId1= UUID.randomUUID();
//    private UUID paymentId2=UUID.randomUUID();
//    private UUID paymentId3=UUID.randomUUID();
//    private UUID deliveryId=UUID.randomUUID();
//     private RepositoryConfiguration RC;
//
//    @BeforeEach
//    void setUp() {
//        repository = RC.orderRepository();
//
//        // Set up test data
//       // storeId = UUID.randomUUID();
//        userName = "testUser";
//        products = new HashMap<>();
//        UUID productId1 = UUID.randomUUID();
//        UUID productId2 = UUID.randomUUID();
//        products.put(productId1, 2); // Product ID 101, quantity 2
//        products.put(productId2, 1); // Product ID 102, quantity 1
//        double totalPrice = 100.0;
//        double finalPrice = 90.0; // After discount
//        orderDate = LocalDateTime.now();
//        OrderStatus initialStatus = OrderStatus.PENDING;
//        //paymentId = UUID.randomUUID();
//
//
//
//       // UUID storeId, String userName, Map<UUID, Integer> products, double totalPrice,
//       // double finalPrice,LocalDateTime orderDate, OrderStatus status, UUID paymentId)
//
//        testOrder = new Order(storeId, userName, products, totalPrice, finalPrice, orderDate, initialStatus, paymentId1);
//    }
//
//    @Test
//    void testSaveAndFindById() {
//        // Save the order
//        Order savedOrder = repository.save(testOrder);
//
//        // Find the order by ID
//        Optional<Order> retrievedOrderOpt = repository.findById(testOrder.getOrderId());
//
//        // Verify
//        assertTrue(retrievedOrderOpt.isPresent());
//        Order retrievedOrder = retrievedOrderOpt.get();
//        assertEquals(testOrder.getOrderId(), retrievedOrder.getOrderId());
//        assertEquals(storeId, retrievedOrder.getStoreId());
//        assertEquals(userName, retrievedOrder.getUserName());
//    }
//
//    @Test
//    void testSaveNull() {
//        // Attempting to save null should throw an exception
//        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
//    }
//
//    @Test
//    void testFindByIdNonExistent() {
//        // Find an order that doesn't exist
//        UUID a=UUID.randomUUID();
//        Optional<Order> retrievedOrderOpt = repository.findById(a);
//
//        // Verify
//        assertFalse(retrievedOrderOpt.isPresent());
//    }
//
//    @Test
//    void testFindByIdNull() {
//        // Find with null ID should return empty
//        Optional<Order> retrievedOrderOpt = repository.findById(null);
//
//        // Verify
//        assertFalse(retrievedOrderOpt.isPresent());
//    }
//
//    @Test
//    void testFindAll() {
//        // Initially empty
//        List<Order> allOrders = repository.findAll();
//        assertTrue(allOrders.isEmpty());
//
//        // Save some orders
//        repository.save(testOrder);
//        Order secondOrder = new Order(storeId, "anotherUser", products, 200.0, 180.0, orderDate, OrderStatus.PAID, paymentId2);
//        repository.save(secondOrder);
//
//        // Find all orders
//        allOrders = repository.findAll();
//
//        // Verify
//        assertEquals(2, allOrders.size());
//        assertTrue(allOrders.stream().anyMatch(o -> o.getOrderId().equals(testOrder.getOrderId())));
//        assertTrue(allOrders.stream().anyMatch(o -> o.getOrderId().equals(secondOrder.getOrderId())));
//    }
//
//    @Test
//    void testDeleteById() {
//        // Save the order
//        repository.save(testOrder);
//
//        // Verify it exists
//        assertTrue(repository.exists(testOrder.getOrderId()));
//
//        // Delete the order
//        repository.deleteById(testOrder.getOrderId());
//
//        // Verify it's gone
//        assertFalse(repository.exists(testOrder.getOrderId()));
//    }
//
//    @Test
//    void testDeleteByIdNull() {
//        // Delete with null ID should not throw an exception
//        repository.deleteById(null);
//    }
//
//    @Test
//    void testExists() {
//        // Initially doesn't exist
//        assertFalse(repository.exists(testOrder.getOrderId()));
//
//        // Save the order
//        repository.save(testOrder);
//
//        // Now it should exist
//        assertTrue(repository.exists(testOrder.getOrderId()));
//    }
//
//    @Test
//    void testExistsNull() {
//        // Exists with null ID should return false
//        assertFalse(repository.exists(null));
//    }
//
//    @Test
//    void testCreateOrder() {
//        // Create a new order using the repository method
//        UUID orderId = repository.createOrder(
//            storeId, userName, products, 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1
//        );
//
//        // Verify
//        assertNotNull(orderId);
//        assertTrue(repository.exists(orderId));
//
//        // Retrieve and check details
//        Optional<Order> createdOrderOpt = repository.findById(orderId);
//        assertTrue(createdOrderOpt.isPresent());
//
//        Order createdOrder = createdOrderOpt.get();
//        assertEquals(storeId, createdOrder.getStoreId());
//        assertEquals(userName, createdOrder.getUserName());
//        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
//    }
//
//    @Test
//    void testCreateOrderWithInvalidParams() {
//        // Test with null store ID
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(null, userName, products, 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with null username
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, null, products, 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with empty username
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, "", products, 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with null products
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, userName, null, 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with empty products
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, userName, new HashMap<>(), 100.0, 90.0, orderDate, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with null order date
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, userName, products, 100.0, 90.0, null, OrderStatus.PENDING, paymentId1);
//        });
//
//        // Test with null status
//        assertThrows(IllegalArgumentException.class, () -> {
//            repository.createOrder(storeId, userName, products, 100.0, 90.0, orderDate, null, paymentId1);
//        });
//    }
//
//    @Test
//    void testUpdateOrderStatus() {
//        // Save the order
//        repository.save(testOrder);
//
//        // Update status
//        boolean updated = repository.updateOrderStatus(testOrder.getOrderId(), OrderStatus.PAID);
//
//        // Verify
//        assertTrue(updated);
//
//        // Check the updated status
//        Optional<Order> updatedOrderOpt = repository.findById(testOrder.getOrderId());
//        assertTrue(updatedOrderOpt.isPresent());
//        assertEquals(OrderStatus.PAID, updatedOrderOpt.get().getStatus());
//    }
//
//    @Test
//    void testUpdateOrderStatusInvalidTransition() {
//        // Save the order
//        repository.save(testOrder);
//
//        // Try an invalid transition (PENDING to SHIPPED)
//        boolean updated = repository.updateOrderStatus(testOrder.getOrderId(), OrderStatus.SHIPPED);
//
//        // Verify
//        assertFalse(updated);
//
//        // Status should not change
//        Optional<Order> orderOpt = repository.findById(testOrder.getOrderId());
//        assertTrue(orderOpt.isPresent());
//        assertEquals(OrderStatus.PENDING, orderOpt.get().getStatus());
//    }
//
//    @Test
//    void testUpdateOrderStatusNonExistentOrder() {
//        // Try to update a non-existent order
//        UUID orderId = UUID.randomUUID();
//        boolean updated = repository.updateOrderStatus(orderId, OrderStatus.PAID);
//
//        // Verify
//        assertFalse(updated);
//    }
//
//    @Test
//    void testUpdateOrderStatusNullParams() {
//        // Save the order
//        repository.save(testOrder);
//
//        // Update with null ID
//        assertFalse(repository.updateOrderStatus(null, OrderStatus.PAID));
//
//        // Update with null status
//        assertFalse(repository.updateOrderStatus(testOrder.getOrderId(), null));
//    }
//
//    @Test
//    void testSetDeliveryId() {
//        // Save the order and update to PAID status
//        repository.save(testOrder);
//        repository.updateOrderStatus(testOrder.getOrderId(), OrderStatus.PAID);
//
//        // Set delivery ID
//       // String deliveryId = "del_123456";
//        boolean updated = repository.setDeliveryId(testOrder.getOrderId(), deliveryId);
//
//        // Verify
//        assertTrue(updated);
//
//        // Check the updated delivery ID
//        Optional<Order> updatedOrderOpt = repository.findById(testOrder.getOrderId());
//        assertTrue(updatedOrderOpt.isPresent());
//        assertEquals(deliveryId, updatedOrderOpt.get().getDeliveryId());
//    }
//
//    @Test
//    void testSetDeliveryIdWrongStatus() {
//        // Save the order (status PENDING)
//        repository.save(testOrder);
//
//        // Try to set delivery ID (should fail for PENDING)
//        boolean updated = repository.setDeliveryId(testOrder.getOrderId(), deliveryId);
//
//        // Verify
//        assertFalse(updated);
//    }
//
//    @Test
//    void testSetDeliveryIdNonExistentOrder() {
//        // Try to set delivery ID for non-existent order
//        UUID orderId = UUID.randomUUID();
//        boolean updated = repository.setDeliveryId(orderId, deliveryId);
//
//        // Verify
//        assertFalse(updated);
//    }
//
//    @Test
//    void testSetDeliveryIdNullOrderId() {
//        // Try to set delivery ID with null order ID
//        boolean updated = repository.setDeliveryId(null, deliveryId);
//
//        // Verify
//        assertFalse(updated);
//    }
//
//    @Test
//    void testFindByStoreId() {
//        // Save some orders
//        repository.save(testOrder); // Store ID: 1001L
//
//        //Long anotherStoreId = 1002L;
//        UUID anotherStoreIdUUID = UUID.randomUUID();
//        Order anotherStoreOrder = new Order(anotherStoreIdUUID, userName, products, 200.0, 180.0, orderDate, OrderStatus.PENDING, paymentId1);
//        repository.save(anotherStoreOrder);
//
//        // Find orders for store 1001L
//        List<Order> storeOrders = repository.findByStoreId(storeId);
//
//        // Verify
//        assertEquals(1, storeOrders.size());
//        assertEquals(testOrder.getOrderId(), storeOrders.get(0).getOrderId());
//    }
//
//    @Test
//    void testFindByStoreIdNull() {
//        // Find orders for null store ID
//        List<Order> storeOrders = repository.findByStoreId(null);
//
//        // Verify
//        assertTrue(storeOrders.isEmpty());
//    }
//
//    @Test
//    void testFindByUserName() {
//        // Save some orders
//        repository.save(testOrder); // Username: testUser
//
//        String anotherUserName = "anotherUser";
//        Order anotherUserOrder = new Order(storeId, anotherUserName, products, 200.0, 180.0, orderDate, OrderStatus.PENDING, paymentId1);
//        repository.save(anotherUserOrder);
//
//        // Find orders for testUser
//        List<Order> userOrders = repository.findByUserName(userName);
//
//        // Verify
//        assertEquals(1, userOrders.size());
//        assertEquals(testOrder.getOrderId(), userOrders.get(0).getOrderId());
//    }
//
//    @Test
//    void testFindByUserNameNull() {
//        // Find orders for null username
//        List<Order> userOrders = repository.findByUserName(null);
//
//        // Verify
//        assertTrue(userOrders.isEmpty());
//    }
//
//    @Test
//    void testFindByUserNameEmpty() {
//        // Find orders for empty username
//        List<Order> userOrders = repository.findByUserName("");
//
//        // Verify
//        assertTrue(userOrders.isEmpty());
//    }
//
//    @Test
//    void testFindByStatus() {
//        // Save some orders
//        repository.save(testOrder); // Status: PENDING
//
//        Order paidOrder = new Order(storeId, userName, products, 200.0, 180.0, orderDate, OrderStatus.PAID, paymentId1);
//        repository.save(paidOrder);
//
//        // Find PENDING orders
//        List<Order> pendingOrders = repository.findByStatus(OrderStatus.PENDING);
//
//        // Verify
//        assertEquals(1, pendingOrders.size());
//        assertEquals(testOrder.getOrderId(), pendingOrders.get(0).getOrderId());
//
//        // Find PAID orders
//        List<Order> paidOrders = repository.findByStatus(OrderStatus.PAID);
//
//        // Verify
//        assertEquals(1, paidOrders.size());
//        assertEquals(paidOrder.getOrderId(), paidOrders.get(0).getOrderId());
//    }
//
//    @Test
//    void testFindByStatusNull() {
//        // Find orders for null status
//        List<Order> orders = repository.findByStatus(null);
//
//        // Verify
//        assertTrue(orders.isEmpty());
//    }
//
//    @Test
//    void testFindByDateRange() {
//        // Save some orders
//        repository.save(testOrder); // Date: now
//
//        LocalDateTime pastDate = LocalDateTime.now().minusDays(2);
//        Order pastOrder = new Order(storeId, userName, products, 200.0, 180.0, pastDate, OrderStatus.PENDING, paymentId1);
//        repository.save(pastOrder);
//
//        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
//        Order futureOrder = new Order(storeId, userName, products, 300.0, 270.0, futureDate, OrderStatus.PENDING, paymentId1);
//        repository.save(futureOrder);
//
//        // Find orders within range (past to future)
//        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
//        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
//        List<Order> rangeOrders = repository.findByDateRange(startDate, endDate);
//
//        // Verify (should find all 3)
//        assertEquals(3, rangeOrders.size());
//
//        // Find orders within narrower range (now to future)
//        startDate = LocalDateTime.now().minusHours(1);
//        endDate = LocalDateTime.now().plusDays(3);
//        rangeOrders = repository.findByDateRange(startDate, endDate);
//
//        // Verify (should find 2 - current and future)
//        assertEquals(2, rangeOrders.size());
//    }
//
//    @Test
//    void testFindByDateRangeNullDates() {
//        // Find orders with null start date
//        List<Order> orders = repository.findByDateRange(null, LocalDateTime.now());
//
//        // Verify
//        assertTrue(orders.isEmpty());
//
//        // Find orders with null end date
//        orders = repository.findByDateRange(LocalDateTime.now(), null);
//
//        // Verify
//        assertTrue(orders.isEmpty());
//    }
//
//    @Test
//    void testFindByDateRangeInvalidRange() {
//        // Find orders with invalid range (end before start)
//        LocalDateTime startDate = LocalDateTime.now();
//        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
//
//        List<Order> orders = repository.findByDateRange(startDate, endDate);
//
//        // Verify
//        assertTrue(orders.isEmpty());
//    }
//
//    @Test
//    void testGetUserPurchaseHistory() {
//        // Save some orders for different users
//        repository.save(testOrder); // User: testUser
//
//        String anotherUserName = "anotherUser";
//        Order anotherUserOrder = new Order(storeId, anotherUserName, products, 200.0, 180.0, orderDate, OrderStatus.PENDING, paymentId1);
//        repository.save(anotherUserOrder);
//
//        // Also save an older order for testUser
//        LocalDateTime olderDate = LocalDateTime.now().minusDays(1);
//        Order olderOrder = new Order(storeId, userName, products, 300.0, 270.0, olderDate, OrderStatus.COMPLETED, paymentId1);
//        repository.save(olderOrder);
//
//        // Get purchase history for testUser
//        List<Order> purchaseHistory = repository.getUserPurchaseHistory(userName);
//
//        // Verify
//        assertEquals(2, purchaseHistory.size());
//
//        // Check sorting (newest first)
//        assertEquals(testOrder.getOrderId(), purchaseHistory.get(0).getOrderId());
//        assertEquals(olderOrder.getOrderId(), purchaseHistory.get(1).getOrderId());
//    }
//
//    @Test
//    void testGetUserPurchaseHistoryNullOrEmpty() {
//        // Get purchase history for null username
//        List<Order> purchaseHistory = repository.getUserPurchaseHistory(null);
//
//        // Verify
//        assertTrue(purchaseHistory.isEmpty());
//
//        // Get purchase history for empty username
//        purchaseHistory = repository.getUserPurchaseHistory("");
//
//        // Verify
//        assertTrue(purchaseHistory.isEmpty());
//    }
//
//    @Test
//    void testGetStorePurchaseHistory() {
//        // Save some orders for different stores
//        repository.save(testOrder); // Store: 1001L
//
//        ///Long anotherStoreId = 1002L;
//        UUID anotherStoreIdUUID = UUID.randomUUID();
//        Order anotherStoreOrder = new Order(anotherStoreIdUUID, userName, products, 200.0, 180.0, orderDate, OrderStatus.PENDING, paymentId1);
//        repository.save(anotherStoreOrder);
//
//        // Also save an older order for the test store
//        LocalDateTime olderDate = LocalDateTime.now().minusDays(1);
//        Order olderOrder = new Order(storeId, userName, products, 300.0, 270.0, olderDate, OrderStatus.COMPLETED, paymentId1);
//        repository.save(olderOrder);
//
//        // Get purchase history for test store
//        List<Order> purchaseHistory = repository.getStorePurchaseHistory(storeId);
//
//        // Verify
//        assertEquals(2, purchaseHistory.size());
//
//        // Check sorting (newest first)
//        assertEquals(testOrder.getOrderId(), purchaseHistory.get(0).getOrderId());
//        assertEquals(olderOrder.getOrderId(), purchaseHistory.get(1).getOrderId());
//    }
//
//    @Test
//    void testGetStorePurchaseHistoryNull() {
//        // Get purchase history for null store ID
//        List<Order> purchaseHistory = repository.getStorePurchaseHistory(null);
//
//        // Verify
//        assertTrue(purchaseHistory.isEmpty());
//    }
//}