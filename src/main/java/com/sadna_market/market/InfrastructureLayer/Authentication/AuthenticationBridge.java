package com.sadna_market.market.InfrastructureLayer.Authentication;

public class AuthenticationBridge {
    private TokenService tokenService;
    private IAuthRepository iAuthRepository;
    public AuthenticationBridge(IAuthRepository iAuthRepository) {
        this.iAuthRepository = iAuthRepository;
        this.tokenService = new TokenService();
    }
    public AuthenticationBridge() {
        this.tokenService = new TokenService();
        this.iAuthRepository = new InMemoryAuthRepository();
    }
    public String createUserSessionToken(String username, String password) {
        return authenticate(username,password);
    }
    public String createGuestSessionToken(String username, String password, int guestId) {
        return authenticate(username,password);
    }
    private String authenticate(String username, String password){
        iAuthRepository.login(username,password);
        // If the user is authenticated, generate a JWT token for the user
        return tokenService.generateToken(username);
    }

    //this method returns the username of the user that is associated with the token
    public String checkSessionToken(String jwt) {
        if(!tokenService.validateToken(jwt)){
            throw new IllegalArgumentException("Invalid token");
        }
        else
            return tokenService.extractUsername(jwt);

    }
}
