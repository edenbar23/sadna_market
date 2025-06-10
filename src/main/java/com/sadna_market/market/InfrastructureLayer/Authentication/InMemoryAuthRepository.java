package com.sadna_market.market.InfrastructureLayer.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IAuthRepository for testing
 * Only active when 'test' profile is enabled
 */
@Repository
@Profile("test")
public class InMemoryAuthRepository implements IAuthRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAuthRepository.class);
    private final Map<String, String> username2Password = new ConcurrentHashMap<>();
    private final Map<String, String> username2Token = new ConcurrentHashMap<>();


    public InMemoryAuthRepository() {
        logger.info("InMemoryAuthRepository initialized for testing");
    }

    @Override
    public void login(String username, String password) {
        if (!hasMember(username)) {
            logger.error("Login failed: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }
        if (!checkPassword(username, password)) {
            logger.error("Login failed: Wrong password for user - {}", username);
            throw new IllegalArgumentException("Wrong password");
        }

        logger.info("Login successful for user: {}", username);
    }

    @Override
    public HashMap<String, String> getAll() {
        return new HashMap<>(username2Password);
    }

    @Override
    public void addUser(String username, String password) {
        if (hasMember(username)) {
            logger.error("Cannot add user: User already exists - {}", username);
            throw new IllegalArgumentException("User already exists");
        }

        String encryptedPassword = PasswordEncryptor.encryptPassword(password);
        username2Password.put(username, encryptedPassword);
        logger.info("User added successfully: {}", username);
    }

    @Override
    public void updateUserPassword(String username, String oldPassword, String newPassword) {
        if (!hasMember(username)) {
            logger.error("Cannot update password: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }
        if (!checkPassword(username, oldPassword)) {
            logger.error("Cannot update password: Wrong old password for user - {}", username);
            throw new IllegalArgumentException("Wrong old password");
        }

        String encryptedPassword = PasswordEncryptor.encryptPassword(newPassword);
        username2Password.put(username, encryptedPassword);
        logger.info("Password updated successfully for user: {}", username);
    }

    @Override
    public void removeUser(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot remove user: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        username2Password.remove(username);
        logger.info("User removed successfully: {}", username);
    }

    @Override
    public void clear() {
        logger.info("Clearing in-memory auth repository");
        username2Password.clear();
        username2Token.clear();
        logger.info("In-memory auth repository cleared");
    }

    @Override
    public synchronized boolean hasMember(String username) {
        return username2Password.containsKey(username);
    }

    @Override
    public void saveUserToken(String username, String token) {
        if (!hasMember(username)) {
            logger.error("Cannot save token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        // For in-memory, we'll store a simple encrypted version for consistency
        if (token != null) {
            String encryptedToken = "ENC:" + java.util.Base64.getEncoder().encodeToString(token.getBytes());
            username2Token.put(username, encryptedToken);
        } else {
            username2Token.put(username, null);
        }

        logger.info("Token saved successfully for user: {}", username);
    }

    @Override
    public String getUserToken(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot get token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        String encryptedToken = username2Token.get(username);
        if (encryptedToken != null && encryptedToken.startsWith("ENC:")) {
            // Decrypt the token
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(encryptedToken.substring(4));
            return new String(decodedBytes);
        }
        return encryptedToken; // null or unencrypted
    }

    @Override
    public void clearUserToken(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot clear token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        username2Token.remove(username);
        logger.info("Token cleared successfully for user: {}", username);
    }

    private boolean checkPassword(String userName, String password) {
        String storedPassword = username2Password.get(userName);
        if (storedPassword == null) {
            return false;
        }
        return PasswordEncryptor.verifyPassword(password, storedPassword);
    }
}