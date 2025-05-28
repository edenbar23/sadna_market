package com.sadna_market.market.InfrastructureLayer.Payment;

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
 * External Payment API client that communicates with the real external payment system
 */
@Service
public class ExternalPaymentAPI {
    private static final Logger logger = LoggerFactory.getLogger(ExternalPaymentAPI.class);

    private final ExternalAPIClient apiClient;
    private final ExternalAPIConfig config;

    @Autowired
    public ExternalPaymentAPI(ExternalAPIClient apiClient, ExternalAPIConfig config) {
        this.apiClient = apiClient;
        this.config = config;
        logger.info("ExternalPaymentAPI initialized");
    }

    /**
     * Processes a credit card payment through the external API
     *
     * @param cardNumber Credit card number
     * @param cardHolderName Name on the credit card
     * @param expiryDate Expiry date in MM/YY format
     * @param cvv Card verification value
     * @param amount Payment amount
     * @return Transaction ID (10000-100000) if successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int sendCreditCardPayment(String cardNumber, String cardHolderName,
                                     String expiryDate, String cvv, double amount)
            throws ExternalAPIException {

        logger.info("Processing credit card payment for amount: {}", amount);

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return generateMockTransactionId();
        }

        try {
            // Parse expiry date from MM/YY format
            String[] expiryParts = expiryDate.split("/");
            if (expiryParts.length != 2) {
                throw new ExternalAPIException.PaymentException("Invalid expiry date format. Expected MM/YY");
            }

            String month = expiryParts[0];
            String year = "20" + expiryParts[1]; // Convert YY to 20YY

            // Generate unique transaction ID for this payment request
            String transactionId = generateTransactionId();

            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "pay");
            parameters.put("amount", String.valueOf((int)(amount * 100))); // Convert to cents
            parameters.put("currency", "USD");
            parameters.put("card_number", cardNumber);
            parameters.put("month", month);
            parameters.put("year", year);
            parameters.put("holder", cardHolderName);
            parameters.put("cvv", cvv);
            parameters.put("id", transactionId);

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be a transaction ID or -1
            int result = parseTransactionResponse(response);

            if (result == -1) {
                logger.error("Payment failed - External API returned -1");
                throw new ExternalAPIException.PaymentException("Payment was declined by external system");
            }

            logger.info("Payment successful - Transaction ID: {}", result);
            return result;

        } catch (ExternalAPIException e) {
            logger.error("Credit card payment failed", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during credit card payment", e);
            throw new ExternalAPIException.PaymentException("Unexpected payment error: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a bank account payment through the external API
     * Note: The external API documentation only shows credit card payments,
     * so this method will use a simplified approach
     *
     * @param accountNumber Bank account number
     * @param bankName Name of the bank
     * @param amount Payment amount
     * @return Transaction ID if successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int sendBankPayment(String accountNumber, String bankName, double amount)
            throws ExternalAPIException {

        logger.info("Processing bank payment for amount: {} from bank: {}", amount, bankName);

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return generateMockTransactionId();
        }

        // Since the external API doesn't specify bank payment format,
        // we'll use a mock implementation that always succeeds
        // In a real system, you'd need to clarify the bank payment API format

        logger.warn("Bank payment processed as mock - External API doesn't specify bank payment format");
        return generateMockTransactionId();
    }

    /**
     * Cancels a payment transaction
     *
     * @param transactionId The transaction ID to cancel
     * @return 1 if cancellation successful, -1 if failed
     * @throws ExternalAPIException if the API call fails
     */
    public int cancelPayment(int transactionId) throws ExternalAPIException {
        logger.info("Cancelling payment transaction: {}", transactionId);

        if (!config.isEnabled()) {
            logger.warn("External API is disabled, returning mock success");
            return 1;
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("action_type", "cancel_pay");
            parameters.put("transaction_id", String.valueOf(transactionId));

            String response = apiClient.sendPostRequest(parameters);

            // Parse response - should be 1 for success, -1 for failure
            int result = parseTransactionResponse(response);

            if (result == 1) {
                logger.info("Payment cancellation successful for transaction: {}", transactionId);
            } else {
                logger.error("Payment cancellation failed for transaction: {}", transactionId);
            }

            return result;

        } catch (ExternalAPIException e) {
            logger.error("Payment cancellation failed for transaction: {}", transactionId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during payment cancellation", e);
            throw new ExternalAPIException.PaymentException("Unexpected cancellation error: " + e.getMessage(), e);
        }
    }

    /**
     * Tests the connection to the external payment API
     *
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try {
            return apiClient.testConnection();
        } catch (ExternalAPIException e) {
            logger.error("Payment API connection test failed", e);
            return false;
        }
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
            throw new ExternalAPIException.PaymentException("Invalid response format: " + response);
        }
    }

    /**
     * Generates a unique transaction ID for payment requests
     */
    private String generateTransactionId() {
        return String.valueOf(System.currentTimeMillis() % 100000000);
    }

    /**
     * Generates a mock transaction ID for testing/disabled mode
     */
    private int generateMockTransactionId() {
        return 10000 + (int)(Math.random() * 90000);
    }
}