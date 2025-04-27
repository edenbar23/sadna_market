package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Permission;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.StoreManager;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.DomainLayer.UserRoleVisitor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreManagerTest {

    private StoreManager storeManager;
    
    private final String USERNAME = "testManager";
    private final UUID STORE_ID = UUID.randomUUID();
    private final String APPOINTED_BY = "appointingOwner";
    
    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeManager = new StoreManager(USERNAME, STORE_ID, APPOINTED_BY);
    }

    @Test
    void testConstructor() {
        assertEquals(USERNAME, storeManager.getUsername());
        assertEquals(STORE_ID, storeManager.getStoreId());
        assertEquals(APPOINTED_BY, storeManager.getAppointedBy());
        assertTrue(storeManager.getAppointees().isEmpty());
    }

    @Test
    void testInitializePermissions() {
        // Store managers should have these base permissions by default
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO));
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO));
        
        // Should not have additional permissions by default
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY));
        assertFalse(storeManager.hasPermission(Permission.MANAGE_DISCOUNT_POLICY));
        assertFalse(storeManager.hasPermission(Permission.APPOINT_STORE_OWNER));
    }

    @Test
    void testGetRoleType() {
        assertEquals(RoleType.STORE_MANAGER, storeManager.getRoleType());
    }

    @Test
    void testAddPermissions() {
        // Create a set of permissions to add
        Set<Permission> permissionsToAdd = EnumSet.of(
            Permission.MANAGE_INVENTORY,
            Permission.MANAGE_DISCOUNT_POLICY
        );
        
        // Add the permissions
        boolean modified = storeManager.addPermissions(permissionsToAdd);
        
        // Verify the result
        assertTrue(modified);
        assertTrue(storeManager.hasPermission(Permission.MANAGE_INVENTORY));
        assertTrue(storeManager.hasPermission(Permission.MANAGE_DISCOUNT_POLICY));
    }

    @Test
    void testAddPermissions_AlreadyExists() {
        // First add some permissions
        Set<Permission> initialPermissions = EnumSet.of(
            Permission.MANAGE_INVENTORY
        );
        storeManager.addPermissions(initialPermissions);
        
        // Try to add the same permission again
        Set<Permission> moreSamePermissions = EnumSet.of(
            Permission.MANAGE_INVENTORY
        );
        boolean modified = storeManager.addPermissions(moreSamePermissions);
        
        // Verify no change was made
        assertFalse(modified);
    }

    @Test
    void testAddPermissions_EmptySet() {
        boolean modified = storeManager.addPermissions(EnumSet.noneOf(Permission.class));
        assertFalse(modified);
    }

    @Test
    void testAddPermissions_NullSet() {
        boolean modified = storeManager.addPermissions(null);
        assertFalse(modified);
    }

    @Test
    void testRemovePermissions() {
        // First add some permissions
        Set<Permission> permissionsToAdd = EnumSet.of(
            Permission.MANAGE_INVENTORY,
            Permission.MANAGE_DISCOUNT_POLICY,
            Permission.RESPOND_TO_USER_INQUIRIES
        );
        storeManager.addPermissions(permissionsToAdd);
        
        // Create a set of permissions to remove
        Set<Permission> permissionsToRemove = EnumSet.of(
            Permission.MANAGE_INVENTORY,
            Permission.MANAGE_DISCOUNT_POLICY
        );
        
        // Remove the permissions
        boolean modified = storeManager.removePermissions(permissionsToRemove);
        
        // Verify the result
        assertTrue(modified);
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY));
        assertFalse(storeManager.hasPermission(Permission.MANAGE_DISCOUNT_POLICY));
        assertTrue(storeManager.hasPermission(Permission.RESPOND_TO_USER_INQUIRIES));
    }

    @Test
    void testRemovePermissions_CorePermissions() {
        // Try to remove core view permissions
        Set<Permission> permissionsToRemove = EnumSet.of(
            Permission.VIEW_STORE_INFO,
            Permission.VIEW_PRODUCT_INFO
        );
        
        // Try to remove the permissions
        boolean modified = storeManager.removePermissions(permissionsToRemove);
        
        // Verify core permissions are still there
        assertFalse(modified);
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO));
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO));
    }

    @Test
    void testRemovePermissions_EmptySet() {
        boolean modified = storeManager.removePermissions(EnumSet.noneOf(Permission.class));
        assertFalse(modified);
    }

    @Test
    void testRemovePermissions_NullSet() {
        boolean modified = storeManager.removePermissions(null);
        assertFalse(modified);
    }

    @Test
    void testSetPermissions() {
        // First add some initial permissions
        Set<Permission> initialPermissions = EnumSet.of(
            Permission.MANAGE_INVENTORY,
            Permission.MANAGE_DISCOUNT_POLICY
        );
        storeManager.addPermissions(initialPermissions);
        
        // Now set a completely different set of permissions
        Set<Permission> newPermissions = EnumSet.of(
            Permission.RESPOND_TO_USER_INQUIRIES,
            Permission.VIEW_STORE_PURCHASE_HISTORY
        );
        storeManager.setPermissions(newPermissions);
        
        // Verify initial permissions are removed
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY));
        assertFalse(storeManager.hasPermission(Permission.MANAGE_DISCOUNT_POLICY));
        
        // Verify new permissions are added
        assertTrue(storeManager.hasPermission(Permission.RESPOND_TO_USER_INQUIRIES));
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_PURCHASE_HISTORY));
        
        // Verify core permissions are still there
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO));
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO));
    }

    @Test
    void testSetPermissions_CorePermissions() {
        // Try to include core permissions in the set
        Set<Permission> newPermissions = EnumSet.of(
            Permission.VIEW_STORE_INFO,
            Permission.VIEW_PRODUCT_INFO,
            Permission.RESPOND_TO_USER_INQUIRIES
        );
        storeManager.setPermissions(newPermissions);
        
        // Verify only the non-core permission was added
        assertTrue(storeManager.hasPermission(Permission.RESPOND_TO_USER_INQUIRIES));
        
        // Core permissions should still be there
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO));
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO));
    }

    @Test
    void testPermissionCheckers() {
        // First add some permissions
        Set<Permission> permissions = EnumSet.of(
            Permission.MANAGE_INVENTORY,
            Permission.MANAGE_DISCOUNT_POLICY,
            Permission.VIEW_STORE_PURCHASE_HISTORY,
            Permission.RESPOND_TO_USER_INQUIRIES
        );
        storeManager.addPermissions(permissions);
        
        // Test permission checkers
        assertTrue(storeManager.canManageInventory());
        assertTrue(storeManager.canManageDiscountPolicy());
        assertFalse(storeManager.canManagePurchasePolicy());
        assertTrue(storeManager.canViewPurchaseHistory());
        assertTrue(storeManager.canRespondToInquiries());
    }

    @Test
    void testProcessRoleRemoval() {
        UserRoleVisitor mockVisitor = mock(UserRoleVisitor.class);
        
        storeManager.processRoleRemoval(mockVisitor, mockUser);
        
        // Verify the visitor was called with the correct method and parameters
        verify(mockVisitor).processManagerRoleRemoval(storeManager, STORE_ID, mockUser);
    }
}