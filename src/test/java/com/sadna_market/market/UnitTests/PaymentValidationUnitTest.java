package com.sadna_market.market.UnitTests;

import com.sadna_market.market.InfrastructureLayer.Payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentValidationUnitTest {

    private ConcretePaymentVisitor visitor;
    private Method isValidCvvMethod;
    private Method isValidExpiryDateMethod;

    @BeforeEach
    public void setUp() throws Exception {
        visitor = new ConcretePaymentVisitor();

        // Get access to private methods using reflection
        isValidCvvMethod = ConcretePaymentVisitor.class.getDeclaredMethod("isValidCvv", String.class);
        isValidCvvMethod.setAccessible(true);

        isValidExpiryDateMethod = ConcretePaymentVisitor.class.getDeclaredMethod("isValidExpiryDate", String.class);
        isValidExpiryDateMethod.setAccessible(true);
    }

    @Test
    public void testCvvValidation() throws Exception {
        // Valid CVVs
        assertTrue((Boolean) isValidCvvMethod.invoke(visitor, "123"));
        assertTrue((Boolean) isValidCvvMethod.invoke(visitor, "1234"));

        // Invalid CVVs
//        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, null));
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, ""));
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, "12"));     // Too short
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, "12345"));  // Too long
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, "abc"));    // Non-numeric
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, "1a3"));    // Contains letters
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "999", "1234", "0000"})
    public void testValidCvvValues(String cvv) throws Exception {
        assertTrue((Boolean) isValidCvvMethod.invoke(visitor, cvv));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12", "12345", "abc", "1a3", " 123"})
    public void testInvalidCvvValues(String cvv) throws Exception {
        assertFalse((Boolean) isValidCvvMethod.invoke(visitor, cvv));
    }

    @Test
    public void testExpiryDateValidation() throws Exception {
        // Get current month and year for testing
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentMonth = cal.get(java.util.Calendar.MONTH) + 1; // Calendar months are 0-based
        int currentYear = cal.get(java.util.Calendar.YEAR) % 100; // Get last two digits

        // Create valid future date
        String futureMonth = String.format("%02d", currentMonth);
        String futureYear = String.format("%02d", currentYear + 1);
        String validFutureDate = futureMonth + "/" + futureYear;

        // Create valid current month/year
        String currentMonthStr = String.format("%02d", currentMonth);
        String currentYearStr = String.format("%02d", currentYear);
        String currentDate = currentMonthStr + "/" + currentYearStr;

        // Create expired date (last year)
        String expiredYear = String.format("%02d", currentYear - 1);
        String expiredDate = currentMonthStr + "/" + expiredYear;

        // Valid dates
        assertTrue((Boolean) isValidExpiryDateMethod.invoke(visitor, validFutureDate));

        // Current date should be valid as cards typically expire at end of month
        assertTrue((Boolean) isValidExpiryDateMethod.invoke(visitor, currentDate));

        // Invalid dates
//        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, null));
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, ""));
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, "1225"));      // Wrong format
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, "12/2025"));   // Wrong year format
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, "13/25"));     // Invalid month
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, "00/25"));     // Invalid month
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, "ab/cd"));     // Non-numeric
        assertFalse((Boolean) isValidExpiryDateMethod.invoke(visitor, expiredDate)); // Expired date
    }

    @ParameterizedTest
    @CsvSource({
            "12/25, true",   // Future date
            "01/30, true",   // Future date
            "13/25, false",  // Invalid month
            "00/25, false",  // Invalid month
            "12/2025, false", // Wrong year format
            "12-25, false",  // Wrong format
            "ab/cd, false"   // Non-numeric
    })
    public void testExpiryDateValues(String expiryDate, boolean expected) throws Exception {
        // Note: This test might fail if the test is run close to the expiry date
        // and the date becomes expired during the test
        assertEquals(expected, (Boolean) isValidExpiryDateMethod.invoke(visitor, expiryDate));
    }
}