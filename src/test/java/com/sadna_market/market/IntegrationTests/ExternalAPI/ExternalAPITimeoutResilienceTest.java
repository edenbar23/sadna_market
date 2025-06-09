package com.sadna_market.market.IntegrationTests.ExternalAPI;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIClient;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "external.api.connection.timeout.seconds=1",
        "external.api.request.timeout.seconds=1"
})
@DisplayName("External API Timeout Resilience Tests")
class ExternalAPITimeoutResilienceTest {

    @Autowired
    private ExternalAPIClient apiClient;

    @Test
    @DisplayName("Should handle very short timeout gracefully")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testVeryShortTimeout() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action_type", "handshake");

        assertDoesNotThrow(() -> {
            try {
                apiClient.sendPostRequest(parameters);
            } catch (ExternalAPIException e) {
                // Timeout is acceptable behavior
                assertTrue(e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Network error"));
            }
        });
    }
}