package com.sadna_market.market.InfrastructureLayer.Payment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ConcretePaymentVisitor implements PaymentVisitor {
    private ExternalPaymentAPI api = new ExternalPaymentAPI();

    @Override
    public boolean visit(CreditCardDTO card, double amount) {
        System.out.println("Visitor: Processing credit card...");

        // Validate CVV
        if (!isValidCvv(card.cvv)) {
            throw new IllegalArgumentException("Card validation failed: Invalid CVV");
        }

        // Validate expiry date
        if (!isValidExpiryDate(card.expiryDate)) {
            throw new IllegalArgumentException("Card validation failed: Invalid or expired expiry date");
        }


        try {
            String prefix = card.getCardPrefix();
            URL url = new URL("https://lookup.binlist.net/" + prefix);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                //System.out.println("Card validation failed: bad response");
                throw new IllegalArgumentException("Card validation failed: invalid card number");

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
            throw new IllegalArgumentException("Card validation failed: invalid card number");
        }
    }

    /**
     * Validates the credit card CVV
     * @param cvv The CVV to validate
     * @return true if the CVV is valid, false otherwise
     */
    private boolean isValidCvv(String cvv) {
        if (cvv == null) {
            return false;
        }

        // CVV should be 3-4 digits
        return cvv.matches("^[0-9]{3,4}$");
    }

    /**
     * Validates the credit card expiry date
     * @param expiryDate The expiry date in MM/YY format
     * @return true if the date is valid and not expired, false otherwise
     */
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

            // Add one day to the last day of the month
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
///

