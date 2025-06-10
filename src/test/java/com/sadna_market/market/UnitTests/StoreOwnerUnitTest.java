package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StoreOwnerUnitTest {

    private StoreOwner storeOwner;
    private final String ownerUsername = "testOwner";
    private final String appointerUsername = "appointer";
    private final String appointeeUsername = "appointee";
    private final UUID storeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        storeOwner = new StoreOwner(ownerUsername, storeId, appointerUsername);
        System.out.println("Test setup: Created StoreOwner for user " + ownerUsername + " in store " + storeId);
    }

    @AfterEach
    void tearDown() {
        storeOwner = null;
        System.out.println("Test cleanup: Completed");
    }

    @Test
    @DisplayName("Constructor should initialize basic properties correctly")
    void testConstructorInitializesBasicProperties() {
        System.out.println("TEST: Verifying constructor initialization");

        // Verify username
        assertEquals(ownerUsername, storeOwner.getUsername(),
                "Username should match the one provided in constructor");

        // Verify store ID
        assertEquals(storeId, storeOwner.getStoreId(),
                "Store ID should match the one provided in constructor");

        // Verify appointer
        assertEquals(appointerUsername, storeOwner.getAppointedBy(),
                "Appointed by should match the one provided in constructor");

        // Verify role type
        assertEquals(RoleType.STORE_OWNER, storeOwner.getRoleType(),
                "Role type should be STORE_OWNER");

        System.out.println("StoreOwner properties correctly initialized");
    }

    @Test
    @DisplayName("StoreOwner should have appropriate default permissions")
    void testDefaultPermissions() {
        System.out.println("TEST: Verifying default permissions");

        // Verify store management permissions
        assertTrue(storeOwner.hasPermission(Permission.VIEW_STORE_INFO),
                "StoreOwner should have VIEW_STORE_INFO permission");
        assertTrue(storeOwner.hasPermission(Permission.VIEW_PRODUCT_INFO),
                "StoreOwner should have VIEW_PRODUCT_INFO permission");
        assertTrue(storeOwner.hasPermission(Permission.MANAGE_DISCOUNT_POLICY),
                "StoreOwner should have MANAGE_DISCOUNT_POLICY permission");
        assertTrue(storeOwner.hasPermission(Permission.MANAGE_PURCHASE_POLICY),
                "StoreOwner should have MANAGE_PURCHASE_POLICY permission");

        // Verify personnel management permissions
        assertTrue(storeOwner.hasPermission(Permission.APPOINT_STORE_OWNER),
                "StoreOwner should have APPOINT_STORE_OWNER permission");
        assertTrue(storeOwner.hasPermission(Permission.REMOVE_STORE_OWNER),
                "StoreOwner should have REMOVE_STORE_OWNER permission");
        assertTrue(storeOwner.hasPermission(Permission.APPOINT_STORE_MANAGER),
                "StoreOwner should have APPOINT_STORE_MANAGER permission");
        assertTrue(storeOwner.hasPermission(Permission.REMOVE_STORE_MANAGER),
                "StoreOwner should have REMOVE_STORE_MANAGER permission");
        assertTrue(storeOwner.hasPermission(Permission.VIEW_STORE_PURCHASE_HISTORY),
                "StoreOwner should have VIEW_STORE_PURCHASE_HISTORY permission");

        System.out.println("Default permissions correctly verified");
    }

    @Test
    @DisplayName("addPermission should throw exception for StoreOwner")
    void testAddPermissionThrowsException() {
        System.out.println("TEST: Verifying addPermission throws exception");

        // Try to add permission
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storeOwner.addPermission(Permission.MANAGE_INVENTORY),
                "addPermission should throw IllegalStateException for StoreOwner"
        );

        // Verify exception message
        assertTrue(exception.getMessage().contains("fixed and cannot be modified"),
                "Exception message should mention that StoreOwner permissions cannot be modified");

        System.out.println("addPermission correctly rejected with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("removePermission should throw exception for StoreOwner")
    void testRemovePermissionThrowsException() {
        System.out.println("TEST: Verifying removePermission throws exception");

        // Try to remove permission
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storeOwner.removePermission(Permission.VIEW_STORE_INFO),
                "removePermission should throw IllegalStateException for StoreOwner"
        );

        // Verify exception message
        assertTrue(exception.getMessage().contains("fixed and cannot be modified"),
                "Exception message should mention that permissions cannot be modified");

        System.out.println("removePermission correctly rejected with message: " + exception.getMessage());
    }

    @Test
    @DisplayName("addAppointee should add username to appointees list")
    void testAddAppointee() {
        System.out.println("TEST: Verifying addAppointee functionality");

        // Initial state
        List<String> initialAppointees = storeOwner.getAppointees();
        assertTrue(initialAppointees.isEmpty(),
                "Appointees list should be empty initially");

        // Add appointee
        String appointeeName = "newAppointee";
        storeOwner.addAppointee(appointeeName);

        // Verify appointee was added
        List<String> updatedAppointees = storeOwner.getAppointees();
        assertEquals(1, updatedAppointees.size(),
                "Appointees list should have 1 entry after adding");
        assertTrue(updatedAppointees.contains(appointeeName),
                "Appointees list should contain the added username");

        System.out.println("Appointee successfully added");
    }

    @Test
    @DisplayName("isAppointedByUser should return true if username was appointed by this owner")
    void testIsAppointedByUserForUserAppointedByOwner() {
        System.out.println("TEST: Verifying isAppointedByUser for user appointed by this owner");

        // First add appointeeUsername to the appointees list
        storeOwner.addAppointee(appointeeUsername);

        // Now check if isAppointedByUser returns true for appointeeUsername
        boolean result = storeOwner.isAppointedByUser(appointerUsername);

        assertTrue(result,
                "isAppointedByUser should return true for user appointed by this owner: " + appointeeUsername);

        System.out.println("isAppointedByUser correctly identified appointed user");
    }

    @Test
    @DisplayName("isAppointedByUser should return false for user not appointed by this owner")
    void testIsAppointedByUserForUserNotAppointedByOwner() {
        System.out.println("TEST: Verifying isAppointedByUser for user not appointed by this owner");

        String nonAppointedUser = "someoneElse";
        boolean result = storeOwner.isAppointedByUser(nonAppointedUser);

        assertFalse(result,
                "isAppointedByUser should return false for user not appointed by this owner: " + nonAppointedUser);

        System.out.println("isAppointedByUser correctly rejected non-appointed user");
    }

    @Test
    @DisplayName("isAppointedByUser should return false for user not appointed by this owner")
    void testIsAppointedByUserForNonAppointer() {
        System.out.println("TEST: Verifying isAppointedByUser for user not appointed by this owner");

        String nonAppointedUser = "someoneElse";
        boolean result = storeOwner.isAppointedByUser(nonAppointedUser);

        assertFalse(result,
                "isAppointedByUser should return false for user not appointed by this owner: " + nonAppointedUser);

        System.out.println("isAppointedByUser correctly rejected user not appointed by this owner");
    }

    @Test
    @DisplayName("processRoleRemoval should call visitor's processOwnerRoleRemoval")
    void testProcessRoleRemoval() {
        System.out.println("TEST: Verifying processRoleRemoval delegates to visitor");

        // Create mock visitor that tracks if it was called
        TestUserRoleVisitor visitor = new TestUserRoleVisitor();
        User user = new User(ownerUsername, "password", "email@test.com", "Test", "Owner");

        // Process role removal
        storeOwner.processRoleRemoval(visitor, user);

        // Verify visitor was called with correct parameters
        assertTrue(visitor.ownerProcessed, "Visitor's processOwnerRoleRemoval should be called");
        assertEquals(storeOwner, visitor.processedOwner, "Correct owner should be passed to visitor");
        assertEquals(storeId, visitor.processedStoreId, "Correct store ID should be passed to visitor");
        assertEquals(user, visitor.processedUser, "Correct user should be passed to visitor");

        System.out.println("processRoleRemoval correctly delegates to visitor");
    }

    // Helper test visitor class
    private static class TestUserRoleVisitor extends UserRoleVisitor {
        boolean ownerProcessed = false;
        StoreOwner processedOwner;
        UUID processedStoreId;
        User processedUser;

        @Override
        public void processOwnerRoleRemoval(StoreOwner owner, UUID storeId, User user) {
            ownerProcessed = true;
            processedOwner = owner;
            processedStoreId = storeId;
            processedUser = user;
        }
    }
}