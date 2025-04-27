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
import com.sadna_market.market.DomainLayer.UserDTO;
import com.sadna_market.market.DomainLayer.UserStoreRoles;

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
        // For tests that need to override the cart
        // user.setCart(mockCart);
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
    void testConstructorFromUserDTO() {
        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD, EMAIL, FIRST_NAME, LAST_NAME);
        User userFromDTO = new User(userDTO);

        assertEquals(USERNAME, userFromDTO.getUserName());
        assertEquals(PASSWORD, userFromDTO.getPassword());
        assertEquals(EMAIL, userFromDTO.getEmail());
        assertEquals(FIRST_NAME, userFromDTO.getFirstName());
        assertEquals(LAST_NAME, userFromDTO.getLastName());
        assertFalse(userFromDTO.isLoggedIn());
    }

    @Test
    void testLoginLogout() {
        assertFalse(user.isLoggedIn());
        
        user.login();
        assertTrue(user.isLoggedIn());
        
        user.logout();
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testLoginWhenAlreadyLoggedIn() {
        user.login();
        assertTrue(user.isLoggedIn());
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.login();
        });
        
        String expectedMessage = "User is already logged in";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
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
    void testUpdateInfo() {
        String newPassword = "NewPassword789!";
        String newEmail = "updated@example.com";
        String newFirstName = "UpdatedFirst";
        String newLastName = "UpdatedLast";

        user.updateInfo(newPassword, newEmail, newFirstName, newLastName);

        assertEquals(newPassword, user.getPassword());
        assertEquals(newEmail, user.getEmail());
        assertEquals(newFirstName, user.getFirstName());
        assertEquals(newLastName, user.getLastName());
    }

    @Test
    void testUpdateInfoWithNullLastName() {
        String newPassword = "NewPassword789!";
        String newEmail = "updated@example.com";
        String newFirstName = "UpdatedFirst";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.updateInfo(newPassword, newEmail, newFirstName, null);
        });
        
        String expectedMessage = "Last name cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testAddAndRemoveStoreRole() {
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
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addStoreRole(null);
        });
        
        String expectedMessage = "Role cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testHasPermission() {
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
        List<Permission> permissions = user.getStoreManagerPermissions(UUID.randomUUID());
        assertTrue(permissions.isEmpty());
    }

    @Test
    void testAddProductToCart() {
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 2;

        // Create a spy to monitor the real cart
        Cart spy = spy(new Cart());
        // Inject the spy cart using reflection
        try {
            java.lang.reflect.Field cartField = User.class.getDeclaredField("cart");
            cartField.setAccessible(true);
            cartField.set(user, spy);
        } catch (Exception e) {
            fail("Failed to inject spy cart: " + e.getMessage());
        }

        user.addProductToCart(storeId, productId, quantity);
        
        verify(spy).addToCart(storeId, productId, quantity);
    }
}