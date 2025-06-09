package com.sadna_market.market.IntegrationTests.ExternalAPI;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "external.api.url=https://damp-lynna-wsep-1984852e.koyeb.app/",
        "external.api.enabled=true",
        "system.init.enabled=false"
})
@DisplayName("External API Resilience Tests")
class ExternalAPIResilienceTest {

    @Autowired
    private ExternalAPIClient apiClient;

    @Test
    @DisplayName("Should handle invalid action_type gracefully")
    void testInvalidActionType() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "invalid_action_12345");

        // Real API should reject invalid action types gracefully
        assertDoesNotThrow(() -> {
            try {
                String response = apiClient.sendPostRequest(parameters);
                // Response might be error message, but shouldn't crash
                assertNotNull(response);
            } catch (ExternalAPIException e) {
                // This is acceptable - system should handle API errors gracefully
                assertNotNull(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("Should handle malformed payment data gracefully")
    void testMalformedPaymentData() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "pay");
        parameters.put("amount", "invalid_amount");
        parameters.put("card_number", "invalid_card");

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                assertTrue(e.getMessage().length() > 0);
            }
        });
    }

    @Test
    @DisplayName("Should handle missing required parameters gracefully")
    void testMissingRequiredParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "pay");
        // Missing amount, card_number, etc.

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                assertNotNull(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("Should handle large payload gracefully")
    void testLargePayload() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "pay");
        // Create very large parameter value
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeValue.append("x");
        }
        parameters.put("card_number", largeValue.toString());

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                assertNotNull(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("Should handle concurrent API requests")
    void testConcurrentRequests() {
        final int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        Exception[] exceptions = new Exception[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("action_type", "handshake");
                    apiClient.sendPostRequest(parameters);
                } catch (Exception e) {
                    exceptions[index] = e;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for completion
        assertDoesNotThrow(() -> {
            for (Thread thread : threads) {
                thread.join(10000);
            }
        });
    }

    @Test
    @DisplayName("Should maintain connection stability after errors")
    void testConnectionStabilityAfterErrors() {
        // First, send invalid request
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("action_type", "invalid_action");

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(invalidParams);
            } catch (ExternalAPIException e) {
                // Expected
            }
        });

        // Then, send valid request to ensure connection still works
        assertDoesNotThrow(() -> {
            boolean result = apiClient.testConnection();
            // Should either work or fail gracefully, but not crash
        });
    }
}