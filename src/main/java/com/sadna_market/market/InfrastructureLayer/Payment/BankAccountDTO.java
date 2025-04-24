package com.sadna_market.market.InfrastructureLayer.Payment;

public class BankAccountDTO implements PaymentMethod {
    public String accountNumber;
    public String bankName;

    public BankAccountDTO(String accountNumber, String bankName) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
    }

    @Override
    public boolean accept(PaymentVisitor visitor, double amount) {
        return visitor.visit(this, amount);
    }
}
