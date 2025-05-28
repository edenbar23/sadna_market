package com.sadna_market.market.UnitTests.Supply;

import com.sadna_market.market.InfrastructureLayer.Supply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Supply DTO Tests")
class SupplyDTOTest {

    @Mock
    private SupplyVisitor mockVisitor;

    private ShipmentDetails testShipment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testShipment = new ShipmentDetails("SHP-001", "123 Main St", 2, "user", false);
    }

    @Test
    @DisplayName("StandardShippingDTO should accept visitor")
    void testStandardShippingDTOAccept() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);
        SupplyResult expectedResult = SupplyResult.success(12345, standardShipping, "info", testShipment);
        when(mockVisitor.visit(any(StandardShippingDTO.class), any(), anyDouble()))
                .thenReturn(expectedResult);

        SupplyResult result = standardShipping.accept(mockVisitor, testShipment, 2.5);

        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("StandardShippingDTO should call visitor with correct parameters")
    void testStandardShippingDTOCallsVisitor() {
        StandardShippingDTO standardShipping = new StandardShippingDTO("FedEx", 3);

        standardShipping.accept(mockVisitor, testShipment, 2.5);

        verify(mockVisitor).visit(standardShipping, testShipment, 2.5);
    }

    @Test
    @DisplayName("ExpressShippingDTO should accept visitor")
    void testExpressShippingDTOAccept() {
        ExpressShippingDTO expressShipping = new ExpressShippingDTO("DHL", 1);
        SupplyResult expectedResult = SupplyResult.success(54321, expressShipping, "info", testShipment);
        when(mockVisitor.visit(any(ExpressShippingDTO.class), any(), anyDouble()))
                .thenReturn(expectedResult);

        SupplyResult result = expressShipping.accept(mockVisitor, testShipment, 2.5);

        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("ExpressShippingDTO should call visitor with correct parameters")
    void testExpressShippingDTOCallsVisitor() {
        ExpressShippingDTO expressShipping = new ExpressShippingDTO("DHL", 1);

        expressShipping.accept(mockVisitor, testShipment, 2.5);

        verify(mockVisitor).visit(expressShipping, testShipment, 2.5);
    }

    @Test
    @DisplayName("PickupDTO should accept visitor")
    void testPickupDTOAccept() {
        PickupDTO pickup = new PickupDTO("Store Location", "PICK123");
        SupplyResult expectedResult = SupplyResult.success(67890, pickup, "info", testShipment);
        when(mockVisitor.visit(any(PickupDTO.class), any(), anyDouble()))
                .thenReturn(expectedResult);

        SupplyResult result = pickup.accept(mockVisitor, testShipment, 2.5);

        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("PickupDTO should call visitor with correct parameters")
    void testPickupDTOCallsVisitor() {
        PickupDTO pickup = new PickupDTO("Store Location", "PICK123");

        pickup.accept(mockVisitor, testShipment, 2.5);

        verify(mockVisitor).visit(pickup, testShipment, 2.5);
    }
}