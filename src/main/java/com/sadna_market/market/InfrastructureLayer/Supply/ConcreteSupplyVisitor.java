package com.sadna_market.market.InfrastructureLayer.Supply;

public class ConcreteSupplyVisitor implements SupplyVisitor {
    private ExternalSupplyAPI api = new ExternalSupplyAPI();

    @Override
    public boolean visit(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight) {
        System.out.println("Visitor: Processing standard shipping with " + standardShipping.carrier + "...");
        System.out.println("Shipment details: " + shipmentDetails);
        return api.sendStandardShippingRequest(
                standardShipping.carrier,
                shipmentDetails,
                weight,
                standardShipping.estimatedDays);
    }

    @Override
    public boolean visit(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight) {
        System.out.println("Visitor: Processing express shipping with priority " + expressShipping.priorityLevel + "...");
        System.out.println("Shipment details: " + shipmentDetails);
        return api.sendExpressShippingRequest(
                expressShipping.carrier,
                shipmentDetails,
                weight,
                expressShipping.priorityLevel);
    }

    @Override
    public boolean visit(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight) {
        System.out.println("Visitor: Processing store pickup at " + pickup.storeLocation + "...");
        System.out.println("Shipment details: " + shipmentDetails);
        return api.registerPickupRequest(
                pickup.storeLocation,
                pickup.pickupCode,
                shipmentDetails,
                weight);
    }
}