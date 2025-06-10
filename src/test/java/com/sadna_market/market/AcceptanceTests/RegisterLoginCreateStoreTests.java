package com.sadna_market.market.AcceptanceTests;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")  // This activates the test profile
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class RegisterLoginCreateStoreTests {

    private static final Logger logger = LoggerFactory.getLogger(RegisterLoginCreateStoreTests.class);
    @Autowired
    private Bridge bridge;
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

// Store Creation
    @Test
    void storeCreationTest() {
        String ownerUsername = "storeowner";
        String ownerPassword = "Password123!";
        RegisterRequest registerRequest = new RegisterRequest(
                ownerUsername,
                ownerPassword,
                ownerUsername + "@example.com",
                "Owner",
                "User"
        );
        Response<String> registerResponse = bridge.registerUser(registerRequest);
        Assertions.assertFalse(registerResponse.isError(), "Store owner registration should succeed");

        Response<String> loginResponse = bridge.loginUser(ownerUsername, ownerPassword);
        Assertions.assertFalse(loginResponse.isError(), "Store owner login should succeed");
        String ownerToken = loginResponse.getData();

        StoreRequest storeRequest = new StoreRequest(
                "My Test Store",
                "Just a test store",
                "Somewhere",
                "store@example.com",
                "123-456-7890",
                ownerUsername
        );
        Response<?> storeResponse = bridge.createStore(ownerUsername, ownerToken, storeRequest);

        Assertions.assertNotNull(storeResponse, "Store creation response should not be null");
        Assertions.assertFalse(storeResponse.isError(), "Store creation should succeed");
    }

    @Test
    void storeCreationByLoggedOutUserFailsTest() {
        String username = "logoutowner";
        String password = "Password123!";

        // Register and login
        bridge.registerUser(new RegisterRequest(
                username, password, username + "@example.com", "Log", "Out"
        ));
        String token = bridge.loginUser(username, password).getData();

        // Logout the user
        bridge.logout(username, token);

        // Try to create a store with logged out token
        StoreRequest storeRequest = new StoreRequest(
                "Store by LoggedOut", "Desc", "Addr", "email@example.com", "123456", username
        );

        Response<?> response = bridge.createStore(username, token, storeRequest);
        Assertions.assertTrue(response.isError(), "Logged-out user should not be able to create a store");
    }

    @Test
    void storeCreationWithoutNameFailsTest() {
        String username = "nonameuser";
        String password = "Password123!";

        bridge.registerUser(new RegisterRequest(
                username, password, username + "@example.com", "No", "Name"
        ));
        String token = bridge.loginUser(username, password).getData();
        StoreRequest storeRequest = new StoreRequest(
                "", "Desc", "Address", "store@example.com", "123456", username
        );
        Response<?> response = bridge.createStore(username, token, storeRequest);
        Assertions.assertTrue(response.isError(), "Store creation without name should fail");
    }

    @Test
    void duplicateStoreNameFailsIfNotAllowedTest() {
        String username = "dupstoreuser";
        String password = "Password123!";

        bridge.registerUser(new RegisterRequest(username, password, username + "@example.com", "Dup", "User"));
        String token = bridge.loginUser(username, password).getData();
        StoreRequest storeRequest = new StoreRequest(
                "Unique Store", "Desc", "Address", "store@example.com", "123456", username
        );

        Response<?> firstResponse = bridge.createStore(username, token, storeRequest);
        Assertions.assertFalse(firstResponse.isError(), "First store creation should succeed");

        Response<?> secondResponse = bridge.createStore(username, token, storeRequest);
        Assertions.assertTrue(secondResponse.isError(), "Second store creation with same name should fail");
    }
}