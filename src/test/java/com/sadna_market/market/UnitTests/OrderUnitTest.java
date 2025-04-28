package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private UUID storeId;
    private String userName;
    private Map<UUID, Integer> products;
    private double totalPrice;
    private double finalPrice;
    private LocalDateTime orderDate;
    private OrderStatus initialStatus;
    private String paymentId;
    
    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        userName = "testUser";
        products = new HashMap<>();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        products.put(productId1, 2); // Product ID 101, quantity 2
        products.put(productId2, 1); // Product ID 102, quantity 1
        totalPrice = 100.0;
        finalPrice = 90.0; // After discount
        orderDate = LocalDateTime.now();
        initialStatus = OrderStatus.PENDING;
        paymentId = "pay_123456";
        
        order = new Order(storeId, userName, products, totalPrice, finalPrice, orderDate, initialStatus, paymentId);
    }

    @Test
    void testConstructor() {
        assertNotNull(order.getOrderId());
        assertEquals(storeId, order.getStoreId());
        assertEquals(userName, order.getUserName());
        assertEquals(totalPrice, order.getTotalPrice());
        assertEquals(finalPrice, order.getFinalPrice());
        assertEquals(orderDate, order.getOrderDate());
        assertEquals(initialStatus, order.getStatus());
        assertEquals(paymentId, order.getPaymentId());
        assertNull(order.getDeliveryId());
    }

    @Test
    void testConstructorWithExistingId() {
        UUID existingOrderId = UUID.randomUUID();
        String deliveryId = "del_789012";
        
        Order orderWithExistingId = new Order(
            existingOrderId, storeId, userName, products, 
            totalPrice, finalPrice, orderDate, initialStatus, 
            paymentId, deliveryId
        );
        
        assertEquals(existingOrderId, orderWithExistingId.getOrderId());
        assertEquals(storeId, orderWithExistingId.getStoreId());
        assertEquals(userName, orderWithExistingId.getUserName());
        assertEquals(totalPrice, orderWithExistingId.getTotalPrice());
        assertEquals(finalPrice, orderWithExistingId.getFinalPrice());
        assertEquals(orderDate, orderWithExistingId.getOrderDate());
        assertEquals(initialStatus, orderWithExistingId.getStatus());
        assertEquals(paymentId, orderWithExistingId.getPaymentId());
        assertEquals(deliveryId, orderWithExistingId.getDeliveryId());
    }

    @Test
    void testUpdateStatus_ValidTransitions() {
        // From PENDING to PAID
        assertTrue(order.updateStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.PAID, order.getStatus());
        
        // From PAID to SHIPPED
        assertTrue(order.updateStatus(OrderStatus.SHIPPED));
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        
        // From SHIPPED to COMPLETED
        assertTrue(order.updateStatus(OrderStatus.COMPLETED));
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testUpdateStatus_InvalidTransitions() {
        // From PENDING to SHIPPED (invalid)
        assertFalse(order.updateStatus(OrderStatus.SHIPPED));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        
        // Make a valid transition
        order.updateStatus(OrderStatus.PAID);
        
        // From PAID to COMPLETED (invalid)
        assertFalse(order.updateStatus(OrderStatus.COMPLETED));
        assertEquals(OrderStatus.PAID, order.getStatus());
        
        // Make a valid transition
        order.updateStatus(OrderStatus.SHIPPED);
        order.updateStatus(OrderStatus.COMPLETED);
        
        // From COMPLETED to PAID (invalid - terminal state)
        assertFalse(order.updateStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testUpdateStatus_Cancel() {
        // From PENDING to CANCELED
        assertTrue(order.updateStatus(OrderStatus.CANCELED));
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        
        // Create a new order and test cancellation from PAID state
        Order paidOrder = new Order(storeId, userName, products, totalPrice, finalPrice, orderDate, OrderStatus.PAID, paymentId);
        assertTrue(paidOrder.updateStatus(OrderStatus.CANCELED));
        assertEquals(OrderStatus.CANCELED, paidOrder.getStatus());
        
        // From CANCELED to PAID (invalid - terminal state)
        assertFalse(order.updateStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void testSetDeliveryTracking() {
        String deliveryId = "del_123456";
        
        // Set from PENDING (should fail)
        assertFalse(order.setDeliveryTracking(deliveryId));
        assertNull(order.getDeliveryId());
        
        // Update to PAID
        order.updateStatus(OrderStatus.PAID);
        
        // Set from PAID (should succeed)
        assertTrue(order.setDeliveryTracking(deliveryId));
        assertEquals(deliveryId, order.getDeliveryId());
    }

    @Test
    void testGetProductsMap() {
        Map<UUID, Integer> productsMap = order.getProductsMap();
        
        // Test unmodifiability
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            UUID productId = UUID.randomUUID();
            productsMap.put(productId, 3);
        });
        
        // Verify content is correct
        assertEquals(2, productsMap.size());
        assertEquals(2, productsMap.get(101L));
        assertEquals(1, productsMap.get(102L));
    }

    /*@Test
    void testContainsProduct() {
        assertTrue(order.containsProduct(productId1));
        assertTrue(order.containsProduct(102L));
        assertFalse(order.containsProduct(103L));
    }*/

   /*  @Test
    void testGetProductQuantity() {
        assertEquals(2, order.getProductQuantity(101L));
        assertEquals(1, order.getProductQuantity(102L));
        assertEquals(0, order.getProductQuantity(103L));
    }*/

    @Test
    void testGetDiscountAmount() {
        assertEquals(10.0, order.getDiscountAmount());
    }

    @Test
    void testIsTerminal() {
        assertFalse(order.isTerminal());
        
        order.updateStatus(OrderStatus.COMPLETED);
        assertTrue(order.isTerminal());
        
        Order canceledOrder = new Order(storeId, userName, products, totalPrice, finalPrice, orderDate, OrderStatus.CANCELED, paymentId);
        assertTrue(canceledOrder.isTerminal());
    }

    @Test
    void testCanCancel() {
        assertTrue(order.canCancel());
        
        order.updateStatus(OrderStatus.PAID);
        assertTrue(order.canCancel());
        
        order.updateStatus(OrderStatus.SHIPPED);
        assertFalse(order.canCancel());
        
        order.updateStatus(OrderStatus.COMPLETED);
        assertFalse(order.canCancel());
    }

    @Test
    void testEquals() {
        // Same ID should be equal
        Order sameOrder = new Order(
            order.getOrderId(), storeId, userName, products, 
            totalPrice, finalPrice, orderDate, initialStatus, 
            paymentId, null
        );
        assertEquals(order, sameOrder);
        
        // Different ID should not be equal
        Order differentOrder = new Order(storeId, userName, products, totalPrice, finalPrice, orderDate, initialStatus, paymentId);
        assertNotEquals(order, differentOrder);
        
        // Not equal to null
        assertNotEquals(order, null);
        
        // Not equal to different object type
        assertNotEquals(order, "Not an order");
    }

    @Test
    void testHashCode() {
        // Same ID should have same hash code
        Order sameOrder = new Order(
            order.getOrderId(), storeId, userName, products, 
            totalPrice, finalPrice, orderDate, initialStatus, 
            paymentId, null
        );
        assertEquals(order.hashCode(), sameOrder.hashCode());
    }
}