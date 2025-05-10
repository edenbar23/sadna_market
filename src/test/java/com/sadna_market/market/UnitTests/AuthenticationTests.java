package com.sadna_market.market.UnitTests;

//package com.sadna_market.market.InfrastructureLayer.Authentication;

import com.sadna_market.market.InfrastructureLayer.Authentication.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationTests {

    @Nested
    @DisplayName("PasswordEncryptor Tests")
    class PasswordEncryptorTests {
        private final String testPassword = "securePassword123";

        @Test
        @DisplayName("Should encrypt password correctly")
        void encryptPassword() {
            String encrypted = PasswordEncryptor.encryptPassword(testPassword);

            // Encrypted password should not be the same as original
            assertNotEquals(testPassword, encrypted);
            // Should be a valid BCrypt hash (starts with $2a$ or similar)
            assertTrue(encrypted.startsWith("$2a$") || encrypted.startsWith("$2b$") || encrypted.startsWith("$2y$"));
        }

        @Test
        @DisplayName("Should verify correct password")
        void verifyPassword() {
            String encrypted = PasswordEncryptor.encryptPassword(testPassword);
            assertTrue(PasswordEncryptor.verifyPassword(testPassword, encrypted));
        }

        @Test
        @DisplayName("Should reject incorrect password")
        void rejectIncorrectPassword() {
            String encrypted = PasswordEncryptor.encryptPassword(testPassword);
            assertFalse(PasswordEncryptor.verifyPassword("wrongPassword", encrypted));
        }
    }

    @Nested
    @DisplayName("InMemoryAuthRepository Tests")
    class InMemoryAuthRepositoryTests {
        private IAuthRepository repository;
        private final String testUsername = "testUser";
        private final String testPassword = "testPassword";

        @BeforeEach
        void setUp() {
            repository = new InMemoryAuthRepository();
            repository.clear();
        }

        @Test
        @DisplayName("Should add user correctly")
        void addUser() {
            repository.addUser(testUsername, testPassword);

            HashMap<String, String> users = repository.getAll();
            assertTrue(users.containsKey(testUsername));
            // Password should be encrypted
            assertNotEquals(testPassword, users.get(testUsername));
        }

        @Test
        @DisplayName("Should throw exception when adding existing user")
        void addExistingUser() {
            repository.addUser(testUsername, testPassword);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.addUser(testUsername, "anotherPassword");
            });

            assertEquals("User already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Should login successfully with correct credentials")
        void loginSuccess() {
            repository.addUser(testUsername, testPassword);

            assertDoesNotThrow(() -> repository.login(testUsername, testPassword));
        }

        @Test
        @DisplayName("Should throw exception when logging in with non-existent user")
        void loginNonExistentUser() {
            Exception exception = assertThrows(NoSuchElementException.class, () -> {
                repository.login("nonExistentUser", testPassword);
            });

            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when logging in with wrong password")
        void loginWrongPassword() {
            repository.addUser(testUsername, testPassword);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.login(testUsername, "wrongPassword");
            });

            assertEquals("Wrong password", exception.getMessage());
        }

        @Test
        @DisplayName("Should update user password correctly")
        void updateUserPassword() {
            repository.addUser(testUsername, testPassword);
            String newPassword = "newPassword";

            repository.updateUserPassword(testUsername, testPassword, newPassword);

            // Should be able to login with new password
            assertDoesNotThrow(() -> repository.login(testUsername, newPassword));

            // Should not be able to login with old password
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.login(testUsername, testPassword);
            });

            assertEquals("Wrong password", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating password with wrong old password")
        void updatePasswordWrongOldPassword() {
            repository.addUser(testUsername, testPassword);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.updateUserPassword(testUsername, "wrongPassword", "newPassword");
            });

            assertEquals("Wrong password", exception.getMessage());
        }

        @Test
        @DisplayName("Should remove user correctly")
        void removeUser() {
            repository.addUser(testUsername, testPassword);
            repository.removeUser(testUsername);

            HashMap<String, String> users = repository.getAll();
            assertFalse(users.containsKey(testUsername));
        }

        @Test
        @DisplayName("Should throw exception when removing non-existent user")
        void removeNonExistentUser() {
            Exception exception = assertThrows(NoSuchElementException.class, () -> {
                repository.removeUser("nonExistentUser");
            });

            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should clear all users")
        void clearUsers() {
            repository.addUser("user1", "password1");
            repository.addUser("user2", "password2");

            repository.clear();

            HashMap<String, String> users = repository.getAll();
            assertTrue(users.isEmpty());
        }
    }

    @Nested
    @DisplayName("TokenService Tests")
    class TokenServiceTests {
        private TokenService tokenService;
        private final String testUsername = "testUser";

        @BeforeEach
        void setUp() {
            tokenService = new TokenService();
        }

        @Test
        @DisplayName("Should generate valid token")
        void generateToken() {
            String token = tokenService.generateToken(testUsername);

            assertNotNull(token);
            assertTrue(token.length() > 0);
        }

        @Test
        @DisplayName("Should validate token correctly")
        void validateToken() {
            String token = tokenService.generateToken(testUsername);

            assertTrue(tokenService.validateToken(token));
        }

        @Test
        @DisplayName("Should reject invalid token")
        void rejectInvalidToken() {
            assertFalse(tokenService.validateToken("invalidToken"));
        }

        @Test
        @DisplayName("Should extract username from token")
        void extractUsername() {
            String token = tokenService.generateToken(testUsername);

            assertEquals(testUsername, tokenService.extractUsername(token));
        }
    }

    @Nested
    @DisplayName("AuthenticationBridge Tests")
    class AuthenticationBridgeTests {
        @Mock
        private IAuthRepository mockAuthRepository;

        private AuthenticationBridge authBridge;
        private final String testUsername = "testUser";
        private final String testPassword = "testPassword";

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            authBridge = new AuthenticationBridge(mockAuthRepository);
        }

        @Test
        @DisplayName("Should create user session token")
        void createUserSessionToken() {
            // No need to verify the actual token, just that it's created
            String token = authBridge.createUserSessionToken(testUsername, testPassword);

            // Verify login was called on repository
            verify(mockAuthRepository).login(testUsername, testPassword);

            assertNotNull(token);
            assertTrue(token.length() > 0);
        }

        @Test
        @DisplayName("Should save user correctly")
        void saveUser() {
            authBridge.saveUser(testUsername, testPassword);

            // Verify addUser was called on repository
            verify(mockAuthRepository).addUser(testUsername, testPassword);
        }

        @Test
        @DisplayName("Should check session token correctly")
        void checkSessionToken() {
            // Create a real token to test with
            String token = new TokenService().generateToken(testUsername);

            assertEquals(testUsername, authBridge.checkSessionToken(token));
        }

        @Test
        @DisplayName("Should throw exception for invalid token during check")
        void checkInvalidSessionToken() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                authBridge.checkSessionToken("invalidToken");
            });

            assertEquals("Invalid token", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate token against username correctly")
        void validateToken() {
            // Create a real token for testUsername
            String token = new TokenService().generateToken(testUsername);

            // Should not throw exception
            assertDoesNotThrow(() -> authBridge.validateToken(testUsername, token));
        }

        @Test
        @DisplayName("Should throw exception when validating token with wrong username")
        void validateTokenWrongUsername() {
            // Create a real token for testUsername
            String token = new TokenService().generateToken(testUsername);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                authBridge.validateToken("differentUser", token);
            });

            assertEquals("Invalid token", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when validating invalid token")
        void validateInvalidToken() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                authBridge.validateToken(testUsername, "invalidToken");
            });

            assertEquals("Invalid token", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        private AuthenticationBridge authBridge;
        private final String testUsername = "integrationUser";
        private final String testPassword = "integrationPassword";

        @BeforeEach
        void setUp() {
            // Use the real implementation with in-memory repository
            authBridge = new AuthenticationBridge();
            // Clear any existing users
            IAuthRepository repository = new InMemoryAuthRepository();
            repository.clear();
        }

        @Test
        @DisplayName("Full authentication flow")
        void fullAuthenticationFlow() {
            // Save a new user
            authBridge.saveUser(testUsername, testPassword);

            // Create a session token
            String token = authBridge.createUserSessionToken(testUsername, testPassword);
            assertNotNull(token);

            // Check the token
            String extractedUsername = authBridge.checkSessionToken(token);
            assertEquals(testUsername, extractedUsername);

            // Validate the token against the username
            assertDoesNotThrow(() -> authBridge.validateToken(testUsername, token));
        }
    }
}
