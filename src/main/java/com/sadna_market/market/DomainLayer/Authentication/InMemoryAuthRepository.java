package com.sadna_market.market.DomainLayer.Authentication;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class InMemoryAuthRepository implements IAuthRepository{
    private static HashMap<String,String> username2Password;
    public InMemoryAuthRepository(){
        username2Password = new HashMap<>();
    }
    @Override
    public void login(String username, String password) {
        if(!hasMember(username)){
            throw new NoSuchElementException("User does not exist");
        }
        if(!checkPassword(username,password)){
            throw new IllegalArgumentException("Wrong password");
        }
    }

    @Override
    public HashMap<String, String> getAll() {
        return username2Password;
    }

    @Override
    public void addUser(String username, String password) {
        if(hasMember(username)){
            throw new IllegalArgumentException("User already exists");
        }
        String encryptedPassword = PasswordEncryptor.encryptPassword(password);
        username2Password.put(username,encryptedPassword);
    }

    @Override
    public void updateUserPassword(String username, String oldPassword, String newPassword) {
        if(!hasMember(username)){
            throw new NoSuchElementException("User does not exist");
        }
        if(!checkPassword(username,oldPassword)){
            throw new IllegalArgumentException("Wrong password");
        }
        String encryptedPassword = PasswordEncryptor.encryptPassword(newPassword);
        username2Password.put(username,encryptedPassword);
    }

    @Override
    public void removeUser(String username) {
        if(!hasMember(username)){
            throw new NoSuchElementException("User does not exist");
        }
        username2Password.remove(username);
    }

    @Override
    public void clear() {
        username2Password.clear();
    }

    private synchronized boolean hasMember(String username){
        return username2Password.containsKey(username);
    }

    private boolean checkPassword(String userName,String password){
        return PasswordEncryptor.verifyPassword(password,username2Password.get(userName));
    }
}
