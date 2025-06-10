package com.sadna_market.market.InfrastructureLayer.Supply;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class MockExternalSupplyAPI extends ExternalSupplyAPI {

    public MockExternalSupplyAPI() {
        super(null, null); // We override all behavior, so we don't need real dependencies
    }

    @Override
    public int sendStandardShippingRequest(String carrier, ShipmentDetails details, double weight, int estimatedDays)
            throws ExternalAPIException {
        return 12345; // Always return a successful mock transaction ID
    }

    @Override
    public int sendExpressShippingRequest(String carrier, ShipmentDetails details, double weight, int priorityLevel)
            throws ExternalAPIException {
        return 67890;
    }

    @Override
    public int registerPickupRequest(String location, String pickupCode, ShipmentDetails details, double weight)
            throws ExternalAPIException {
        return 11111;
    }

    @Override
    public int cancelSupply(int transactionId) throws ExternalAPIException {
        return 1; // success
    }

    @Override
    public boolean testConnection() {
        return true;
    }
}
