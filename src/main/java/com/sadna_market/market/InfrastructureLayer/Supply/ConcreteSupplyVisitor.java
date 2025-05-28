package com.sadna_market.market.InfrastructureLayer.Supply;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of SupplyVisitor that processes different shipping methods
 * through the external supply API
 */
@Component
public class ConcreteSupplyVisitor implements SupplyVisitor {
    private static final Logger logger = LoggerFactory.getLogger(ConcreteSupplyVisitor.class);

    private final ExternalSupplyAPI api;

    @Autowired
    public ConcreteSupplyVisitor(ExternalSupplyAPI api) {
        this.api = api;
        logger.info("ConcreteSupplyVisitor initialized with external API integration");
    }

    @Override
    public SupplyResult visit(StandardShippingDTO standardShipping, ShipmentDetails shipmentDetails, double weight) {
        logger.info("Processing standard shipping with carrier: {} for shipment: {}",
                standardShipping.carrier, shipmentDetails.getShipmentId());

        try {
            // Validate shipping details
            validateShipmentDetails(shipmentDetails);
            validateWeight(weight);

            // Log shipping details for tracking
            logShipmentInfo("Standard Shipping", standardShipping.carrier, shipmentDetails, weight);

            // Process shipping through external API
            int transactionId = api.sendStandardShippingRequest(
                    standardShipping.carrier,
                    shipmentDetails,
                    weight,
                    standardShipping.estimatedDays
            );

            if (transactionId == -1) {
                String error = "Standard shipping was rejected by external system";
                logger.error(error);
                return SupplyResult.failure(error, standardShipping, shipmentDetails);
            }

            String trackingInfo = String.format("Standard shipping via %s, estimated %d days",
                    standardShipping.carrier, standardShipping.estimatedDays);

            logger.info("Standard shipping successful - Transaction ID: {}, Estimated delivery: {} days",
                    transactionId, standardShipping.estimatedDays);

            return SupplyResult.success(transactionId, standardShipping, trackingInfo, shipmentDetails);

        } catch (ExternalAPIException e) {
            logger.error("External API error during standard shipping", e);
            return SupplyResult.failure("Shipping processing failed: " + e.getMessage(), standardShipping, shipmentDetails);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during standard shipping", e);
            return SupplyResult.failure("Validation error: " + e.getMessage(), standardShipping, shipmentDetails);
        } catch (Exception e) {
            logger.error("Unexpected error during standard shipping", e);
            return SupplyResult.failure("Unexpected shipping error: " + e.getMessage(), standardShipping, shipmentDetails);
        }
    }

    @Override
    public SupplyResult visit(ExpressShippingDTO expressShipping, ShipmentDetails shipmentDetails, double weight) {
        logger.info("Processing express shipping with priority level: {} for shipment: {}",
                expressShipping.priorityLevel, shipmentDetails.getShipmentId());

        try {
            // Validate shipping details
            validateShipmentDetails(shipmentDetails);
            validateWeight(weight);
            validatePriorityLevel(expressShipping.priorityLevel);

            // Log shipping details for tracking
            logShipmentInfo("Express Shipping", expressShipping.carrier, shipmentDetails, weight);

            // Process shipping through external API
            int transactionId = api.sendExpressShippingRequest(
                    expressShipping.carrier,
                    shipmentDetails,
                    weight,
                    expressShipping.priorityLevel
            );

            if (transactionId == -1) {
                String error = "Express shipping was rejected by external system";
                logger.error(error);
                return SupplyResult.failure(error, expressShipping, shipmentDetails);
            }

            String trackingInfo = String.format("Express shipping via %s, priority level %d",
                    expressShipping.carrier, expressShipping.priorityLevel);

            logger.info("Express shipping successful - Transaction ID: {}, Priority level: {}",
                    transactionId, expressShipping.priorityLevel);

            return SupplyResult.success(transactionId, expressShipping, trackingInfo, shipmentDetails);

        } catch (ExternalAPIException e) {
            logger.error("External API error during express shipping", e);
            return SupplyResult.failure("Express shipping processing failed: " + e.getMessage(), expressShipping, shipmentDetails);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during express shipping", e);
            return SupplyResult.failure("Validation error: " + e.getMessage(), expressShipping, shipmentDetails);
        } catch (Exception e) {
            logger.error("Unexpected error during express shipping", e);
            return SupplyResult.failure("Unexpected express shipping error: " + e.getMessage(), expressShipping, shipmentDetails);
        }
    }

    @Override
    public SupplyResult visit(PickupDTO pickup, ShipmentDetails shipmentDetails, double weight) {
        logger.info("Processing store pickup at location: {} for shipment: {}",
                pickup.storeLocation, shipmentDetails.getShipmentId());

        try {
            // Validate pickup details
            validateShipmentDetails(shipmentDetails);
            validateWeight(weight);
            validatePickupDetails(pickup);

            // Log pickup details for tracking
            logPickupInfo(pickup, shipmentDetails, weight);

            // Register pickup through external API
            int transactionId = api.registerPickupRequest(
                    pickup.storeLocation,
                    pickup.pickupCode,
                    shipmentDetails,
                    weight
            );

            if (transactionId == -1) {
                String error = "Pickup registration was rejected by external system";
                logger.error(error);
                return SupplyResult.failure(error, pickup, shipmentDetails);
            }

            String trackingInfo = String.format("Store pickup at %s, code: %s",
                    pickup.storeLocation, pickup.pickupCode);

            logger.info("Pickup registration successful - Transaction ID: {}, Location: {}, Code: {}",
                    transactionId, pickup.storeLocation, pickup.pickupCode);

            return SupplyResult.success(transactionId, pickup, trackingInfo, shipmentDetails);

        } catch (ExternalAPIException e) {
            logger.error("External API error during pickup registration", e);
            return SupplyResult.failure("Pickup processing failed: " + e.getMessage(), pickup, shipmentDetails);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during pickup registration", e);
            return SupplyResult.failure("Validation error: " + e.getMessage(), pickup, shipmentDetails);
        } catch (Exception e) {
            logger.error("Unexpected error during pickup registration", e);
            return SupplyResult.failure("Unexpected pickup error: " + e.getMessage(), pickup, shipmentDetails);
        }
    }

    // Validation methods

    /**
     * Validates shipment details
     */
    private void validateShipmentDetails(ShipmentDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("Shipment details cannot be null");
        }

        if (details.getShipmentId() == null || details.getShipmentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipment ID cannot be null or empty");
        }

        if (details.getAddress() == null || details.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipment address cannot be null or empty");
        }

        if (details.getQuantity() <= 0) {
            throw new IllegalArgumentException("Shipment quantity must be positive");
        }

        // Validate username for registered users
        if (!details.isGuest() && (details.getUsername() == null || details.getUsername().trim().isEmpty())) {
            throw new IllegalArgumentException("Username cannot be null or empty for registered users");
        }
    }

    /**
     * Validates package weight
     */
    private void validateWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Package weight must be positive");
        }

        // Check for reasonable weight limits (e.g., max 100kg)
        if (weight > 100.0) {
            throw new IllegalArgumentException("Package weight exceeds maximum limit of 100kg");
        }
    }

    /**
     * Validates express shipping priority level
     */
    private void validatePriorityLevel(int priorityLevel) {
        if (priorityLevel < 1 || priorityLevel > 5) {
            throw new IllegalArgumentException("Priority level must be between 1 and 5");
        }
    }

    /**
     * Validates pickup details
     */
    private void validatePickupDetails(PickupDTO pickup) {
        if (pickup.storeLocation == null || pickup.storeLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Store location cannot be null or empty");
        }

        if (pickup.pickupCode == null || pickup.pickupCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup code cannot be null or empty");
        }

        // Validate pickup code format (assuming alphanumeric)
        if (!pickup.pickupCode.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("Pickup code must be alphanumeric");
        }
    }

    // Logging methods for better tracking

    /**
     * Logs shipment information for tracking purposes
     */
    private void logShipmentInfo(String shippingType, String carrier, ShipmentDetails details, double weight) {
        logger.info("{} Details:", shippingType);
        logger.info("  Carrier: {}", carrier);
        logger.info("  Shipment ID: {}", details.getShipmentId());
        logger.info("  Address: {}", details.getAddress());
        logger.info("  Quantity: {}", details.getQuantity());
        logger.info("  Weight: {} kg", weight);
        logger.info("  Recipient: {}", details.isGuest() ? "Guest Customer" : details.getUsername());
    }

    /**
     * Logs pickup information for tracking purposes
     */
    private void logPickupInfo(PickupDTO pickup, ShipmentDetails details, double weight) {
        logger.info("Store Pickup Details:");
        logger.info("  Location: {}", pickup.storeLocation);
        logger.info("  Pickup Code: {}", pickup.pickupCode);
        logger.info("  Shipment ID: {}", details.getShipmentId());
        logger.info("  Quantity: {}", details.getQuantity());
        logger.info("  Weight: {} kg", weight);
        logger.info("  Customer: {}", details.isGuest() ? "Guest Customer" : details.getUsername());
    }
}