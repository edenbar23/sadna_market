package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Request object for user registration in the market system.
 * Contains all necessary user information required for account creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String userName;    // Unique username (required)
    private String password;    // User password (required)
    private String email;       // User email address (required)
    private String firstName;   // User's first name (required)
    private String lastName;    // User's last name (required)
    private String phoneNumber; // User's phone number (optional)
    private String address;     // User's address (optional)

    /**
     * Constructor with essential fields for user registration
     * 
     * @param userName  The unique username for the account
     * @param password  The account password
     * @param email     The user's email address
     * @param firstName The user's first name
     * @param lastName  The user's last name
     */
    public RegisterRequest(String userName, String password, String email, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor for simplified registration (with minimal information)
     * 
     * @param userName The unique username for the account
     * @param password The account password
     * @param email    The user's email address
     */
    public RegisterRequest(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = "";
        this.lastName = "";
    }

    /**
     * Validates that all required fields are present and properly formatted
     * 
     * @return true if the registration request is valid, false otherwise
     */
    public boolean isValid() {
        // Check required fields are present
        if (userName == null || userName.isEmpty() ||
            password == null || password.isEmpty() ||
            email == null || email.isEmpty()) {
            return false;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            return false;
        }
        
        // Validate password strength (minimum 8 characters)
        if (password.length() < 8) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates email format using regex pattern
     * 
     * @param email The email address to validate
     * @return true if the email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    /**
     * Checks if this is a minimal registration (without first/last name)
     * 
     * @return true if this is a minimal registration, false if it's a full registration
     */
    public boolean isMinimalRegistration() {
        return (firstName == null || firstName.isEmpty()) && 
               (lastName == null || lastName.isEmpty());
    }
    
    /**
     * Creates a string representation suitable for logging (without password)
     * 
     * @return A string representation with the password field masked
     */
    public String toSafeString() {
        return "RegisterRequest{" +
                "userName='" + userName + '\'' +
                ", password='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}