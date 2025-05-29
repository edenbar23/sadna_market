package com.sadna_market.market.InfrastructureLayer.Payment;

import lombok.Getter;

/**
 * Result object for validation operations
 * Encapsulates validation status and error messages
 */
@Getter
public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a valid validation result
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    /**
     * Creates an invalid validation result with error message
     */
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }

    /**
     * Check if validation failed
     */
    public boolean isInvalid() {
        return !valid;
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult[VALID]";
        } else {
            return String.format("ValidationResult[INVALID: %s]", errorMessage);
        }
    }
}