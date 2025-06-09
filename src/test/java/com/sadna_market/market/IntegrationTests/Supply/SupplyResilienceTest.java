package com.sadna_market.market.IntegrationTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Supply System Resilience Tests")
class SupplyResilienceTest {

    @Autowired
    private SupplyService supplyService;

    @Test
    @DisplayName("Should handle null supply method gracefully")
    void testNullSupplyMethod() {
        ShipmentDetails validShipment = new ShipmentDetails("SHP-001", "123 Test St", 1, "user", false);
        SupplyResult result = supplyService.processShipment(null, validShipment, 2.5);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should handle null shipment details gracefully")
    void testNullShipmentDetails() {
        StandardShippingDTO shipping = new StandardShippingDTO("TestCarrier", 3);
        SupplyResult result = supplyService.processShipment(shipping, null, 2.5);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should handle negative weight gracefully")
    void testNegativeWeight() {
        StandardShippingDTO shipping = new StandardShippingDTO("TestCarrier", 3);
        ShipmentDetails shipment = new ShipmentDetails("SHP-001", "123 Test St", 1, "user", false);
        SupplyResult result = supplyService.processShipment(shipping, shipment, -1.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("must be positive"));
    }

    @Test
    @DisplayName("Should handle zero weight gracefully")
    void testZeroWeight() {
        StandardShippingDTO shipping = new StandardShippingDTO("TestCarrier", 3);
        ShipmentDetails shipment = new ShipmentDetails("SHP-001", "123 Test St", 1, "user", false);
        SupplyResult result = supplyService.processShipment(shipping, shipment, 0.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("must be positive"));
    }

    @Test
    @DisplayName("Should handle invalid carrier name gracefully")
    void testInvalidCarrierName() {
        StandardShippingDTO shipping = new StandardShippingDTO("", 3);
        ShipmentDetails shipment = new ShipmentDetails("SHP-001", "123 Test St", 1, "user", false);
        SupplyResult result = supplyService.processShipment(shipping, shipment, 2.5);

        assertNotNull(result);
        // API should handle empty carrier name appropriately
    }

    @Test
    @DisplayName("Should handle very long address gracefully")
    void testVeryLongAddress() {
        StringBuilder longAddress = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longAddress.append("Very long address ");
        }

        StandardShippingDTO shipping = new StandardShippingDTO("TestCarrier", 3);
        ShipmentDetails shipment = new ShipmentDetails("SHP-001", longAddress.toString(), 1, "user", false);
        SupplyResult result = supplyService.processShipment(shipping, shipment, 2.5);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle very large quantity gracefully")
    void testVeryLargeQuantity() {
        StandardShippingDTO shipping = new StandardShippingDTO("TestCarrier", 3);
        ShipmentDetails shipment = new ShipmentDetails("SHP-001", "123 Test St", 999999, "user", false);
        SupplyResult result = supplyService.processShipment(shipping, shipment, 2.5);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should reject null shipment ID in creation")
    void testCreateShipmentWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails(null, "123 Test St", 1, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject empty shipment ID in creation")
    void testCreateShipmentWithEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("", "123 Test St", 1, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject null address in creation")
    void testCreateShipmentWithNullAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", null, 1, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject empty address in creation")
    void testCreateShipmentWithEmptyAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "", 1, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject zero quantity in creation")
    void testCreateShipmentWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Test St", 0, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject negative quantity in creation")
    void testCreateShipmentWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Test St", -1, "user", false);
        });
    }

    @Test
    @DisplayName("Should reject null username for registered user")
    void testCreateShipmentWithNullUsernameForRegisteredUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Test St", 1, null, false);
        });
    }

    @Test
    @DisplayName("Should reject empty username for registered user")
    void testCreateShipmentWithEmptyUsernameForRegisteredUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Test St", 1, "", false);
        });
    }

    @Test
    @DisplayName("Should handle cancellation of negative transaction ID")
    void testCancelNegativeTransactionId() {
        boolean result = supplyService.cancelShipment(-1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle cancellation of zero transaction ID")
    void testCancelZeroTransactionId() {
        boolean result = supplyService.cancelShipment(0);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should maintain shipment data integrity during processing")
    void testShipmentDataIntegrity() {
        StandardShippingDTO originalShipping = new StandardShippingDTO("OriginalCarrier", 5);
        ShipmentDetails originalShipment = new ShipmentDetails(
                "SHP-ORIGINAL", "Original Address", 2, "originaluser", false
        );

        supplyService.processShipment(originalShipping, originalShipment, 3.0);

        // Verify original data is unchanged
        assertEquals("OriginalCarrier", originalShipping.getCarrier());
        assertEquals(5, originalShipping.getEstimatedDays());
        assertEquals("SHP-ORIGINAL", originalShipment.getShipmentId());
        assertEquals("Original Address", originalShipment.getAddress());
        assertEquals(2, originalShipment.getQuantity());
        assertEquals("originaluser", originalShipment.getUsername());
        assertFalse(originalShipment.isGuest());
    }
}