package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor // Required by JPA
public class User extends IUser {
    private static final Logger logger = LogManager.getLogger(User.class);

    @Id
    @Setter
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String userName;

    @Setter
    @Column(name = "password", nullable = true, length = 255)
    private String password;

    @Setter
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Setter
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "is_logged_in", nullable = false)
    private boolean isLoggedIn;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "username")
    private List<UserStoreRoles> userStoreRoles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_orders_history",
            joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "order_id")
    private List<UUID> ordersHistory = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_reports",
            joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "report_id")
    private List<UUID> myReports = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_addresses",
            joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "address_id")
    private List<UUID> addressIds = new ArrayList<>();

    public User(String userName, String password, String email, String firstName, String lastName) {
        super();
        this.userName = userName;
        this.password = null;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = false;
        this.userStoreRoles = new ArrayList<>();
        this.ordersHistory = new ArrayList<>();
        this.myReports = new ArrayList<>();
        this.addressIds = new ArrayList<>();
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
        if (username == null) {
            logger.error("Username cannot be null");
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (!this.userName.equals(username)) {
            logger.error("Invalid username for user {}", userName);
            throw new IllegalArgumentException("Invalid username");
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

    public void addOrderToHistory(UUID orderId) {
        logger.info("Adding order {} to user {} history", orderId, userName);
        ordersHistory.add(orderId);
    }

    public List<UUID> getOrdersHistory() {
        logger.info("Fetching order history for user {}", userName);
        logger.info("Order history: {}", ordersHistory);
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

    public List<UUID> getStoresIds(){
        List<UUID> ids = new ArrayList<>();
        for (UserStoreRoles userRole : userStoreRoles){
            UUID storeId = userRole.getStoreId();
            ids.add(storeId);
        }
        return ids;
    }

    public void addAddressId(UUID addressId) {
        if (addressId != null && !addressIds.contains(addressId)) {
            addressIds.add(addressId);
            logger.info("Address {} added to user {}", addressId, userName);
        }
    }

    public void removeAddressId(UUID addressId) {
        if (addressIds.remove(addressId)) {
            logger.info("Address {} removed from user {}", addressId, userName);
        }
    }

    public List<UUID> getAddressIds() {
        return new ArrayList<>(addressIds);
    }

    public boolean hasAddress(UUID addressId) {
        return addressIds.contains(addressId);
    }
}