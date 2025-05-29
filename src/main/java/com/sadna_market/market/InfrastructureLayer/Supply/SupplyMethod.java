package com.sadna_market.market.InfrastructureLayer.Supply;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for supply methods using visitor pattern
 * Updated to return SupplyResult instead of boolean
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StandardShippingDTO.class, name = "standardShipping"),
        @JsonSubTypes.Type(value = ExpressShippingDTO.class, name = "expressShipping"),
        @JsonSubTypes.Type(value = PickupDTO.class, name = "pickup")
})

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