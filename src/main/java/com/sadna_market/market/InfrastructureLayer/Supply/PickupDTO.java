package com.sadna_market.market.InfrastructureLayer.Supply;

/**
 * DTO for store pickup method
 * Updated to return SupplyResult
 */
public class PickupDTO implements SupplyMethod {
    public String storeLocation;
    public String pickupCode;

    public PickupDTO(String storeLocation, String pickupCode) {
        this.storeLocation = storeLocation;
        this.pickupCode = pickupCode;
    }

    @Override
    public SupplyResult accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight) {
        return visitor.visit(this, shipmentDetails, weight);
    }

    @Override
    public String toString() {
        return String.format("Pickup[location=%s, code=%s]", storeLocation, pickupCode);
    }
}