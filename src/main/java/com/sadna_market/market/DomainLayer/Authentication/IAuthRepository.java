package com.sadna_market.market.DomainLayer.Authentication;
import java.util.HashMap;

// This interface defines the methods for the authentication repository.
public interface IAuthRepository {
    void login(String username,String password);
    HashMap<String,String> getAll();
    void addUser(String username,String password);
    void updateUserPassword(String username, String oldPassword, String newPassword);
    void removeUser(String username);
    void clear();
}