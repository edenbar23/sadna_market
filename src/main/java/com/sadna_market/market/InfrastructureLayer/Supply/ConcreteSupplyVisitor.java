package com.sadna_market.market.InfrastructureLayer.Supply;

public class ConcreteSupplyVisitor implements SupplyVisitor {
    private ExternalSupplyAPI api = new ExternalSupplyAPI();

    @Override
    public boolean visit(StandardShippingDTO standardShipping, String address, double weight) {
        System.out.println("Visitor: Processing standard shipping with " + standardShipping.carrier + "...");
        return api.sendStandardShippingRequest(standardShipping.carrier, address, weight, standardShipping.estimatedDays);
    }

    @Override
    public boolean visit(ExpressShippingDTO expressShipping, String address, double weight) {
        System.out.println("Visitor: Processing express shipping with priority " + expressShipping.priorityLevel + "...");
        return api.sendExpressShippingRequest(expressShipping.carrier, address, weight, expressShipping.priorityLevel);
    }

    @Override
    public boolean visit(PickupDTO pickup, String address, double weight) {
        System.out.println("Visitor: Processing store pickup at " + pickup.storeLocation + "...");
        return api.registerPickupRequest(pickup.storeLocation, pickup.pickupCode, weight);
    }
}