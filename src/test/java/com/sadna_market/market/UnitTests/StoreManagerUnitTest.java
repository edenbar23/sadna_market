package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StoreManagerUnitTest {

    private StoreManager storeManager;
    private final String managerUsername = "testManager";
    private final String appointerUsername = "appointer";
    private final UUID storeId = UUID.randomUUID();
    private Set<Permission> testPermissions;

    @BeforeEach
    void setUp() {
        storeManager = new StoreManager(managerUsername, storeId, appointerUsername);
        testPermissions = new HashSet<>();
        System.out.println("Test setup: Created StoreManager for user " + managerUsername + " in store " + storeId);
    }

    @AfterEach
    void tearDown() {
        storeManager = null;
        testPermissions = null;
        System.out.println("Test cleanup: Completed");
    }

    @Test
    @DisplayName("Constructor should initialize basic properties correctly")
    void testConstructorInitializesBasicProperties() {
        System.out.println("TEST: Verifying constructor initialization");

        // Verify username
        assertEquals(managerUsername, storeManager.getUsername(),
                "Username should match the one provided in constructor");

        // Verify store ID
        assertEquals(storeId, storeManager.getStoreId(),
                "Store ID should match the one provided in constructor");

        // Verify appointer
        assertEquals(appointerUsername, storeManager.getAppointedBy(),
                "Appointed by should match the one provided in constructor");

        // Verify role type
        assertEquals(RoleType.STORE_MANAGER, storeManager.getRoleType(),
                "Role type should be STORE_MANAGER");

        System.out.println("StoreManager properties correctly initialized");
    }

    @Test
    @DisplayName("StoreManager should have default view permissions")
    void testDefaultPermissions() {
        System.out.println("TEST: Verifying default permissions");

        // Verify view store info permission
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO),
                "StoreManager should have VIEW_STORE_INFO permission by default");

        // Verify view product info permission
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO),
                "StoreManager should have VIEW_PRODUCT_INFO permission by default");

        // Verify another permission that shouldn't be present
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY),
                "StoreManager should not have MANAGE_INVENTORY permission by default");

        System.out.println("Default permissions correctly verified");
    }

    @Test
    @DisplayName("addPermissions should add new permissions")
    void testAddPermissions() {
        System.out.println("TEST: Verifying addPermissions functionality");

        // Create permissions to add
        testPermissions.add(Permission.MANAGE_INVENTORY);
        testPermissions.add(Permission.ADD_PRODUCT);

        // Add permissions
        boolean modified = storeManager.addPermissions(testPermissions);

        // Verify permissions were added
        assertTrue(modified, "addPermissions should return true when permissions are added");
        assertTrue(storeManager.hasPermission(Permission.MANAGE_INVENTORY),
                "StoreManager should have MANAGE_INVENTORY permission after adding");
        assertTrue(storeManager.hasPermission(Permission.ADD_PRODUCT),
                "StoreManager should have ADD_PRODUCT permission after adding");

        System.out.println("Permissions successfully added");
    }

    @Test
    @DisplayName("addPermissions should not add existing permissions")
    void testAddExistingPermissions() {
        System.out.println("TEST: Verifying addPermissions with existing permissions");

        // Add permissions first time
        testPermissions.add(Permission.MANAGE_INVENTORY);
        boolean firstModification = storeManager.addPermissions(testPermissions);
        assertTrue(firstModification, "First permission addition should return true");

        // Try to add same permission again
        boolean secondModification = storeManager.addPermissions(testPermissions);
        assertFalse(secondModification,
                "addPermissions should return false when no new permissions are added");

        System.out.println("Duplicate permissions handling verified");
    }

    @Test
    @DisplayName("addPermissions should return false for null or empty permissions")
    void testAddNullOrEmptyPermissions() {
        System.out.println("TEST: Verifying addPermissions with null or empty permissions");

        // Test with null
        boolean nullResult = storeManager.addPermissions(null);
        assertFalse(nullResult, "addPermissions should return false for null permissions");

        // Test with empty set
        boolean emptyResult = storeManager.addPermissions(new HashSet<>());
        assertFalse(emptyResult, "addPermissions should return false for empty permissions set");

        System.out.println("Null and empty permissions handling verified");
    }

    @Test
    @DisplayName("removePermissions should remove permissions")
    void testRemovePermissions() {
        System.out.println("TEST: Verifying removePermissions functionality");

        // First add permissions
        testPermissions.add(Permission.MANAGE_INVENTORY);
        storeManager.addPermissions(testPermissions);
        assertTrue(storeManager.hasPermission(Permission.MANAGE_INVENTORY),
                "Permission should be added for test setup");

        // Remove permissions
        boolean modified = storeManager.removePermissions(testPermissions);

        // Verify permissions were removed
        assertTrue(modified, "removePermissions should return true when permissions are removed");
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY),
                "StoreManager should not have MANAGE_INVENTORY permission after removal");

        System.out.println("Permissions successfully removed");
    }

    @Test
    @DisplayName("removePermissions should not remove core view permissions")
    void testRemoveCorePermissions() {
        System.out.println("TEST: Verifying removePermissions with core view permissions");

        // Create set with core permissions
        Set<Permission> corePermissions = new HashSet<>();
        corePermissions.add(Permission.VIEW_STORE_INFO);
        corePermissions.add(Permission.VIEW_PRODUCT_INFO);

        // Try to remove core permissions
        boolean modified = storeManager.removePermissions(corePermissions);

        // Verify core permissions were not removed
        assertFalse(modified, "removePermissions should return false when only core permissions are targeted");
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO),
                "VIEW_STORE_INFO permission should not be removable");
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO),
                "VIEW_PRODUCT_INFO permission should not be removable");

        System.out.println("Core permissions protection verified");
    }

    @Test
    @DisplayName("setPermissions should set exact permissions while preserving core permissions")
    void testSetPermissions() {
        System.out.println("TEST: Verifying setPermissions functionality");

        // First add some permissions
        Set<Permission> initialPermissions = new HashSet<>();
        initialPermissions.add(Permission.MANAGE_INVENTORY);
        initialPermissions.add(Permission.ADD_PRODUCT);
        storeManager.addPermissions(initialPermissions);

        // New permissions set (different from initial)
        Set<Permission> newPermissions = new HashSet<>();
        newPermissions.add(Permission.MANAGE_DISCOUNT_POLICY);
        newPermissions.add(Permission.RESPOND_TO_USER_INQUIRIES);

        // Set new permissions
        storeManager.setPermissions(newPermissions);

        // Verify core permissions preserved
        assertTrue(storeManager.hasPermission(Permission.VIEW_STORE_INFO),
                "Core VIEW_STORE_INFO permission should be preserved");
        assertTrue(storeManager.hasPermission(Permission.VIEW_PRODUCT_INFO),
                "Core VIEW_PRODUCT_INFO permission should be preserved");

        // Verify old non-core permissions removed
        assertFalse(storeManager.hasPermission(Permission.MANAGE_INVENTORY),
                "Old MANAGE_INVENTORY permission should be removed");
        assertFalse(storeManager.hasPermission(Permission.ADD_PRODUCT),
                "Old ADD_PRODUCT permission should be removed");

        // Verify new permissions added
        assertTrue(storeManager.hasPermission(Permission.MANAGE_DISCOUNT_POLICY),
                "New MANAGE_DISCOUNT_POLICY permission should be added");
        assertTrue(storeManager.hasPermission(Permission.RESPOND_TO_USER_INQUIRIES),
                "New RESPOND_TO_USER_INQUIRIES permission should be added");

        System.out.println("Permission set operation successfully verified");
    }

    @Test
    @DisplayName("canManageInventory should return true when has permission")
    void testCanManageInventoryWithPermission() {
        System.out.println("TEST: Verifying canManageInventory with permission");

        // Add inventory permission
        testPermissions.add(Permission.MANAGE_INVENTORY);
        storeManager.addPermissions(testPermissions);

        // Check can manage inventory
        boolean canManage = storeManager.canManageInventory();

        assertTrue(canManage, "canManageInventory should return true when manager has MANAGE_INVENTORY permission");

        System.out.println("canManageInventory correctly returned true with permission");
    }

    @Test
    @DisplayName("canManageInventory should return false when missing permission")
    void testCanManageInventoryWithoutPermission() {
        System.out.println("TEST: Verifying canManageInventory without permission");

        // Check can manage inventory (without adding permission)
        boolean canManage = storeManager.canManageInventory();

        assertFalse(canManage, "canManageInventory should return false when manager lacks MANAGE_INVENTORY permission");

        System.out.println("canManageInventory correctly returned false without permission");
    }

    @Test
    @DisplayName("processRoleRemoval should call visitor's processManagerRoleRemoval")
    void testProcessRoleRemoval() {
        System.out.println("TEST: Verifying processRoleRemoval delegates to visitor");

        // Create mock visitor that tracks if it was called
        TestUserRoleVisitor visitor = new TestUserRoleVisitor();
        User user = new User(managerUsername, "password", "email@test.com", "Test", "Manager");

        // Process role removal
        storeManager.processRoleRemoval(visitor, user);

        // Verify visitor was called with correct parameters
        assertTrue(visitor.managerProcessed, "Visitor's processManagerRoleRemoval should be called");
        assertEquals(storeManager, visitor.processedManager, "Correct manager should be passed to visitor");
        assertEquals(storeId, visitor.processedStoreId, "Correct store ID should be passed to visitor");
        assertEquals(user, visitor.processedUser, "Correct user should be passed to visitor");

        System.out.println("processRoleRemoval correctly delegates to visitor");
    }

    // Helper test visitor class
    private static class TestUserRoleVisitor extends UserRoleVisitor {
        boolean managerProcessed = false;
        StoreManager processedManager;
        UUID processedStoreId;
        User processedUser;

        @Override
        public void processManagerRoleRemoval(StoreManager manager, UUID storeId, User user) {
            managerProcessed = true;
            processedManager = manager;
            processedStoreId = storeId;
            processedUser = user;
        }
    }
}