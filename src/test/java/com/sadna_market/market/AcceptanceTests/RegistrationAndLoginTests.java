package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.InfrastructureLayer.Payment.CreditCardDTO;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class RegistrationAndLoginTests {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationAndLoginTests.class);
    private Bridge bridge = new Bridge();
    @AfterEach
    void tearDown() {
        // Clear system state after each test
        bridge.clear();
    }
    @Test
    void registerUserTest() {
        // Create a unique username
        String testUsername = "testuser";

        // Create registration request
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                "Password123!",
                testUsername + "@example.com",
                "Test",
                "User"
        );

        // Register the user
        Response response = bridge.registerUser(registerRequest);

        // Verify response
        Assertions.assertNotNull(response, "Registration response should not be null");
        Assertions.assertFalse(response.isError(), "Registration should succeed");
        Assertions.assertNotNull(response.getJson(), "Registration response JSON should not be null");
    }

    @Test
    void loginUserTest() {
        // Create a unique username
        String testUsername = "loginuser";
        String testPassword = "Password123!";

        // Register the user first
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testUsername + "@example.com",
                "Login",
                "User"
        );
        Response registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Login with valid credentials
        Response loginResponse = bridge.loginUser(testUsername, testPassword);

        // Verify response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");
        Assertions.assertNotNull(loginResponse.getJson(), "Login response JSON should not be null");
    }

    @Test
    void loginUserWithInvalidCredentialsTest() {
        // Try to login with credentials that don't exist
        Response loginResponse = bridge.loginUser(
                "nonexistentuser" + UUID.randomUUID().toString(),
                "InvalidPassword123!"
        );

        // Verify error response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertTrue(loginResponse.isError(), "Login with invalid credentials should fail");
        Assertions.assertNotNull(loginResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void logout(){
        // Create a unique username
        String testUsername = "logoutuser";
        String testPassword = "Password123!";

        // Register the user first
        RegisterRequest registerRequest = new RegisterRequest(
                testUsername,
                testPassword,
                testUsername + "@example.com",
                "Logout",
                "User"
        );
        Response registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Login with valid credentials
        Response loginResponse = bridge.loginUser(testUsername, testPassword);
        // Extract the token from the login response
        String testToken = loginResponse.getJson();


        // Verify response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");
        Assertions.assertNotNull(loginResponse.getJson(), "Login response JSON should not be null");

        // Logout
        Response logoutResponse = bridge.logout(testUsername, testToken);

        // Verify response
        Assertions.assertNotNull(logoutResponse, "Logout response should not be null");
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
    }


}
