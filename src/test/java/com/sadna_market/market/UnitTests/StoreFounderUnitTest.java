package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StoreFounderUnitTest {

    private StoreFounder storeFounder;
    private final String founderUsername = "testFounder";
    private final UUID storeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        storeFounder = new StoreFounder(founderUsername, storeId, null);
        System.out.println("Test setup: Created StoreFounder for user " + founderUsername + " in store " + storeId);
    }

    @AfterEach
    void tearDown() {
        storeFounder = null;
        System.out.println("Test cleanup: Completed");
    }

    @Test
    @DisplayName("Constructor should initialize basic properties correctly")
    void testConstructorInitializesBasicProperties() {
        System.out.println("TEST: Verifying constructor initialization");

        // Verify username
        assertEquals(founderUsername, storeFounder.getUsername(),
                "Username should match the one provided in constructor");

        // Verify store ID
        assertEquals(storeId, storeFounder.getStoreId(),
                "Store ID should match the one provided in constructor");

        // Verify appointer is null for founder
        assertNull(storeFounder.getAppointedBy(),
                "Appointed by should be null for store founder");

        // Verify role type
        assertEquals(RoleType.STORE_FOUNDER, storeFounder.getRoleType(),
                "Role type should be STORE_FOUNDER");

        System.out.println("StoreFounder properties correctly initialized");
    }

    @Test
    @DisplayName("StoreFounder should have store opening/closing permissions")
    void testDefaultPermissions() {
        System.out.println("TEST: Verifying default permissions");

        // Verify close store permission
        assertTrue(storeFounder.hasPermission(Permission.CLOSE_STORE),
                "StoreFounder should have CLOSE_STORE permission by default");

        // Verify reopen store permission
        assertTrue(storeFounder.hasPermission(Permission.REOPEN_STORE),
                "StoreFounder should have REOPEN_STORE permission by default");

        System.out.println("Default permissions correctly verified");
    }

    @Test
    @DisplayName("StoreFounder should have all permissions regardless of explicit assignment")
    void testHasAllPermissions() {
        System.out.println("TEST: Verifying StoreFounder has all permissions");

        // Check all permission types
        for (Permission permission : Permission.values()) {
            assertTrue(storeFounder.hasPermission(permission),
                    "StoreFounder should have " + permission + " permission");
        }

        System.out.println("All permissions verified to be available");
    }

    @Test
    @DisplayName("addPermission should log warning but not throw exception for StoreFounder")
    void testAddPermissionWarnsButDoesNotThrow() {
        System.out.println("TEST: Verifying addPermission logs warning but doesn't throw");

        // Test that no exception is thrown
        assertDoesNotThrow(
                () -> storeFounder.addPermission(Permission.MANAGE_INVENTORY),
                "addPermission should not throw an exception for StoreFounder"
        );

        // We can't directly test the logging in this unit test,
        // but we can verify permissions are still comprehensive
        for (Permission permission : Permission.values()) {
            assertTrue(storeFounder.hasPermission(permission),
                    "StoreFounder should have " + permission + " permission after attempting to add a permission");
        }

        System.out.println("addPermission correctly handles permission addition attempts");
    }

    @Test
    @DisplayName("removePermission should throw exception for StoreFounder")
    void testRemovePermissionThrowsException() {
        System.out.println("TEST: Verifying removePermission throws exception");

        // Try to remove permission
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storeFounder.removePermission(Permission.CLOSE_STORE),
                "removePermission should throw IllegalStateException for StoreFounder"
        );

        // Verify exception message
        assertTrue(exception.getMessage().contains("can't remove"),
                "Exception message should mention that permissions can't be removed");

        System.out.println("removePermission correctly rejected with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("processRoleRemoval should call visitor's processFounderRoleRemoval")
    void testProcessRoleRemoval() {
        System.out.println("TEST: Verifying processRoleRemoval delegates to visitor");

        // Create mock visitor that tracks if it was called
        TestUserRoleVisitor visitor = new TestUserRoleVisitor();
        User user = new User(founderUsername, "password", "email@test.com", "Test", "Founder");

        // Process role removal
        storeFounder.processRoleRemoval(visitor, user);

        // Verify visitor was called with correct parameters
        assertTrue(visitor.founderProcessed, "Visitor's processFounderRoleRemoval should be called");
        assertEquals(storeFounder, visitor.processedFounder, "Correct founder should be passed to visitor");
        assertEquals(storeId, visitor.processedStoreId, "Correct store ID should be passed to visitor");
        assertEquals(user, visitor.processedUser, "Correct user should be passed to visitor");

        System.out.println("processRoleRemoval correctly delegates to visitor");
    }

    @Test
    @DisplayName("toString should identify as store founder")
    void testToString() {
        System.out.println("TEST: Verifying toString method");

        String result = storeFounder.toString();

        // Verify toString includes "store founder" text
        assertTrue(result.contains("store founder"),
                "toString should identify object as 'store founder'");

        System.out.println("toString correctly identifies as store founder: " + result);
    }

    // Helper test visitor class
    private static class TestUserRoleVisitor extends UserRoleVisitor {
        boolean founderProcessed = false;
        StoreFounder processedFounder;
        UUID processedStoreId;
        User processedUser;

        @Override
        public void processFounderRoleRemoval(StoreFounder founder, UUID storeId, User user) {
            founderProcessed = true;
            processedFounder = founder;
            processedStoreId = storeId;
            processedUser = user;
        }
    }
}