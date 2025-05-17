package com.sadna_market.market.AcceptanceTests;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import org.junit.jupiter.api.*;
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
        Response<String> response = bridge.registerUser(registerRequest);

        // Verify response
        Assertions.assertNotNull(response, "Registration response should not be null");
        Assertions.assertFalse(response.isError(), "Registration should succeed");
        Assertions.assertNotNull(response.getData(), "Registration response data should not be null");
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
        Response<String> registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Login with valid credentials
        Response<String> loginResponse = bridge.loginUser(testUsername, testPassword);

        // Verify response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");
        Assertions.assertNotNull(loginResponse.getData(), "Login response data should not be null");
    }

    @Test
    void loginUserWithInvalidCredentialsTest() {
        // Try to login with credentials that don't exist
        Response<String> loginResponse = bridge.loginUser(
                "nonexistentuser" + UUID.randomUUID().toString(),
                "InvalidPassword123!"
        );

        // Verify error response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertTrue(loginResponse.isError(), "Login with invalid credentials should fail");
        Assertions.assertNotNull(loginResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
    void logout() {
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
        Response<String> registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "User registration should succeed");

        // Login with valid credentials
        Response<String> loginResponse = bridge.loginUser(testUsername, testPassword);
        // Extract the token from the login response
        String testToken = loginResponse.getData();

        // Verify response
        Assertions.assertNotNull(loginResponse, "Login response should not be null");
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");
        Assertions.assertNotNull(loginResponse.getData(), "Login response data should not be null");

        // Logout
        Response<String> logoutResponse = bridge.logout(testUsername, testToken);

        // Verify response
        Assertions.assertNotNull(logoutResponse, "Logout response should not be null");
        Assertions.assertFalse(logoutResponse.isError(), "Logout should succeed");
    }

    @Test
    void invalidLogout() {
        // Create two users
        String loggedInUsername = "loggedInUser";
        String notLoggedInUsername = "notLoggedInUser";
        String password = "Password123!";

        // Register both users
        RegisterRequest registerRequest1 = new RegisterRequest(
                loggedInUsername,
                password,
                loggedInUsername + "@example.com",
                "Logged",
                "User"
        );
        RegisterRequest registerRequest2 = new RegisterRequest(
                notLoggedInUsername,
                password,
                notLoggedInUsername + "@example.com",
                "NotLogged",
                "User"
        );

        bridge.registerUser(registerRequest1);
        bridge.registerUser(registerRequest2);

        // Login the first user
        Response<String> loginResponse = bridge.loginUser(loggedInUsername, password);
        Assertions.assertFalse(loginResponse.isError(), "Login should succeed");

        // Extract the token from the logged-in user
        String validToken = loginResponse.getData();

        // Try to logout the second user using the first user's token
        // Did this because i wanted to use a valid token structure
        Response<String> logoutResponse = bridge.logout(notLoggedInUsername, validToken);

        // Verify that the logout attempt fails
        Assertions.assertNotNull(logoutResponse, "Logout response should not be null");
        Assertions.assertTrue(logoutResponse.isError(), "Logout with mismatched token should fail");
        Assertions.assertNotNull(logoutResponse.getErrorMessage(), "Error message should not be null");
    }
}