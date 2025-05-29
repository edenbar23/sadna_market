package com.sadna_market.market.InfrastructureLayer.Supply;

/**
 * DTO for express shipping method
 * Updated to return SupplyResult
 */
public class ExpressShippingDTO implements SupplyMethod {
    public String carrier;
    public int priorityLevel;

    public ExpressShippingDTO(String carrier, int priorityLevel) {
        this.carrier = carrier;
        this.priorityLevel = priorityLevel;
    }

    @Override
    public SupplyResult accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight) {
        return visitor.visit(this, shipmentDetails, weight);
    }

    @Override
    public String toString() {
        return String.format("ExpressShipping[carrier=%s, priorityLevel=%d]", carrier, priorityLevel);
    }
}