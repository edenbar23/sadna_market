package com.sadna_market.market.InfrastructureLayer.Supply;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * DTO for standard shipping method
 * Updated to return SupplyResult
 */
public class StandardShippingDTO implements SupplyMethod {
    @JsonProperty("carrier")
    @Getter
    public String carrier;

    @JsonProperty("estimatedDays")
    @Getter
    public int estimatedDays;

    @JsonCreator
    public StandardShippingDTO(
            @JsonProperty("carrier") String carrier,
            @JsonProperty("estimatedDays") int estimatedDays) {
        this.carrier = carrier;
        this.estimatedDays = estimatedDays;
    }

    // Default constructor for JSON deserialization
    public StandardShippingDTO() {
        this.carrier = "Standard";
        this.estimatedDays = 3;
    }

    @Override
    public SupplyResult accept(SupplyVisitor visitor, ShipmentDetails shipmentDetails, double weight) {
        return visitor.visit(this, shipmentDetails, weight);
    }

    @Override
    public String toString() {
        return String.format("StandardShipping[carrier=%s, estimatedDays=%d]", carrier, estimatedDays);
    }
}