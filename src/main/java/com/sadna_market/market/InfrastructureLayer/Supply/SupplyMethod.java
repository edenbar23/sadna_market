package com.sadna_market.market.InfrastructureLayer.Supply;

/**
 * Interface for supply methods using visitor pattern
 * Updated to return SupplyResult instead of boolean
 */
public interface SupplyMethod {

    /**
     * Accept a supply visitor to process this supply method
     * @param visitor The supply visitor to process this shipment
     * @param shipmentDetails The shipment information
     * @param weight The package weight
     * @return SupplyResult containing transaction information and status
     */
    SupplyResult accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight);
}