package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Permission;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.StoreOwner;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.DomainLayer.UserRoleVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreOwnerTest {

    private StoreOwner storeOwner;
    
    private final String USERNAME = "testOwner";
    private final UUID STORE_ID = UUID.randomUUID();
    private final String APPOINTED_BY = "appointingOwner";
    
    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeOwner = new StoreOwner(USERNAME, STORE_ID, APPOINTED_BY);
    }

    @Test
    void testConstructor() {
        assertEquals(USERNAME, storeOwner.getUsername());
        assertEquals(STORE_ID, storeOwner.getStoreId());
        assertEquals(APPOINTED_BY, storeOwner.getAppointedBy());
        assertTrue(storeOwner.getAppointees().isEmpty());
    }

    @Test
    void testInitializePermissions() {
        // StoreOwner should have all these permissions by default
        assertTrue(storeOwner.hasPermission(Permission.VIEW_STORE_INFO));
        assertTrue(storeOwner.hasPermission(Permission.VIEW_PRODUCT_INFO));
        assertTrue(storeOwner.hasPermission(Permission.MANAGE_DISCOUNT_POLICY));
        assertTrue(storeOwner.hasPermission(Permission.MANAGE_PURCHASE_POLICY));
        assertTrue(storeOwner.hasPermission(Permission.APPOINT_STORE_OWNER));
        assertTrue(storeOwner.hasPermission(Permission.REMOVE_STORE_OWNER));
        assertTrue(storeOwner.hasPermission(Permission.APPOINT_STORE_MANAGER));
        assertTrue(storeOwner.hasPermission(Permission.REMOVE_STORE_MANAGER));
        assertTrue(storeOwner.hasPermission(Permission.VIEW_STORE_PURCHASE_HISTORY));
    }

    @Test
    void testAddPermission() {
        // Should throw exception because store owners already have all permissions
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            storeOwner.addPermission(Permission.MANAGE_AUCTIONS);
        });
        
        String expectedMessage = "Store owner has all the permissions";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testRemovePermission() {
        // Should throw exception because store owners permissions cannot be removed
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            storeOwner.removePermission(Permission.VIEW_STORE_INFO);
        });
        
        String expectedMessage = "Store owner has all the permissions";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testIsAppointedByUser() {
        assertFalse(storeOwner.isAppointedByUser("randomUser"));
        // Add an appointee
        storeOwner.addAppointee("appointee1");
        // Now it should return true
        assertTrue(storeOwner.isAppointedByUser("appointee1"));
    }

    @Test
    void testGetAppointees() {
        assertTrue(storeOwner.getAppointees().isEmpty());
        
        // Add appointees
        storeOwner.addAppointee("appointee1");
        storeOwner.addAppointee("appointee2");
        
        List<String> appointees = storeOwner.getAppointees();
        assertEquals(2, appointees.size());
        assertTrue(appointees.contains("appointee1"));
        assertTrue(appointees.contains("appointee2"));
    }

    @Test
    void testGetAppointedBy() {
        assertEquals(APPOINTED_BY, storeOwner.getAppointedBy());
    }

    @Test
    void testAddAppointee() {
        assertTrue(storeOwner.getAppointees().isEmpty());
        
        storeOwner.addAppointee("appointee1");
        
        List<String> appointees = storeOwner.getAppointees();
        assertEquals(1, appointees.size());
        assertEquals("appointee1", appointees.get(0));
    }

    @Test
    void testGetRoleType() {
        assertEquals(RoleType.STORE_OWNER, storeOwner.getRoleType());
    }

    @Test
    void testProcessRoleRemoval() {
        UserRoleVisitor mockVisitor = mock(UserRoleVisitor.class);
        
        storeOwner.processRoleRemoval(mockVisitor, mockUser);
        
        // Verify the visitor was called with the correct method and parameters
        verify(mockVisitor).processOwnerRoleRemoval(storeOwner, STORE_ID, mockUser);
    }
}