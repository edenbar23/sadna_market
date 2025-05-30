package com.sadna_market.market.UnitTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Supply Result Tests")
class SupplyResultTest {

    private ShipmentDetails testShipment;
    private StandardShippingDTO standardShipping;

    @BeforeEach
    void setUp() {
        testShipment = new ShipmentDetails("SHP-001", "123 Main St", 2, "user", false);
        standardShipping = new StandardShippingDTO("FedEx", 3);
    }

    @Test
    @DisplayName("Successful supply result should be marked as success")
    void testSuccessfulSupplyResultIsSuccess() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Successful supply result should not be marked as failure")
    void testSuccessfulSupplyResultIsNotFailure() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertFalse(result.isFailure());
    }

    @Test
    @DisplayName("Successful supply result should have correct transaction ID")
    void testSuccessfulSupplyResultTransactionId() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertEquals(12345, result.getTransactionId());
    }

    @Test
    @DisplayName("Successful supply result should have correct tracking info")
    void testSuccessfulSupplyResultTrackingInfo() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertEquals("Standard shipping info", result.getTrackingInfo());
    }

    @Test
    @DisplayName("Successful supply result should have correct shipment details")
    void testSuccessfulSupplyResultShipmentDetails() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertEquals(testShipment, result.getShipmentDetails());
    }

    @Test
    @DisplayName("Successful supply result should have correct supply method")
    void testSuccessfulSupplyResultSupplyMethod() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertEquals(standardShipping, result.getSupplyMethod());
    }

    @Test
    @DisplayName("Successful supply result should have no error message")
    void testSuccessfulSupplyResultNoErrorMessage() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Successful supply result should have valid transaction ID")
    void testSuccessfulSupplyResultHasValidTransactionId() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertTrue(result.hasValidTransactionId());
    }

    @Test
    @DisplayName("Successful supply result should return correct shipment ID")
    void testSuccessfulSupplyResultShipmentId() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "Standard shipping info", testShipment);

        assertEquals("SHP-001", result.getShipmentId());
    }

    @Test
    @DisplayName("Failed supply result should not be marked as success")
    void testFailedSupplyResultIsNotSuccess() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Failed supply result should be marked as failure")
    void testFailedSupplyResultIsFailure() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("Failed supply result should have -1 transaction ID")
    void testFailedSupplyResultTransactionId() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertEquals(-1, result.getTransactionId());
    }

    @Test
    @DisplayName("Failed supply result should have correct error message")
    void testFailedSupplyResultErrorMessage() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertEquals("Shipping failed", result.getErrorMessage());
    }

    @Test
    @DisplayName("Failed supply result should have no tracking info")
    void testFailedSupplyResultNoTrackingInfo() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertNull(result.getTrackingInfo());
    }

    @Test
    @DisplayName("Failed supply result should not have valid transaction ID")
    void testFailedSupplyResultHasInvalidTransactionId() {
        SupplyResult result = SupplyResult.failure("Shipping failed", standardShipping, testShipment);

        assertFalse(result.hasValidTransactionId());
    }

    @Test
    @DisplayName("Standard shipping should return correct supply method type")
    void testStandardShippingSupplyMethodType() {
        SupplyResult result = SupplyResult.success(12345, standardShipping, "info", testShipment);

        assertEquals("Standard Shipping", result.getSupplyMethodType());
    }

    @Test
    @DisplayName("Express shipping should return correct supply method type")
    void testExpressShippingSupplyMethodType() {
        ExpressShippingDTO expressShipping = new ExpressShippingDTO("DHL", 1);
        SupplyResult result = SupplyResult.success(12345, expressShipping, "info", testShipment);

        assertEquals("Express Shipping", result.getSupplyMethodType());
    }

    @Test
    @DisplayName("Pickup should return correct supply method type")
    void testPickupSupplyMethodType() {
        PickupDTO pickup = new PickupDTO("Store Location", "PICK123");
        SupplyResult result = SupplyResult.success(12345, pickup, "info", testShipment);

        assertEquals("Store Pickup", result.getSupplyMethodType());
    }
}