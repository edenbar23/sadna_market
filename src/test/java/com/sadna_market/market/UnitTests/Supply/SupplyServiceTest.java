package com.sadna_market.market.UnitTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Supply Service Tests")
class SupplyServiceTest {

    @Mock
    private ConcreteSupplyVisitor mockVisitor;

    @Mock
    private ExternalSupplyAPI mockExternalAPI;

    private SupplyService supplyService;
    private ShipmentDetails testShipment;
    private StandardShippingDTO standardShipping;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        supplyService = new SupplyService(mockVisitor, mockExternalAPI);
        testShipment = new ShipmentDetails("SHP-001", "123 Main St", 2, "user", false);
        standardShipping = new StandardShippingDTO("FedEx", 3);
    }

    @Test
    @DisplayName("Should return success when shipment processing succeeds")
    void testProcessSuccessfulShipment() {
        SupplyResult expectedResult = SupplyResult.success(12345, standardShipping, "tracking info", testShipment);
        when(mockVisitor.visit(any(StandardShippingDTO.class), any(), anyDouble()))
                .thenReturn(expectedResult);

        SupplyResult result = supplyService.processShipment(standardShipping, testShipment, 2.5);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when supply method is null")
    void testProcessShipmentWithNullMethod() {
        SupplyResult result = supplyService.processShipment(null, testShipment, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct error message when supply method is null")
    void testProcessShipmentWithNullMethodErrorMessage() {
        SupplyResult result = supplyService.processShipment(null, testShipment, 2.5);

        assertEquals("Supply method cannot be null", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should not call visitor when supply method is null")
    void testNoVisitorCallWhenSupplyMethodIsNull() {
        supplyService.processShipment(null, testShipment, 2.5);

        verifyNoInteractions(mockVisitor);
    }

    @Test
    @DisplayName("Should return failure when shipment details are null")
    void testProcessShipmentWithNullDetails() {
        SupplyResult result = supplyService.processShipment(standardShipping, null, 2.5);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when weight is negative")
    void testProcessShipmentWithNegativeWeight() {
        SupplyResult result = supplyService.processShipment(standardShipping, testShipment, -1.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when weight is zero")
    void testProcessShipmentWithZeroWeight() {
        SupplyResult result = supplyService.processShipment(standardShipping, testShipment, 0.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return true when shipment cancellation succeeds")
    void testCancelShipmentSuccess() throws ExternalAPIException {
        when(mockExternalAPI.cancelSupply(12345)).thenReturn(1);

        boolean result = supplyService.cancelShipment(12345);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should call external API when cancelling shipment")
    void testCancelShipmentCallsExternalAPI() throws ExternalAPIException {
        when(mockExternalAPI.cancelSupply(12345)).thenReturn(1);

        supplyService.cancelShipment(12345);

        verify(mockExternalAPI).cancelSupply(12345);
    }

    @Test
    @DisplayName("Should return false when shipment cancellation fails")
    void testCancelShipmentFailure() throws ExternalAPIException {
        when(mockExternalAPI.cancelSupply(12345)).thenReturn(-1);

        boolean result = supplyService.cancelShipment(12345);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when cancelling shipment with invalid ID")
    void testCancelShipmentWithInvalidId() {
        boolean result = supplyService.cancelShipment(-1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should not call external API when transaction ID is invalid")
    void testNoCancelCallWhenTransactionIdInvalid() {
        supplyService.cancelShipment(-1);

        verifyNoInteractions(mockExternalAPI);
    }

    @Test
    @DisplayName("Should return false when external API throws exception during cancellation")
    void testCancelShipmentAPIException() throws ExternalAPIException {
        when(mockExternalAPI.cancelSupply(12345)).thenThrow(new ExternalAPIException("Network error"));

        boolean result = supplyService.cancelShipment(12345);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when supply API is available")
    void testSupplyAPIConnectivity() {
        when(mockExternalAPI.testConnection()).thenReturn(true);

        boolean result = supplyService.testSupplyAPI();

        assertTrue(result);
    }

    @Test
    @DisplayName("Should call external API when testing connectivity")
    void testSupplyAPIConnectivityCallsExternalAPI() {
        when(mockExternalAPI.testConnection()).thenReturn(true);

        supplyService.testSupplyAPI();

        verify(mockExternalAPI).testConnection();
    }

    @Test
    @DisplayName("Should create valid shipment details")
    void testCreateValidShipmentDetails() {
        ShipmentDetails result = supplyService.createShipmentDetails(
                "SHP-001", "123 Main St", 2, "user", false);

        assertEquals("SHP-001", result.getShipmentId());
    }

    @Test
    @DisplayName("Should throw exception when shipment ID is empty")
    void testCreateShipmentDetailsWithEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("", "123 Main St", 2, "user", false);
        });
    }

    @Test
    @DisplayName("Should throw exception when address is empty")
    void testCreateShipmentDetailsWithEmptyAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "", 2, "user", false);
        });
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero")
    void testCreateShipmentDetailsWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Main St", 0, "user", false);
        });
    }

    @Test
    @DisplayName("Should throw exception when registered user has empty username")
    void testCreateShipmentDetailsWithEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            supplyService.createShipmentDetails("SHP-001", "123 Main St", 2, "", false);
        });
    }

    @Test
    @DisplayName("Should create valid guest shipment details")
    void testCreateGuestShipmentDetails() {
        ShipmentDetails result = supplyService.createShipmentDetails(
                "SHP-002", "456 Oak Ave", 1, "guest123", true);

        assertTrue(result.isGuest());
    }
}