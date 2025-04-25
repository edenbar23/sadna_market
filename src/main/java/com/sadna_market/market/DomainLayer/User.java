package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
    private ArrayList<UUID> ordersHistory; // List of order IDs

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


    public void addToCart(UUID productId, int quantity) {
        logger.info("Adding product {} with quantity {} to cart", productId, quantity);
        cart.addProduct(productId, quantity);
    }

    public HashMap<UUID, OrderDTO> getOrdersHistory() {
        HashMap<UUID, OrderDTO> orders = new HashMap<>();
        for (UUID orderId : ordersHistory) {
            OrderDTO order = new OrderDTO(orderId);
            orders.put(order.getOrderID(), order);
        }
        return orders;
    }

    public void removeFromCart(UUID productId) {
    }

    public void updateCart(UUID productId, int quantity) {
    }

    public void checkout() {
    }

    public void saveReview(UUID storeId, UUID productId, int rate, String review) {
        logger.info("Saving review for product {}: {}", productId, review);
        // Implement the logic to save the review
    }

    public void saveRate(UUID storeId, UUID productId, int rate) {
        logger.info("Saving rate for product {}: {}", productId, rate);
        // Implement the logic to save the rate
    }


    public void sendMessage(UUID storeId, String message) {
        logger.info("Sending message to store {}: {}", storeId, message);
        // Implement the logic to send the message
    }

    public void reportViolation(UUID storeId,UUID productId, String violation) {
        logger.info("Reporting violation for store {}: {}", storeId, violation);
        // Implement the logic to report the violation
    }

    public void updateInfo(String password, String email, String firstName, String lastName) {
        logger.info("Updating user info");
        if (password != null) {
            this.password = password;
        }
        if (email != null) {
            this.email = email;
        }
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
        else {
            logger.error("Exception in updateInfo: object details are null");
            throw new IllegalArgumentException("Last name cannot be null");
        }
        logger.info("User info updated: password={}, email={}, firstName={}, lastName={}", password, email, firstName, lastName);

    }

    public boolean hasPermission(UUID storeId,Permission permission) {
        for (UserStoreRoles role : userStoreRoles) {
            if (role.getStoreId() == storeId && role.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
