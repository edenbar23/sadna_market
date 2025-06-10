package com.sadna_market.market.InfrastructureLayer.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class TokenService {

    private static final Logger logger = LogManager.getLogger(TokenService.class);

    @Value("${market.jwt.expiration:86400000}")
    private long sessionExpirationTime = 86400000;

    @Value("${market.jwt.secret:MySecretJWTKey1234567890!@#$%^&*()}")
    private String jwtSecret;

    // Thread-safe blacklist
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Getter
    private SecretKey key;

    public TokenService() {
        logger.info("TokenService initialized");
    }

    /**
     * FIXED: Get consistent signing key from configuration (same across restarts)
     */
    private SecretKey getSigningKey() {
        if (key == null) {
            // Create consistent key from secret string
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            // Ensure we have at least 32 bytes for HS256
            byte[] finalKey = new byte[32];

            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, finalKey, 0, 32);
            } else {
                System.arraycopy(keyBytes, 0, finalKey, 0, keyBytes.length);
                // Fill remaining with pattern for consistency
                for (int i = keyBytes.length; i < 32; i++) {
                    finalKey[i] = (byte) (i % 256);
                }
            }

            key = new SecretKeySpec(finalKey, SignatureAlgorithm.HS256.getJcaName());
            logger.info("JWT signing key initialized from configuration");
        }
        return key;
    }

    public String generateToken(String username) {
        logger.info("Generating token for user: {}", username);
        logger.debug("Token expiration time: {} ms", sessionExpirationTime);

        String output = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + sessionExpirationTime))
                .signWith(getSigningKey()) // FIXED: Uses consistent key
                .compact();

        logger.debug("Token generated successfully for user: {}", username);
        return output;
    }

    public boolean validateToken(String token) {
        logger.debug("Validating token");

        // Check if token is blacklisted
        if (blacklistedTokens.contains(token)) {
            logger.debug("Token is blacklisted");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // FIXED: Uses consistent key
                    .build()
                    .parseClaimsJws(token);

            logger.debug("Token is valid");
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
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
                .setSigningKey(getSigningKey()) // FIXED: Uses consistent key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void invalidateToken(String token) {
        if (token != null && !token.isEmpty()) {
            logger.info("Invalidating token");
            blacklistedTokens.add(token);
            logger.debug("Token added to blacklist. Blacklist size: {}", blacklistedTokens.size());
        }
    }

    /**
     * Get current blacklist size (for monitoring)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Clear blacklist (for testing)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
        logger.info("Token blacklist cleared");
    }
}