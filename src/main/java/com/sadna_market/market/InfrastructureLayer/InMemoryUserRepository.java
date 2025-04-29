package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the IUserRepository interface.
 * Uses thread-safe collections to store and manage user data without an actual database.
 */
@Repository
public class InMemoryUserRepository implements IUserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);
    private static InMemoryUserRepository instance = new InMemoryUserRepository();
    // Thread-safe map to store users by username
    private final Map<String, User> users = new ConcurrentHashMap<>();

    // Private constructor
    private InMemoryUserRepository() {}

    // Synchronized getInstance method
    public static synchronized InMemoryUserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }

    // Optional: Reset method for testing
    public static synchronized void reset() {
        instance = null;
    }



    /**
     * Finds a user by their username
     * 
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot find user with null or empty username");
            return Optional.empty();
        }
        
        logger.debug("Finding user by username: {}", username);
        return Optional.ofNullable(users.get(username));
    }
    
    /**
     * Checks if a user with the given username exists
     * 
     * @param username The username to check
     * @return true if the user exists
     */
    @Override
    public boolean contains(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot check existence of user with null or empty username");
            return false;
        }
        
        boolean exists = users.containsKey(username);
        logger.debug("Checking if user exists with username {}: {}", username, exists);
        return exists;
    }
    
    /**
     * Saves a user to the repository
     * 
     * @param user The user to save
     * @throws IllegalArgumentException if the user is null or username is empty
     */
    @Override
    public void save(User user) {
        if (user == null) {
            logger.error("Cannot save null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            logger.error("Cannot save user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        logger.debug("Saving user: {}", user.getUserName());
        users.put(user.getUserName(), user);
        logger.info("User saved successfully: {}", user.getUserName());
    }
    
    /**
     * Updates an existing user in the repository
     * 
     * @param user The user to update
     * @throws IllegalArgumentException if the user is null, username is empty, or user doesn't exist
     */
    @Override
    public void update(User user) {
        if (user == null) {
            logger.error("Cannot update null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        String username = user.getUserName();
        if (username == null || username.isEmpty()) {
            logger.error("Cannot update user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (!users.containsKey(username)) {
            logger.error("Cannot update non-existent user: {}", username);
            throw new IllegalArgumentException("User does not exist: " + username);
        }
        
        logger.debug("Updating user: {}", username);
        users.put(username, user);
        logger.info("User updated successfully: {}", username);
    }
    
    /**
     * Deletes a user from the repository
     * 
     * @param username The username of the user to delete
     */
    @Override
    public void delete(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot delete user with null or empty username");
            return;
        }
        
        logger.debug("Deleting user with username: {}", username);
        users.remove(username);
        logger.info("User deleted: {}", username);
    }
    
    /**
     * Gets all users in the repository
     * 
     * @return List of all users
     */
    @Override
    public List<User> findAll() {
        logger.debug("Getting all users (total: {})", users.size());
        return new ArrayList<>(users.values());
    }
    
    /**
     * Finds users by email address
     * 
     * @param email The email address to search for
     * @return List of users with the matching email
     */
    @Override
    public List<User> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.error("Cannot find users with null or empty email");
            return new ArrayList<>();
        }
        
        logger.debug("Finding users with email: {}", email);
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .collect(Collectors.toList());
    }
    
    /**
     * Finds users by role type
     * 
     * @param role The role type to search for
     * @return List of users with the matching role
     */
    @Override
    public List<User> findByPhoneRole(RoleType role) {
        if (role == null) {
            logger.error("Cannot find users with null role");
            return new ArrayList<>();
        }
        
        logger.debug("Finding users with role: {}", role);
        
        // Note: This implementation is simplified and may need to be adjusted
        // based on how roles are actually stored and accessed in the User class
        return users.values().stream()
                .filter(user -> {
                    // This would need to be implemented based on how roles are stored in User
                    // For now, return all users as a placeholder
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Finds active users (currently logged in)
     * 
     * @return List of active users
     */
    @Override
    public List<User> findActiveUsers() {
        logger.debug("Finding active users");
        return users.values().stream()
                .filter(User::isLoggedIn)
                .collect(Collectors.toList());
    }
}