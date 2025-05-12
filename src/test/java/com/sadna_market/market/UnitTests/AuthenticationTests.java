package com.sadna_market.market.UnitTests;

import com.sadna_market.market.InfrastructureLayer.Authentication.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.NoSuchElementException;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationTests {

    // Rest of the test classes remain the same...

    @Nested
    @DisplayName("AuthenticationBridge Tests")
    class AuthenticationBridgeTests {
        @Mock
        private IAuthRepository mockAuthRepository;

        private TokenService tokenService;
        private AuthenticationBridge authBridge;
        private final String testUsername = "testUser";
        private final String testPassword = "testPassword";

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            // Create a token service first
            tokenService = new TokenService();
            // Pass it to the auth bridge
            authBridge = new AuthenticationBridge(mockAuthRepository, tokenService);
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
            // Create a token using the SAME token service instance
            String token = tokenService.generateToken(testUsername);

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
            // Create a token using the SAME token service instance
            String token = tokenService.generateToken(testUsername);

            // Should not throw exception
            assertDoesNotThrow(() -> authBridge.validateToken(testUsername, token));
        }

        @Test
        @DisplayName("Should throw exception when validating token with wrong username")
        void validateTokenWrongUsername() {
            // Create a token using the SAME token service instance
            String token = tokenService.generateToken(testUsername);

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