//package com.sadna_market.market.UnitTests;
//
//import com.sadna_market.market.DomainLayer.User;
//import com.sadna_market.market.DomainLayer.RoleType;
//import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryUserRepository;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DisplayName("InMemoryUserRepository Tests")
//public class InMemoryUserRepositoryUnitTest {
//
//    private InMemoryUserRepository userRepository;
//    private String testUsername;
//    private String testPassword;
//    private String testEmail;
//    private String testFirstName;
//    private String testLastName;
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        System.out.println("\n===== Setting up test environment =====");
//        userRepository = new InMemoryUserRepository();
//
//        testUsername = "testUser";
//        testPassword = "testPassword";
//        testEmail = "test@example.com";
//        testFirstName = "Test";
//        testLastName = "User";
//
//        // Create a test user
//        testUser = new User(testUsername, testPassword, testEmail, testFirstName, testLastName);
//        userRepository.save(testUser);
//
//        System.out.println("Created test user with username: " + testUsername);
//        System.out.println("Test user email: " + testEmail);
//        System.out.println("Test user first name: " + testFirstName);
//        System.out.println("Test user last name: " + testLastName);
//        System.out.println("===== Setup complete =====");
//    }
//
//    @AfterEach
//    void tearDown() {
//        System.out.println("===== Cleaning up test resources =====");
//        userRepository.clear();
//        testUser = null;
//        System.out.println("User repository cleared");
//        System.out.println("Test user reference set to null");
//        System.out.println("===== Cleanup complete =====\n");
//    }
//
//    // CRUD Operations Tests
//
//    @Test
//    @DisplayName("Find existing user by username should return user")
//    void testFindByUsername_ExistingUser_ReturnsUser() {
//        System.out.println("TEST: Verifying findByUsername with existing user");
//
//        System.out.println("Looking for user with username: " + testUsername);
//        Optional<User> result = userRepository.findByUsername(testUsername);
//
//        System.out.println("Expected: User should be present");
//        System.out.println("Actual: User is present = " + result.isPresent());
//        assertTrue(result.isPresent(), "User should be found");
//
//        System.out.println("Expected username: " + testUsername);
//        System.out.println("Actual username: " + result.get().getUserName());
//        assertEquals(testUsername, result.get().getUserName(), "Username should match");
//
//        System.out.println("Expected email: " + testEmail);
//        System.out.println("Actual email: " + result.get().getEmail());
//        assertEquals(testEmail, result.get().getEmail(), "Email should match");
//
//        System.out.println("✓ findByUsername correctly returns the user");
//    }
//
//    @Test
//    @DisplayName("Find non-existing user by username should return empty")
//    void testFindByUsername_NonExistingUser_ReturnsEmpty() {
//        System.out.println("TEST: Verifying findByUsername with non-existing user");
//
//        String nonExistingUsername = "nonExistingUser";
//        System.out.println("Looking for non-existing user with username: " + nonExistingUsername);
//        Optional<User> result = userRepository.findByUsername(nonExistingUsername);
//
//        System.out.println("Expected: User should not be present");
//        System.out.println("Actual: User is present = " + result.isPresent());
//        assertFalse(result.isPresent(), "User should not be found");
//
//        System.out.println("✓ findByUsername correctly returns empty for non-existing user");
//    }
//
//    @Test
//    @DisplayName("Contains should return true for existing user")
//    void testContains_ExistingUser_ReturnsTrue() {
//        System.out.println("TEST: Verifying contains returns true for existing user");
//
//        System.out.println("Checking if user exists with username: " + testUsername);
//        boolean exists = userRepository.contains(testUsername);
//
//        System.out.println("Expected: true");
//        System.out.println("Actual: " + exists);
//        assertTrue(exists, "User should exist");
//
//        System.out.println("✓ contains correctly returns true for existing user");
//    }
//
//    @Test
//    @DisplayName("Contains should return false for non-existing user")
//    void testContains_NonExistingUser_ReturnsFalse() {
//        System.out.println("TEST: Verifying contains returns false for non-existing user");
//
//        String nonExistingUsername = "nonExistingUser";
//        System.out.println("Checking if non-existing user exists with username: " + nonExistingUsername);
//        boolean exists = userRepository.contains(nonExistingUsername);
//
//        System.out.println("Expected: false");
//        System.out.println("Actual: " + exists);
//        assertFalse(exists, "User should not exist");
//
//        System.out.println("✓ contains correctly returns false for non-existing user");
//    }
//
//    @Test
//    @DisplayName("Save should store a new user successfully")
//    void testSave_NewUser_StoresSuccessfully() {
//        System.out.println("TEST: Verifying save stores a new user successfully");
//
//        // Create a new user to save
//        String newUsername = "newUser";
//        String newPassword = "newPassword";
//        String newEmail = "new@example.com";
//        String newFirstName = "New";
//        String newLastName = "User";
//
//        System.out.println("Creating new user with username: " + newUsername);
//        User newUser = new User(newUsername, newPassword, newEmail, newFirstName, newLastName);
//
//        userRepository.save(newUser);
//
//        Optional<User> foundUser = userRepository.findByUsername(newUsername);
//        System.out.println("Expected: User should be found after saving");
//        System.out.println("Actual: User is present = " + foundUser.isPresent());
//        assertTrue(foundUser.isPresent(), "User should be found");
//
//        System.out.println("Expected username: " + newUsername);
//        System.out.println("Actual username: " + foundUser.get().getUserName());
//        assertEquals(newUsername, foundUser.get().getUserName(), "Username should match");
//
//        System.out.println("Expected email: " + newEmail);
//        System.out.println("Actual email: " + foundUser.get().getEmail());
//        assertEquals(newEmail, foundUser.get().getEmail(), "Email should match");
//
//        System.out.println("✓ save correctly stores a new user");
//    }
//
//    @Test
//    @DisplayName("Update should update an existing user successfully")
//    void testUpdate_ExistingUser_UpdatesSuccessfully() {
//        System.out.println("TEST: Verifying update updates an existing user successfully");
//
//        // Modify the test user
//        String updatedEmail = "updated@example.com";
//        String updatedFirstName = "Updated";
//        String updatedLastName = "Name";
//
//        System.out.println("Updating user with username: " + testUsername);
//        testUser.setEmail(updatedEmail);
//        testUser.setFirstName(updatedFirstName);
//        testUser.setLastName(updatedLastName);
//
//        userRepository.update(testUser);
//
//        Optional<User> updatedUser = userRepository.findByUsername(testUsername);
//        System.out.println("Expected: User should be found after updating");
//        System.out.println("Actual: User is present = " + updatedUser.isPresent());
//        assertTrue(updatedUser.isPresent(), "User should be found");
//
//        System.out.println("Expected email: " + updatedEmail);
//        System.out.println("Actual email: " + updatedUser.get().getEmail());
//        assertEquals(updatedEmail, updatedUser.get().getEmail(), "Email should be updated");
//
//        System.out.println("Expected first name: " + updatedFirstName);
//        System.out.println("Actual first name: " + updatedUser.get().getFirstName());
//        assertEquals(updatedFirstName, updatedUser.get().getFirstName(), "First name should be updated");
//
//        System.out.println("✓ update correctly updates an existing user");
//    }
//
//    @Test
//    @DisplayName("Delete should remove an existing user")
//    void testDelete_ExistingUser_DeletesSuccessfully() {
//        System.out.println("TEST: Verifying delete removes an existing user");
//
//        System.out.println("Deleting user with username: " + testUsername);
//        userRepository.delete(testUsername);
//
//        Optional<User> result = userRepository.findByUsername(testUsername);
//        System.out.println("Expected: User should not be present after deletion");
//        System.out.println("Actual: User is present = " + result.isPresent());
//        assertFalse(result.isPresent(), "User should be deleted");
//
//        System.out.println("✓ delete correctly removes the user");
//    }
//
//    @Test
//    @DisplayName("FindAll should return all users")
//    void testFindAll_ReturnsAllUsers() {
//        System.out.println("TEST: Verifying findAll returns all users");
//
//        // Create another user
//        String anotherUsername = "anotherUser";
//        String anotherPassword = "anotherPassword";
//        String anotherEmail = "another@example.com";
//        String anotherFirstName = "Another";
//        String anotherLastName = "User";
//
//        System.out.println("Creating another test user with username: " + anotherUsername);
//        User anotherUser = new User(anotherUsername, anotherPassword, anotherEmail, anotherFirstName, anotherLastName);
//        userRepository.save(anotherUser);
//
//        List<User> users = userRepository.findAll();
//
//        System.out.println("Expected number of users: 2");
//        System.out.println("Actual number of users: " + users.size());
//        assertEquals(2, users.size(), "Should return 2 users");
//
//        boolean containsFirstUser = users.stream().anyMatch(u -> u.getUserName().equals(testUsername));
//        System.out.println("Expected to contain first user: true");
//        System.out.println("Actually contains first user: " + containsFirstUser);
//        assertTrue(containsFirstUser, "Should contain first user");
//
//        boolean containsSecondUser = users.stream().anyMatch(u -> u.getUserName().equals(anotherUsername));
//        System.out.println("Expected to contain second user: true");
//        System.out.println("Actually contains second user: " + containsSecondUser);
//        assertTrue(containsSecondUser, "Should contain second user");
//
//        System.out.println("✓ findAll correctly returns all users");
//    }
//
//    @Test
//    @DisplayName("FindByEmail should return users with matching email")
//    void testFindByEmail_ExistingEmail_ReturnsUsers() {
//        System.out.println("TEST: Verifying findByEmail with existing email");
//
//        System.out.println("Looking for users with email: " + testEmail);
//        List<User> result = userRepository.findByEmail(testEmail);
//
//        System.out.println("Expected: Result should not be empty");
//        System.out.println("Actual: Result is empty = " + result.isEmpty());
//        assertFalse(result.isEmpty(), "Result should not be empty");
//
//        System.out.println("Expected number of users: 1");
//        System.out.println("Actual number of users: " + result.size());
//        assertEquals(1, result.size(), "Should return 1 user");
//
//        System.out.println("Expected username: " + testUsername);
//        System.out.println("Actual username: " + result.get(0).getUserName());
//        assertEquals(testUsername, result.get(0).getUserName(), "Username should match");
//
//        System.out.println("✓ findByEmail correctly returns users by email");
//    }
//
//    @Test
//    @DisplayName("FindByEmail should return empty list for non-existing email")
//    void testFindByEmail_NonExistingEmail_ReturnsEmptyList() {
//        System.out.println("TEST: Verifying findByEmail with non-existing email");
//
//        String nonExistingEmail = "nonexisting@example.com";
//        System.out.println("Looking for users with non-existing email: " + nonExistingEmail);
//        List<User> result = userRepository.findByEmail(nonExistingEmail);
//
//        System.out.println("Expected: Result should be empty");
//        System.out.println("Actual: Result is empty = " + result.isEmpty());
//        assertTrue(result.isEmpty(), "Result should be empty");
//
//        System.out.println("✓ findByEmail correctly returns empty list for non-existing email");
//    }
//
//    @Test
//    @DisplayName("FindActiveUsers should return only logged in users")
//    void testFindActiveUsers_ReturnsActiveUsers() {
//        System.out.println("TEST: Verifying findActiveUsers returns only active users");
//
//        // Login the test user to make it active
//        System.out.println("Logging in test user: " + testUsername);
//        testUser.login(testUsername, testPassword);
//        userRepository.update(testUser);
//
//        // Create another user that is not logged in
//        String inactiveUsername = "inactiveUser";
//        User inactiveUser = new User(inactiveUsername, "password", "inactive@example.com", "Inactive", "User");
//        userRepository.save(inactiveUser);
//        System.out.println("Created inactive user: " + inactiveUsername);
//
//        List<User> activeUsers = userRepository.findActiveUsers();
//
//        System.out.println("Expected number of active users: 1");
//        System.out.println("Actual number of active users: " + activeUsers.size());
//        assertEquals(1, activeUsers.size(), "Should return 1 active user");
//
//        System.out.println("Expected active username: " + testUsername);
//        System.out.println("Actual active username: " + activeUsers.get(0).getUserName());
//        assertEquals(testUsername, activeUsers.get(0).getUserName(), "Active username should match");
//
//        System.out.println("✓ findActiveUsers correctly returns only active users");
//    }
//
//    @Test
//    @DisplayName("Clear should empty the repository")
//    void testClear_ClearsRepository() {
//        System.out.println("TEST: Verifying clear empties the repository");
//
//        // Create another user
//        String anotherUsername = "anotherUser";
//        User anotherUser = new User(anotherUsername, "password", "another@example.com", "Another", "User");
//        userRepository.save(anotherUser);
//        System.out.println("Added another user: " + anotherUsername);
//
//        // Verify users exist before clearing
//        List<User> usersBefore = userRepository.findAll();
//        System.out.println("Number of users before clearing: " + usersBefore.size());
//        assertEquals(2, usersBefore.size(), "Should have 2 users before clearing");
//
//        // Clear the repository
//        System.out.println("Clearing repository");
//        userRepository.clear();
//
//        // Verify repository is empty
//        List<User> usersAfter = userRepository.findAll();
//        System.out.println("Expected number of users after clearing: 0");
//        System.out.println("Actual number of users after clearing: " + usersAfter.size());
//        assertEquals(0, usersAfter.size(), "Repository should be empty after clearing");
//
//        System.out.println("✓ clear correctly empties the repository");
//    }
//}