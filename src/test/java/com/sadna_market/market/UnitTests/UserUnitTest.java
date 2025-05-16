package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Tests")
public class UserUnitTest {

    private User user;
    private String testUsername;
    private String testPassword;
    private String testEmail;
    private String testFirstName;
    private String testLastName;
    private UUID testStoreId;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");

        testUsername = "testUser";
        testPassword = "testPassword";
        testEmail = "test@example.com";
        testFirstName = "Test";
        testLastName = "User";
        testStoreId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        // Create a test user
        user = new User(testUsername, testPassword, testEmail, testFirstName, testLastName);

        System.out.println("Created test user with username: " + testUsername);
        System.out.println("Test user email: " + testEmail);
        System.out.println("Test user first name: " + testFirstName);
        System.out.println("Test user last name: " + testLastName);
        System.out.println("Test store ID: " + testStoreId);
        System.out.println("Test product ID: " + testProductId);
        System.out.println("===== Setup complete =====");
    }

    // Basic Properties Tests

    @Test
    @DisplayName("User should be created with correct properties")
    void testUserCreation() {
        System.out.println("TEST: Verifying user creation with correct properties");

        assertEquals(testUsername, user.getUserName(), "Username should match");
        assertEquals(testPassword, user.getPassword(), "Password should match");
        assertEquals(testEmail, user.getEmail(), "Email should match");
        assertEquals(testFirstName, user.getFirstName(), "First name should match");
        assertEquals(testLastName, user.getLastName(), "Last name should match");
        assertFalse(user.isLoggedIn(), "User should not be logged in initially");
        assertNotNull(user.getCart(), "User should have a cart");
        assertTrue(user.getOrdersHistory().isEmpty(), "User should have empty order history initially");

        System.out.println("✓ User created with correct properties");
    }

    // Login/Logout Functionality Tests

    @Test
    @DisplayName("User login with correct credentials should succeed")
    void testLogin_CorrectCredentials_Succeeds() {
        System.out.println("TEST: Verifying login with correct credentials");

        assertFalse(user.isLoggedIn(), "User should not be logged in initially");

        user.login(testUsername, testPassword);

        assertTrue(user.isLoggedIn(), "User should be logged in after successful login");

        System.out.println("✓ Login with correct credentials succeeded");
    }

    @Test
    @DisplayName("User login with incorrect username should fail")
    void testLogin_IncorrectUsername_Fails() {
        System.out.println("TEST: Verifying login with incorrect username");

        String wrongUsername = "wrongUsername";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login(wrongUsername, testPassword);
        });

        assertFalse(user.isLoggedIn(), "User should not be logged in after failed login");
        System.out.println("Exception message: " + exception.getMessage());

        System.out.println("✓ Login with incorrect username failed as expected");
    }

    @Test
    @DisplayName("User login with incorrect password should fail")
    void testLogin_IncorrectPassword_Fails() {
        System.out.println("TEST: Verifying login with incorrect password");

        String wrongPassword = "wrongPassword";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login(testUsername, wrongPassword);
        });

        assertFalse(user.isLoggedIn(), "User should not be logged in after failed login");
        System.out.println("Exception message: " + exception.getMessage());

        System.out.println("✓ Login with incorrect password failed as expected");
    }

    @Test
    @DisplayName("Already logged in user attempting to login again should fail")
    void testLogin_AlreadyLoggedIn_Fails() {
        System.out.println("TEST: Verifying login when already logged in");

        // First login
        user.login(testUsername, testPassword);
        assertTrue(user.isLoggedIn(), "User should be logged in after first login");

        // Try to login again
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.login(testUsername, testPassword);
        });

        System.out.println("Exception message: " + exception.getMessage());

        System.out.println("✓ Login when already logged in failed as expected");
    }

    @Test
    @DisplayName("User logout when logged in should succeed")
    void testLogout_WhenLoggedIn_Succeeds() {
        System.out.println("TEST: Verifying logout when logged in");

        // First login
        user.login(testUsername, testPassword);
        assertTrue(user.isLoggedIn(), "User should be logged in after login");

        // Now logout
        user.logout();

        assertFalse(user.isLoggedIn(), "User should not be logged in after logout");

        System.out.println("✓ Logout when logged in succeeded");
    }

    @Test
    @DisplayName("User logout when not logged in should fail")
    void testLogout_WhenNotLoggedIn_Fails() {
        System.out.println("TEST: Verifying logout when not logged in");

        assertFalse(user.isLoggedIn(), "User should not be logged in initially");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.logout();
        });

        System.out.println("Exception message: " + exception.getMessage());

        System.out.println("✓ Logout when not logged in failed as expected");
    }

    // Cart Operations Tests

    @Test
    @DisplayName("Adding product to cart should succeed")
    void testAddToCart_Succeeds() {
        System.out.println("TEST: Verifying adding product to cart");

        int quantity = 5;

        Cart updatedCart = user.addToCart(testStoreId, testProductId, quantity);

        assertNotNull(updatedCart, "Updated cart should not be null");
        assertTrue(updatedCart.getShoppingBaskets().containsKey(testStoreId), "Cart should contain the store");

        int actualQuantity = updatedCart.getShoppingBaskets().get(testStoreId).getProductsList().get(testProductId);
        assertEquals(quantity, actualQuantity, "Product quantity should match");

        System.out.println("✓ Adding product to cart succeeded");
    }

    @Test
    @DisplayName("Removing product from cart should succeed")
    void testRemoveFromCart_Succeeds() {
        System.out.println("TEST: Verifying removing product from cart");

        // First add a product
        int quantity = 5;
        user.addToCart(testStoreId, testProductId, quantity);

        // Now remove it
        Cart updatedCart = user.removeFromCart(testStoreId, testProductId);

        assertNotNull(updatedCart, "Updated cart should not be null");

        // The basket might be removed if it was the only product
        if (updatedCart.getShoppingBaskets().containsKey(testStoreId)) {
            assertFalse(updatedCart.getShoppingBaskets().get(testStoreId).getProductsList().containsKey(testProductId),
                    "Product should be removed from cart");
        }

        System.out.println("✓ Removing product from cart succeeded");
    }

    @Test
    @DisplayName("Updating product quantity in cart should succeed")
    void testUpdateCart_Succeeds() {
        System.out.println("TEST: Verifying updating product quantity in cart");

        // First add a product
        int initialQuantity = 5;
        user.addToCart(testStoreId, testProductId, initialQuantity);

        // Now update quantity
        int newQuantity = 10;
        Cart updatedCart = user.updateCart(testStoreId, testProductId, newQuantity);

        assertNotNull(updatedCart, "Updated cart should not be null");
        assertTrue(updatedCart.getShoppingBaskets().containsKey(testStoreId), "Cart should contain the store");

        int actualQuantity = updatedCart.getShoppingBaskets().get(testStoreId).getProductsList().get(testProductId);
        assertEquals(newQuantity, actualQuantity, "Product quantity should be updated");

        System.out.println("✓ Updating product quantity in cart succeeded");
    }

    @Test
    @DisplayName("Clearing cart should succeed")
    void testClearCart_Succeeds() {
        System.out.println("TEST: Verifying clearing cart");

        // First add some products
        user.addToCart(testStoreId, testProductId, 5);
        user.addToCart(UUID.randomUUID(), UUID.randomUUID(), 3);

        assertFalse(user.getCart().isEmpty(), "Cart should not be empty after adding products");

        // Now clear the cart
        Cart clearedCart = user.clearCart();

        assertNotNull(clearedCart, "Cleared cart should not be null");
        assertTrue(clearedCart.isEmpty(), "Cart should be empty after clearing");

        System.out.println("✓ Clearing cart succeeded");
    }

    // Order History Management Tests

    @Test
    @DisplayName("Adding order to history should succeed")
    void testAddOrderToHistory_Succeeds() {
        System.out.println("TEST: Verifying adding order to history");

        UUID orderId = UUID.randomUUID();

        user.addOrderToHistory(orderId);

        List<UUID> orderHistory = user.getOrdersHistory();
        assertFalse(orderHistory.isEmpty(), "Order history should not be empty after adding an order");
        assertTrue(orderHistory.contains(orderId), "Order history should contain the added order");

        System.out.println("✓ Adding order to history succeeded");
    }

    // Store Role Management Tests

    @Test
    @DisplayName("Adding store role should succeed")
    void testAddStoreRole_Succeeds() {
        System.out.println("TEST: Verifying adding store role");

        // Create a store role
        StoreOwner storeRole = new StoreOwner(testUsername, testStoreId, "appointer");

        user.addStoreRole(storeRole);

        List<UserStoreRoles> userRoles = user.getUserStoreRoles();
        assertFalse(userRoles.isEmpty(), "User roles should not be empty after adding a role");

        UserStoreRoles foundRole = userRoles.stream()
                .filter(role -> role.getStoreId().equals(testStoreId) && role.getRoleType() == RoleType.STORE_OWNER)
                .findFirst()
                .orElse(null);

        assertNotNull(foundRole, "Added role should be found");
        assertEquals(testUsername, foundRole.getUsername(), "Role username should match");

        System.out.println("✓ Adding store role succeeded");
    }

    @Test
    @DisplayName("Removing store role should succeed")
    void testRemoveStoreRole_Succeeds() {
        System.out.println("TEST: Verifying removing store role");

        // First add a store role
        StoreOwner storeRole = new StoreOwner(testUsername, testStoreId, "appointer");
        user.addStoreRole(storeRole);

        List<UserStoreRoles> userRolesBefore = user.getUserStoreRoles();
        assertFalse(userRolesBefore.isEmpty(), "User roles should not be empty after adding a role");

        // Now remove the role
        user.removeStoreRole(testStoreId, RoleType.STORE_OWNER);

        List<UserStoreRoles> userRolesAfter = user.getUserStoreRoles();

        boolean roleFound = userRolesAfter.stream()
                .anyMatch(role -> role.getStoreId().equals(testStoreId) && role.getRoleType() == RoleType.STORE_OWNER);

        assertFalse(roleFound, "Role should be removed");

        System.out.println("✓ Removing store role succeeded");
    }

    @Test
    @DisplayName("Checking permission with existing permission should succeed")
    void testHasPermission_ExistingPermission_ReturnsTrue() {
        System.out.println("TEST: Verifying hasPermission with existing permission");

        // Create a store role with a permission
        StoreOwner storeRole = new StoreOwner(testUsername, testStoreId, "appointer");
        user.addStoreRole(storeRole);

        boolean hasPermission = user.hasPermission(testStoreId, Permission.VIEW_STORE_INFO);

        assertTrue(hasPermission, "User should have the permission");

        System.out.println("✓ hasPermission with existing permission returned true as expected");
    }

    @Test
    @DisplayName("Checking permission with non-existing permission should fail")
    void testHasPermission_NonExistingPermission_ReturnsFalse() {
        System.out.println("TEST: Verifying hasPermission with non-existing permission");

        // Create a store manager role with limited permissions
        StoreManager storeRole = new StoreManager(testUsername, testStoreId, "appointer");
        user.addStoreRole(storeRole);

        // The StoreManager role doesn't have MANAGE_AUCTIONS permission by default
        boolean hasPermission = user.hasPermission(testStoreId, Permission.MANAGE_AUCTIONS);

        assertFalse(hasPermission, "User should not have the permission");

        System.out.println("✓ hasPermission with non-existing permission returned false as expected");
    }

    @Test
    @DisplayName("Getting store manager permissions should succeed")
    void testGetStoreManagerPermissions_Succeeds() {
        System.out.println("TEST: Verifying getStoreManagerPermissions");

        // Create a store manager role
        StoreManager storeRole = new StoreManager(testUsername, testStoreId, "appointer");
        user.addStoreRole(storeRole);

        List<Permission> permissions = user.getStoreManagerPermissions(testStoreId);

        assertNotNull(permissions, "Permissions should not be null");
        assertTrue(permissions.contains(Permission.VIEW_STORE_INFO), "Permissions should contain VIEW_STORE_INFO");
        assertTrue(permissions.contains(Permission.VIEW_PRODUCT_INFO), "Permissions should contain VIEW_PRODUCT_INFO");

        System.out.println("✓ getStoreManagerPermissions succeeded");
    }
}