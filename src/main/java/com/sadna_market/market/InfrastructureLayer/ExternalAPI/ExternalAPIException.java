package com.sadna_market.market.InfrastructureLayer.ExternalAPI;

/**
 * Exception thrown when external API operations fail
 */
public class ExternalAPIException extends Exception {

    /**
     * Creates a new ExternalAPIException with the specified message
     */
    public ExternalAPIException(String message) {
        super(message);
    }

    /**
     * Creates a new ExternalAPIException with the specified message and cause
     */
    public ExternalAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new ExternalAPIException with the specified cause
     */
    public ExternalAPIException(Throwable cause) {
        super(cause);
    }

    /**
     * Specific exception for payment-related failures
     */
    public static class PaymentException extends ExternalAPIException {
        public PaymentException(String message) {
            super("Payment failed: " + message);
        }

        public PaymentException(String message, Throwable cause) {
            super("Payment failed: " + message, cause);
        }
    }

    /**
     * Specific exception for supply/shipping-related failures
     */
    public static class SupplyException extends ExternalAPIException {
        public SupplyException(String message) {
            super("Supply operation failed: " + message);
        }

        public SupplyException(String message, Throwable cause) {
            super("Supply operation failed: " + message, cause);
        }
    }

    /**
     * Exception for network connectivity issues
     */
    public static class NetworkException extends ExternalAPIException {
        public NetworkException(String message) {
            super("Network error: " + message);
        }

        public NetworkException(String message, Throwable cause) {
            super("Network error: " + message, cause);
        }
    }

    /**
     * Exception for API timeout issues
     */
    public static class TimeoutException extends ExternalAPIException {
        public TimeoutException(String message) {
            super("Request timeout: " + message);
        }

        public TimeoutException(String message, Throwable cause) {
            super("Request timeout: " + message, cause);
        }
    }
}