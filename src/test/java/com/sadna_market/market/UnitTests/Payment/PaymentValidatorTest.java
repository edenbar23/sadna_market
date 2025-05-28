// PaymentValidatorTest.java
package com.sadna_market.market.UnitTests.Payment;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Validator Tests")
class PaymentValidatorTest {

    private PaymentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PaymentValidator();
    }

    @Test
    @DisplayName("Should validate correct credit card successfully")
    void testValidCreditCard() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject null credit card")
    void testNullCreditCard() {
        ValidationResult result = validator.validateCreditCard(null, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should return correct error message for null credit card")
    void testNullCreditCardErrorMessage() {
        ValidationResult result = validator.validateCreditCard(null, 100.0);

        assertEquals("Credit card details cannot be null", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should reject empty card number")
    void testEmptyCardNumber() {
        CreditCardDTO card = new CreditCardDTO("", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject too short card number")
    void testTooShortCardNumber() {
        CreditCardDTO card = new CreditCardDTO("123", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject too long card number")
    void testTooLongCardNumber() {
        CreditCardDTO card = new CreditCardDTO("12345678901234567890", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject alphabetic card number")
    void testAlphabeticCardNumber() {
        CreditCardDTO card = new CreditCardDTO("abcd1234567890123", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject empty CVV")
    void testEmptyCvv() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject too short CVV")
    void testTooShortCvv() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "12");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should accept 3-digit CVV")
    void testValidThreeDigitCvv() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should accept 4-digit CVV")
    void testValidFourDigitCvv() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "1234");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject expired card")
    void testExpiredCard() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/20", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject invalid expiry format")
    void testInvalidExpiryFormat() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "1/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject empty card holder name")
    void testEmptyCardHolderName() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject too short card holder name")
    void testTooShortCardHolderName() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "J", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should validate correct bank account successfully")
    void testValidBankAccount() {
        BankAccountDTO account = new BankAccountDTO("123456789012", "Test Bank");

        ValidationResult result = validator.validateBankAccount(account, 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject null bank account")
    void testNullBankAccount() {
        ValidationResult result = validator.validateBankAccount(null, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject too short bank account number")
    void testTooShortBankAccountNumber() {
        BankAccountDTO account = new BankAccountDTO("123", "Test Bank");

        ValidationResult result = validator.validateBankAccount(account, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject empty bank name")
    void testEmptyBankName() {
        BankAccountDTO account = new BankAccountDTO("123456789012", "");

        ValidationResult result = validator.validateBankAccount(account, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should validate correct PayPal successfully")
    void testValidPayPal() {
        PayPalDTO paypal = new PayPalDTO("test@example.com");

        ValidationResult result = validator.validatePayPal(paypal, 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject null PayPal")
    void testNullPayPal() {
        ValidationResult result = validator.validatePayPal(null, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject invalid PayPal email")
    void testInvalidPayPalEmail() {
        PayPalDTO paypal = new PayPalDTO("invalid-email");

        ValidationResult result = validator.validatePayPal(paypal, 100.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject negative payment amount")
    void testNegativeAmount() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, -1.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject zero payment amount")
    void testZeroAmount() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 0.0);

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject excessive payment amount")
    void testExcessiveAmount() {
        CreditCardDTO card = new CreditCardDTO("4153765102904401", "John Doe", "12/25", "123");

        ValidationResult result = validator.validateCreditCard(card, 1000001.0);

        assertFalse(result.isValid());
    }
}