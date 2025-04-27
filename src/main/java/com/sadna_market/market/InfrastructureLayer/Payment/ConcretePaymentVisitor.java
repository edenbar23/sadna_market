package com.sadna_market.market.InfrastructureLayer.Payment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConcretePaymentVisitor implements PaymentVisitor {
    private ExternalPaymentAPI api = new ExternalPaymentAPI();

    @Override
    public boolean visit(CreditCardDTO card, double amount) {
        System.out.println("Visitor: Processing credit card...");

        try {
            String prefix = card.getCardPrefix();
            URL url = new URL("https://lookup.binlist.net/" + prefix);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Card validation failed: bad response");
                return false;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Simple check: just print info
            //System.out.println("Binlist response: " + response.toString());

            System.out.println("approved");
            // Optional: parse and act on the JSON using org.json or manual parsing

            return api.sendCreditCardPayment(card.cardNumber, card.cardHolderName, card.expiryDate, card.cvv, amount); // validation passed
        } catch (Exception e) {
            System.out.println("Error during card validation: " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean visit(BankAccountDTO account, double amount) {
        System.out.println("Visitor: Processing bank account...");
        return api.sendBankPayment(account.accountNumber, account.bankName, amount);
    }

    @Override
    public boolean visit(PayPalDTO paypal, double amount) {
        System.out.println("Visitor: Processing PayPal payment for " + paypal.email + "...");
        return true; // simulate success
    }
}

