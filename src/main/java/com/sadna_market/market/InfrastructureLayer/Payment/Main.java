package com.sadna_market.market.InfrastructureLayer.Payment;

public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();
        //4571736012345678
        PaymentMethod card = new CreditCardDTO("4571736012345678", "Alice", "12/25", "123");
        PaymentMethod bank = new BankAccountDTO("987654321", "MyBank");
        PaymentMethod paypal = new PayPalDTO("alice@example.com");

        service.pay(card, 99.99);
        service.pay(bank, 199.99);
        service.pay(paypal, 49.99);
    }

}
