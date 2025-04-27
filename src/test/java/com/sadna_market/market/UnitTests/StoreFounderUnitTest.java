package com.sadna_market.market.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sadna_market.market.DomainLayer.Permission;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.StoreFounder;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.DomainLayer.UserRoleVisitor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreFounderTest {

    private StoreFounder storeFounder;
    
    private final String USERNAME = "testFounder";
    private final UUID STORE_ID = UUID.randomUUID();
    private final String APPOINTED_BY = null; // Founders don't have an appointer
    
    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeFounder = new StoreFounder(USERNAME, STORE_ID, APPOINTED_BY);
    }

    @Test
    void testConstructor() {
        assertEquals(USERNAME, storeFounder.getUsername());
        assertEquals(STORE_ID, storeFounder.getStoreId());
        assertEquals(APPOINTED_BY, storeFounder.getAppointedBy());
        assertTrue(storeFounder.getAppointees().isEmpty());
    }

    @Test
    void testHasPermission() {
        // StoreFounder should have all permissions
        for (Permission permission : Permission.values()) {
            assertTrue(storeFounder.hasPermission(permission));
        }
    }

    @Test
    void testGetRoleType() {
        assertEquals(RoleType.STORE_FOUNDER, storeFounder.getRoleType());
    }

    @Test
    void testInitializePermissions() {
        // StoreFounder should have at least these permissions initialized explicitly
        assertTrue(storeFounder.hasPermission(Permission.CLOSE_STORE));
        assertTrue(storeFounder.hasPermission(Permission.REOPEN_STORE));
    }

    @Test
    void testToString() {
        assertEquals("store founder", storeFounder.toString());
    }

    @Test
    void testProcessRoleRemoval() {
        UserRoleVisitor mockVisitor = mock(UserRoleVisitor.class);
        
        // Set up mock visitor to throw exception for founder removal
        doThrow(new IllegalStateException("Store founders cannot leave their role"))
            .when(mockVisitor).processFounderRoleRemoval(any(), any(), any());
        
        // Try to process removal - should throw exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            storeFounder.processRoleRemoval(mockVisitor, mockUser);
        });
        
        // Verify exception message
        assertEquals("Store founders cannot leave their role", exception.getMessage());
        
        // Verify the visitor was called with the correct method and parameters
        verify(mockVisitor).processFounderRoleRemoval(storeFounder, STORE_ID, mockUser);
    }
}