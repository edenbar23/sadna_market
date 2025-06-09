package com.sadna_market.market.IntegrationTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIConfig;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Payment System Integration Tests")
class PaymentIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("Should process valid credit card payment successfully")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testValidCreditCardPayment() {
        CreditCardDTO validCard = new CreditCardDTO("4111111111111111", "John Doe", "12/25", "123");

        PaymentResult result = paymentService.processPayment(validCard, 100.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return valid transaction ID for successful payment")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testValidCreditCardPaymentTransactionId() {
        CreditCardDTO validCard = new CreditCardDTO("4111111111111111", "John Doe", "12/25", "123");

        PaymentResult result = paymentService.processPayment(validCard, 100.0);

        if (result.isSuccess()) {
            assertTrue(result.getTransactionId() >= 10000 && result.getTransactionId() <= 100000);
        }
    }

    @Test
    @DisplayName("Should handle payment cancellation without exception")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testPaymentCancellation() {
        CreditCardDTO validCard = new CreditCardDTO("4111111111111111", "John Doe", "12/25", "123");
        PaymentResult paymentResult = paymentService.processPayment(validCard, 100.0);

        if (paymentResult.isSuccess()) {
            assertDoesNotThrow(() -> {
                boolean cancelResult = paymentService.cancelPayment(paymentResult.getTransactionId());
                if (cancelResult) {
                    assertTrue(cancelResult, "Payment cancellation should succeed");
                } else {
                    fail("Payment cancellation failed");
                }
            });
        }
    }

    @Test
    @DisplayName("Should handle API unavailability gracefully")
    void testPaymentServiceResilience() {
        CreditCardDTO card = new CreditCardDTO("4111111111111111", "Test User", "01/30", "999");

        assertDoesNotThrow(() -> {
            PaymentResult result = paymentService.processPayment(card, 10.0);
            assertNotNull(result);
        });
    }
}