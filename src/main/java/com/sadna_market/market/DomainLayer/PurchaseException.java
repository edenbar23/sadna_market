package com.sadna_market.market.DomainLayer;

public class PurchaseException extends RuntimeException {
    public PurchaseException(String message) {
        super(message);
    }

    public static class InvalidPurchaseException extends PurchaseException {
        public InvalidPurchaseException(String message) {
            super(message);
        }
    }

    public static class PaymentFailedException extends PurchaseException {
        public PaymentFailedException(String message) {
            super(message);
        }
    }

    public static class ItemNotAvailableException extends PurchaseException {
        public ItemNotAvailableException(String message) {
            super(message);
        }
    }

    public static class InsufficientStockException extends PurchaseException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedAccessException extends PurchaseException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class EmptyCartException extends PurchaseException {
        public EmptyCartException(String message) {
            super(message);
        }
    }

    public static class DuplicateCheckoutException extends PurchaseException {
        public DuplicateCheckoutException(String message) {
            super(message);
        }
    }
}
