package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserRepository implements IUserRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);

    // Thread-safe map to store users by username
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public InMemoryUserRepository() {
        logger.info("InMemoryUserRepository initialized");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.info("beginning to find user by username: {}", username);
        if (username == null || username.isEmpty()) {
            logger.error("Cannot find user with null or empty username");
            return Optional.empty();
        }

        logger.info("Finding user by username: {}", username);
        Optional<User> user = Optional.ofNullable(users.get(username));
        if (user.isPresent()) {
            logger.info("User found: {}", user.get());
        } else {
            logger.warn("User not found with username: {}", username);
        }
        return user;
    }

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

    @Override
    public List<User> findAll() {
        logger.debug("Getting all users (total: {})", users.size());
        return new ArrayList<>(users.values());
    }

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

    @Override
    public List<User> findByPhoneRole(RoleType role) {
        if (role == null) {
            logger.error("Cannot find users with null role");
            return new ArrayList<>();
        }

        logger.debug("Finding users with role: {}", role);

        // Implementation based on how roles are stored in User
        return users.values().stream()
                .filter(user -> user.getUserStoreRoles().stream()
                        .anyMatch(userRole -> userRole.getRoleType() == role))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findActiveUsers() {
        logger.debug("Finding active users");
        return users.values().stream()
                .filter(User::isLoggedIn)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        users.clear();
        logger.info("User repository cleared");
    }
}