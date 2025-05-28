package com.sadna_market.market.InfrastructureLayer.Supply;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for processing shipments with transaction tracking and rollback capabilities
 */
@Service
public class SupplyService {
    private static final Logger logger = LoggerFactory.getLogger(SupplyService.class);

    private final SupplyVisitor visitor;
    private final ExternalSupplyAPI externalSupplyAPI;

    @Autowired
    public SupplyService(ConcreteSupplyVisitor visitor, ExternalSupplyAPI externalSupplyAPI) {
        this.visitor = visitor;
        this.externalSupplyAPI = externalSupplyAPI;
        logger.info("SupplyService initialized with transaction support");
    }

    /**
     * Processes a shipment and returns detailed result information
     *
     * @param method The supply method to use
     * @param shipmentDetails The shipment information
     * @param weight The package weight
     * @return SupplyResult containing transaction ID and status
     */
    public SupplyResult processShipment(SupplyMethod method, ShipmentDetails shipmentDetails, double weight) {
        if (method == null) {
            logger.error("Supply method cannot be null");
            return SupplyResult.failure("Supply method cannot be null", method, shipmentDetails);
        }

        if (shipmentDetails == null) {
            logger.error("Shipment details cannot be null");
            return SupplyResult.failure("Shipment details cannot be null", method, shipmentDetails);
        }

        logger.info("Processing shipment {} using method: {}",
                shipmentDetails.getShipmentId(), method.getClass().getSimpleName());

        if (weight <= 0) {
            logger.error("Package weight must be positive: {}", weight);
            return SupplyResult.failure("Package weight must be positive", method, shipmentDetails);
        }

        try {
            SupplyResult result = method.accept(visitor, shipmentDetails, weight);

            if (result.isSuccess()) {
                logger.info("Shipment processed successfully - Transaction ID: {}", result.getTransactionId());
            } else {
                logger.error("Shipment failed: {}", result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error during shipment processing", e);
            return SupplyResult.failure("Unexpected error: " + e.getMessage(), method, shipmentDetails);
        }
    }

    /**
     * Cancels a supply transaction using the transaction ID
     *
     * @param transactionId The transaction ID to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelShipment(int transactionId) {
        logger.info("Cancelling supply transaction: {}", transactionId);

        if (transactionId <= 0) {
            logger.error("Invalid transaction ID for cancellation: {}", transactionId);
            return false;
        }

        try {
            int result = externalSupplyAPI.cancelSupply(transactionId);

            if (result == 1) {
                logger.info("Supply cancellation successful for transaction: {}", transactionId);
                return true;
            } else {
                logger.error("Supply cancellation failed for transaction: {}", transactionId);
                return false;
            }

        } catch (ExternalAPIException e) {
            logger.error("Error cancelling supply transaction: {}", transactionId, e);
            return false;
        }
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use processShipment() instead which returns SupplyResult
     */
    @Deprecated
    public boolean ship(SupplyMethod method, ShipmentDetails shipmentDetails, double weight) {
        logger.warn("ship() method is deprecated. Use processShipment() for better transaction tracking.");
        SupplyResult result = processShipment(method, shipmentDetails, weight);
        return result.isSuccess();
    }

    /**
     * Tests connectivity to the external supply API
     *
     * @return true if supply API is available
     */
    public boolean testSupplyAPI() {
        logger.info("Testing supply API connectivity");
        return externalSupplyAPI.testConnection();
    }

    /**
     * Gets supply service status information
     *
     * @return Status information about the supply service
     */
    public String getServiceStatus() {
        boolean apiAvailable = testSupplyAPI();
        return String.format("SupplyService[API Available: %s]", apiAvailable);
    }

    /**
     * Creates a shipment details object for processing
     * Helper method for creating shipment details with proper validation
     */
    public ShipmentDetails createShipmentDetails(String shipmentId, String address,
                                                 int quantity, String username, boolean isGuest) {
        if (shipmentId == null || shipmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Shipment ID cannot be null or empty");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!isGuest && (username == null || username.trim().isEmpty())) {
            throw new IllegalArgumentException("Username cannot be null or empty for registered users");
        }

        return new ShipmentDetails(shipmentId, address, quantity, username, isGuest);
    }
}