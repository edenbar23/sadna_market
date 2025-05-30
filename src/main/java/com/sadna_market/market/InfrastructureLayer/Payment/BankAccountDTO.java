package com.sadna_market.market.InfrastructureLayer.Payment;

/**
 * DTO for bank account payment information
 * Updated to return PaymentResult
 */
public class BankAccountDTO implements PaymentMethod {
    public String accountNumber;
    public String bankName;

    public BankAccountDTO(String accountNumber, String bankName) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
    }

    @Override
    public PaymentResult accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }

    @Override
    public String toString() {
        return String.format("BankAccount[bank=%s, account=****%s]",
                bankName,
                accountNumber.length() > 4 ? accountNumber.substring(accountNumber.length() - 4) : "****");
    }
}