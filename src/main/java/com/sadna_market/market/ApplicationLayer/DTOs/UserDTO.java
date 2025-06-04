package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.User;
import lombok.Getter;

public class UserDTO {
    @Getter
    private String userName;
    @Getter
    private String email;
    @Getter
    private String firstName;
    @Getter
    private String lastName;
    @Getter
    private boolean isLoggedIn;
    @Getter
    private int cartItemsCount;
    @Getter
    private boolean isAdmin;

    public UserDTO(User user) {
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.isLoggedIn = user.isLoggedIn();
        this.cartItemsCount = user.getCart().getTotalItems();
        this.isAdmin = user.isAdmin();
    }

    public UserDTO(String userName, String email, String firstName, String lastName, boolean isLoggedIn, int cartItemsCount) {
        this.userName = userName;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = isLoggedIn;
        this.cartItemsCount = cartItemsCount;
        this.isAdmin = false; // Default value, can be set later if needed

    }

    public UserDTO(String userName, String email, String firstName, String lastName, boolean isLoggedIn, int cartItemsCount, boolean isAdmin) {
        this.userName = userName;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = isLoggedIn;
        this.cartItemsCount = cartItemsCount;
        this.isAdmin = isAdmin;
    }
}
