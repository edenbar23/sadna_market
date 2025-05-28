package com.sadna_market.market.InfrastructureLayer.Payment;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for processing payments with transaction tracking and rollback capabilities
 */
@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentVisitor visitor;
    private final ExternalPaymentAPI externalPaymentAPI;

    @Autowired
    public PaymentService(ConcretePaymentVisitor visitor, ExternalPaymentAPI externalPaymentAPI) {
        this.visitor = visitor;
        this.externalPaymentAPI = externalPaymentAPI;
        logger.info("PaymentService initialized with transaction support");
    }

    /**
     * Processes a payment and returns detailed result information
     *
     * @param method The payment method to use
     * @param amount The payment amount
     * @return PaymentResult containing transaction ID and status
     */
    public PaymentResult processPayment(PaymentMethod method, double amount) {
        logger.info("Processing payment for amount: {} using method: {}", amount, method.getClass().getSimpleName());

        if (method == null) {
            logger.error("Payment method cannot be null");
            return PaymentResult.failure("Payment method cannot be null", method, amount);
        }

        if (amount <= 0) {
            logger.error("Payment amount must be positive: {}", amount);
            return PaymentResult.failure("Payment amount must be positive", method, amount);
        }

        try {
            PaymentResult result = method.accept(visitor, amount);

            if (result.isSuccess()) {
                logger.info("Payment processed successfully - Transaction ID: {}", result.getTransactionId());
            } else {
                logger.error("Payment failed: {}", result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error during payment processing", e);
            return PaymentResult.failure("Unexpected error: " + e.getMessage(), method, amount);
        }
    }

    /**
     * Cancels a payment transaction using the transaction ID
     *
     * @param transactionId The transaction ID to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelPayment(int transactionId) {
        logger.info("Cancelling payment transaction: {}", transactionId);

        if (transactionId <= 0) {
            logger.error("Invalid transaction ID for cancellation: {}", transactionId);
            return false;
        }

        try {
            int result = externalPaymentAPI.cancelPayment(transactionId);

            if (result == 1) {
                logger.info("Payment cancellation successful for transaction: {}", transactionId);
                return true;
            } else {
                logger.error("Payment cancellation failed for transaction: {}", transactionId);
                return false;
            }

        } catch (ExternalAPIException e) {
            logger.error("Error cancelling payment transaction: {}", transactionId, e);
            return false;
        }
    }

    /**
     * Refunds a payment transaction (alias for cancelPayment for backward compatibility)
     *
     * @param transactionId The transaction ID to refund
     * @return true if refund was successful, false otherwise
     */
    public boolean refund(int transactionId) {
        logger.info("Processing refund for transaction: {}", transactionId);
        return cancelPayment(transactionId);
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use processPayment() instead which returns PaymentResult
     */
    @Deprecated
    public boolean pay(PaymentMethod method, double amount) {
        logger.warn("pay() method is deprecated. Use processPayment() for better transaction tracking.");
        PaymentResult result = processPayment(method, amount);
        return result.isSuccess();
    }

    /**
     * Tests connectivity to the external payment API
     *
     * @return true if payment API is available
     */
    public boolean testPaymentAPI() {
        logger.info("Testing payment API connectivity");
        return externalPaymentAPI.testConnection();
    }

    /**
     * Gets payment service status information
     *
     * @return Status information about the payment service
     */
    public String getServiceStatus() {
        boolean apiAvailable = testPaymentAPI();
        return String.format("PaymentService[API Available: %s]", apiAvailable);
    }
}