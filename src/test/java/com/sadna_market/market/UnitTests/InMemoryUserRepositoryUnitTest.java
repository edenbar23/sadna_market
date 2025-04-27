package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.InfrastructureLayer.InMemoryUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;
    private User testUser;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        
        // Set up test data
        username = "testUser";
        password = "Password123!";
        email = "test@example.com";
        firstName = "Test";
        lastName = "User";
        
        // Create a test user
        testUser = new User(username, password, email, firstName, lastName);
    }

    @Test
    void testSaveAndFindByUsername() {
        // Save the user
        repository.save(testUser);
        
        // Find by username
        Optional<User> retrievedUserOpt = repository.findByUsername(username);
        
        // Verify
        assertTrue(retrievedUserOpt.isPresent());
        User retrievedUser = retrievedUserOpt.get();
        assertEquals(username, retrievedUser.getUserName());
        assertEquals(password, retrievedUser.getPassword());
        assertEquals(email, retrievedUser.getEmail());
        assertEquals(firstName, retrievedUser.getFirstName());
        assertEquals(lastName, retrievedUser.getLastName());
    }

    @Test
    void testFindByUsernameNonExistent() {
        // Find non-existent user
        Optional<User> retrievedUserOpt = repository.findByUsername("nonExistentUser");
        
        // Verify
        assertFalse(retrievedUserOpt.isPresent());
    }

    @Test
    void testFindByUsernameNull() {
        // Find with null username
        Optional<User> retrievedUserOpt = repository.findByUsername(null);
        
        // Verify
        assertFalse(retrievedUserOpt.isPresent());
    }

    @Test
    void testFindByUsernameEmpty() {
        // Find with empty username
        Optional<User> retrievedUserOpt = repository.findByUsername("");
        
        // Verify
        assertFalse(retrievedUserOpt.isPresent());
    }

    @Test
    void testContains() {
        // Initially does not contain
        assertFalse(repository.contains(username));
        
        // Save the user
        repository.save(testUser);
        
        // Now should contain
        assertTrue(repository.contains(username));
    }

    @Test
    void testContainsNull() {
        // Contains with null username
        assertFalse(repository.contains(null));
    }

    @Test
    void testContainsEmpty() {
        // Contains with empty username
        assertFalse(repository.contains(""));
    }

    @Test
    void testUpdate() {
        // Save the user
        repository.save(testUser);
        
        // Modify and update
        String newEmail = "updated@example.com";
        testUser.setEmail(newEmail);
        repository.update(testUser);
        
        // Verify the update
        Optional<User> updatedUserOpt = repository.findByUsername(username);
        assertTrue(updatedUserOpt.isPresent());
        assertEquals(newEmail, updatedUserOpt.get().getEmail());
    }

    @Test
    void testUpdateNonExistentUser() {
        // Creating a user that hasn't been saved
        User nonExistentUser = new User("nonExistentUser", password, email, firstName, lastName);
        
        // Trying to update the non-existent user should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.update(nonExistentUser);
        });
        
        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateNullUser() {
        // Update with null user should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.update(null);
        });
        
        String expectedMessage = "User cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateUserWithNullUsername() {
        // Create a user with null username
        User invalidUser = new User(null, password, email, firstName, lastName);
        
        // Update with user having null username should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.update(invalidUser);
        });
        
        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateUserWithEmptyUsername() {
        // Create a user with empty username
        User invalidUser = new User("", password, email, firstName, lastName);
        
        // Update with user having empty username should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.update(invalidUser);
        });
        
        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testDelete() {
        // Save the user
        repository.save(testUser);
        assertTrue(repository.contains(username));
        
        // Delete the user
        repository.delete(username);
        
        // Verify
        assertFalse(repository.contains(username));
    }

    @Test
    void testDeleteNonExistentUser() {
        // Delete non-existent user - should not throw exception
        repository.delete("nonExistentUser");
    }

    @Test
    void testDeleteNullUsername() {
        // Delete with null username - should not throw exception
        repository.delete(null);
    }

    @Test
    void testDeleteEmptyUsername() {
        // Delete with empty username - should not throw exception
        repository.delete("");
    }

    @Test
    void testFindAll() {
        // Initially empty
        List<User> allUsers = repository.findAll();
        assertTrue(allUsers.isEmpty());
        
        // Save some users
        repository.save(testUser);
        
        User anotherUser = new User("anotherUser", password, "another@example.com", firstName, lastName);
        repository.save(anotherUser);
        
        // Find all
        allUsers = repository.findAll();
        
        // Verify
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getUserName().equals(username)));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUserName().equals("anotherUser")));
    }

    @Test
    void testFindByEmail() {
        // Save a user
        repository.save(testUser);
        
        // Find by email
        List<User> usersByEmail = repository.findByEmail(email);
        
        // Verify
        assertEquals(1, usersByEmail.size());
        assertEquals(username, usersByEmail.get(0).getUserName());
        
        // Find by non-existent email
        usersByEmail = repository.findByEmail("nonexistent@example.com");
        
        // Verify
        assertTrue(usersByEmail.isEmpty());
    }

    @Test
    void testFindByEmailNull() {
        // Find by null email
        List<User> usersByEmail = repository.findByEmail(null);
        
        // Verify
        assertTrue(usersByEmail.isEmpty());
    }

    @Test
    void testFindByEmailEmpty() {
        // Find by empty email
        List<User> usersByEmail = repository.findByEmail("");
        
        // Verify
        assertTrue(usersByEmail.isEmpty());
    }

    @Test
    void testFindByPhoneRole() {
        // This method is not fully implemented in InMemoryUserRepository
        // It always returns all users regardless of role
        
        // Save a user
        repository.save(testUser);
        
        // Find by role
        List<User> usersByRole = repository.findByPhoneRole(RoleType.STORE_OWNER);
        
        // In the current implementation, it should return all users
        assertEquals(1, usersByRole.size());
    }

    @Test
    void testFindByPhoneRoleNull() {
        // Find by null role
        List<User> usersByRole = repository.findByPhoneRole(null);
        
        // Verify
        assertTrue(usersByRole.isEmpty());
    }

    @Test
    void testFindActiveUsers() {
        // Save a user
        repository.save(testUser);
        
        // Initially not logged in
        List<User> activeUsers = repository.findActiveUsers();
        assertTrue(activeUsers.isEmpty());
        
        // Log user in
        testUser.login();
        repository.update(testUser);
        
        // Now should be active
        activeUsers = repository.findActiveUsers();
        assertEquals(1, activeUsers.size());
        assertEquals(username, activeUsers.get(0).getUserName());
    }

    @Test
    void testSaveNullUser() {
        // Save null user should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(null);
        });
        
        String expectedMessage = "User cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testSaveUserWithNullUsername() {
        // Create a user with null username
        User invalidUser = new User(null, password, email, firstName, lastName);
        
        // Save with user having null username should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(invalidUser);
        });
        
        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testSaveUserWithEmptyUsername() {
        // Create a user with empty username
        User invalidUser = new User("", password, email, firstName, lastName);
        
        // Save with user having empty username should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(invalidUser);
        });
        
        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testSaveDuplicateUser() {
        // Save a user
        repository.save(testUser);
        
        // Create another user with the same username
        User duplicateUser = new User(username, "DifferentPassword123!", "different@example.com", "Different", "User");
        
        // Saving the duplicate user should overwrite the first one
        repository.save(duplicateUser);
        
        // Verify
        Optional<User> retrievedUserOpt = repository.findByUsername(username);
        assertTrue(retrievedUserOpt.isPresent());
        User retrievedUser = retrievedUserOpt.get();
        assertEquals("different@example.com", retrievedUser.getEmail());
    }
}