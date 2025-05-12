package com.sadna_market.market.InfrastructureLayer.Authentication;

import java.sql.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class TokenService {
    private String secret;
    private final long sessionExpirationTime = 1000 * 60 * 60 * 24;
    private static final Logger logger = LogManager.getLogger(TokenService.class);
    // 24 hours
    private SecretKey key;
    // Your existing fields
    private static TokenService instance;

    // Singleton instance

    // Private constructor - can only be called within this class
    private TokenService() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        logger.info("TokenService instance created with key: " + key);
    }

    // Static method to get the singleton instance
    public static synchronized TokenService getInstance() {
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }

    // Constructor that accepts a provided key (for testing)
    public TokenService(SecretKey key) {
        this.key = key;
    }

    // Getter for the key (for testing)
    public SecretKey getKey() {
        return this.key;
    }

    // create a session token for the user with a imported builder
    public String generateToken(String username) {
        logger.info("start-generateToken. args: "+username);
        logger.info("1111 the secret key is: "+key);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + sessionExpirationTime))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        logger.info("Validate Token: "+token);
        logger.info("2222 the secret key is: "+key);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            logger.info("Token is valid");
            return true; }
        catch (Exception e) {
            return false; }
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return (Date) extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody(); }

}