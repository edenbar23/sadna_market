package com.sadna_market.market.DomainLayer;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class User extends IUser {
    private static final Logger logger = LogManager.getLogger(User.class);
    @Setter
    @Getter
    private String userName;
    @Setter
    @Getter
    private String password;
    @Setter
    @Getter
    private String email;
    @Setter
    @Getter
    private String firstName;
    @Setter
    @Getter
    private String lastName;
    private boolean isLoggedIn;
    private Cart cart;
    private ArrayList<UserStoreRoles> userStoreRoles; // List of roles in stores
    private ArrayList<UUID> ordersHistory; // List of order IDs
    private ArrayList<UUID> myReports;

    public User(String userName, String password, String email, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = false;
        this.cart = new Cart();
        this.userStoreRoles = new ArrayList<>();
        this.ordersHistory = new ArrayList<>();
        this.myReports = new ArrayList<>();
    }

    @Override
    public synchronized boolean isLoggedIn() {
        return isLoggedIn;
    }

    public synchronized void login(String username,String password) {
        if (isLoggedIn) {
            logger.error("User {} is already logged in", userName);
            throw new IllegalStateException("User is already logged in");
        }
        if (username == null || password == null) {
            logger.error("Username or password cannot be null");
            throw new IllegalArgumentException("Username or password cannot be null");
        }
        if (!this.userName.equals(username) || !this.password.equals(password)) {
            logger.error("Invalid username or password for user {}", userName);
            throw new IllegalArgumentException("Invalid username or password");
        }
        this.isLoggedIn = true;
        logger.info("User {} logged in successfully", userName);
    }

    public synchronized void logout() {
        if (!isLoggedIn) {
            logger.error("User {} is not logged in", userName);
            throw new IllegalStateException("User is not logged in");
        }
        this.isLoggedIn = false;
        logger.info("User {} logged out successfully", userName);
    }

    // Cart operations - delegating to Cart object
    public Cart addToCart(UUID storeId, UUID productId, int quantity) {
        logger.info("User {} adding product {} to cart for store {}", userName, productId, storeId);
        return cart.addToCart(storeId, productId, quantity);
    }

    public Cart removeFromCart(UUID storeId, UUID productId) {
        logger.info("User {} removing product {} from cart for store {}", userName, productId, storeId);
        return cart.removeFromCart(storeId, productId);
    }

    public Cart updateCart(UUID storeId, UUID productId, int quantity) {
        logger.info("User {} updating product {} quantity to {} in cart for store {}",
                userName, productId, quantity, storeId);
        return cart.changeProductQuantity(storeId, productId, quantity);
    }

    public Cart clearCart() {
        logger.info("User {} clearing cart", userName);
        this.cart = new Cart();
        return cart;
    }

    // Order history management
    public void addOrderToHistory(UUID orderId) {
        logger.info("Adding order {} to user {} history", orderId, userName);
        ordersHistory.add(orderId);
    }

    public List<UUID> getOrdersHistory() {
        return new ArrayList<>(ordersHistory);
    }

    // User store roles management
    public void addStoreRole(UserStoreRoles role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        userStoreRoles.add(role);
        logger.info("Added {} role for store {} to user {}",
                role.getRoleType(), role.getStoreId(), userName);
    }

    public void removeStoreRole(UUID storeId, RoleType roleType) {
        userStoreRoles.removeIf(role ->
                role.getStoreId().equals(storeId) && role.getRoleType() == roleType);
        logger.info("Removed {} role for store {} from user {}",
                roleType, storeId, userName);
    }

    public List<UserStoreRoles> getUserStoreRoles() {
        return new ArrayList<>(userStoreRoles);
    }

    public boolean hasPermission(UUID storeId, Permission permission) {
        return userStoreRoles.stream()
                .anyMatch(role -> role.getStoreId().equals(storeId) &&
                        role.hasPermission(permission));
    }

    public List<Permission> getStoreManagerPermissions(UUID storeId) {
        return userStoreRoles.stream()
                .filter(role -> role.getStoreId().equals(storeId) &&
                        role instanceof StoreManager)
                .findFirst()
                .map(UserStoreRoles::getPermissions)
                .orElse(new ArrayList<>());
    }

    public void addReport(UUID reportId) {
        myReports.add(reportId);
    }
}
