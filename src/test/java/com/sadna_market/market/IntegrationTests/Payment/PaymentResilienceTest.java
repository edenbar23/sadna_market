package com.sadna_market.market.IntegrationTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Payment System Resilience Tests")
class PaymentResilienceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("Should handle null payment method gracefully")
    void testNullPaymentMethod() {
        PaymentResult result = paymentService.processPayment(null, 100.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should handle negative payment amount gracefully")
    void testNegativePaymentAmount() {
        CreditCardDTO card = new CreditCardDTO("4111111111111111", "Test User", "12/25", "123");
        PaymentResult result = paymentService.processPayment(card, -50.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("must be positive"));
    }

    @Test
    @DisplayName("Should handle zero payment amount gracefully")
    void testZeroPaymentAmount() {
        CreditCardDTO card = new CreditCardDTO("4111111111111111", "Test User", "12/25", "123");
        PaymentResult result = paymentService.processPayment(card, 0.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("must be positive"));
    }

    @Test
    @DisplayName("Should handle invalid card number gracefully")
    void testInvalidCardNumber() {
        CreditCardDTO invalidCard = new CreditCardDTO("invalid_card_number", "Test User", "12/25", "123");
        PaymentResult result = paymentService.processPayment(invalidCard, 100.0);

        assertNotNull(result);
        // Result may succeed or fail depending on API validation, but shouldn't crash
    }

    @Test
    @DisplayName("Should handle expired card gracefully")
    void testExpiredCard() {
        CreditCardDTO expiredCard = new CreditCardDTO("4111111111111111", "Test User", "01/20", "123");
        PaymentResult result = paymentService.processPayment(expiredCard, 100.0);

        assertNotNull(result);
        // API should handle expired cards appropriately
    }

    @Test
    @DisplayName("Should handle invalid CVV gracefully")
    void testInvalidCVV() {
        CreditCardDTO invalidCVVCard = new CreditCardDTO("4111111111111111", "Test User", "12/25", "invalid");
        PaymentResult result = paymentService.processPayment(invalidCVVCard, 100.0);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle very large payment amount gracefully")
    void testVeryLargePaymentAmount() {
        CreditCardDTO card = new CreditCardDTO("4111111111111111", "Test User", "12/25", "123");
        PaymentResult result = paymentService.processPayment(card, 999999999.99);

        assertNotNull(result);
        // API should handle large amounts appropriately
    }

    @Test
    @DisplayName("Should handle cancellation of invalid transaction ID")
    void testCancelInvalidTransactionId() {
        boolean result = paymentService.cancelPayment(-1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle cancellation of zero transaction ID")
    void testCancelZeroTransactionId() {
        boolean result = paymentService.cancelPayment(0);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle cancellation of too low transaction ID")
    void testCancelTooLowTransactionId() {
        boolean result = paymentService.cancelPayment(1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle cancellation of too high transaction ID")
    void testCancelTooHighTransactionId() {
        boolean result = paymentService.cancelPayment(100001);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should maintain card data integrity during processing")
    void testCardDataIntegrity() {
        CreditCardDTO originalCard = new CreditCardDTO("4111111111111111", "John Doe", "12/25", "123");

        paymentService.processPayment(originalCard, 100.0);

        // Verify original card data is unchanged
        assertEquals("4111111111111111", originalCard.getCardNumber());
        assertEquals("John Doe", originalCard.getCardHolderName());
        assertEquals("12/25", originalCard.getExpiryDate());
        assertEquals("123", originalCard.getCvv());
    }
}