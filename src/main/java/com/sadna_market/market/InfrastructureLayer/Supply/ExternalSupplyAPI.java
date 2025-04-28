package com.sadna_market.market.InfrastructureLayer.Supply;

public class ExternalSupplyAPI {
    public boolean sendStandardShippingRequest(String carrier, ShipmentDetails details, double weight, int estimatedDays) {
        System.out.println("External API: Arranging standard shipping");
        System.out.println("Carrier: " + carrier);
        System.out.println("Shipment ID: " + details.getShipmentId());
        System.out.println("Address: " + details.getAddress());
        System.out.println("Quantity: " + details.getQuantity());
        System.out.println("User: " + (details.isGuest() ? "Guest" : details.getUsername()));
        System.out.println("Weight: " + weight + " kg");
        System.out.println("Estimated delivery: " + estimatedDays + " days");
        return true;
    }

    public boolean sendExpressShippingRequest(String carrier, ShipmentDetails details, double weight, int priorityLevel) {
        System.out.println("External API: Arranging express shipping");
        System.out.println("Carrier: " + carrier);
        System.out.println("Shipment ID: " + details.getShipmentId());
        System.out.println("Address: " + details.getAddress());
        System.out.println("Quantity: " + details.getQuantity());
        System.out.println("User: " + (details.isGuest() ? "Guest" : details.getUsername()));
        System.out.println("Weight: " + weight + " kg");
        System.out.println("Priority level: " + priorityLevel);
        return true;
    }

    public boolean registerPickupRequest(String location, String pickupCode, ShipmentDetails details, double weight) {
        System.out.println("External API: Registering pickup");
        System.out.println("Location: " + location);
        System.out.println("Pickup code: " + pickupCode);
        System.out.println("Shipment ID: " + details.getShipmentId());
        System.out.println("Address: " + details.getAddress());
        System.out.println("Quantity: " + details.getQuantity());
        System.out.println("User: " + (details.isGuest() ? "Guest" : details.getUsername()));
        System.out.println("Weight: " + weight + " kg");
        return true;
    }
}