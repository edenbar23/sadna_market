package com.sadna_market.market.InfrastructureLayer.Supply;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIConfig;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * External Supply API client that communicates with the real external supply system
 */
@Service
public class ExternalSupplyAPI {
    private static final Logger logger = LoggerFactory.getLogger(ExternalSupplyAPI.class);

    private final ExternalAPIClient apiClient;
    private final ExternalAPIConfig config;

    @Autowired
    public ExternalSupplyAPI(ExternalAPIClient apiClient, ExternalAPIConfig config) {
        this.apiClient = apiClient;
        this.config = config;
        logger.info("ExternalSupplyAPI initialized");
    }

    /**
     * Sends a standard shipping request to the external supply system
     *
     * @param carrier Shipping carrier name
     * @param details Shipment details including address and recipient info
     * @param weight Package weight in kg
     * @param estimatedDays Estimated delivery days
     * @return Transaction ID (10000-100000) if successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int sendStandardShippingRequest(String carrier, ShipmentDetails details,
                                           double weight, int estimatedDays)
            throws ExternalAPIException {

        logger.info("Processing standard shipping request for shipment: {}", details.getShipmentId());

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return generateMockTransactionId();
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "supply");
            parameters.put("name", extractRecipientName(details));
            parameters.put("address", extractStreetAddress(details.getAddress()));
            parameters.put("city", extractCity(details.getAddress()));
            parameters.put("country", extractCountry(details.getAddress()));
            parameters.put("zip", extractZipCode(details.getAddress()));

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be a transaction ID or -1
            int result = parseTransactionResponse(response);

            if (result == -1) {
                logger.error("Standard shipping failed - External API returned -1");
                throw new ExternalAPIException.SupplyException("Shipping request was rejected by external system");
            }

            logger.info("Standard shipping successful - Transaction ID: {}", result);
            return result;

        } catch (ExternalAPIException e) {
            logger.error("Standard shipping request failed", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during standard shipping request", e);
            throw new ExternalAPIException.SupplyException("Unexpected shipping error: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an express shipping request to the external supply system
     *
     * @param carrier Shipping carrier name
     * @param details Shipment details including address and recipient info
     * @param weight Package weight in kg
     * @param priorityLevel Priority level for express shipping
     * @return Transaction ID if successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int sendExpressShippingRequest(String carrier, ShipmentDetails details,
                                          double weight, int priorityLevel)
            throws ExternalAPIException {

        logger.info("Processing express shipping request for shipment: {}", details.getShipmentId());

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return generateMockTransactionId();
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "supply");
            parameters.put("name", extractRecipientName(details));
            parameters.put("address", extractStreetAddress(details.getAddress()));
            parameters.put("city", extractCity(details.getAddress()));
            parameters.put("country", extractCountry(details.getAddress()));
            parameters.put("zip", extractZipCode(details.getAddress()));

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be a transaction ID or -1
            int result = parseTransactionResponse(response);

            if (result == -1) {
                logger.error("Express shipping failed - External API returned -1");
                throw new ExternalAPIException.SupplyException("Express shipping request was rejected by external system");
            }

            logger.info("Express shipping successful - Transaction ID: {}", result);
            return result;

        } catch (ExternalAPIException e) {
            logger.error("Express shipping request failed", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during express shipping request", e);
            throw new ExternalAPIException.SupplyException("Unexpected express shipping error: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a pickup request with the external supply system
     *
     * @param location Store location for pickup
     * @param pickupCode Pickup code for the customer
     * @param details Shipment details
     * @param weight Package weight in kg
     * @return Transaction ID if successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int registerPickupRequest(String location, String pickupCode,
                                     ShipmentDetails details, double weight)
            throws ExternalAPIException {

        logger.info("Processing pickup request for shipment: {} at location: {}",
                details.getShipmentId(), location);

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return generateMockTransactionId();
        }

        try {
            // For pickup, we'll use the store location as the address
            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "supply");
            parameters.put("name", extractRecipientName(details));
            parameters.put("address", location);
            parameters.put("city", "Store Location"); // Placeholder
            parameters.put("country", "Default"); // Placeholder
            parameters.put("zip", "00000"); // Placeholder

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be a transaction ID or -1
            int result = parseTransactionResponse(response);

            if (result == -1) {
                logger.error("Pickup registration failed - External API returned -1");
                throw new ExternalAPIException.SupplyException("Pickup request was rejected by external system");
            }

            logger.info("Pickup registration successful - Transaction ID: {}", result);
            return result;

        } catch (ExternalAPIException e) {
            logger.error("Pickup registration failed", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during pickup registration", e);
            throw new ExternalAPIException.SupplyException("Unexpected pickup error: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels a supply transaction
     *
     * @param transactionId The transaction ID to cancel
     * @return 1 if cancellation successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int cancelSupply(int transactionId) throws ExternalAPIException {
        logger.info("Cancelling supply transaction: {}", transactionId);

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return 1;
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "cancel_supply");
            parameters.put("transaction_id", String.valueOf(transactionId));

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be 1 for success, -1 for failure
            int result = parseTransactionResponse(response);

            if (result == 1) {
                logger.info("Supply cancellation successful for transaction: {}", transactionId);
            } else {
                logger.error("Supply cancellation failed for transaction: {}", transactionId);
            }

            return result;

        } catch (ExternalAPIException e) {
            logger.error("Supply cancellation failed for transaction: {}", transactionId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during supply cancellation", e);
            throw new ExternalAPIException.SupplyException("Unexpected cancellation error: " + e.getMessage(), e);
        }
    }

    /**
     * Tests the connection to the external supply API
     *
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try {
            return apiClient.testConnection();
        } catch (ExternalAPIException e) {
            logger.error("Supply API connection test failed", e);
            return false;
        }
    }

    // Helper methods for parsing address information

    private String extractRecipientName(ShipmentDetails details) {
        if (details.isGuest()) {
            return "Guest Customer";
        }
        return details.getUsername();
    }

    private String extractStreetAddress(String fullAddress) {
        // Simple parsing - in a real system, you'd have more sophisticated address parsing
        String[] parts = fullAddress.split(",");
        return parts.length > 0 ? parts[0].trim() : fullAddress;
    }

    private String extractCity(String fullAddress) {
        // Simple parsing - extract city from address
        String[] parts = fullAddress.split(",");
        return parts.length > 1 ? parts[1].trim() : "Unknown City";
    }

    private String extractCountry(String fullAddress) {
        // Simple parsing - extract country from address
        String[] parts = fullAddress.split(",");
        return parts.length > 2 ? parts[parts.length - 1].trim() : "Unknown Country";
    }

    private String extractZipCode(String fullAddress) {
        // Simple parsing - extract zip code (last numeric part)
        String[] parts = fullAddress.split("[\\s,]+");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].matches("\\d+")) {
                return parts[i];
            }
        }
        return "00000"; // Default if no zip found
    }

    /**
     * Parses the transaction response from the external API
     *
     * @param response Raw response string
     * @return Transaction ID or result code
     * @throws ExternalAPIException if response cannot be parsed
     */
    private int parseTransactionResponse(String response) throws ExternalAPIException {
        try {
            return Integer.parseInt(response.trim());
        } catch (NumberFormatException e) {
            throw new ExternalAPIException.SupplyException("Invalid response format: " + response);
        }
    }

    /**
     * Generates a mock transaction ID for testing/disabled mode
     */
    private int generateMockTransactionId() {
        return 10000 + (int)(Math.random() * 90000);
    }
}