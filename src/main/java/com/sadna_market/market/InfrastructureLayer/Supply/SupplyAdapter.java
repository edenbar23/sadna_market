package com.sadna_market.market.InfrastructureLayer.Supply;

public class SupplyAdapter implements SupplyInterface {
    private ExternalSupplyAPI api = new ExternalSupplyAPI();

    @Override
    public boolean shipStandard(StandardShippingDTO standardShipping, String address, double weight) {
        return api.sendStandardShippingRequest(standardShipping.carrier, address, weight, standardShipping.estimatedDays);
    }

    @Override
    public boolean shipExpress(ExpressShippingDTO expressShipping, String address, double weight) {
        return api.sendExpressShippingRequest(expressShipping.carrier, address, weight, expressShipping.priorityLevel);
    }

    @Override
    public boolean arrangePickup(PickupDTO pickup, String address, double weight) {
        return api.registerPickupRequest(pickup.storeLocation, pickup.pickupCode, weight);
    }
}