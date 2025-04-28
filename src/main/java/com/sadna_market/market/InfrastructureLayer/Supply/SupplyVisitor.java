package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyVisitor {
    boolean visit(StandardShippingDTO standardShipping, String address, double weight);
    boolean visit(ExpressShippingDTO expressShipping, String address, double weight);
    boolean visit(PickupDTO pickup, String address, double weight);
}