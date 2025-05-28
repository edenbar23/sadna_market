package com.sadna_market.market.IntegrationTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
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
@DisplayName("Supply System Integration Tests")
class SupplyIntegrationTest {

    private SupplyService supplyService;
    private ShipmentDetails testShipment;

    @BeforeEach
    void setUp() {
        ExternalAPIConfig config = new ExternalAPIConfig();
        ExternalAPIClient apiClient = new ExternalAPIClient(config, new ObjectMapper());
        ExternalSupplyAPI externalSupplyAPI = new ExternalSupplyAPI(apiClient, config);
        ConcreteSupplyVisitor supplyVisitor = new ConcreteSupplyVisitor(externalSupplyAPI);
        supplyService = new SupplyService(supplyVisitor, externalSupplyAPI);

        testShipment = new ShipmentDetails("SHP-001", "123 Main St, City, Country", 2, "user", false);
    }

    @Test
    @DisplayName("Should process standard shipping successfully")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testStandardShipping() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);

        SupplyResult result = supplyService.processShipment(standardShipping, testShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return valid transaction ID for successful shipping")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testStandardShippingTransactionId() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);

        SupplyResult result = supplyService.processShipment(standardShipping, testShipment, 2.5);

        if (result.isSuccess()) {
            assertTrue(result.getTransactionId() >= 10000 && result.getTransactionId() <= 100000);
        }
    }

    @Test
    @DisplayName("Should process express shipping successfully")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testExpressShipping() {
        ExpressShippingDTO expressShipping = new ExpressShippingDTO("DHL", 1);

        SupplyResult result = supplyService.processShipment(expressShipping, testShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should process pickup successfully")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testPickup() {
        PickupDTO pickup = new PickupDTO("Store Location", "PICK123");

        SupplyResult result = supplyService.processShipment(pickup, testShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle shipment cancellation without exception")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testShipmentCancellation() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);
        SupplyResult shipmentResult = supplyService.processShipment(standardShipping, testShipment, 2.5);

        if (shipmentResult.isSuccess()) {
            assertDoesNotThrow(() -> {
                boolean cancelResult = supplyService.cancelShipment(shipmentResult.getTransactionId());
                // We don't assert the result because external API behavior may vary
            });
        }
    }

    @Test
    @DisplayName("Should handle guest user shipment")
    @EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_API_TESTS", matches = "true")
    void testGuestUserShipment() {
        ShipmentDetails guestShipment = new ShipmentDetails("SHP-002", "456 Oak Ave, Town, Country", 1, "guest123", true);
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);

        SupplyResult result = supplyService.processShipment(standardShipping, guestShipment, 1.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle API unavailability gracefully")
    void testSupplyServiceResilience() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("TestCarrier", 5);

        assertDoesNotThrow(() -> {
            SupplyResult result = supplyService.processShipment(standardShipping, testShipment, 1.0);
            assertNotNull(result);
        });
    }
}