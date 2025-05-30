package com.sadna_market.market.UnitTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Concrete Supply Visitor Tests")
class ConcreteSupplyVisitorTest {

    @Mock
    private ExternalSupplyAPI mockExternalAPI;

    private ConcreteSupplyVisitor visitor;
    private ShipmentDetails validShipment;
    private StandardShippingDTO standardShipping;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        visitor = new ConcreteSupplyVisitor(mockExternalAPI);
        validShipment = new ShipmentDetails("SHP-001", "123 Main St, City, Country", 2, "user", false);
        standardShipping = new StandardShippingDTO("FedEx", 3);
    }

    @Test
    @DisplayName("Should return success when standard shipping succeeds")
    void testSuccessfulStandardShipping() throws ExternalAPIException {
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(12345);

        SupplyResult result = visitor.visit(standardShipping, validShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct transaction ID when standard shipping succeeds")
    void testSuccessfulStandardShippingTransactionId() throws ExternalAPIException {
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(12345);

        SupplyResult result = visitor.visit(standardShipping, validShipment, 2.5);

        assertEquals(12345, result.getTransactionId());
    }

    @Test
    @DisplayName("Should return correct supply method type when standard shipping succeeds")
    void testSuccessfulStandardShippingMethodType() throws ExternalAPIException {
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(12345);

        SupplyResult result = visitor.visit(standardShipping, validShipment, 2.5);

        assertEquals("Standard Shipping", result.getSupplyMethodType());
    }

    @Test
    @DisplayName("Should return failure when shipment details are null")
    void testStandardShippingWithNullShipment() {
        SupplyResult result = visitor.visit(standardShipping, null, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct error message when shipment details are null")
    void testStandardShippingWithNullShipmentErrorMessage() {
        SupplyResult result = visitor.visit(standardShipping, null, 2.5);

        assertTrue(result.getErrorMessage().contains("Shipment details cannot be null"));
    }

    @Test
    @DisplayName("Should return failure when weight is negative")
    void testStandardShippingWithNegativeWeight() {
        SupplyResult result = visitor.visit(standardShipping, validShipment, -1.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when weight is zero")
    void testStandardShippingWithZeroWeight() {
        SupplyResult result = visitor.visit(standardShipping, validShipment, 0.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when weight exceeds maximum")
    void testStandardShippingWithExcessiveWeight() {
        SupplyResult result = visitor.visit(standardShipping, validShipment, 101.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when shipment ID is empty")
    void testStandardShippingWithEmptyShipmentId() {
        ShipmentDetails invalidShipment = new ShipmentDetails("", "123 Main St", 2, "user", false);

        SupplyResult result = visitor.visit(standardShipping, invalidShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when address is empty")
    void testStandardShippingWithEmptyAddress() {
        ShipmentDetails invalidShipment = new ShipmentDetails("SHP-001", "", 2, "user", false);

        SupplyResult result = visitor.visit(standardShipping, invalidShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when quantity is zero")
    void testStandardShippingWithZeroQuantity() {
        ShipmentDetails invalidShipment = new ShipmentDetails("SHP-001", "123 Main St", 0, "user", false);

        SupplyResult result = visitor.visit(standardShipping, invalidShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when registered user has empty username")
    void testStandardShippingWithEmptyUsername() {
        ShipmentDetails invalidShipment = new ShipmentDetails("SHP-001", "123 Main St", 2, "", false);

        SupplyResult result = visitor.visit(standardShipping, invalidShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when external API returns -1")
    void testExternalAPIDeclined() throws ExternalAPIException {
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(-1);

        SupplyResult result = visitor.visit(standardShipping, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when external API throws exception")
    void testExternalAPIException() throws ExternalAPIException {
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenThrow(new ExternalAPIException("Network error"));

        SupplyResult result = visitor.visit(standardShipping, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return success when express shipping succeeds")
    void testSuccessfulExpressShipping() throws ExternalAPIException {
        ExpressShippingDTO expressShipping = new ExpressShippingDTO("DHL", 1);
        when(mockExternalAPI.sendExpressShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(54321);

        SupplyResult result = visitor.visit(expressShipping, validShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when priority level is too low")
    void testExpressShippingWithInvalidLowPriority() {
        ExpressShippingDTO invalidExpress = new ExpressShippingDTO("DHL", 0);

        SupplyResult result = visitor.visit(invalidExpress, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when priority level is too high")
    void testExpressShippingWithInvalidHighPriority() {
        ExpressShippingDTO invalidExpress = new ExpressShippingDTO("DHL", 6);

        SupplyResult result = visitor.visit(invalidExpress, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return success when pickup succeeds")
    void testSuccessfulPickup() throws ExternalAPIException {
        PickupDTO pickup = new PickupDTO("Store Location", "PICK123");
        when(mockExternalAPI.registerPickupRequest(anyString(), anyString(), any(), anyDouble()))
                .thenReturn(67890);

        SupplyResult result = visitor.visit(pickup, validShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when store location is empty")
    void testPickupWithEmptyLocation() {
        PickupDTO invalidPickup = new PickupDTO("", "PICK123");

        SupplyResult result = visitor.visit(invalidPickup, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when pickup code is empty")
    void testPickupWithEmptyCode() {
        PickupDTO invalidPickup = new PickupDTO("Store Location", "");

        SupplyResult result = visitor.visit(invalidPickup, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when pickup code has invalid format")
    void testPickupWithInvalidCodeFormat() {
        PickupDTO invalidPickup = new PickupDTO("Store Location", "PICK@123");

        SupplyResult result = visitor.visit(invalidPickup, validShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should work correctly with guest user shipment")
    void testGuestUserShipment() throws ExternalAPIException {
        ShipmentDetails guestShipment = new ShipmentDetails("SHP-002", "456 Oak Ave", 1, "guest123", true);
        when(mockExternalAPI.sendStandardShippingRequest(anyString(), any(), anyDouble(), anyInt()))
                .thenReturn(11111);

        SupplyResult result = visitor.visit(standardShipping, guestShipment, 1.0);

        assertTrue(result.isSuccess());
    }
}