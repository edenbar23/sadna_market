//package com.sadna_market.market.InfrastructureLayer.Supply;
//
//public class SupplyProxy implements SupplyInterface {
//    private SupplyAdapter adapter = new SupplyAdapter();
//
//    @Override
//    public boolean shipStandard(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight) {
//        System.out.println("Proxy: Logging standard shipping request...");
//        System.out.println("Shipment ID: " + shipmentDetails.getShipmentId());
//        return adapter.shipStandard(standardShipping, shipmentDetails, weight);
//    }
//
//    @Override
//    public boolean shipExpress(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight) {
//        System.out.println("Proxy: Logging express shipping request...");
//        System.out.println("Shipment ID: " + shipmentDetails.getShipmentId());
//        return adapter.shipExpress(expressShipping, shipmentDetails, weight);
//    }
//
//    @Override
//    public boolean arrangePickup(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight) {
//        System.out.println("Proxy: Logging pickup request...");
//        System.out.println("Shipment ID: " + shipmentDetails.getShipmentId());
//        return adapter.arrangePickup(pickup, shipmentDetails, weight);
//    }
//}
