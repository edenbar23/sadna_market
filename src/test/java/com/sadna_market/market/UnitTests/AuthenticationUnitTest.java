package com.sadna_market.market.UnitTests;

import com.sadna_market.market.InfrastructureLayer.Authentication.AuthCredential;
import com.sadna_market.market.InfrastructureLayer.Authentication.PasswordEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication Unit Tests")
public class AuthenticationUnitTest {

    private AuthCredential authCredential;
    private String testUsername;
    private String testPassword;
    private String testToken;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up authentication test data =====");

        testUsername = "testUser123";
        testPassword = "securePassword456";
        testToken = "jwt.token.example.12345";

        System.out.println("Test username: " + testUsername);
        System.out.println("Test password: " + testPassword);
        System.out.println("Test token: " + testToken);

        authCredential = new AuthCredential(testUsername, testPassword);
        System.out.println("AuthCredential created with username: " + authCredential.getUsername());
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up authentication test resources =====");
        authCredential = null;
        testUsername = null;
        testPassword = null;
        testToken = null;
        System.out.println("Authentication test references set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    @DisplayName("Should set username correctly during creation")
    void testUsernameIsSetCorrectly() {
        System.out.println("TEST: Verifying username is set correctly");
        System.out.println("Expected: " + testUsername);
        System.out.println("Actual: " + authCredential.getUsername());

        assertEquals(testUsername, authCredential.getUsername());
        System.out.println("✓ Username correctly set");
    }

    @Test
    @DisplayName("Should encrypt password during creation")
    void testPasswordIsEncrypted() {
        System.out.println("TEST: Verifying password is encrypted");
        System.out.println("Original password: " + testPassword);
        System.out.println("Encrypted password: " + authCredential.getEncryptedPassword());

        assertNotEquals(testPassword, authCredential.getEncryptedPassword());
        System.out.println("✓ Password is encrypted (not stored as plain text)");
    }

    @Test
    @DisplayName("Should use BCrypt format for encrypted password")
    void testPasswordUsesBCryptFormat() {
        System.out.println("TEST: Verifying password uses BCrypt format");
        System.out.println("Encrypted password: " + authCredential.getEncryptedPassword());
        System.out.println("Checking if starts with '$2a$'...");

        assertTrue(authCredential.getEncryptedPassword().startsWith("$2a$"));
        System.out.println("✓ Password uses BCrypt format");
    }

    @Test
    @DisplayName("Should set creation timestamp automatically")
    void testCreatedAtIsSet() {
        System.out.println("TEST: Verifying creation timestamp is set");
        System.out.println("Created at: " + authCredential.getCreatedAt());

        assertNotNull(authCredential.getCreatedAt());
        System.out.println("✓ Creation timestamp is set");
    }

    @Test
    @DisplayName("Should verify correct password successfully")
    void testVerifyCorrectPassword() {
        System.out.println("TEST: Verifying correct password verification");
        System.out.println("Testing password: " + testPassword);

        boolean result = authCredential.verifyPassword(testPassword);
        System.out.println("Verification result: " + result);

        assertTrue(result);
        System.out.println("✓ Correct password verified successfully");
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyIncorrectPassword() {
        System.out.println("TEST: Verifying incorrect password rejection");
        String wrongPassword = "wrongPassword123";
        System.out.println("Testing wrong password: " + wrongPassword);

        boolean result = authCredential.verifyPassword(wrongPassword);
        System.out.println("Verification result: " + result);

        assertFalse(result);
        System.out.println("✓ Incorrect password rejected");
    }

    @Test
    @DisplayName("Should reject null password gracefully")
    void testVerifyNullPassword() {
        System.out.println("TEST: Verifying null password rejection");
        System.out.println("Testing null password...");

        boolean result = authCredential.verifyPassword(null);
        System.out.println("Verification result: " + result);

        assertFalse(result);
        System.out.println("✓ Null password rejected gracefully");
    }

    @Test
    @DisplayName("Should store encrypted token when set")
    void testSetSessionToken() {
        System.out.println("TEST: Verifying session token storage");
        System.out.println("Setting token: " + testToken);

        authCredential.setSessionToken(testToken);
        System.out.println("Encrypted token in database: " + authCredential.getEncryptedToken());

        assertNotNull(authCredential.getEncryptedToken());
        System.out.println("✓ Session token stored (encrypted)");
    }

    @Test
    @DisplayName("Should encrypt token differently from plain text")
    void testTokenIsEncrypted() {
        System.out.println("TEST: Verifying token encryption");
        System.out.println("Original token: " + testToken);

        authCredential.setSessionToken(testToken);
        String encryptedToken = authCredential.getEncryptedToken();
        System.out.println("Encrypted token: " + encryptedToken);

        assertNotEquals(testToken, encryptedToken);
        System.out.println("✓ Token is encrypted (different from plain text)");
    }

    @Test
    @DisplayName("Should retrieve original token when getting session token")
    void testGetSessionToken() {
        System.out.println("TEST: Verifying session token retrieval");
        System.out.println("Setting token: " + testToken);
        authCredential.setSessionToken(testToken);

        String retrievedToken = authCredential.getSessionToken();
        System.out.println("Retrieved token: " + retrievedToken);

        assertEquals(testToken, retrievedToken);
        System.out.println("✓ Original token retrieved successfully");
    }

    @Test
    @DisplayName("Should clear encrypted token when setting null")
    void testClearSessionToken() {
        System.out.println("TEST: Verifying session token clearing");
        authCredential.setSessionToken(testToken);
        System.out.println("Token set, encrypted token: " + authCredential.getEncryptedToken());

        authCredential.setSessionToken(null);
        System.out.println("Token cleared, encrypted token: " + authCredential.getEncryptedToken());

        assertNull(authCredential.getEncryptedToken());
        System.out.println("✓ Session token cleared successfully");
    }

    @Test
    @DisplayName("Should return null when getting cleared session token")
    void testGetClearedSessionToken() {
        System.out.println("TEST: Verifying cleared session token retrieval");
        authCredential.setSessionToken(testToken);
        authCredential.setSessionToken(null);

        String retrievedToken = authCredential.getSessionToken();
        System.out.println("Retrieved token after clearing: " + retrievedToken);

        assertNull(retrievedToken);
        System.out.println("✓ Null returned for cleared session token");
    }

    // Additional tests for password updates...
    @Test
    @DisplayName("Should update password successfully")
    void testUpdatePassword() {
        System.out.println("TEST: Verifying password update");
        String newPassword = "newSecurePassword789";
        String oldEncryptedPassword = authCredential.getEncryptedPassword();

        System.out.println("Old encrypted password: " + oldEncryptedPassword);
        System.out.println("Updating to new password: " + newPassword);

        authCredential.updatePassword(newPassword);
        System.out.println("New encrypted password: " + authCredential.getEncryptedPassword());

        assertNotEquals(oldEncryptedPassword, authCredential.getEncryptedPassword());
        System.out.println("✓ Password updated successfully");
    }

    @Test
    @DisplayName("Should verify new password after update")
    void testVerifyNewPasswordAfterUpdate() {
        System.out.println("TEST: Verifying new password works after update");
        String newPassword = "updatedPassword123";

        System.out.println("Updating to new password: " + newPassword);
        authCredential.updatePassword(newPassword);

        boolean result = authCredential.verifyPassword(newPassword);
        System.out.println("Verification result for new password: " + result);

        assertTrue(result);
        System.out.println("✓ New password verified after update");
    }
}