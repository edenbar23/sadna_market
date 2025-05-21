package com.sadna_market.market.InfrastructureLayer.Authentication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class AuthenticationAdapter {

    private final TokenService tokenService;
    private final IAuthRepository authRepository;
    private final Logger logger = LogManager.getLogger(AuthenticationAdapter.class);

    @Autowired
    public AuthenticationAdapter(IAuthRepository authRepository, TokenService tokenService) {
        this.authRepository = authRepository;
        this.tokenService = tokenService;
        logger.info("AuthenticationBridge initialized");
    }

    public String createUserSessionToken(String username, String password) {
        return authenticate(username, password);
    }

    public void saveUser(String username, String password) {
        logger.info("Saving user: {}", username);
        authRepository.addUser(username, password);
    }

    private String authenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);
        authRepository.login(username, password);
        // If login was successful (no exception thrown), generate a token
        String token = tokenService.generateToken(username);
        logger.info("Authentication successful, token generated for user: {}", username);
        return token;
    }

    public String checkSessionToken(String jwt) {
        if (!tokenService.validateToken(jwt)) {
            logger.info("Invalid token");
            throw new IllegalArgumentException("Invalid token");
        }

        String username = tokenService.extractUsername(jwt);
        logger.info("Username extracted from token: {}", username);
        return username;
    }

    public void validateToken(String username, String jwt) {
        logger.info("Validating token for user: {}", username);
        // Check if the user exists
        if (!authRepository.hasMember(username)) {
            logger.error("User does not exist: {}", username);
            throw new NoSuchElementException("User does not exist");
        }
        // First check if the token is valid
        if (!tokenService.validateToken(jwt)) {
            logger.error("Invalid token");
            throw new IllegalArgumentException("Invalid token");
        }

        // Then check if the token belongs to the specified user
        String tokenUsername = tokenService.extractUsername(jwt);
        logger.info("Username extracted from token: {}", tokenUsername);

        if (!tokenUsername.equals(username)) {
            logger.error("Token does not belong to user: {}", username);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public void logout(String username, String token) {
        logger.info("Logging out user: {}", username);
        // Add token to blacklist to invalidate it
        tokenService.invalidateToken(token);


        // UserAccessService will mark the user as logged out
    }

    public void clear() {
        authRepository.clear();
        logger.info("Authentication bridge cleared");
    }
}