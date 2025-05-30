package com.sadna_market.market.InfrastructureLayer.Supply;

/**
 * DTO for standard shipping method
 * Updated to return SupplyResult
 */
public class StandardShippingDTO implements SupplyMethod {
    public String carrier;
    public int estimatedDays;

    public StandardShippingDTO(String carrier, int estimatedDays) {
        this.carrier = carrier;
        this.estimatedDays = estimatedDays;
    }

    @Override
    public SupplyResult accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight) {
        return visitor.visit(this, shipmentDetails, weight);
    }

    @Override
    public String toString() {
        return String.format("StandardShipping[carrier=%s, estimatedDays=%d]", carrier, estimatedDays);
    }
}