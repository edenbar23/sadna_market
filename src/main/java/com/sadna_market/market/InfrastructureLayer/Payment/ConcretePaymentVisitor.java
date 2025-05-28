package com.sadna_market.market.InfrastructureLayer.Payment;

import com.sadna_market.market.InfrastructureLayer.ExternalAPI.ExternalAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of PaymentVisitor that processes different payment methods
 * through the external payment API. Focuses on payment processing, delegates validation
 * to PaymentValidator.
 */
@Component
public class ConcretePaymentVisitor implements PaymentVisitor {
    private static final Logger logger = LoggerFactory.getLogger(ConcretePaymentVisitor.class);

    private final ExternalPaymentAPI api;
    private final PaymentValidator validator;

    @Autowired
    public ConcretePaymentVisitor(ExternalPaymentAPI api, PaymentValidator validator) {
        this.api = api;
        this.validator = validator;
        logger.info("ConcretePaymentVisitor initialized with external API integration");
    }

    @Override
    public PaymentResult visit(CreditCardDTO card, double amount) {
        logger.info("Processing credit card payment for amount: {}", amount);

        // Validate payment details
        ValidationResult validation = validator.validateCreditCard(card, amount);
        if (!validation.isValid()) {
            logger.error("Credit card validation failed: {}", validation.getErrorMessage());
            return PaymentResult.failure(validation.getErrorMessage(), card, amount);
        }

        // Process payment through external API
        return processExternalPayment(() ->
                        api.sendCreditCardPayment(card.cardNumber, card.cardHolderName, card.expiryDate, card.cvv, amount),
                card, amount, "Credit Card"
        );
    }

    @Override
    public PaymentResult visit(BankAccountDTO account, double amount) {
        logger.info("Processing bank account payment for amount: {}", amount);

        // Validate payment details
        ValidationResult validation = validator.validateBankAccount(account, amount);
        if (!validation.isValid()) {
            logger.error("Bank account validation failed: {}", validation.getErrorMessage());
            return PaymentResult.failure(validation.getErrorMessage(), account, amount);
        }

        // Process payment through external API
        return processExternalPayment(() ->
                        api.sendBankPayment(account.accountNumber, account.bankName, amount),
                account, amount, "Bank Account"
        );
    }

    @Override
    public PaymentResult visit(PayPalDTO paypal, double amount) {
        logger.info("Processing PayPal payment for amount: {} with email: {}", amount, paypal.email);

        // Validate payment details
        ValidationResult validation = validator.validatePayPal(paypal, amount);
        if (!validation.isValid()) {
            logger.error("PayPal validation failed: {}", validation.getErrorMessage());
            return PaymentResult.failure(validation.getErrorMessage(), paypal, amount);
        }

        // Note: External API doesn't specify PayPal integration
        // Using mock implementation for now
        logger.info("PayPal payment processed successfully (simulated)");
        int mockTransactionId = generateMockTransactionId();
        return PaymentResult.success(mockTransactionId, paypal, amount);
    }

    /**
     * Common method to process external payment API calls
     * Handles all the exception catching and result conversion logic
     */
    private PaymentResult processExternalPayment(PaymentAPICall apiCall,
                                                 PaymentMethod method,
                                                 double amount,
                                                 String paymentType) {
        try {
            int transactionId = apiCall.call();

            if (transactionId == -1) {
                String error = paymentType + " payment was declined by external system";
                logger.error(error);
                return PaymentResult.failure(error, method, amount);
            }

            logger.info("{} payment successful - Transaction ID: {}", paymentType, transactionId);
            return PaymentResult.success(transactionId, method, amount);

        } catch (ExternalAPIException e) {
            logger.error("External API error during {} payment", paymentType, e);
            return PaymentResult.failure("Payment processing failed: " + e.getMessage(), method, amount);
        } catch (Exception e) {
            logger.error("Unexpected error during {} payment", paymentType, e);
            return PaymentResult.failure("Unexpected payment error: " + e.getMessage(), method, amount);
        }
    }

    /**
     * Functional interface for payment API calls
     */
    @FunctionalInterface
    private interface PaymentAPICall {
        int call() throws ExternalAPIException;
    }

    /**
     * Generates a mock transaction ID for PayPal (since external API doesn't support it)
     */
    private int generateMockTransactionId() {
        return 10000 + (int)(Math.random() * 90000);
    }
}