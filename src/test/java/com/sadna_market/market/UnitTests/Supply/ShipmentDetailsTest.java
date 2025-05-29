// ShipmentDetailsTest.java
package com.sadna_market.market.UnitTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.ShipmentDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shipment Details Tests")
class ShipmentDetailsTest {

    @Test
    @DisplayName("Should return correct shipment ID")
    void testGetShipmentId() {
        ShipmentDetails details = new ShipmentDetails("SHP-001", "123 Main St", 5, "john_doe", false);

        assertEquals("SHP-001", details.getShipmentId());
    }

    @Test
    @DisplayName("Should return correct address")
    void testGetAddress() {
        ShipmentDetails details = new ShipmentDetails("SHP-001", "123 Main St", 5, "john_doe", false);

        assertEquals("123 Main St", details.getAddress());
    }

    @Test
    @DisplayName("Should return correct quantity")
    void testGetQuantity() {
        ShipmentDetails details = new ShipmentDetails("SHP-001", "123 Main St", 5, "john_doe", false);

        assertEquals(5, details.getQuantity());
    }

    @Test
    @DisplayName("Should return correct username")
    void testGetUsername() {
        ShipmentDetails details = new ShipmentDetails("SHP-001", "123 Main St", 5, "john_doe", false);

        assertEquals("john_doe", details.getUsername());
    }

    @Test
    @DisplayName("Should return false for registered user")
    void testIsNotGuest() {
        ShipmentDetails details = new ShipmentDetails("SHP-001", "123 Main St", 5, "john_doe", false);

        assertFalse(details.isGuest());
    }

    @Test
    @DisplayName("Should return true for guest user")
    void testIsGuest() {
        ShipmentDetails details = new ShipmentDetails("SHP-002", "456 Oak Ave", 2, "guest123", true);

        assertTrue(details.isGuest());
    }
}