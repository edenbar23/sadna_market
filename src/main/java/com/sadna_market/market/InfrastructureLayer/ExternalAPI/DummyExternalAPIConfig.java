package com.sadna_market.market.InfrastructureLayer.ExternalAPI;

public class DummyExternalAPIConfig extends ExternalAPIConfig {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getApiUrl() {
        return "http://mock.local";
    }
}
