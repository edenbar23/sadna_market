package com.sadna_market.market.IntegrationTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIConfig;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "external.api.url=https://damp-lynna-wsep-1984852e.koyeb.app/",
        "external.api.enabled=true"
})
@DisplayName("Payment System Integration Tests")
class PaymentIntegrationTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ExternalAPIConfig config = new ExternalAPIConfig();
        ExternalAPIClient apiClient = new ExternalAPIClient(config, new ObjectMapper());
        ExternalPaymentAPI externalPaymentAPI = new ExternalPaymentAPI(apiClient, config);
        PaymentValidator paymentValidator = new PaymentValidator();
        ConcretePaymentVisitor paymentVisitor = new ConcretePaymentVisitor(externalPaymentAPI, paymentValidator);
        paymentService = new PaymentService(paymentVisitor, externalPaymentAPI);
    }

    @Test
    @DisplayName("Should process valid credit card payment successfully")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testValidCreditCardPayment() {
        CreditCardDTO validCard = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");

        PaymentResult result = paymentService.processPayment(validCard, 100.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return valid transaction ID for successful payment")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testValidCreditCardPaymentTransactionId() {
        CreditCardDTO validCard = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");

        PaymentResult result = paymentService.processPayment(validCard, 100.0);

        if (result.isSuccess()) {
            assertTrue(result.getTransactionId() >= 10000 && result.getTransactionId() <= 100000);
        }
    }

    @Test
    @DisplayName("Should handle bank account payment without external API exception")
    void testBankAccountPayment() {
        BankAccountDTO bankAccount = new BankAccountDTO("123456789012", "Test Bank");

        assertDoesNotThrow(() -> {
            PaymentResult result = paymentService.processPayment(bankAccount, 200.0);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle PayPal payment without exception")
    void testPayPalPayment() {
        PayPalDTO paypal = new PayPalDTO("test@example.com");

        assertDoesNotThrow(() -> {
            PaymentResult result = paymentService.processPayment(paypal, 75.0);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should handle payment cancellation without exception")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testPaymentCancellation() {
        CreditCardDTO validCard = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
        PaymentResult paymentResult = paymentService.processPayment(validCard, 100.0);

        if (paymentResult.isSuccess()) {
            assertDoesNotThrow(() -> {
                boolean cancelResult = paymentService.cancelPayment(paymentResult.getTransactionId());
                // We don't assert the result because external API behavior may vary
            });
        }
    }

    @Test
    @DisplayName("Should handle API unavailability gracefully")
    void testPaymentServiceResilience() {
        CreditCardDTO card = new CreditCardDTO("1111222233334444", "Test User", "01/30", "999");

        assertDoesNotThrow(() -> {
            PaymentResult result = paymentService.processPayment(card, 10.0);
            assertNotNull(result);
        });
    }
}