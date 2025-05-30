package com.sadna_market.market.InfrastructureLayer.Supply;

/**
 * Visitor interface for processing different supply/shipping methods
 * Updated to return SupplyResult instead of boolean for better transaction tracking
 */
public interface SupplyVisitor {

    /**
     * Process standard shipping
     * @param standardShipping Standard shipping details
     * @param shipmentDetails Shipment information
     * @param weight Package weight
     * @return SupplyResult containing transaction information
     */
    SupplyResult visit(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight);

    /**
     * Process express shipping
     * @param expressShipping Express shipping details
     * @param shipmentDetails Shipment information
     * @param weight Package weight
     * @return SupplyResult containing transaction information
     */
    SupplyResult visit(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight);

    /**
     * Process store pickup
     * @param pickup Pickup details
     * @param shipmentDetails Shipment information
     * @param weight Package weight
     * @return SupplyResult containing transaction information
     */
    SupplyResult visit(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight);
}