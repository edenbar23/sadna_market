package com.sadna_market.market.InfrastructureLayer.Authentication;
import java.util.HashMap;
import java.util.Optional;

public interface IAuthRepository {
    void login(String username, String password);
    HashMap<String, String> getAll();
    void addUser(String username, String password);
    void updateUserPassword(String username, String oldPassword, String newPassword);
    void removeUser(String username);
    void clear();
    boolean hasMember(String username);
    void saveUserToken(String username, String token);
    String getUserToken(String username);
    void clearUserToken(String username);
}