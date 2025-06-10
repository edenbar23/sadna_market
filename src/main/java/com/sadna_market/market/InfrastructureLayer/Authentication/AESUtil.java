package com.sadna_market.market.InfrastructureLayer.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple AES encryption utility for tokens
 */
@Component
public class AESUtil {
    private static final Logger logger = LoggerFactory.getLogger(AESUtil.class);

    private static final String ALGORITHM = "AES";
    private static String encryptionKey;

    @Value("${app.encryption.key:MySecretEncryptionKey1234567890!}")
    private String instanceEncryptionKey;

    @jakarta.annotation.PostConstruct
    private void initializeKey() {
        encryptionKey = instanceEncryptionKey;
        logger.info("AESUtil initialized");
    }

    private static SecretKeySpec getSecretKey() {
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[16]; // AES-128

        if (key.length >= 16) {
            System.arraycopy(key, 0, keyBytes, 0, 16);
        } else {
            System.arraycopy(key, 0, keyBytes, 0, key.length);
        }

        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static String encrypt(String plainText) throws Exception {
        if (plainText == null || encryptionKey == null) {
            return plainText;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptionKey == null) {
            return encryptedText;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String safeEncrypt(String plainText) {
        try {
            return encrypt(plainText);
        } catch (Exception e) {
            logger.warn("Encryption failed, returning plain text: {}", e.getMessage());
            return plainText;
        }
    }

    public static String safeDecrypt(String encryptedText) {
        try {
            return decrypt(encryptedText);
        } catch (Exception e) {
            logger.warn("Decryption failed, returning encrypted text: {}", e.getMessage());
            return encryptedText;
        }
    }
}