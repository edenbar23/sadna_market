package com.sadna_market.market.InfrastructureLayer.Authentication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthenticationBridge {
    private TokenService tokenService;
    private IAuthRepository iAuthRepository;

    private final Logger logger = LogManager.getLogger(AuthenticationBridge.class);

    public AuthenticationBridge(IAuthRepository iAuthRepository) {
        this.iAuthRepository = iAuthRepository;
        this.tokenService = new TokenService();
    }

    // Constructor for tests to inject tokenService
    public AuthenticationBridge(IAuthRepository iAuthRepository, TokenService tokenService) {
        this.iAuthRepository = iAuthRepository;
        this.tokenService = tokenService;
    }

    public AuthenticationBridge() {
        this.tokenService = new TokenService();
        this.iAuthRepository = new InMemoryAuthRepository();
    }

    public String createUserSessionToken(String username, String password) {
        return authenticate(username,password);
    }

    public void saveUser(String username, String password) {
        iAuthRepository.addUser(username,password);
    }

    private String authenticate(String username, String password){
        iAuthRepository.login(username,password);
        // If the user is authenticated, generate a JWT token for the user
        return tokenService.generateToken(username);
    }

    //this method returns the username of the user that is associated with the token
    public String checkSessionToken(String jwt) {

        if(!tokenService.validateToken(jwt)){
            logger.info("###invalid token###");
            throw new IllegalArgumentException("Invalid token");
        }
        else {
            logger.info("username extracted from token: " + tokenService.extractUsername(jwt));
            return tokenService.extractUsername(jwt);
        }
    }

    public void validateToken(String username,String jwt) {
        logger.info("Validating token for user: " + username);
        logger.info("extracted username from token: " + checkSessionToken(jwt));

        if(!checkSessionToken(jwt).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    // For testing purposes - get the token service
    public TokenService getTokenService() {
        return this.tokenService;
    }
}