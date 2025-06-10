package com.sadna_market.market.InfrastructureLayer.ExternalAPI;

import java.util.Map;

public class DummyExternalAPIClient extends ExternalAPIClient {
    public DummyExternalAPIClient() {
        super(new ExternalAPIConfig(), null);
    }

    @Override
    public String sendPostRequest(Map<String, String> params) {
        return "10001";  // Always succeed
    }

    @Override
    public boolean testConnection() {
        return true;
    }
}

