package com.sadna_market.market.InfrastructureLayer.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAuthRepository implements IAuthRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAuthRepository.class);
    private final Map<String, String> username2Password = new ConcurrentHashMap<>();

    public InMemoryAuthRepository() {
        logger.info("InMemoryAuthRepository initialized");
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
            throw new IllegalArgumentException("Wrong password");
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
        username2Password.clear();
        logger.info("Auth repository cleared");
    }

    public synchronized boolean hasMember(String username) {
        return username2Password.containsKey(username);
    }

    private boolean checkPassword(String userName, String password) {
        return PasswordEncryptor.verifyPassword(password, username2Password.get(userName));
    }


}