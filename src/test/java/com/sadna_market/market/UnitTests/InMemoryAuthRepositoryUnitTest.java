package com.sadna_market.market.UnitTests;

import com.sadna_market.market.InfrastructureLayer.Authentication.InMemoryAuthRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryAuthRepository Unit Tests")
public class InMemoryAuthRepositoryUnitTest {

    private InMemoryAuthRepository authRepository;
    private final String testUsername = "testUser";
    private final String testPassword = "Password123!";

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");
        authRepository = new InMemoryAuthRepository();

        // Add a test user
        System.out.println("Adding test user: " + testUsername);
        authRepository.addUser(testUsername, testPassword);
        System.out.println("Test user added successfully");
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        authRepository.clear();
        System.out.println("Auth repository cleared");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    @DisplayName("Login succeeds with correct credentials")
    void testLogin_CorrectCredentials_LoginSucceeds() {
        System.out.println("TEST: Verifying login with correct credentials");

        System.out.println("Attempting login with username: " + testUsername);
        assertDoesNotThrow(() -> {
            authRepository.login(testUsername, testPassword);
            System.out.println("Login successful");
        }, "Login should succeed with correct credentials");

        System.out.println("✓ login correctly authenticates user with valid credentials");
    }

    @Test
    @DisplayName("Login fails with non-existent username")
    void testLogin_NonExistentUser_ThrowsException() {
        System.out.println("TEST: Verifying login with non-existent user");

        String nonExistentUser = "nonExistentUser";
        System.out.println("Attempting login with non-existent username: " + nonExistentUser);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authRepository.login(nonExistentUser, testPassword);
        }, "Login should throw exception for non-existent user");

        System.out.println("Expected: Exception message containing 'User does not exist'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("User does not exist"),
                "Exception message should indicate user does not exist");

        System.out.println("✓ login correctly rejects non-existent user");
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    void testLogin_IncorrectPassword_ThrowsException() {
        System.out.println("TEST: Verifying login with incorrect password");

        String wrongPassword = "WrongPassword456!";
        System.out.println("Attempting login with incorrect password");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authRepository.login(testUsername, wrongPassword);
        }, "Login should throw exception for incorrect password");

        System.out.println("Expected: Exception message containing 'Wrong password'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Wrong password"),
                "Exception message should indicate wrong password");

        System.out.println("✓ login correctly rejects incorrect password");
    }

    @Test
    @DisplayName("GetAll returns all users and passwords")
    void testGetAll_ReturnsAllUsers() {
        System.out.println("TEST: Verifying getAll returns all users");

        // Add another user
        String anotherUsername = "anotherUser";
        String anotherPassword = "AnotherPassword123!";
        System.out.println("Adding another user: " + anotherUsername);
        authRepository.addUser(anotherUsername, anotherPassword);

        HashMap<String, String> users = authRepository.getAll();

        System.out.println("Expected: Users map size = 2");
        System.out.println("Actual: Users map size = " + users.size());
        assertEquals(2, users.size(), "Should return 2 users");

        System.out.println("Expected: Map contains testUser = true");
        System.out.println("Actual: Map contains testUser = " + users.containsKey(testUsername));
        assertTrue(users.containsKey(testUsername), "Users map should contain test user");

        System.out.println("Expected: Map contains anotherUser = true");
        System.out.println("Actual: Map contains anotherUser = " + users.containsKey(anotherUsername));
        assertTrue(users.containsKey(anotherUsername), "Users map should contain another user");

        System.out.println("✓ getAll correctly returns all users");
    }

    @Test
    @DisplayName("AddUser successfully adds a new user")
    void testAddUser_NewUser_UserAdded() {
        System.out.println("TEST: Verifying addUser adds a new user");

        String newUsername = "newUser";
        String newPassword = "NewPassword123!";

        System.out.println("Adding new user: " + newUsername);
        authRepository.addUser(newUsername, newPassword);

        // Verify the user was added by attempting to login
        System.out.println("Verifying user was added by attempting login");
        assertDoesNotThrow(() -> {
            authRepository.login(newUsername, newPassword);
            System.out.println("Login successful, user was added");
        }, "Login should succeed for newly added user");

        System.out.println("✓ addUser correctly adds a new user");
    }

    @Test
    @DisplayName("AddUser fails with existing username")
    void testAddUser_ExistingUser_ThrowsException() {
        System.out.println("TEST: Verifying addUser fails with existing username");

        System.out.println("Attempting to add user with existing username: " + testUsername);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authRepository.addUser(testUsername, "NewPassword123!");
        }, "AddUser should throw exception for existing username");

        System.out.println("Expected: Exception message containing 'User already exists'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("User already exists"),
                "Exception message should indicate user already exists");

        System.out.println("✓ addUser correctly rejects existing username");
    }

    @Test
    @DisplayName("UpdateUserPassword successfully changes password")
    void testUpdateUserPassword_CorrectOldPassword_PasswordUpdated() {
        System.out.println("TEST: Verifying updateUserPassword with correct old password");

        String newPassword = "NewPassword456!";

        System.out.println("Updating password for user: " + testUsername);
        authRepository.updateUserPassword(testUsername, testPassword, newPassword);

        // Verify the password was updated by attempting to login with new password
        System.out.println("Verifying password was updated by attempting login with new password");
        assertDoesNotThrow(() -> {
            authRepository.login(testUsername, newPassword);
            System.out.println("Login successful with new password");
        }, "Login should succeed with new password");

        // Verify old password no longer works
        System.out.println("Verifying old password no longer works");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authRepository.login(testUsername, testPassword);
        }, "Login should fail with old password");

        System.out.println("✓ updateUserPassword correctly updates password");
    }

    @Test
    @DisplayName("UpdateUserPassword fails with non-existent user")
    void testUpdateUserPassword_NonExistentUser_ThrowsException() {
        System.out.println("TEST: Verifying updateUserPassword with non-existent user");

        String nonExistentUser = "nonExistentUser";
        String newPassword = "NewPassword456!";

        System.out.println("Attempting to update password for non-existent user: " + nonExistentUser);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authRepository.updateUserPassword(nonExistentUser, testPassword, newPassword);
        }, "UpdateUserPassword should throw exception for non-existent user");

        System.out.println("Expected: Exception message containing 'User does not exist'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("User does not exist"),
                "Exception message should indicate user does not exist");

        System.out.println("✓ updateUserPassword correctly rejects non-existent user");
    }

    @Test
    @DisplayName("UpdateUserPassword fails with incorrect old password")
    void testUpdateUserPassword_IncorrectOldPassword_ThrowsException() {
        System.out.println("TEST: Verifying updateUserPassword with incorrect old password");

        String wrongPassword = "WrongPassword456!";
        String newPassword = "NewPassword789!";

        System.out.println("Attempting to update password with incorrect old password");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authRepository.updateUserPassword(testUsername, wrongPassword, newPassword);
        }, "UpdateUserPassword should throw exception for incorrect old password");

        System.out.println("Expected: Exception message containing 'Wrong password'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Wrong old password"),
                "Exception message should indicate wrong old password");

        System.out.println("✓ updateUserPassword correctly rejects incorrect old password");
    }

    @Test
    @DisplayName("RemoveUser successfully removes a user")
    void testRemoveUser_ExistingUser_UserRemoved() {
        System.out.println("TEST: Verifying removeUser with existing user");

        System.out.println("Removing user: " + testUsername);
        authRepository.removeUser(testUsername);

        // Verify the user was removed by attempting to login
        System.out.println("Verifying user was removed by attempting login");
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authRepository.login(testUsername, testPassword);
        }, "Login should fail for removed user");

        System.out.println("Expected: Exception message containing 'User does not exist'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("User does not exist"),
                "Exception message should indicate user does not exist");

        System.out.println("✓ removeUser correctly removes a user");
    }

    @Test
    @DisplayName("RemoveUser fails with non-existent user")
    void testRemoveUser_NonExistentUser_ThrowsException() {
        System.out.println("TEST: Verifying removeUser with non-existent user");

        String nonExistentUser = "nonExistentUser";

        System.out.println("Attempting to remove non-existent user: " + nonExistentUser);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authRepository.removeUser(nonExistentUser);
        }, "RemoveUser should throw exception for non-existent user");

        System.out.println("Expected: Exception message containing 'User does not exist'");
        System.out.println("Actual: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("User does not exist"),
                "Exception message should indicate user does not exist");

        System.out.println("✓ removeUser correctly rejects non-existent user");
    }

    @Test
    @DisplayName("Clear successfully removes all users")
    void testClear_AllUsersRemoved() {
        System.out.println("TEST: Verifying clear removes all users");

        System.out.println("Clearing all users");
        authRepository.clear();

        HashMap<String, String> users = authRepository.getAll();

        System.out.println("Expected: Users map size = 0");
        System.out.println("Actual: Users map size = " + users.size());
        assertEquals(0, users.size(), "Users map should be empty after clear");

        // Verify the test user was removed by attempting to login
        System.out.println("Verifying test user was removed by attempting login");
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authRepository.login(testUsername, testPassword);
        }, "Login should fail after clear");

        System.out.println("✓ clear correctly removes all users");
    }
}