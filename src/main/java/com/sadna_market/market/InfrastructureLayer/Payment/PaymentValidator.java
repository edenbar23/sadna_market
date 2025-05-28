package com.sadna_market.market.InfrastructureLayer.Payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Validator for payment methods and amounts
 * Centralizes all payment validation logic
 */
@Component
public class PaymentValidator {
    private static final Logger logger = LoggerFactory.getLogger(PaymentValidator.class);

    /**
     * Validates credit card payment details
     */
    public ValidationResult validateCreditCard(CreditCardDTO card, double amount) {
        logger.debug("Validating credit card payment for amount: {}", amount);

        // Validate amount first
        ValidationResult amountValidation = validateAmount(amount);
        if (!amountValidation.isValid()) {
            return amountValidation;
        }

        // Validate card details
        if (card == null) {
            return ValidationResult.invalid("Credit card details cannot be null");
        }

        if (!isValidCardNumber(card.cardNumber)) {
            return ValidationResult.invalid("Invalid credit card number format");
        }

        if (!isValidCvv(card.cvv)) {
            return ValidationResult.invalid("Invalid CVV format");
        }

        if (!isValidExpiryDate(card.expiryDate)) {
            return ValidationResult.invalid("Invalid or expired expiry date");
        }

        if (!isValidCardHolderName(card.cardHolderName)) {
            return ValidationResult.invalid("Invalid card holder name");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates bank account payment details
     */
    public ValidationResult validateBankAccount(BankAccountDTO account, double amount) {
        logger.debug("Validating bank account payment for amount: {}", amount);

        // Validate amount first
        ValidationResult amountValidation = validateAmount(amount);
        if (!amountValidation.isValid()) {
            return amountValidation;
        }

        // Validate account details
        if (account == null) {
            return ValidationResult.invalid("Bank account details cannot be null");
        }

        if (!isValidAccountNumber(account.accountNumber)) {
            return ValidationResult.invalid("Invalid bank account number");
        }

        if (!isValidBankName(account.bankName)) {
            return ValidationResult.invalid("Invalid bank name");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates PayPal payment details
     */
    public ValidationResult validatePayPal(PayPalDTO paypal, double amount) {
        logger.debug("Validating PayPal payment for amount: {}", amount);

        // Validate amount first
        ValidationResult amountValidation = validateAmount(amount);
        if (!amountValidation.isValid()) {
            return amountValidation;
        }

        // Validate PayPal details
        if (paypal == null) {
            return ValidationResult.invalid("PayPal details cannot be null");
        }

        if (!isValidEmail(paypal.email)) {
            return ValidationResult.invalid("Invalid PayPal email address");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment amount
     */
    private ValidationResult validateAmount(double amount) {
        if (amount <= 0) {
            return ValidationResult.invalid("Payment amount must be positive");
        }

        if (amount > 1000000) { // Max amount check
            return ValidationResult.invalid("Payment amount exceeds maximum limit");
        }

        // Check for reasonable decimal places (max 2)
        if (Math.round(amount * 100) != amount * 100) {
            return ValidationResult.invalid("Payment amount cannot have more than 2 decimal places");
        }

        return ValidationResult.valid();
    }

    // Credit Card Validation Methods

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        // Remove spaces and check if it's all digits
        String cleanCardNumber = cardNumber.replaceAll("\\s", "");

        // Check if it's between 13-19 digits (standard card length)
        if (!cleanCardNumber.matches("^[0-9]{13,19}$")) {
            return false;
        }

        // Optional: Add Luhn algorithm check for more thorough validation
        return isValidLuhn(cleanCardNumber);
    }

    private boolean isValidCvv(String cvv) {
        if (cvv == null) {
            return false;
        }
        // CVV should be 3-4 digits
        return cvv.matches("^[0-9]{3,4}$");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null) {
            return false;
        }

        // Check format MM/YY
        if (!expiryDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
            sdf.setLenient(false);

            // Parse the expiry date
            Date expiry = sdf.parse(expiryDate);

            // Add one day to the last day of the month to check if still valid
            Calendar cal = Calendar.getInstance();
            cal.setTime(expiry);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.add(Calendar.DAY_OF_MONTH, 1);
            expiry = cal.getTime();

            // Compare with current date
            return !expiry.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidCardHolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Name should contain only letters, spaces, and common punctuation
        return name.matches("^[a-zA-Z\\s.'\\-]+$") && name.trim().length() >= 2;
    }

    // Bank Account Validation Methods

    private boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }

        // Account number should be digits and reasonable length
        return accountNumber.matches("^[0-9]{8,20}$");
    }

    private boolean isValidBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return false;
        }

        // Bank name should be reasonable length and contain valid characters
        return bankName.trim().length() >= 2 && bankName.trim().length() <= 100;
    }

    // PayPal Validation Methods

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Luhn Algorithm for Credit Card Validation

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        // Process digits from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }
}