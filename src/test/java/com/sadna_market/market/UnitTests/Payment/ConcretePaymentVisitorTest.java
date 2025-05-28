// ConcretePaymentVisitorTest.java
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

@DisplayName("Concrete Payment Visitor Tests")
class ConcretePaymentVisitorTest {

    @Mock
    private ExternalPaymentAPI mockExternalAPI;

    @Mock
    private PaymentValidator mockValidator;

    private ConcretePaymentVisitor visitor;
    private CreditCardDTO validCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        visitor = new ConcretePaymentVisitor(mockExternalAPI, mockValidator);
        validCard = new CreditCardDTO("4571736012345678", "John Doe", "12/25", "123");
    }

    @Test
    @DisplayName("Should return success when credit card payment succeeds")
    void testSuccessfulCreditCardPayment() throws ExternalAPIException {
        when(mockValidator.validateCreditCard(any(), anyDouble())).thenReturn(ValidationResult.valid());
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(12345);

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct transaction ID when credit card payment succeeds")
    void testSuccessfulCreditCardPaymentTransactionId() throws ExternalAPIException {
        when(mockValidator.validateCreditCard(any(), anyDouble())).thenReturn(ValidationResult.valid());
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(12345);

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertEquals(12345, result.getTransactionId());
    }

    @Test
    @DisplayName("Should return failure when credit card validation fails")
    void testFailedCreditCardValidation() {
        when(mockValidator.validateCreditCard(any(), anyDouble()))
                .thenReturn(ValidationResult.invalid("Invalid card"));

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return correct error message when validation fails")
    void testFailedCreditCardValidationErrorMessage() {
        when(mockValidator.validateCreditCard(any(), anyDouble()))
                .thenReturn(ValidationResult.invalid("Invalid card"));

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertEquals("Invalid card", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should not call external API when validation fails")
    void testNoExternalAPICallWhenValidationFails() {
        when(mockValidator.validateCreditCard(any(), anyDouble()))
                .thenReturn(ValidationResult.invalid("Invalid card"));

        visitor.visit(validCard, 100.0);

        verifyNoInteractions(mockExternalAPI);
    }

    @Test
    @DisplayName("Should return failure when external API returns -1")
    void testExternalAPIDeclined() throws ExternalAPIException {
        when(mockValidator.validateCreditCard(any(), anyDouble())).thenReturn(ValidationResult.valid());
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(-1);

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return failure when external API throws exception")
    void testExternalAPIException() throws ExternalAPIException {
        when(mockValidator.validateCreditCard(any(), anyDouble())).thenReturn(ValidationResult.valid());
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenThrow(new ExternalAPIException("Network error"));

        PaymentResult result = visitor.visit(validCard, 100.0);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should return success when bank account payment succeeds")
    void testSuccessfulBankAccountPayment() throws ExternalAPIException {
        BankAccountDTO validAccount = new BankAccountDTO("123456789012", "Test Bank");
        when(mockValidator.validateBankAccount(any(), anyDouble())).thenReturn(ValidationResult.valid());
        when(mockExternalAPI.sendBankPayment(anyString(), anyString(), anyDouble()))
                .thenReturn(54321);

        PaymentResult result = visitor.visit(validAccount, 200.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return success when PayPal payment is processed")
    void testPayPalPayment() {
        PayPalDTO validPayPal = new PayPalDTO("test@example.com");
        when(mockValidator.validatePayPal(any(), anyDouble())).thenReturn(ValidationResult.valid());

        PaymentResult result = visitor.visit(validPayPal, 50.0);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should return valid transaction ID range for PayPal payment")
    void testPayPalPaymentTransactionIdRange() {
        PayPalDTO validPayPal = new PayPalDTO("test@example.com");
        when(mockValidator.validatePayPal(any(), anyDouble())).thenReturn(ValidationResult.valid());

        PaymentResult result = visitor.visit(validPayPal, 50.0);

        assertTrue(result.getTransactionId() >= 10000 && result.getTransactionId() <= 99999);
    }
}