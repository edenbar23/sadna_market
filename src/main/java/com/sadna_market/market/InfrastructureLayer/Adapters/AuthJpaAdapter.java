package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.InfrastructureLayer.Authentication.AuthCredential;
import com.sadna_market.market.InfrastructureLayer.Authentication.IAuthRepository;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.AuthCredentialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * JPA implementation of IAuthRepository with encrypted storage
 * Implements the exact same interface as InMemoryAuthRepository
 */
@Repository
@Profile({"dev", "prod", "default", "!test"})
@Primary
@Transactional
public class AuthJpaAdapter implements IAuthRepository {

    private static final Logger logger = LoggerFactory.getLogger(AuthJpaAdapter.class);
    private final AuthCredentialJpaRepository repository;

    @Autowired
    public AuthJpaAdapter(AuthCredentialJpaRepository repository) {
        this.repository = repository;
        logger.info("AuthJpaAdapter initialized");
    }

    @Override
    public void login(String username, String password) {
        if (!hasMember(username)) {
            logger.error("Login failed: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        Optional<AuthCredential> credentialOpt = repository.findByUsername(username);
        AuthCredential credential = credentialOpt.get();

        if (!credential.verifyPassword(password)) {
            logger.error("Login failed: Wrong password for user - {}", username);
            throw new IllegalArgumentException("Wrong password");
        }

        logger.info("Login successful for user: {}", username);
    }

    @Override
    public HashMap<String, String> getAll() {
        List<AuthCredential> credentials = repository.findAll();
        HashMap<String, String> result = new HashMap<>();

        for (AuthCredential credential : credentials) {
            result.put(credential.getUsername(), credential.getEncryptedPassword());
        }

        return result;
    }

    @Override
    public void addUser(String username, String password) {
        if (hasMember(username)) {
            logger.error("Cannot add user: User already exists - {}", username);
            throw new IllegalArgumentException("User already exists");
        }

        AuthCredential credential = new AuthCredential(username, password);
        repository.save(credential);
        logger.info("User added successfully: {}", username);
    }

    @Override
    public void updateUserPassword(String username, String oldPassword, String newPassword) {
        if (!hasMember(username)) {
            logger.error("Cannot update password: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        Optional<AuthCredential> credentialOpt = repository.findByUsername(username);
        AuthCredential credential = credentialOpt.get();

        if (!credential.verifyPassword(oldPassword)) {
            logger.error("Cannot update password: Wrong old password for user - {}", username);
            throw new IllegalArgumentException("Wrong old password");
        }

        credential.updatePassword(newPassword);
        repository.save(credential);
        logger.info("Password updated successfully for user: {}", username);
    }

    @Override
    public void removeUser(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot remove user: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        repository.deleteByUsername(username);
        logger.info("User removed successfully: {}", username);
    }

    @Override
    public void clear() {
        repository.deleteAll();
        logger.info("Auth repository cleared");
    }

    @Override
    public boolean hasMember(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public void saveUserToken(String username, String token) {
        if (!hasMember(username)) {
            logger.error("Cannot save token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        Optional<AuthCredential> credentialOpt = repository.findByUsername(username);
        AuthCredential credential = credentialOpt.get();

        // Use the existing setSessionToken method which handles encryption
        credential.setSessionToken(token);
        credential.setUpdatedAt(java.time.LocalDateTime.now());

        repository.save(credential);
        logger.info("Token saved successfully for user: {}", username);
    }

    @Override
    public String getUserToken(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot get token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        Optional<AuthCredential> credentialOpt = repository.findByUsername(username);
        AuthCredential credential = credentialOpt.get();

        // Use the existing getSessionToken method which handles decryption
        return credential.getSessionToken();
    }

    @Override
    public void clearUserToken(String username) {
        if (!hasMember(username)) {
            logger.error("Cannot clear token: User does not exist - {}", username);
            throw new NoSuchElementException("User does not exist");
        }

        Optional<AuthCredential> credentialOpt = repository.findByUsername(username);
        AuthCredential credential = credentialOpt.get();

        credential.setSessionToken(null);
        credential.setUpdatedAt(java.time.LocalDateTime.now());

        repository.save(credential);
        logger.info("Token cleared successfully for user: {}", username);
    }
}
