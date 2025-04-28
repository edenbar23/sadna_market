package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyMethod {
    boolean accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight);
}

