//package com.sadna_market.market.InfrastructureLayer.Supply;
//
//public class SupplyAdapter implements SupplyInterface {
//    private ExternalSupplyAPI api = new ExternalSupplyAPI();
//
//    @Override
//    public boolean shipStandard(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight) {
//        return api.sendStandardShippingRequest(
//                standardShipping.carrier,
//                shipmentDetails,
//                weight,
//                standardShipping.estimatedDays);
//    }
//
//    @Override
//    public boolean shipExpress(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight) {
//        return api.sendExpressShippingRequest(
//                expressShipping.carrier,
//                shipmentDetails,
//                weight,
//                expressShipping.priorityLevel);
//    }
//
//    @Override
//    public boolean arrangePickup(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight) {
//        return api.registerPickupRequest(
//                pickup.storeLocation,
//                pickup.pickupCode,
//                shipmentDetails,
//                weight);
//    }
//}