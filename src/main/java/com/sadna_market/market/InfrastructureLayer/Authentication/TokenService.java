package com.sadna_market.market.InfrastructureLayer.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.util.function.Function;

@Component
public class TokenService {

    private static final Logger logger = LogManager.getLogger(TokenService.class);

    // This could be loaded from a configuration file or environment variable
    @Value("${market.jwt.expiration:86400000}") // Default to 24 hours
    private long sessionExpirationTime = 86400000; // 24 hours in milliseconds

    // Getter for the key (for testing)
    // Generate a secure key for signing JWT tokens
    @Getter
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public TokenService() {
        logger.info("TokenService initialized with key");
    }

    public String generateToken(String username) {
        logger.info("Generating token for user: {}", username);
        logger.info("(generating token) encoding key: {}", key);
        String output = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + sessionExpirationTime))
                .signWith(key)
                .compact();
        logger.info("Generated token: {}", output);
        return output;
    }

    public boolean validateToken(String token) {
        logger.info("Token expiration time set to: {} ms", sessionExpirationTime);

        logger.info("Validating token: {}", token);
        logger.info("(validating token) encoding key: {}", key);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            logger.info("Token is valid");
            return true;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}