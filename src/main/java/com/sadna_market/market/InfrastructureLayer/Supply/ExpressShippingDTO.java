//package com.sadna_market.market.InfrastructureLayer.Supply;

public class ExpressShippingDTO implements SupplyMethod {
    public String carrier;
    public int priorityLevel;

    public ExpressShippingDTO(String carrier, int priorityLevel) {
        this.carrier = carrier;
        this.priorityLevel = priorityLevel;
    }

    @Override
    public boolean accept(SupplyVisitor visitor, String address, double weight) {
        return visitor.visit(this, address, weight);
    }
}