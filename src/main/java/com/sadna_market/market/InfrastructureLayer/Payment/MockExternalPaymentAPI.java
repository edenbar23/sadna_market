package com.sadna_market.market.InfrastructureLayer.Payment;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.*;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@Primary
public class MockExternalPaymentAPI extends ExternalPaymentAPI {

    private boolean simulateUnavailable = false;
    private boolean simulateFailure = false;

    @Autowired
    public MockExternalPaymentAPI(ExternalAPIClient apiClient, ExternalAPIConfig config) {
        super(apiClient, config);
    }

    public void setSimulateUnavailable(boolean value) {
        this.simulateUnavailable = value;
    }

    public void setSimulateFailure(boolean value) {
        this.simulateFailure = value;
    }

    @Override
    public int cancelPayment(int transactionId) throws ExternalAPIException {
        if (simulateUnavailable) throw new ExternalAPIException("Simulated network unavailability");
        return simulateFailure ? -1 : 1;
    }

    @Override
    public boolean testConnection() {
        return !simulateUnavailable;
    }

    @PostConstruct
    public void confirmMockInjected(){
        System.out.println("MockExternalPaymentAPI is Active");
    }
}
