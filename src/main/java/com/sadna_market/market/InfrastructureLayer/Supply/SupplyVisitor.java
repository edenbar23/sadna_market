package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyVisitor {
    boolean visit(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight);
    boolean visit(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight);
    boolean visit(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight);
}