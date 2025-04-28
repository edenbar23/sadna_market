package com.sadna_market.market.InfrastructureLayer.Supply;

public class PickupDTO implements SupplyMethod {
    public String storeLocation;
    public String pickupCode;

    public PickupDTO(String storeLocation, String pickupCode) {
        this.storeLocation = storeLocation;
        this.pickupCode = pickupCode;
    }

    @Override
    public boolean accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight) {
        return visitor.visit(this, shipmentDetails, weight);
    }
}