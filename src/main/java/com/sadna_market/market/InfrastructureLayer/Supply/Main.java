package com.sadna_market.market.InfrastructureLayer.Supply;

public class Main {
    public static void main(String[] args) {
        SupplyService service = new SupplyService();

        // Create shipping methods
        SupplyMethod standard = new StandardShippingDTO("FedEx", 3);
        SupplyMethod express = new ExpressShippingDTO("DHL", 1);
        SupplyMethod pickup = new PickupDTO("Central Store", "PICK123456");

        // Create shipment details
        ShipmentDetails registeredUserShipment = new ShipmentDetails(
                "SHP-001",
                "123 Main St, City, Country",
                2,
                "john_doe",
                false
        );

        ShipmentDetails guestUserShipment = new ShipmentDetails(
                "SHP-002",
                "456 Oak Ave, Town, Country",
                1,
                "guest123",
                true
        );

        double weight = 2.5; // kg

        // Process shipments
        System.out.println("\n=== REGISTERED USER SHIPMENT ===");
        service.ship(standard, registeredUserShipment, weight);

        System.out.println("\n=== GUEST USER SHIPMENT ===");
        service.ship(express, guestUserShipment, weight);

        System.out.println("\n=== PICKUP SHIPMENT ===");
        service.ship(pickup, registeredUserShipment, weight * 2);
    }
}