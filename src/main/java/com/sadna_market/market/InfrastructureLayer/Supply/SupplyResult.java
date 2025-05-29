package com.sadna_market.market.InfrastructureLayer.Supply;

import lombok.Getter;

/**
 * Result object for supply/shipping processing operations
 * Contains transaction information needed for tracking and rollback
 */
@Getter
public class SupplyResult {
    /**
     * -- GETTER --
     *  Check if supply operation was successful
     */
    private final boolean success;
    /**
     * -- GETTER --
     *  Get transaction ID (returns -1 if operation failed)
     */
    private final int transactionId;
    private final String errorMessage;
    private final SupplyMethod supplyMethod;
    private final String trackingInfo;
    private final ShipmentDetails shipmentDetails;

    /**
     * Constructor for successful supply operation
     */
    public SupplyResult(boolean success, int transactionId, SupplyMethod supplyMethod,
                        String trackingInfo, ShipmentDetails shipmentDetails) {
        this.success = success;
        this.transactionId = transactionId;
        this.errorMessage = null;
        this.supplyMethod = supplyMethod;
        this.trackingInfo = trackingInfo;
        this.shipmentDetails = shipmentDetails;
    }

    /**
     * Constructor for failed supply operation
     */
    public SupplyResult(boolean success, String errorMessage, SupplyMethod supplyMethod,
                        ShipmentDetails shipmentDetails) {
        this.success = success;
        this.transactionId = -1;
        this.errorMessage = errorMessage;
        this.supplyMethod = supplyMethod;
        this.trackingInfo = null;
        this.shipmentDetails = shipmentDetails;
    }

    /**
     * Static factory method for successful supply operation
     */
    public static SupplyResult success(int transactionId, SupplyMethod supplyMethod,
                                       String trackingInfo, ShipmentDetails shipmentDetails) {
        return new SupplyResult(true, transactionId, supplyMethod, trackingInfo, shipmentDetails);
    }

    /**
     * Static factory method for failed supply operation
     */
    public static SupplyResult failure(String errorMessage, SupplyMethod supplyMethod,
                                       ShipmentDetails shipmentDetails) {
        return new SupplyResult(false, errorMessage, supplyMethod, shipmentDetails);
    }

    /**
     * Check if supply operation failed
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Check if transaction ID is valid
     */
    public boolean hasValidTransactionId() {
        return success && transactionId > 0;
    }

    /**
     * Get shipment ID from details
     */
    public String getShipmentId() {
        return shipmentDetails != null ? shipmentDetails.getShipmentId() : null;
    }

    /**
     * Get supply method type as string
     */
    public String getSupplyMethodType() {
        if (supplyMethod instanceof StandardShippingDTO) {
            return "Standard Shipping";
        } else if (supplyMethod instanceof ExpressShippingDTO) {
            return "Express Shipping";
        } else if (supplyMethod instanceof PickupDTO) {
            return "Store Pickup";
        }
        return "Unknown";
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("SupplyResult[SUCCESS: transactionId=%d, method=%s, shipmentId=%s]",
                    transactionId, getSupplyMethodType(), getShipmentId());
        } else {
            return String.format("SupplyResult[FAILURE: error='%s', method=%s, shipmentId=%s]",
                    errorMessage, getSupplyMethodType(), getShipmentId());
        }
    }
}