package com.sadna_market.market.DomainLayer.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;


// this class is used only to encrypt & verify passwords functions
public class PasswordEncryptor {
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String encryptedPassword) {
        return BCrypt.checkpw(password, encryptedPassword);
    }
}
