package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyInterface {
    boolean shipStandard(StandardShippingDTO standardShipping, String address, double weight);
    boolean shipExpress(ExpressShippingDTO expressShipping, String address, double weight);
    boolean arrangePickup(PickupDTO pickup, String address, double weight);
}