package com.sadna_market.market.UnitTests;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PaymentUnitTest {

    private PaymentService paymentService;
    private ExternalPaymentAPI mockExternalAPI;
    private PaymentProxy paymentProxy;
    private CreditCardDTO creditCardDTO;
    private BankAccountDTO bankAccountDTO;
    private PayPalDTO payPalDTO;
    private double paymentAmount;

    @BeforeEach
    @DisplayName("Set up test environment")
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");

        // Create mock for the external payment API
        mockExternalAPI = Mockito.mock(ExternalPaymentAPI.class);

        // Create payment DTOs
        creditCardDTO = new CreditCardDTO("4571736012345678", "Test User", "12/25", "123");
        bankAccountDTO = new BankAccountDTO("987654321", "Test Bank");
        payPalDTO = new PayPalDTO("test@example.com");

        // Set up payment amount
        paymentAmount = 99.99;

        // Create payment service
        paymentService = new PaymentService();

        // Create payment proxy
        paymentProxy = new PaymentProxy();

        System.out.println("Created payment service and proxy");
        System.out.println("Created test payment methods:");
        System.out.println("Credit Card: " + creditCardDTO.cardNumber + ", " + creditCardDTO.cardHolderName);
        System.out.println("Bank Account: " + bankAccountDTO.accountNumber + ", " + bankAccountDTO.bankName);
        System.out.println("PayPal: " + payPalDTO.email);
        System.out.println("Payment amount: " + paymentAmount);
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    @DisplayName("Clean up test resources")
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        paymentService = null;
        paymentProxy = null;
        creditCardDTO = null;
        bankAccountDTO = null;
        payPalDTO = null;
        mockExternalAPI = null;
        System.out.println("Payment resources set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    @DisplayName("PaymentService processes credit card payment successfully")
    void testPay_CreditCard_SuccessfulPayment() {
        System.out.println("TEST: Verifying credit card payment processing");

        // Test payment with credit card
        System.out.println("Processing credit card payment with amount: " + paymentAmount);
        boolean result = paymentService.pay(creditCardDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Credit card payment should be successful");

        System.out.println("✓ Credit card payment processed successfully");
    }

    @Test
    @DisplayName("PaymentService processes bank account payment successfully")
    void testPay_BankAccount_SuccessfulPayment() {
        System.out.println("TEST: Verifying bank account payment processing");

        // Test payment with bank account
        System.out.println("Processing bank account payment with amount: " + paymentAmount);
        boolean result = paymentService.pay(bankAccountDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Bank account payment should be successful");

        System.out.println("✓ Bank account payment processed successfully");
    }

    @Test
    @DisplayName("PaymentService processes PayPal payment successfully")
    void testPay_PayPal_SuccessfulPayment() {
        System.out.println("TEST: Verifying PayPal payment processing");

        // Test payment with PayPal
        System.out.println("Processing PayPal payment with amount: " + paymentAmount);
        boolean result = paymentService.pay(payPalDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "PayPal payment should be successful");

        System.out.println("✓ PayPal payment processed successfully");
    }

    @Test
    @DisplayName("PaymentService handles payment refund properly")
    void testRefund_ValidPaymentId_RefundsSuccessfully() {
        System.out.println("TEST: Verifying payment refund functionality");

        // Create a payment ID for refund
        UUID paymentId = UUID.randomUUID();
        System.out.println("Processing refund for payment ID: " + paymentId);

        // Note: This test just verifies the refund method doesn't throw exceptions
        // since the implementation only logs the refund without returning a value
        paymentService.refund(paymentId);

        System.out.println("Refund processed without exceptions");

        System.out.println("✓ Payment refund processed successfully");
    }

    @Test
    @DisplayName("PaymentProxy logs and delegates credit card payment")
    void testPaymentProxy_CreditCard_LogsAndDelegates() {
        System.out.println("TEST: Verifying PaymentProxy logs and delegates credit card payment");

        // Construct a mock adapter to verify delegation
        PaymentAdapter mockAdapter = Mockito.mock(PaymentAdapter.class);
        when(mockAdapter.payWithCreditCard(any(CreditCardDTO.class), anyDouble())).thenReturn(true);

        // Use reflection to replace the adapter in the proxy
        try {
            java.lang.reflect.Field adapterField = PaymentProxy.class.getDeclaredField("adapter");
            adapterField.setAccessible(true);
            adapterField.set(paymentProxy, mockAdapter);
        } catch (Exception e) {
            System.out.println("Failed to set mock adapter: " + e.getMessage());
            fail("Could not set mock adapter");
        }

        System.out.println("Processing credit card payment through proxy with amount: " + paymentAmount);
        boolean result = paymentProxy.payWithCreditCard(creditCardDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Proxy should return successful result from adapter");

        System.out.println("Verifying adapter was called with correct parameters");
        verify(mockAdapter).payWithCreditCard(eq(creditCardDTO), eq(paymentAmount));

        System.out.println("✓ Payment proxy correctly logs and delegates credit card payment");
    }

    @Test
    @DisplayName("PaymentProxy logs and delegates bank account payment")
    void testPaymentProxy_BankAccount_LogsAndDelegates() {
        System.out.println("TEST: Verifying PaymentProxy logs and delegates bank account payment");

        // Construct a mock adapter to verify delegation
        PaymentAdapter mockAdapter = Mockito.mock(PaymentAdapter.class);
        when(mockAdapter.payWithBankAccount(any(BankAccountDTO.class), anyDouble())).thenReturn(true);

        // Use reflection to replace the adapter in the proxy
        try {
            java.lang.reflect.Field adapterField = PaymentProxy.class.getDeclaredField("adapter");
            adapterField.setAccessible(true);
            adapterField.set(paymentProxy, mockAdapter);
        } catch (Exception e) {
            System.out.println("Failed to set mock adapter: " + e.getMessage());
            fail("Could not set mock adapter");
        }

        System.out.println("Processing bank account payment through proxy with amount: " + paymentAmount);
        boolean result = paymentProxy.payWithBankAccount(bankAccountDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Proxy should return successful result from adapter");

        System.out.println("Verifying adapter was called with correct parameters");
        verify(mockAdapter).payWithBankAccount(eq(bankAccountDTO), eq(paymentAmount));

        System.out.println("✓ Payment proxy correctly logs and delegates bank account payment");
    }

    @Test
    @DisplayName("PaymentAdapter forwards credit card payment to external API")
    void testPaymentAdapter_CreditCard_ForwardsToExternalAPI() {
        System.out.println("TEST: Verifying PaymentAdapter forwards credit card payment to external API");

        // Create a mock external payment API
        ExternalPaymentAPI mockAPI = Mockito.mock(ExternalPaymentAPI.class);
        when(mockAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble())).thenReturn(true);

        // Create payment adapter with the mock API
        PaymentAdapter adapter = new PaymentAdapter();

        // Use reflection to replace the API in the adapter
        try {
            java.lang.reflect.Field apiField = PaymentAdapter.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(adapter, mockAPI);
        } catch (Exception e) {
            System.out.println("Failed to set mock API: " + e.getMessage());
            fail("Could not set mock API");
        }

        System.out.println("Processing credit card payment through adapter with amount: " + paymentAmount);
        boolean result = adapter.payWithCreditCard(creditCardDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Adapter should return successful result from external API");

        System.out.println("Verifying external API was called with correct parameters");
        verify(mockAPI).sendCreditCardPayment(
                eq(creditCardDTO.cardNumber),
                eq(creditCardDTO.cardHolderName),
                eq(creditCardDTO.expiryDate),
                eq(creditCardDTO.cvv),
                eq(paymentAmount)
        );

        System.out.println("✓ Payment adapter correctly forwards credit card payment to external API");
    }

    @Test
    @DisplayName("PaymentAdapter forwards bank account payment to external API")
    void testPaymentAdapter_BankAccount_ForwardsToExternalAPI() {
        System.out.println("TEST: Verifying PaymentAdapter forwards bank account payment to external API");

        // Create a mock external payment API
        ExternalPaymentAPI mockAPI = Mockito.mock(ExternalPaymentAPI.class);
        when(mockAPI.sendBankPayment(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Create payment adapter with the mock API
        PaymentAdapter adapter = new PaymentAdapter();

        // Use reflection to replace the API in the adapter
        try {
            java.lang.reflect.Field apiField = PaymentAdapter.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(adapter, mockAPI);
        } catch (Exception e) {
            System.out.println("Failed to set mock API: " + e.getMessage());
            fail("Could not set mock API");
        }

        System.out.println("Processing bank account payment through adapter with amount: " + paymentAmount);
        boolean result = adapter.payWithBankAccount(bankAccountDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Adapter should return successful result from external API");

        System.out.println("Verifying external API was called with correct parameters");
        verify(mockAPI).sendBankPayment(
                eq(bankAccountDTO.accountNumber),
                eq(bankAccountDTO.bankName),
                eq(paymentAmount)
        );

        System.out.println("✓ Payment adapter correctly forwards bank account payment to external API");
    }

    @Test
    @DisplayName("CreditCardDTO implements PaymentMethod interface correctly")
    void testCreditCardDTO_ImplementsPaymentMethod_Correctly() {
        System.out.println("TEST: Verifying CreditCardDTO implements PaymentMethod interface correctly");

        System.out.println("Checking if CreditCardDTO instance can be assigned to PaymentMethod variable");
        PaymentMethod paymentMethod = creditCardDTO;

        System.out.println("Creating mock visitor to test accept method");
        PaymentVisitor mockVisitor = Mockito.mock(PaymentVisitor.class);
        when(mockVisitor.visit(any(CreditCardDTO.class), anyDouble())).thenReturn(true);

        System.out.println("Calling accept method with visitor and amount: " + paymentAmount);
        boolean result = paymentMethod.accept(mockVisitor, paymentAmount);

        System.out.println("Expected: Accept method should call visitor's visit method and return true");
        System.out.println("Actual: Accept method result = " + result);
        assertTrue(result, "Accept method should return result from visitor's visit method");

        System.out.println("Verifying visitor's visit method was called with correct parameters");
        verify(mockVisitor).visit(eq(creditCardDTO), eq(paymentAmount));

        System.out.println("✓ CreditCardDTO correctly implements PaymentMethod interface");
    }

    @Test
    @DisplayName("BankAccountDTO implements PaymentMethod interface correctly")
    void testBankAccountDTO_ImplementsPaymentMethod_Correctly() {
        System.out.println("TEST: Verifying BankAccountDTO implements PaymentMethod interface correctly");

        System.out.println("Checking if BankAccountDTO instance can be assigned to PaymentMethod variable");
        PaymentMethod paymentMethod = bankAccountDTO;

        System.out.println("Creating mock visitor to test accept method");
        PaymentVisitor mockVisitor = Mockito.mock(PaymentVisitor.class);
        when(mockVisitor.visit(any(BankAccountDTO.class), anyDouble())).thenReturn(true);

        System.out.println("Calling accept method with visitor and amount: " + paymentAmount);
        boolean result = paymentMethod.accept(mockVisitor, paymentAmount);

        System.out.println("Expected: Accept method should call visitor's visit method and return true");
        System.out.println("Actual: Accept method result = " + result);
        assertTrue(result, "Accept method should return result from visitor's visit method");

        System.out.println("Verifying visitor's visit method was called with correct parameters");
        verify(mockVisitor).visit(eq(bankAccountDTO), eq(paymentAmount));

        System.out.println("✓ BankAccountDTO correctly implements PaymentMethod interface");
    }

    @Test
    @DisplayName("PayPalDTO implements PaymentMethod interface correctly")
    void testPayPalDTO_ImplementsPaymentMethod_Correctly() {
        System.out.println("TEST: Verifying PayPalDTO implements PaymentMethod interface correctly");

        System.out.println("Checking if PayPalDTO instance can be assigned to PaymentMethod variable");
        PaymentMethod paymentMethod = payPalDTO;

        System.out.println("Creating mock visitor to test accept method");
        PaymentVisitor mockVisitor = Mockito.mock(PaymentVisitor.class);
        when(mockVisitor.visit(any(PayPalDTO.class), anyDouble())).thenReturn(true);

        System.out.println("Calling accept method with visitor and amount: " + paymentAmount);
        boolean result = paymentMethod.accept(mockVisitor, paymentAmount);

        System.out.println("Expected: Accept method should call visitor's visit method and return true");
        System.out.println("Actual: Accept method result = " + result);
        assertTrue(result, "Accept method should return result from visitor's visit method");

        System.out.println("Verifying visitor's visit method was called with correct parameters");
        verify(mockVisitor).visit(eq(payPalDTO), eq(paymentAmount));

        System.out.println("✓ PayPalDTO correctly implements PaymentMethod interface");
    }

    @Test
    @DisplayName("ConcretePaymentVisitor processes credit card payment")
    void testConcretePaymentVisitor_CreditCard_ProcessesPayment() {
        System.out.println("TEST: Verifying ConcretePaymentVisitor processes credit card payment");

        // Create a mock external payment API
        ExternalPaymentAPI mockAPI = Mockito.mock(ExternalPaymentAPI.class);
        when(mockAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble())).thenReturn(true);

        // Create a concrete payment visitor with the mock API
        ConcretePaymentVisitor visitor = new ConcretePaymentVisitor();

        // Use reflection to replace the API in the visitor
        try {
            java.lang.reflect.Field apiField = ConcretePaymentVisitor.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(visitor, mockAPI);
        } catch (Exception e) {
            System.out.println("Failed to set mock API: " + e.getMessage());
            fail("Could not set mock API");
        }

        System.out.println("Processing credit card payment through visitor with amount: " + paymentAmount);
        boolean result = visitor.visit(creditCardDTO, paymentAmount);

        System.out.println("Expected: Payment should be successful");
        System.out.println("Actual: Payment result = " + result);
        assertTrue(result, "Visitor should return successful result from external API");

        System.out.println("Verifying external API was called with correct parameters");
        verify(mockAPI).sendCreditCardPayment(
                eq(creditCardDTO.cardNumber),
                eq(creditCardDTO.cardHolderName),
                eq(creditCardDTO.expiryDate),
                eq(creditCardDTO.cvv),
                eq(paymentAmount)
        );

        System.out.println("✓ Concrete payment visitor correctly processes credit card payment");
    }

    // Helper methods for mocking
    private <T> T any(Class<T> clazz) {
        return Mockito.any(clazz);
    }

    private <T> T eq(T value) {
        return Mockito.eq(value);
    }
}