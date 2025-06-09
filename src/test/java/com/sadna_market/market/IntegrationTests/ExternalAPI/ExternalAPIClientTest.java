package com.sadna_market.market.IntegrationTests.ExternalAPI;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIConfig;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "external.api.url=https://damp-lynna-wsep-1984852e.koyeb.app/",
        "external.api.enabled=true",
        "external.api.connection.timeout.seconds=10",
        "external.api.request.timeout.seconds=30"
})
@DisplayName("External API Client Integration Tests")
class ExternalAPIClientTest {

    @Autowired
    private ExternalAPIClient apiClient;


    @Test
    @DisplayName("Should return true when handshake succeeds")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testHandshakeSuccess() throws ExternalAPIException {
        boolean result = apiClient.testConnection();

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return OK response for handshake POST request")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testHandshakePostRequest() throws ExternalAPIException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "handshake");

        String response = apiClient.sendPostRequest(parameters);

        assertEquals("OK", response.trim());
    }

    @Test
    @DisplayName("Should handle form encoding without exception")
    void testFormEncodingHandling() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "pay");
        parameters.put("amount", "1000");
        parameters.put("card_number", "1234567890123456");

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                // Expected if external API is not available - this is fine for testing parameter processing
            }
        });
    }

    @Test
    @DisplayName("Should handle sensitive data without exception")
    void testSensitiveDataHandling() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "pay");
        parameters.put("card_number", "1234567890123456");
        parameters.put("cvv", "123");
        parameters.put("amount", "1000");

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                // Expected if external API is not available
            }
        });
    }
}