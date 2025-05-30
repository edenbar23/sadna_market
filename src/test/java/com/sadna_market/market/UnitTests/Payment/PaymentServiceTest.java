package com.sadna_market.market.UnitTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private ConcretePaymentVisitor mockVisitor;

    @Mock
    private ExternalPaymentAPI mockExternalAPI;

    private PaymentService paymentService;
    private CreditCardDTO testCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(mockVisitor, mockExternalAPI);
        testCard = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
    }

    @Test
    @DisplayName("Should return success when payment processing succeeds")
    void testProcessSuccessfulPayment() {
        PaymentResult expectedResult = PaymentResult.success(12345, testCard, 100.0);
        when(mockVisitor.visit(any(CreditCardDTO.class), anyDouble())).thenReturn(expectedResult);

        PaymentResult result = paymentService.processPayment(testCard, 100.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when payment method is null")
    void testProcessPaymentWithNullMethod() {
        PaymentResult result = paymentService.processPayment(null, 100.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct error message when payment method is null")
    void testProcessPaymentWithNullMethodErrorMessage() {
        PaymentResult result = paymentService.processPayment(null, 100.0);

        assertEquals("Payment method cannot be null", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should not call visitor when payment method is null")
    void testNoVisitorCallWhenPaymentMethodIsNull() {
        paymentService.processPayment(null, 100.0);

        verifyNoInteractions(mockVisitor);
    }

    @Test
    @DisplayName("Should return failure when payment amount is negative")
    void testProcessPaymentWithNegativeAmount() {
        PaymentResult result = paymentService.processPayment(testCard, -50.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when payment amount is zero")
    void testProcessPaymentWithZeroAmount() {
        PaymentResult result = paymentService.processPayment(testCard, 0.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return true when payment cancellation succeeds")
    void testCancelPaymentSuccess() throws ExternalAPIException {
        when(mockExternalAPI.cancelPayment(12345)).thenReturn(1);

        boolean result = paymentService.cancelPayment(12345);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should call external API when cancelling payment")
    void testCancelPaymentCallsExternalAPI() throws ExternalAPIException {
        when(mockExternalAPI.cancelPayment(12345)).thenReturn(1);

        paymentService.cancelPayment(12345);

        verify(mockExternalAPI).cancelPayment(12345);
    }

    @Test
    @DisplayName("Should return false when payment cancellation fails")
    void testCancelPaymentFailure() throws ExternalAPIException {
        when(mockExternalAPI.cancelPayment(12345)).thenReturn(-1);

        boolean result = paymentService.cancelPayment(12345);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when cancelling payment with invalid ID")
    void testCancelPaymentWithInvalidId() {
        boolean result = paymentService.cancelPayment(-1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should not call external API when transaction ID is invalid")
    void testNoCancelCallWhenTransactionIdInvalid() {
        paymentService.cancelPayment(-1);

        verifyNoInteractions(mockExternalAPI);
    }

    @Test
    @DisplayName("Should return false when external API throws exception during cancellation")
    void testCancelPaymentAPIException() throws ExternalAPIException {
        when(mockExternalAPI.cancelPayment(12345)).thenThrow(new ExternalAPIException("Network error"));

        boolean result = paymentService.cancelPayment(12345);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when payment API is available")
    void testPaymentAPIConnectivity() {
        when(mockExternalAPI.testConnection()).thenReturn(true);

        boolean result = paymentService.testPaymentAPI();

        assertTrue(result);
    }

    @Test
    @DisplayName("Should call external API when testing connectivity")
    void testPaymentAPIConnectivityCallsExternalAPI() {
        when(mockExternalAPI.testConnection()).thenReturn(true);

        paymentService.testPaymentAPI();

        verify(mockExternalAPI).testConnection();
    }
}