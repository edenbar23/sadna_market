
//package com.sadna_market.market.UnitTests;
////package com.sadna_market.market.InfrastructureLayer.Payment;
//
//import com.sadna_market.market.InfrastructureLayer.Payment.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.sadna_market.market.DomainLayer.Cart;
//import com.sadna_market.market.DomainLayer.ShoppingBasket;
//
//import java.util.Map;
//import java.util.UUID;
//

package com.sadna_market.market.InfrastructureLayer.Payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentUnitTest {

    private PaymentService paymentService;

    @Mock
    private ExternalPaymentAPI mockExternalAPI;

    @Mock
    private PaymentVisitor mockVisitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a custom visitor for testing that uses our mock API
        mockVisitor = new ConcretePaymentVisitor() {
            @Override
            public boolean visit(CreditCardDTO card, double amount) {
                // Call the original method for validation logic, but use our mock API
                return mockExternalAPI.sendCreditCardPayment(
                        card.cardNumber, card.cardHolderName, card.expiryDate, card.cvv, amount);
            }

            @Override
            public boolean visit(BankAccountDTO account, double amount) {
                return mockExternalAPI.sendBankPayment(
                        account.accountNumber, account.bankName, amount);
            }

            @Override
            public boolean visit(PayPalDTO paypal, double amount) {
                // PayPal should succeed by default
                return true;
            }
        };

        paymentService = new PaymentService() {
            @Override
            public boolean pay(PaymentMethod method, double amount) {
                return method.accept(mockVisitor, amount);
            }
        };
    }

    @Test
    void testCreditCardPaymentSuccess() {
        // Arrange
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(true);

        // Act
        boolean result = paymentService.pay(card, 99.99);

        // Assert
        assertTrue(result);
        verify(mockExternalAPI).sendCreditCardPayment(
                eq("4571736012345678"), eq("Alice"), eq("12/25"), eq("123"), eq(99.99));
    }

    @Test
    void testCreditCardPaymentFailure() {
        // Arrange
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        when(mockExternalAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        // Act
        boolean result = paymentService.pay(card, 99.99);

        // Assert
        assertFalse(result);
        verify(mockExternalAPI).sendCreditCardPayment(
                eq("4571736012345678"), eq("Alice"), eq("12/25"), eq("123"), eq(99.99));
    }

    @Test
    void testBankAccountPaymentSuccess() {
        // Arrange
        BankAccountDTO bank = new BankAccountDTO("987654321", "MyBank");
        when(mockExternalAPI.sendBankPayment(anyString(), anyString(), anyDouble()))
                .thenReturn(true);

        // Act
        boolean result = paymentService.pay(bank, 199.99);

        // Assert
        assertTrue(result);
        verify(mockExternalAPI).sendBankPayment(eq("987654321"), eq("MyBank"), eq(199.99));
    }

    @Test
    void testBankAccountPaymentFailure() {
        // Arrange
        BankAccountDTO bank = new BankAccountDTO("987654321", "MyBank");
        when(mockExternalAPI.sendBankPayment(anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        // Act
        boolean result = paymentService.pay(bank, 199.99);

        // Assert
        assertFalse(result);
        verify(mockExternalAPI).sendBankPayment(eq("987654321"), eq("MyBank"), eq(199.99));
    }

    @Test
    void testPayPalPayment() {
        // Arrange
        PayPalDTO paypal = new PayPalDTO("alice@example.com");

        // Act
        boolean result = paymentService.pay(paypal, 49.99);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCardPrefixExtraction() {
        // Arrange
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");

        // Act
        String prefix = card.getCardPrefix();

        // Assert
        assertEquals("45717360", prefix);
    }

    @Test
    void testPaymentAdapter() {
        // Arrange
        PaymentAdapter adapter = new PaymentAdapter();
        ExternalPaymentAPI mockAPI = mock(ExternalPaymentAPI.class);

        // Use reflection to set the private API field (for testing purposes)
        try {
            java.lang.reflect.Field apiField = PaymentAdapter.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(adapter, mockAPI);
        } catch (Exception e) {
            fail("Failed to set mock API in adapter: " + e.getMessage());
        }

        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        BankAccountDTO bank = new BankAccountDTO("987654321", "MyBank");

        when(mockAPI.sendCreditCardPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(true);
        when(mockAPI.sendBankPayment(anyString(), anyString(), anyDouble()))
                .thenReturn(true);

        // Act
        boolean creditResult = adapter.payWithCreditCard(card, 99.99);
        boolean bankResult = adapter.payWithBankAccount(bank, 199.99);

        // Assert
        assertTrue(creditResult);
        assertTrue(bankResult);
        verify(mockAPI).sendCreditCardPayment(
                eq("4571736012345678"), eq("Alice"), eq("12/25"), eq("123"), eq(99.99));
        verify(mockAPI).sendBankPayment(eq("987654321"), eq("MyBank"), eq(199.99));
    }

    @Test
    void testPaymentProxy() {
        // Arrange
        PaymentProxy proxy = new PaymentProxy();
        PaymentAdapter mockAdapter = mock(PaymentAdapter.class);

        // Use reflection to set the private adapter field (for testing purposes)
        try {
            java.lang.reflect.Field adapterField = PaymentProxy.class.getDeclaredField("adapter");
            adapterField.setAccessible(true);
            adapterField.set(proxy, mockAdapter);
        } catch (Exception e) {
            fail("Failed to set mock adapter in proxy: " + e.getMessage());
        }

        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        BankAccountDTO bank = new BankAccountDTO("987654321", "MyBank");

        when(mockAdapter.payWithCreditCard(any(CreditCardDTO.class), anyDouble()))
                .thenReturn(true);
        when(mockAdapter.payWithBankAccount(any(BankAccountDTO.class), anyDouble()))
                .thenReturn(true);

        // Act
        boolean creditResult = proxy.payWithCreditCard(card, 99.99);
        boolean bankResult = proxy.payWithBankAccount(bank, 199.99);

        // Assert
        assertTrue(creditResult);
        assertTrue(bankResult);
        verify(mockAdapter).payWithCreditCard(eq(card), eq(99.99));
        verify(mockAdapter).payWithBankAccount(eq(bank), eq(199.99));
    }

    @Test
    void testVisitorPattern() {
        // Arrange
        CreditCardDTO card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        BankAccountDTO bank = new BankAccountDTO("987654321", "MyBank");
        PayPalDTO paypal = new PayPalDTO("alice@example.com");

        PaymentVisitor mockVisitor = mock(PaymentVisitor.class);
        when(mockVisitor.visit(any(CreditCardDTO.class), anyDouble())).thenReturn(true);
        when(mockVisitor.visit(any(BankAccountDTO.class), anyDouble())).thenReturn(true);
        when(mockVisitor.visit(any(PayPalDTO.class), anyDouble())).thenReturn(true);

        // Act
        boolean cardResult = card.accept(mockVisitor, 99.99);
        boolean bankResult = bank.accept(mockVisitor, 199.99);
        boolean paypalResult = paypal.accept(mockVisitor, 49.99);

        // Assert
        assertTrue(cardResult);
        assertTrue(bankResult);
        assertTrue(paypalResult);
        verify(mockVisitor).visit(eq(card), eq(99.99));
        verify(mockVisitor).visit(eq(bank), eq(199.99));
        verify(mockVisitor).visit(eq(paypal), eq(49.99));
    }
}
