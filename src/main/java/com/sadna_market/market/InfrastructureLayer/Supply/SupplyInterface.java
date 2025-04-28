package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyInterface {
    boolean shipStandard(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight);
    boolean shipExpress(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight);
    boolean arrangePickup(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight);
}