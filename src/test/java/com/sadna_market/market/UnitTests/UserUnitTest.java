package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Cart;
import com.sadna_market.market.DomainLayer.Permission;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.StoreManager;
import com.sadna_market.market.DomainLayer.StoreOwner;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.DomainLayer.UserStoreRoles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    private User user;
    private final String USERNAME = "testUser";
    private final String PASSWORD = "Password123!";
    private final String EMAIL = "test@example.com";
    private final String FIRST_NAME = "Test";
    private final String LAST_NAME = "User";

    @Mock
    private Cart mockCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(USERNAME, PASSWORD, EMAIL, FIRST_NAME, LAST_NAME);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(USERNAME, user.getUserName());
        assertEquals(PASSWORD, user.getPassword());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testSetters() {
        String newPassword = "NewPassword456!";
        String newEmail = "new@example.com";
        String newFirstName = "NewFirst";
        String newLastName = "NewLast";

        user.setPassword(newPassword);
        user.setEmail(newEmail);
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);

        assertEquals(newPassword, user.getPassword());
        assertEquals(newEmail, user.getEmail());
        assertEquals(newFirstName, user.getFirstName());
        assertEquals(newLastName, user.getLastName());
    }

    @Test
    void testLoginLogout() {
        assertFalse(user.isLoggedIn());
        
        // Login with correct credentials
        user.login(USERNAME, PASSWORD);
        assertTrue(user.isLoggedIn());
        
        // Logout
        user.logout();
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testLoginWhenAlreadyLoggedIn() {
        // First login
        user.login(USERNAME, PASSWORD);
        assertTrue(user.isLoggedIn());
        
        // Try to login again
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.login(USERNAME, PASSWORD);
        });
        
        String expectedMessage = "User is already logged in";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testLoginWithNullCredentials() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login(null, PASSWORD);
        });
        
        String expectedMessage = "Username or password cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login(USERNAME, null);
        });
        
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testLoginWithInvalidCredentials() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login("wrongUsername", PASSWORD);
        });
        
        String expectedMessage = "Invalid username or password";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            user.login(USERNAME, "wrongPassword");
        });
        
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testLogoutWhenNotLoggedIn() {
        assertFalse(user.isLoggedIn());
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.logout();
        });
        
        String expectedMessage = "User is not logged in";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCartOperations() {
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 2;
        
        // Add to cart
        Cart returnedCart1 = user.addToCart(storeId, productId, quantity);
        assertNotNull(returnedCart1);
        
        // Update cart
        Cart returnedCart2 = user.updateCart(storeId, productId, quantity + 1);
        assertNotNull(returnedCart2);
        
        // Remove from cart
        Cart returnedCart3 = user.removeFromCart(storeId, productId);
        assertNotNull(returnedCart3);
        
        // Clear cart
        Cart returnedCart4 = user.clearCart();
        assertNotNull(returnedCart4);
        assertTrue(returnedCart4.isEmpty());
    }

    @Test
    void testOrderHistory() {
        // Initialize ordersHistory via reflection since it's not initialized in constructor
        try {
            java.lang.reflect.Field ordersHistoryField = User.class.getDeclaredField("ordersHistory");
            ordersHistoryField.setAccessible(true);
            ordersHistoryField.set(user, new ArrayList<UUID>());
        } catch (Exception e) {
            fail("Failed to initialize ordersHistory: " + e.getMessage());
        }
        
        // Add some orders
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        
        user.addOrderToHistory(orderId1);
        user.addOrderToHistory(orderId2);
        
        // Get order history
        List<UUID> orderHistory = user.getOrdersHistory();
        
        // Verify
        assertEquals(2, orderHistory.size());
        assertTrue(orderHistory.contains(orderId1));
        assertTrue(orderHistory.contains(orderId2));
    }

    @Test
    void testUserStoreRoles() {
        // Initialize userStoreRoles via reflection since it's not initialized in constructor
        try {
            java.lang.reflect.Field userStoreRolesField = User.class.getDeclaredField("userStoreRoles");
            userStoreRolesField.setAccessible(true);
            userStoreRolesField.set(user, new ArrayList<UserStoreRoles>());
        } catch (Exception e) {
            fail("Failed to initialize userStoreRoles: " + e.getMessage());
        }
        
        UUID storeId = UUID.randomUUID();
        UserStoreRoles mockRole = mock(StoreOwner.class);
        when(mockRole.getStoreId()).thenReturn(storeId);
        when(mockRole.getRoleType()).thenReturn(RoleType.STORE_OWNER);

        user.addStoreRole(mockRole);
        
        List<UserStoreRoles> roles = user.getUserStoreRoles();
        assertEquals(1, roles.size());
        assertEquals(mockRole, roles.get(0));
        
        user.removeStoreRole(storeId, RoleType.STORE_OWNER);
        
        roles = user.getUserStoreRoles();
        assertEquals(0, roles.size());
    }

    @Test
    void testAddNullStoreRole() {
        // Initialize userStoreRoles via reflection
        try {
            java.lang.reflect.Field userStoreRolesField = User.class.getDeclaredField("userStoreRoles");
            userStoreRolesField.setAccessible(true);
            userStoreRolesField.set(user, new ArrayList<UserStoreRoles>());
        } catch (Exception e) {
            fail("Failed to initialize userStoreRoles: " + e.getMessage());
        }
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addStoreRole(null);
        });
        
        String expectedMessage = "Role cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testHasPermission() {
        // Initialize userStoreRoles via reflection
        try {
            java.lang.reflect.Field userStoreRolesField = User.class.getDeclaredField("userStoreRoles");
            userStoreRolesField.setAccessible(true);
            userStoreRolesField.set(user, new ArrayList<UserStoreRoles>());
        } catch (Exception e) {
            fail("Failed to initialize userStoreRoles: " + e.getMessage());
        }
        
        UUID storeId = UUID.randomUUID();
        UserStoreRoles mockRole = mock(StoreOwner.class);
        
        when(mockRole.getStoreId()).thenReturn(storeId);
        when(mockRole.getRoleType()).thenReturn(RoleType.STORE_OWNER);
        when(mockRole.hasPermission(Permission.VIEW_STORE_INFO)).thenReturn(true);
        when(mockRole.hasPermission(Permission.CLOSE_STORE)).thenReturn(false);
        
        user.addStoreRole(mockRole);
        
        assertTrue(user.hasPermission(storeId, Permission.VIEW_STORE_INFO));
        assertFalse(user.hasPermission(storeId, Permission.CLOSE_STORE));
        assertFalse(user.hasPermission(UUID.randomUUID(), Permission.VIEW_STORE_INFO));
    }

    @Test
    void testGetStoreManagerPermissions() {
        // Initialize userStoreRoles via reflection
        try {
            java.lang.reflect.Field userStoreRolesField = User.class.getDeclaredField("userStoreRoles");
            userStoreRolesField.setAccessible(true);
            userStoreRolesField.set(user, new ArrayList<UserStoreRoles>());
        } catch (Exception e) {
            fail("Failed to initialize userStoreRoles: " + e.getMessage());
        }
        
        UUID storeId = UUID.randomUUID();
        StoreManager mockManager = mock(StoreManager.class);
        List<Permission> permissions = Arrays.asList(
            Permission.VIEW_STORE_INFO,
            Permission.VIEW_PRODUCT_INFO
        );
        
        when(mockManager.getStoreId()).thenReturn(storeId);
        when(mockManager.getRoleType()).thenReturn(RoleType.STORE_MANAGER);
        when(mockManager.getPermissions()).thenReturn(permissions);
        
        user.addStoreRole(mockManager);
        
        List<Permission> retrievedPermissions = user.getStoreManagerPermissions(storeId);
        assertEquals(2, retrievedPermissions.size());
        assertEquals(Permission.VIEW_STORE_INFO, retrievedPermissions.get(0));
        assertEquals(Permission.VIEW_PRODUCT_INFO, retrievedPermissions.get(1));
    }

    @Test
    void testGetStoreManagerPermissionsForNonExistingStore() {
        // Initialize userStoreRoles via reflection
        try {
            java.lang.reflect.Field userStoreRolesField = User.class.getDeclaredField("userStoreRoles");
            userStoreRolesField.setAccessible(true);
            userStoreRolesField.set(user, new ArrayList<UserStoreRoles>());
        } catch (Exception e) {
            fail("Failed to initialize userStoreRoles: " + e.getMessage());
        }
        
        List<Permission> permissions = user.getStoreManagerPermissions(UUID.randomUUID());
        assertTrue(permissions.isEmpty());
    }
}