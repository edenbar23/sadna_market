package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;

public class User extends IUser {
    private static final Logger logger = LogManager.getLogger(User.class);
    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isLoggedIn;
    //private HashMap<String,Role> roles;
    private Cart cart;
    private ArrayList<UserStoreRoles> userStoreRoles; // List of roles in stores
    private ArrayList<Integer> ordersHistory; // List of order IDs

    public User(String userName, String password, String email, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = false;
        this.cart = new Cart();
    }

    public User(UserDTO user) {
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.isLoggedIn = false;
        this.cart = new Cart();
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

    public synchronized boolean isLoggedIn() {
        logger.info("Checks isLoggedIn");
        boolean result = isLoggedIn;
        logger.info("Exiting isLoggedIn with result={}", result);
        return result;
    }

    public synchronized void setLoggedIn(boolean loggedIn) {
        logger.info("Setting isLoggedIn to {}", loggedIn);
        this.isLoggedIn = loggedIn;
    }

    public void setLogin(boolean login) {
        logger.info("Setting login to {}", login);
        this.isLoggedIn = login;
        logger.info("Exiting setLogin to {}", login);
    }

    public synchronized void login() {
        logger.info("Entering login");
        if (isLoggedIn) {
            logger.error("Exception in login: user is already logged in");
            throw new IllegalStateException("User is already logged in");
        }
        this.setLogin(true);
        logger.info("Exiting login");
    }

    public synchronized void logout() {
        logger.info("Entering logout");
        if (!isLoggedIn) {
            logger.error("Exception in logout: user isn't logged in");
            throw new IllegalStateException("User is not logged in");
        }
        this.setLogin(false);
        logger.info("Exiting logout");
    }


    public void addToCart(String productId, int quantity) {
        logger.info("Adding product {} with quantity {} to cart", productId, quantity);
        cart.addProduct(productId, quantity);
    }
}
