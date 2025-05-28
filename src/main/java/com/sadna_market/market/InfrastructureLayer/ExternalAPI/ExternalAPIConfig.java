package com.sadna_market.market.InfrastructureLayer.ExternalAPI;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for external payment and supply API integration
 */
@Configuration
@Getter
public class ExternalAPIConfig {

    @Value("${external.api.url:https://damp-lynna-wsep-1984852e.koyeb.app/}")
    private String apiUrl;

    @Value("${external.api.connection.timeout.seconds:10}")
    private int connectionTimeoutSeconds;

    @Value("${external.api.request.timeout.seconds:30}")
    private int requestTimeoutSeconds;

    @Value("${external.api.retry.attempts:3}")
    private int retryAttempts;

    @Value("${external.api.retry.delay.seconds:2}")
    private int retryDelaySeconds;

    @Value("${external.api.enabled:true}")
    private boolean enabled;

    /**
     * Check if external API integration is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the base URL for external API calls
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Get connection timeout in seconds
     */
    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    /**
     * Get request timeout in seconds
     */
    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    /**
     * Get number of retry attempts for failed requests
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Get delay between retry attempts in seconds
     */
    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    @Override
    public String toString() {
        return String.format("ExternalAPIConfig{url='%s', enabled=%s, connectionTimeout=%ds, requestTimeout=%ds}",
                apiUrl, enabled, connectionTimeoutSeconds, requestTimeoutSeconds);
    }
}