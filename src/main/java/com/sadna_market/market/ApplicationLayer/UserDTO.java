package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.User;

public class UserDTO {
    private  String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    public UserDTO(String userName, String password, String email, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserDTO(User u) {
        this.userName = u.getUserName();
        this.password = u.getPassword();
        this.email = u.getEmail();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
