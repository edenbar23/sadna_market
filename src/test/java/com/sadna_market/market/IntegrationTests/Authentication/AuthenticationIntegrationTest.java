package com.sadna_market.market.IntegrationTests.Authentication;

import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import com.sadna_market.market.InfrastructureLayer.Authentication.IAuthRepository;
import com.sadna_market.market.InfrastructureLayer.Authentication.InMemoryAuthRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Uses application-test.properties with H2 and InMemoryAuthRepository
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @Autowired
    private AuthenticationAdapter authAdapter;

    @Autowired
    private IAuthRepository authRepository;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up authentication test =====");
        System.out.println("Repository type: " + authRepository.getClass().getSimpleName());
        authRepository.clear();
        System.out.println("Repository cleared");
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up authentication test =====");
        authRepository.clear();
        System.out.println("Repository cleared");
        System.out.println("===== Cleanup complete =====\n");
    }

    @Test
    @DisplayName("Should use in-memory repository with test profile")
    void testUsesInMemoryRepository() {
        System.out.println("TEST: Verifying in-memory repository is used");
        System.out.println("Repository instance: " + authRepository.getClass().getName());

        assertInstanceOf(InMemoryAuthRepository.class, authRepository);
        System.out.println("✓ In-memory repository is used");
    }

    @Test
    @DisplayName("Should store user successfully")
    void testUserStorage() {
        System.out.println("TEST: Verifying user storage");
        String username = "testUser";
        String password = "testPassword";

        System.out.println("Adding user: " + username);
        authRepository.addUser(username, password);

        boolean exists = authRepository.hasMember(username);
        System.out.println("User exists: " + exists);

        assertTrue(exists);
        System.out.println("✓ User stored successfully");
    }

    @Test
    @DisplayName("Should store encrypted password")
    void testPasswordEncryption() {
        System.out.println("TEST: Verifying password encryption");
        String username = "encryptUser";
        String password = "plainPassword123";

        System.out.println("Adding user with password: " + password);
        authRepository.addUser(username, password);

        HashMap<String, String> allUsers = authRepository.getAll();
        String storedPassword = allUsers.get(username);
        System.out.println("Stored password: " + storedPassword);

        assertNotEquals(password, storedPassword);
        assertTrue(storedPassword.startsWith("$2a$"));
        System.out.println("✓ Password encrypted with BCrypt");
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void testSuccessfulLogin() {
        System.out.println("TEST: Verifying successful login");
        String username = "loginUser";
        String password = "loginPassword";

        System.out.println("Adding user: " + username);
        authRepository.addUser(username, password);
        System.out.println("Attempting login...");

        assertDoesNotThrow(() -> authRepository.login(username, password));
        System.out.println("✓ Login successful");
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testFailedLoginWrongPassword() {
        System.out.println("TEST: Verifying login failure with wrong password");
        String username = "wrongPassUser";
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";

        authRepository.addUser(username, password);
        System.out.println("Attempting login with wrong password: " + wrongPassword);

        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> authRepository.login(username, wrongPassword));
        System.out.println("✓ Login rejected with wrong password");
    }

    @Test
    @DisplayName("Should reject login for non-existent user")
    void testFailedLoginNonExistentUser() {
        System.out.println("TEST: Verifying login failure for non-existent user");
        String username = "nonExistentUser";
        String password = "somePassword";

        System.out.println("Attempting login for non-existent user: " + username);

        assertThrows(NoSuchElementException.class,
                () -> authRepository.login(username, password));
        System.out.println("✓ Login rejected for non-existent user");
    }

    @Test
    @DisplayName("Should update password successfully")
    void testPasswordUpdate() {
        System.out.println("TEST: Verifying password update");
        String username = "updateUser";
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword456";

        authRepository.addUser(username, oldPassword);
        System.out.println("Updating password from '" + oldPassword + "' to '" + newPassword + "'");
        authRepository.updateUserPassword(username, oldPassword, newPassword);

        assertDoesNotThrow(() -> authRepository.login(username, newPassword));
        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> authRepository.login(username, oldPassword));
        System.out.println("✓ Password updated successfully");
    }

    @Test
    @DisplayName("Should remove user successfully")
    void testUserRemoval() {
        System.out.println("TEST: Verifying user removal");
        String username = "removeUser";
        String password = "removePassword";

        authRepository.addUser(username, password);
        System.out.println("User added: " + authRepository.hasMember(username));

        authRepository.removeUser(username);
        boolean exists = authRepository.hasMember(username);
        System.out.println("User exists after removal: " + exists);

        assertFalse(exists);
        System.out.println("✓ User removed successfully");
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testTokenGeneration() {
        System.out.println("TEST: Verifying JWT token generation");
        String username = "tokenUser";
        String password = "tokenPassword";

        authAdapter.saveUser(username, password);
        System.out.println("Generating token for user: " + username);

        String token = authAdapter.createUserSessionToken(username, password);
        System.out.println("Generated token: " + (token != null ? "***" + token.substring(token.length()-10) : "null"));

        assertNotNull(token);
        System.out.println("✓ Valid JWT token generated");
    }

    @Test
    @DisplayName("Should validate correct token successfully")
    void testTokenValidation() {
        System.out.println("TEST: Verifying token validation");
        String username = "validateUser";
        String password = "validatePassword";

        authAdapter.saveUser(username, password);
        String token = authAdapter.createUserSessionToken(username, password);
        System.out.println("Validating token for user: " + username);

        assertDoesNotThrow(() -> authAdapter.validateToken(username, token));
        System.out.println("✓ Token validated successfully");
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testInvalidTokenRejection() {
        System.out.println("TEST: Verifying invalid token rejection");
        String username = "invalidTokenUser";
        String password = "password123";
        String invalidToken = "invalid.token.here";

        authAdapter.saveUser(username, password);
        System.out.println("Testing invalid token: " + invalidToken);

        assertThrows(IllegalArgumentException.class,
                () -> authAdapter.validateToken(username, invalidToken));
        System.out.println("✓ Invalid token rejected");
    }

    @Test
    @DisplayName("Should clear all users")
    void testClearAll() {
        System.out.println("TEST: Verifying clear functionality");
        authRepository.addUser("user1", "pass1");
        authRepository.addUser("user2", "pass2");
        System.out.println("Added 2 users");

        authRepository.clear();
        HashMap<String, String> users = authRepository.getAll();
        System.out.println("Users after clear: " + users.size());

        assertTrue(users.isEmpty());
        System.out.println("✓ All users cleared successfully");
    }

    @Test
    @DisplayName("Should verify password correctly through adapter")
    void testPasswordVerification() {
        System.out.println("TEST: Verifying password verification through adapter");
        String username = "verifyUser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        // Use adapter to save user (which uses the repository)
        authAdapter.saveUser(username, correctPassword);

        // Test correct password - should not throw exception
        assertDoesNotThrow(() -> authRepository.login(username, correctPassword));
        System.out.println("✓ Correct password accepted");

        // Test wrong password - should throw exception
        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> authRepository.login(username, wrongPassword));
        System.out.println("✓ Wrong password rejected");

        System.out.println("✓ Password verification working correctly");
    }
}