package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sadna_market.market.InfrastructureLayer.Authentication.InMemoryAuthRepository;
import com.sadna_market.market.InfrastructureLayer.Authentication.PasswordEncryptor;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAuthRepositoryTest {

    private InMemoryAuthRepository repository;
    private String username;
    private String password;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAuthRepository();
        
        // Set up test data
        username = "testUser";
        password = "Password123!";
    }

    @Test
    void testAddUser() {
        // Add a user
        repository.addUser(username, password);
        
        // Verify user was added
        HashMap<String, String> users = repository.getAll();
        assertTrue(users.containsKey(username));
        
        // Password should be encrypted
        String encryptedPassword = users.get(username);
        assertNotNull(encryptedPassword);
        assertNotEquals(password, encryptedPassword);
        
        // But should be verifiable
        assertTrue(PasswordEncryptor.verifyPassword(password, encryptedPassword));
    }

    @Test
    void testAddDuplicateUser() {
        // Add a user
        repository.addUser(username, password);
        
        // Try to add a duplicate user
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addUser(username, "DifferentPassword123!");
        });
        
        String expectedMessage = "User already exists";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testLogin() {
        // Add a user
        repository.addUser(username, password);
        
        // Login with correct credentials - should not throw exception
        repository.login(username, password);
    }

    @Test
    void testLoginNonExistentUser() {
        // Try to login with non-existent user
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            repository.login("nonExistentUser", password);
        });
        
        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testLoginWrongPassword() {
        // Add a user
        repository.addUser(username, password);
        
        // Try to login with wrong password
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.login(username, "WrongPassword123!");
        });
        
        String expectedMessage = "Wrong password";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testGetAll() {
        // Initially empty
        HashMap<String, String> users = repository.getAll();
        assertTrue(users.isEmpty());
        
        // Add some users
        repository.addUser(username, password);
        repository.addUser("anotherUser", "AnotherPassword123!");
        
        // Get all users
        users = repository.getAll();
        
        // Verify
        assertEquals(2, users.size());
        assertTrue(users.containsKey(username));
        assertTrue(users.containsKey("anotherUser"));
    }

    @Test
    void testUpdateUserPassword() {
        // Add a user
        repository.addUser(username, password);
        
        // Update password
        String newPassword = "NewPassword123!";
        repository.updateUserPassword(username, password, newPassword);
        
        // Verify old password no longer works
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.login(username, password);
        });
        
        // Verify new password works
        repository.login(username, newPassword);
    }

    @Test
    void testUpdateUserPasswordNonExistentUser() {
        // Try to update password for non-existent user
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            repository.updateUserPassword("nonExistentUser", password, "NewPassword123!");
        });
        
        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateUserPasswordWrongOldPassword() {
        // Add a user
        repository.addUser(username, password);
        
        // Try to update with wrong old password
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateUserPassword(username, "WrongPassword123!", "NewPassword123!");
        });
        
        String expectedMessage = "Wrong password";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemoveUser() {
        // Add a user
        repository.addUser(username, password);
        
        // Verify user exists
        HashMap<String, String> users = repository.getAll();
        assertTrue(users.containsKey(username));
        
        // Remove the user
        repository.removeUser(username);
        
        // Verify user was removed
        users = repository.getAll();
        assertFalse(users.containsKey(username));
    }

    @Test
    void testRemoveNonExistentUser() {
        // Try to remove non-existent user
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            repository.removeUser("nonExistentUser");
        });
        
        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testClear() {
        // Add some users
        repository.addUser(username, password);
        repository.addUser("anotherUser", "AnotherPassword123!");
        
        // Verify users exist
        HashMap<String, String> users = repository.getAll();
        assertEquals(2, users.size());
        
        // Clear all users
        repository.clear();
        
        // Verify all users were removed
        users = repository.getAll();
        assertTrue(users.isEmpty());
    }
}