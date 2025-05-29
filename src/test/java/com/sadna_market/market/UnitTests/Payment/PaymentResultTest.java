package com.sadna_market.market.UnitTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Result Tests")
class PaymentResultTest {

    @Test
    @DisplayName("Successful payment result should be marked as success")
    void testSuccessfulPaymentResultIsSuccess() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Successful payment result should not be marked as failure")
    void testSuccessfulPaymentResultIsNotFailure() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertFalse(result.isFailure());
    }

    @Test
    @DisplayName("Successful payment result should have correct transaction ID")
    void testSuccessfulPaymentResultTransactionId() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertEquals(12345, result.getTransactionId());
    }

    @Test
    @DisplayName("Successful payment result should have correct amount")
    void testSuccessfulPaymentResultAmount() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertEquals(100.0, result.getAmount());
    }

    @Test
    @DisplayName("Successful payment result should have correct payment method")
    void testSuccessfulPaymentResultPaymentMethod() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertEquals(card, result.getPaymentMethod());
    }

    @Test
    @DisplayName("Successful payment result should have no error message")
    void testSuccessfulPaymentResultNoErrorMessage() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Successful payment result should have valid transaction ID")
    void testSuccessfulPaymentResultHasValidTransactionId() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.success(12345, card, 100.0);

        assertTrue(result.hasValidTransactionId());
    }

    @Test
    @DisplayName("Failed payment result should not be marked as success")
    void testFailedPaymentResultIsNotSuccess() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.failure("Payment declined", card, 100.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Failed payment result should be marked as failure")
    void testFailedPaymentResultIsFailure() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.failure("Payment declined", card, 100.0);

        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("Failed payment result should have -1 transaction ID")
    void testFailedPaymentResultTransactionId() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.failure("Payment declined", card, 100.0);

        assertEquals(-1, result.getTransactionId());
    }

    @Test
    @DisplayName("Failed payment result should have correct error message")
    void testFailedPaymentResultErrorMessage() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.failure("Payment declined", card, 100.0);

        assertEquals("Payment declined", result.getErrorMessage());
    }

    @Test
    @DisplayName("Failed payment result should not have valid transaction ID")
    void testFailedPaymentResultHasInvalidTransactionId() {
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult result = PaymentResult.failure("Payment declined", card, 100.0);

        assertFalse(result.hasValidTransactionId());
    }
}