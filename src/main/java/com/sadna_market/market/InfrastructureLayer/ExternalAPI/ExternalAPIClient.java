package com.sadna_market.market.InfrastructureLayer.ExternalAPI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP client for communicating with external payment and supply APIs
 */
@Service
public class ExternalAPIClient {
    private static final Logger logger = LoggerFactory.getLogger(ExternalAPIClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExternalAPIConfig config;

    @Autowired
    public ExternalAPIClient(ExternalAPIConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectionTimeoutSeconds()))
                .build();

        logger.info("ExternalAPIClient initialized with URL: {}", config.getApiUrl());
    }

    /**
     * Sends a POST request to the external API
     *
     * @param parameters Map of parameters to send in the request body
     * @return The response body as a string
     * @throws ExternalAPIException if the request fails
     */
    public String sendPostRequest(Map<String, String> parameters) throws ExternalAPIException {
        try {
            logger.debug("Sending POST request to external API with parameters: {}",
                    maskSensitiveData(parameters));

            // Convert parameters to form-encoded string
            String formData = buildFormData(parameters);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getApiUrl()))
                    .timeout(Duration.ofSeconds(config.getRequestTimeoutSeconds()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            logger.debug("Received response with status code: {}", response.statusCode());

            if (response.statusCode() != 200) {
                throw new ExternalAPIException(
                        "External API returned error status: " + response.statusCode() +
                                ", body: " + response.body());
            }

            String responseBody = response.body();
            logger.debug("Response body: {}", responseBody);

            return responseBody;

        } catch (IOException e) {
            logger.error("Network error while calling external API", e);
            throw new ExternalAPIException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Request interrupted while calling external API", e);
            Thread.currentThread().interrupt();
            throw new ExternalAPIException("Request interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while calling external API", e);
            throw new ExternalAPIException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Tests connectivity to the external API using handshake
     *
     * @return true if handshake is successful
     * @throws ExternalAPIException if handshake fails
     */
    public boolean testConnection() throws ExternalAPIException {
        try {
            logger.info("Testing connection to external API");

            Map<String, String> handshakeParams = Map.of("action_type", "handshake");
            String response = sendPostRequest(handshakeParams);

            boolean isOk = "OK".equals(response.trim());

            if (isOk) {
                logger.info("Handshake successful - External API is available");
            } else {
                logger.warn("Handshake failed - Expected 'OK', got: {}", response);
            }

            return isOk;

        } catch (ExternalAPIException e) {
            logger.error("Handshake failed with external API", e);
            throw e;
        }
    }

    /**
     * Builds form-encoded data from parameters map
     */
    private String buildFormData(Map<String, String> parameters) {
        StringBuilder formData = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) {
                formData.append("&");
            }
            formData.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            first = false;
        }

        return formData.toString();
    }

    /**
     * Masks sensitive data in parameters for logging
     */
    private Map<String, String> maskSensitiveData(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> isSensitiveField(entry.getKey()) ? "****" : entry.getValue()
                ));
    }

    /**
     * Checks if a field contains sensitive data that should be masked
     */
    private boolean isSensitiveField(String fieldName) {
        return fieldName.toLowerCase().contains("card_number") ||
                fieldName.toLowerCase().contains("cvv") ||
                fieldName.toLowerCase().contains("password");
    }
}