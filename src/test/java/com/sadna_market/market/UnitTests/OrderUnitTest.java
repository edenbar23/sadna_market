package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderUnitTest {

    private Order order;
    private UUID orderId;
    private UUID storeId;
    private String userName;
    private HashMap<UUID, Integer> products;
    private double totalPrice;
    private double finalPrice;
    private LocalDateTime orderDate;
    private OrderStatus initialStatus;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test data =====");

        // Initialize test data
        storeId = UUID.randomUUID();
        userName = "testUser";
        products = new HashMap<>();

        // Add some products to the order
        UUID product1Id = UUID.randomUUID();
        UUID product2Id = UUID.randomUUID();
        products.put(product1Id, 2); // 2 units of product 1
        products.put(product2Id, 3); // 3 units of product 2

        System.out.println("Created products map with:");
        System.out.println("- Product " + product1Id + ": 2 units");
        System.out.println("- Product " + product2Id + ": 3 units");

        totalPrice = 100.0;
        finalPrice = 85.0; // After a 15% discount
        orderDate = LocalDateTime.now();
        initialStatus = OrderStatus.PENDING;
        paymentId = UUID.randomUUID();

        System.out.println("Test order details:");
        System.out.println("- Total price: $" + totalPrice);
        System.out.println("- Final price: $" + finalPrice + " (after discount)");
        System.out.println("- Initial status: " + initialStatus);

        // Create the order
        order = new Order(storeId, userName, products, totalPrice, finalPrice,
                orderDate, initialStatus, paymentId);

        orderId = order.getOrderId();
        System.out.println("Order created with ID: " + orderId);
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        order = null;
        products = null;
        System.out.println("Order and products references set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    void testOrderStoreIdIsSetCorrectly() {
        System.out.println("TEST: Verifying store ID is set correctly");
        System.out.println("Expected: " + storeId);
        System.out.println("Actual: " + order.getStoreId());
        assertEquals(storeId, order.getStoreId());
        System.out.println("✓ Store ID correctly set");
    }

    @Test
    void testOrderUserNameIsSetCorrectly() {
        System.out.println("TEST: Verifying user name is set correctly");
        System.out.println("Expected: " + userName);
        System.out.println("Actual: " + order.getUserName());
        assertEquals(userName, order.getUserName());
        System.out.println("✓ User name correctly set");
    }

    @Test
    void testOrderTotalPriceIsSetCorrectly() {
        System.out.println("TEST: Verifying total price is set correctly");
        System.out.println("Expected: $" + totalPrice);
        System.out.println("Actual: $" + order.getTotalPrice());
        assertEquals(totalPrice, order.getTotalPrice());
        System.out.println("✓ Total price correctly set");
    }

    @Test
    void testOrderFinalPriceIsSetCorrectly() {
        System.out.println("TEST: Verifying final price is set correctly");
        System.out.println("Expected: $" + finalPrice);
        System.out.println("Actual: $" + order.getFinalPrice());
        assertEquals(finalPrice, order.getFinalPrice());
        System.out.println("✓ Final price correctly set");
    }

    @Test
    void testOrderDateIsSetCorrectly() {
        System.out.println("TEST: Verifying order date is set correctly");
        System.out.println("Expected: " + orderDate);
        System.out.println("Actual: " + order.getOrderDate());
        assertEquals(orderDate, order.getOrderDate());
        System.out.println("✓ Order date correctly set");
    }

    @Test
    void testOrderStatusIsSetCorrectly() {
        System.out.println("TEST: Verifying order status is set correctly");
        System.out.println("Expected: " + initialStatus);
        System.out.println("Actual: " + order.getStatus());
        assertEquals(initialStatus, order.getStatus());
        System.out.println("✓ Order status correctly set");
    }

    @Test
    void testOrderPaymentIdIsSetCorrectly() {
        System.out.println("TEST: Verifying payment ID is set correctly");
        System.out.println("Expected: " + paymentId);
        System.out.println("Actual: " + order.getPaymentId());
        assertEquals(paymentId, order.getPaymentId());
        System.out.println("✓ Payment ID correctly set");
    }

    @Test
    void testOrderInitialDeliveryIdIsNull() {
        System.out.println("TEST: Verifying initial delivery ID is null");
        System.out.println("Order delivery ID: " + order.getDeliveryId());
        assertNull(order.getDeliveryId());
        System.out.println("✓ Initial delivery ID is null");
    }

    @Test
    void testOrderProductsAreStoredCorrectly() {
        System.out.println("TEST: Verifying products are stored correctly");
        Map<UUID, Integer> retrievedProducts = order.getProductsMap();

        System.out.println("Expected products count: " + products.size());
        System.out.println("Actual products count: " + retrievedProducts.size());
        assertEquals(products.size(), retrievedProducts.size());

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            int expectedQuantity = entry.getValue();

            System.out.println("Checking product " + productId);
            System.out.println("- Expected quantity: " + expectedQuantity);
            System.out.println("- Contains product: " + retrievedProducts.containsKey(productId));

            assertTrue(retrievedProducts.containsKey(productId));

            int actualQuantity = retrievedProducts.get(productId);
            System.out.println("- Actual quantity: " + actualQuantity);
            assertEquals(expectedQuantity, actualQuantity);
        }
        System.out.println("✓ All products stored correctly");
    }

    @Test
    void testTransitionFromPendingToPaidIsSuccessful() {
        System.out.println("TEST: Verifying transition from PENDING to PAID");
        System.out.println("Initial status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.PAID);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("New status: " + order.getStatus());

        assertTrue(transitionResult);
        assertEquals(OrderStatus.PAID, order.getStatus());
        System.out.println("✓ Successfully transitioned from PENDING to PAID");
    }

    @Test
    void testTransitionFromPaidToShippedIsSuccessful() {
        System.out.println("TEST: Verifying transition from PAID to SHIPPED");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("New status: " + order.getStatus());

        assertTrue(transitionResult);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        System.out.println("✓ Successfully transitioned from PAID to SHIPPED");
    }

    @Test
    void testTransitionFromShippedToCompletedIsSuccessful() {
        System.out.println("TEST: Verifying transition from SHIPPED to COMPLETED");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        System.out.println("Setting order to SHIPPED status...");
        order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Current status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.COMPLETED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("New status: " + order.getStatus());

        assertTrue(transitionResult);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        System.out.println("✓ Successfully transitioned from SHIPPED to COMPLETED");
    }

    @Test
    void testInvalidTransitionFromPendingToShippedIsDenied() {
        System.out.println("TEST: Verifying invalid transition from PENDING to SHIPPED is denied");
        System.out.println("Initial status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("Status after attempt: " + order.getStatus());

        assertFalse(transitionResult);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        System.out.println("✓ Invalid transition from PENDING to SHIPPED denied");
    }

    @Test
    void testInvalidTransitionFromPaidToCompletedIsDenied() {
        System.out.println("TEST: Verifying invalid transition from PAID to COMPLETED is denied");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.COMPLETED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("Status after attempt: " + order.getStatus());

        assertFalse(transitionResult);
        assertEquals(OrderStatus.PAID, order.getStatus());
        System.out.println("✓ Invalid transition from PAID to COMPLETED denied");
    }

    @Test
    void testNoTransitionAllowedFromCompletedState() {
        System.out.println("TEST: Verifying no transitions allowed from COMPLETED state");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Setting order to SHIPPED status...");
        order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Setting order to COMPLETED status...");
        order.updateStatus(OrderStatus.COMPLETED);
        System.out.println("Current status: " + order.getStatus());

        System.out.println("Attempting to transition from COMPLETED to PENDING...");
        boolean transitionResult = order.updateStatus(OrderStatus.PENDING);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("Status after attempt: " + order.getStatus());

        assertFalse(transitionResult);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        System.out.println("✓ Transition from COMPLETED state denied");
    }

    @Test
    void testPendingOrderCanBeCanceled() {
        System.out.println("TEST: Verifying PENDING order can be canceled");
        System.out.println("Initial status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.CANCELED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("New status: " + order.getStatus());

        assertTrue(transitionResult);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        System.out.println("✓ Successfully canceled PENDING order");
    }

    @Test
    void testPaidOrderCanBeCanceled() {
        System.out.println("TEST: Verifying PAID order can be canceled");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.CANCELED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("New status: " + order.getStatus());

        assertTrue(transitionResult);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        System.out.println("✓ Successfully canceled PAID order");
    }

    @Test
    void testShippedOrderCannotBeCanceled() {
        System.out.println("TEST: Verifying SHIPPED order cannot be canceled");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Setting order to SHIPPED status...");
        order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Current status: " + order.getStatus());

        boolean transitionResult = order.updateStatus(OrderStatus.CANCELED);
        System.out.println("Transition result: " + transitionResult);
        System.out.println("Status after attempt: " + order.getStatus());

        assertFalse(transitionResult);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        System.out.println("✓ Cancellation of SHIPPED order denied");
    }

    @Test
    void testDeliveryTrackingCanBeSetForPaidOrder() {
        System.out.println("TEST: Verifying delivery tracking can be set for PAID order");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        UUID deliveryId = UUID.randomUUID();
        System.out.println("Attempting to set delivery ID: " + deliveryId);

        boolean result = order.setDeliveryTracking(deliveryId);
        System.out.println("Set delivery tracking result: " + result);
        System.out.println("Order delivery ID: " + order.getDeliveryId());

        assertTrue(result);
        assertEquals(deliveryId, order.getDeliveryId());
        System.out.println("✓ Successfully set delivery tracking for PAID order");
    }

    @Test
    void testDeliveryTrackingCannotBeSetForPendingOrder() {
        System.out.println("TEST: Verifying delivery tracking cannot be set for PENDING order");
        System.out.println("Initial status: " + order.getStatus());

        UUID deliveryId = UUID.randomUUID();
        System.out.println("Attempting to set delivery ID: " + deliveryId);

        boolean result = order.setDeliveryTracking(deliveryId);
        System.out.println("Set delivery tracking result: " + result);
        System.out.println("Order delivery ID: " + order.getDeliveryId());

        assertFalse(result);
        assertNull(order.getDeliveryId());
        System.out.println("✓ Setting delivery tracking for PENDING order denied");
    }

    @Test
    void testOrderContainsProductReturnsTrue() {
        System.out.println("TEST: Verifying containsProduct returns true for existing product");

        UUID productId = products.keySet().iterator().next();
        System.out.println("Checking for product: " + productId);

        boolean result = order.containsProduct(productId);
        System.out.println("Contains product result: " + result);

        assertTrue(result);
        System.out.println("✓ containsProduct returns true for existing product");
    }

    @Test
    void testOrderContainsProductReturnsFalseForNonExistentProduct() {
        System.out.println("TEST: Verifying containsProduct returns false for non-existent product");

        UUID nonExistentProductId = UUID.randomUUID();
        System.out.println("Checking for non-existent product: " + nonExistentProductId);

        boolean result = order.containsProduct(nonExistentProductId);
        System.out.println("Contains product result: " + result);

        assertFalse(result);
        System.out.println("✓ containsProduct returns false for non-existent product");
    }

    @Test
    void testGetProductQuantityReturnsCorrectValue() {
        System.out.println("TEST: Verifying getProductQuantity returns correct value");

        UUID productId = products.keySet().iterator().next();
        int expectedQuantity = products.get(productId);

        System.out.println("Checking quantity for product: " + productId);
        System.out.println("Expected quantity: " + expectedQuantity);

        int actualQuantity = order.getProductQuantity(productId);
        System.out.println("Actual quantity: " + actualQuantity);

        assertEquals(expectedQuantity, actualQuantity);
        System.out.println("✓ getProductQuantity returns correct value");
    }

    @Test
    void testGetProductQuantityReturnsZeroForNonExistentProduct() {
        System.out.println("TEST: Verifying getProductQuantity returns zero for non-existent product");

        UUID nonExistentProductId = UUID.randomUUID();
        System.out.println("Checking quantity for non-existent product: " + nonExistentProductId);

        int quantity = order.getProductQuantity(nonExistentProductId);
        System.out.println("Returned quantity: " + quantity);

        assertEquals(0, quantity);
        System.out.println("✓ getProductQuantity returns zero for non-existent product");
    }

    @Test
    void testDiscountAmountCalculatedCorrectly() {
        System.out.println("TEST: Verifying discount amount is calculated correctly");

        double expectedDiscount = totalPrice - finalPrice;
        System.out.println("Expected discount: $" + expectedDiscount);

        double actualDiscount = order.getDiscountAmount();
        System.out.println("Actual discount: $" + actualDiscount);

        assertEquals(expectedDiscount, actualDiscount);
        System.out.println("✓ Discount amount calculated correctly");
    }

    @Test
    void testPendingOrderIsNotTerminal() {
        System.out.println("TEST: Verifying PENDING order is not terminal");
        System.out.println("Order status: " + order.getStatus());

        boolean isTerminal = order.isTerminal();
        System.out.println("Is terminal: " + isTerminal);

        assertFalse(isTerminal);
        System.out.println("✓ PENDING order is not terminal");
    }

    @Test
    void testCompletedOrderIsTerminal() {
        System.out.println("TEST: Verifying COMPLETED order is terminal");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Setting order to SHIPPED status...");
        order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Setting order to COMPLETED status...");
        order.updateStatus(OrderStatus.COMPLETED);
        System.out.println("Current status: " + order.getStatus());

        boolean isTerminal = order.isTerminal();
        System.out.println("Is terminal: " + isTerminal);

        assertTrue(isTerminal);
        System.out.println("✓ COMPLETED order is terminal");
    }

    @Test
    void testCanceledOrderIsTerminal() {
        System.out.println("TEST: Verifying CANCELED order is terminal");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to CANCELED status...");
        order.updateStatus(OrderStatus.CANCELED);
        System.out.println("Current status: " + order.getStatus());

        boolean isTerminal = order.isTerminal();
        System.out.println("Is terminal: " + isTerminal);

        assertTrue(isTerminal);
        System.out.println("✓ CANCELED order is terminal");
    }

    @Test
    void testPendingOrderCanBeCancelled() {
        System.out.println("TEST: Verifying PENDING order can be cancelled");
        System.out.println("Order status: " + order.getStatus());

        boolean canCancel = order.canCancel();
        System.out.println("Can cancel: " + canCancel);

        assertTrue(canCancel);
        System.out.println("✓ PENDING order can be cancelled");
    }

    @Test
    void testPaidOrderCanBeCancelled() {
        System.out.println("TEST: Verifying PAID order can be cancelled");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Current status: " + order.getStatus());

        boolean canCancel = order.canCancel();
        System.out.println("Can cancel: " + canCancel);

        assertTrue(canCancel);
        System.out.println("✓ PAID order can be cancelled");
    }

    @Test
    void testShippedOrderCannotBeCancelled() {
        System.out.println("TEST: Verifying SHIPPED order cannot be cancelled");
        System.out.println("Initial status: " + order.getStatus());

        System.out.println("Setting order to PAID status...");
        order.updateStatus(OrderStatus.PAID);
        System.out.println("Setting order to SHIPPED status...");
        order.updateStatus(OrderStatus.SHIPPED);
        System.out.println("Current status: " + order.getStatus());

        boolean canCancel = order.canCancel();
        System.out.println("Can cancel: " + canCancel);

        assertFalse(canCancel);
        System.out.println("✓ SHIPPED order cannot be cancelled");
    }

    @Test
    void testFullConstructorSetsAllFields() {
        System.out.println("TEST: Verifying full constructor sets all fields");

        UUID deliveryId = UUID.randomUUID();

        System.out.println("Creating order with full constructor and delivery ID: " + deliveryId);
        Order fullOrder = new Order(orderId, storeId, userName, products, totalPrice, finalPrice,
                orderDate, OrderStatus.PAID, paymentId, deliveryId);

        System.out.println("Verifying all fields are set correctly");
        assertEquals(orderId, fullOrder.getOrderId());
        assertEquals(storeId, fullOrder.getStoreId());
        assertEquals(userName, fullOrder.getUserName());
        assertEquals(totalPrice, fullOrder.getTotalPrice());
        assertEquals(finalPrice, fullOrder.getFinalPrice());
        assertEquals(orderDate, fullOrder.getOrderDate());
        assertEquals(OrderStatus.PAID, fullOrder.getStatus());
        assertEquals(paymentId, fullOrder.getPaymentId());
        assertEquals(deliveryId, fullOrder.getDeliveryId());

        System.out.println("✓ Full constructor sets all fields correctly");
    }
}