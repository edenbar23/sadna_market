package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.UserStoreRolesJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class UserStoreRolesJpaAdapter implements IUserStoreRolesRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserStoreRolesJpaAdapter.class);

    private final UserStoreRolesJpaRepository jpaRepository;

    // Map RoleType enum to actual entity classes for TYPE() queries
    private static final Map<RoleType, Class<? extends UserStoreRoles>> ROLE_TYPE_TO_CLASS = Map.of(
            RoleType.STORE_MANAGER, StoreManager.class,
            RoleType.STORE_OWNER, StoreOwner.class,
            RoleType.STORE_FOUNDER, StoreFounder.class
    );

    @Autowired
    public UserStoreRolesJpaAdapter(UserStoreRolesJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        logger.info("UserStoreRolesJpaAdapter initialized with JPA repository");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStoreRoles> findById(UUID roleId) {
        logger.debug("Finding role by ID: {}", roleId);
        if (roleId == null) {
            logger.warn("Cannot find role with null ID");
            return Optional.empty();
        }

        try {
            Optional<UserStoreRoles> role = jpaRepository.findById(roleId);
            if (role.isPresent()) {
                logger.debug("Role found: {}", role.get());
                initializeLazyCollections(role.get());
            } else {
                logger.debug("Role not found with ID: {}", roleId);
            }
            return role;
        } catch (Exception e) {
            logger.error("Error finding role by ID: {}", roleId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findByUsername(String username) {
        logger.debug("Finding roles by username: {}", username);
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot find roles with null or empty username");
            return List.of();
        }

        try {
            List<UserStoreRoles> roles = jpaRepository.findByUsername(username);
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} roles for username: {}", roles.size(), username);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding roles by username: {}", username, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findByStoreId(UUID storeId) {
        logger.debug("Finding roles by store ID: {}", storeId);
        if (storeId == null) {
            logger.warn("Cannot find roles with null store ID");
            return List.of();
        }

        try {
            List<UserStoreRoles> roles = jpaRepository.findByStoreId(storeId);
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} roles for store ID: {}", roles.size(), storeId);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding roles by store ID: {}", storeId, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStoreRoles> findByUsernameAndStoreIdAndRoleType(String username, UUID storeId, RoleType roleType) {
        logger.debug("Finding role by username: {}, store ID: {}, role type: {}", username, storeId, roleType);

        if (username == null || username.isEmpty() || storeId == null || roleType == null) {
            logger.warn("Cannot find role with null parameters");
            return Optional.empty();
        }

        // Handle GUEST case - guests don't have UserStoreRoles entities
        if (roleType == RoleType.GUEST) {
            logger.debug("GUEST role type requested - guests don't have stored roles");
            return Optional.empty();
        }

        try {
            Class<? extends UserStoreRoles> roleClass = ROLE_TYPE_TO_CLASS.get(roleType);
            if (roleClass == null) {
                logger.error("Unknown role type: {}", roleType);
                return Optional.empty();
            }

            Optional<UserStoreRoles> role = jpaRepository.findByUsernameAndStoreIdAndRoleType(username, storeId, roleClass);
            role.ifPresent(this::initializeLazyCollections);
            return role;
        } catch (Exception e) {
            logger.error("Error finding role by username, store and type", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findByRoleType(RoleType roleType) {
        logger.debug("Finding roles by role type: {}", roleType);
        if (roleType == null) {
            logger.warn("Cannot find roles with null role type");
            return List.of();
        }

        // Handle GUEST case - guests don't have UserStoreRoles entities
        if (roleType == RoleType.GUEST) {
            logger.debug("GUEST role type requested - guests don't have stored roles");
            return List.of();
        }

        try {
            Class<? extends UserStoreRoles> roleClass = ROLE_TYPE_TO_CLASS.get(roleType);
            if (roleClass == null) {
                logger.error("Unknown role type: {}", roleType);
                return List.of();
            }

            List<UserStoreRoles> roles = jpaRepository.findByRoleType(roleClass);
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} roles of type: {}", roles.size(), roleType);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding roles by type: {}", roleType, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findByAppointedBy(String appointedBy) {
        logger.debug("Finding roles appointed by: {}", appointedBy);
        if (appointedBy == null || appointedBy.isEmpty()) {
            logger.warn("Cannot find roles with null or empty appointedBy");
            return List.of();
        }

        try {
            List<UserStoreRoles> roles = jpaRepository.findByAppointedBy(appointedBy);
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} roles appointed by: {}", roles.size(), appointedBy);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding roles by appointedBy: {}", appointedBy, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findAppointeesOf(String appointerUsername) {
        logger.debug("Finding appointees of: {}", appointerUsername);
        if (appointerUsername == null || appointerUsername.isEmpty()) {
            logger.warn("Cannot find appointees with null or empty appointer username");
            return List.of();
        }

        try {
            List<UserStoreRoles> roles = jpaRepository.findAppointeesOf(appointerUsername);
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} appointees of: {}", roles.size(), appointerUsername);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding appointees of: {}", appointerUsername, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findAllFounders() {
        logger.debug("Finding all store founders");
        try {
            List<UserStoreRoles> founders = jpaRepository.findAllFounders();
            founders.forEach(this::initializeLazyCollections);
            logger.debug("Found {} store founders", founders.size());
            return founders;
        } catch (Exception e) {
            logger.error("Error finding all founders", e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findOwnersByStoreId(UUID storeId) {
        logger.debug("Finding owners by store ID: {}", storeId);
        if (storeId == null) {
            return List.of();
        }

        try {
            List<UserStoreRoles> owners = jpaRepository.findOwnersByStoreId(storeId);
            owners.forEach(this::initializeLazyCollections);
            return owners;
        } catch (Exception e) {
            logger.error("Error finding owners by store ID: {}", storeId, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findManagersByStoreId(UUID storeId) {
        logger.debug("Finding managers by store ID: {}", storeId);
        if (storeId == null) {
            return List.of();
        }

        try {
            List<UserStoreRoles> managers = jpaRepository.findManagersByStoreId(storeId);
            managers.forEach(this::initializeLazyCollections);
            return managers;
        } catch (Exception e) {
            logger.error("Error finding managers by store ID: {}", storeId, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStoreRoles> findFounderByStoreId(UUID storeId) {
        logger.debug("Finding founder by store ID: {}", storeId);
        if (storeId == null) {
            return Optional.empty();
        }

        try {
            Optional<UserStoreRoles> founder = jpaRepository.findFounderByStoreId(storeId);
            founder.ifPresent(this::initializeLazyCollections);
            return founder;
        } catch (Exception e) {
            logger.error("Error finding founder by store ID: {}", storeId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findByPermission(Permission permission) {
        logger.debug("Finding roles by permission: {}", permission);
        if (permission == null) {
            return List.of();
        }

        try {
            List<UserStoreRoles> roles = jpaRepository.findByPermission(permission);
            roles.forEach(this::initializeLazyCollections);
            return roles;
        } catch (Exception e) {
            logger.error("Error finding roles by permission: {}", permission, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(String username, UUID storeId, RoleType roleType) {
        if (username == null || storeId == null || roleType == null) {
            return false;
        }

        // Handle GUEST case - guests don't have UserStoreRoles entities
        if (roleType == RoleType.GUEST) {
            logger.debug("GUEST role type requested - guests don't have stored roles");
            return false;
        }

        try {
            Class<? extends UserStoreRoles> roleClass = ROLE_TYPE_TO_CLASS.get(roleType);
            return roleClass != null && jpaRepository.hasRole(username, storeId, roleClass);
        } catch (Exception e) {
            logger.error("Error checking if user has role", e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyRoleInStore(String username, UUID storeId) {
        if (username == null || storeId == null) {
            return false;
        }

        try {
            return jpaRepository.existsByUsernameAndStoreId(username, storeId);
        } catch (Exception e) {
            logger.error("Error checking if user has any role in store", e);
            return false;
        }
    }

    @Override
    @Transactional
    public void save(UserStoreRoles role) {
        if (role == null) {
            logger.error("Cannot save null role");
            throw new IllegalArgumentException("Role cannot be null");
        }

        try {
            logger.debug("Saving role: {}", role);
            jpaRepository.save(role);
            logger.info("Role saved successfully: {}", role.getRoleId());
        } catch (Exception e) {
            logger.error("Error saving role: {}", role, e);
            throw new RuntimeException("Failed to save role", e);
        }
    }

    @Override
    @Transactional
    public void update(UserStoreRoles role) {
        if (role == null) {
            logger.error("Cannot update null role");
            throw new IllegalArgumentException("Role cannot be null");
        }

        try {
            if (!jpaRepository.existsById(role.getRoleId())) {
                logger.error("Cannot update non-existent role: {}", role.getRoleId());
                throw new IllegalArgumentException("Role does not exist: " + role.getRoleId());
            }

            logger.debug("Updating role: {}", role);
            jpaRepository.save(role);
            logger.info("Role updated successfully: {}", role.getRoleId());
        } catch (Exception e) {
            logger.error("Error updating role: {}", role, e);
            throw new RuntimeException("Failed to update role", e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID roleId) {
        if (roleId == null) {
            logger.error("Cannot delete role with null ID");
            return;
        }

        try {
            logger.debug("Deleting role with ID: {}", roleId);
            jpaRepository.deleteById(roleId);
            logger.info("Role deleted: {}", roleId);
        } catch (Exception e) {
            logger.error("Error deleting role: {}", roleId, e);
            throw new RuntimeException("Failed to delete role: " + roleId, e);
        }
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }

        try {
            logger.debug("Deleting all roles for username: {}", username);
            jpaRepository.deleteByUsername(username);
            logger.info("Deleted all roles for username: {}", username);
        } catch (Exception e) {
            logger.error("Error deleting roles by username: {}", username, e);
            throw new RuntimeException("Failed to delete roles for user: " + username, e);
        }
    }

    @Override
    @Transactional
    public void deleteByStoreId(UUID storeId) {
        if (storeId == null) {
            return;
        }

        try {
            logger.debug("Deleting all roles for store ID: {}", storeId);
            jpaRepository.deleteByStoreId(storeId);
            logger.info("Deleted all roles for store ID: {}", storeId);
        } catch (Exception e) {
            logger.error("Error deleting roles by store ID: {}", storeId, e);
            throw new RuntimeException("Failed to delete roles for store: " + storeId, e);
        }
    }

    @Override
    @Transactional
    public void deleteByUsernameAndStoreIdAndRoleType(String username, UUID storeId, RoleType roleType) {
        if (username == null || storeId == null || roleType == null) {
            return;
        }

        // Handle GUEST case - guests don't have UserStoreRoles entities to delete
        if (roleType == RoleType.GUEST) {
            logger.debug("GUEST role type requested - guests don't have stored roles to delete");
            return;
        }

        try {
            Class<? extends UserStoreRoles> roleClass = ROLE_TYPE_TO_CLASS.get(roleType);
            if (roleClass != null) {
                logger.debug("Deleting role for username: {}, store: {}, type: {}", username, storeId, roleType);
                jpaRepository.deleteByUsernameAndStoreIdAndRoleType(username, storeId, roleClass);
                logger.info("Deleted role for username: {}, store: {}, type: {}", username, storeId, roleType);
            }
        } catch (Exception e) {
            logger.error("Error deleting specific role", e);
            throw new RuntimeException("Failed to delete specific role", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoreRoles> findAll() {
        try {
            logger.debug("Finding all roles");
            List<UserStoreRoles> roles = jpaRepository.findAll();
            roles.forEach(this::initializeLazyCollections);
            logger.debug("Found {} total roles", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("Error finding all roles", e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            return jpaRepository.count();
        } catch (Exception e) {
            logger.error("Error counting roles", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public void clear() {
        try {
            jpaRepository.deleteAll();
            logger.info("Role repository cleared");
        } catch (Exception e) {
            logger.error("Error clearing role repository", e);
            throw new RuntimeException("Failed to clear role repository", e);
        }
    }

    /**
     * Helper method to initialize lazy collections to avoid LazyInitializationException.
     */
    private void initializeLazyCollections(UserStoreRoles role) {
        try {
            // Initialize lazy collections
            role.getAppointees().size();
            role.getPermissions().size();
        } catch (Exception e) {
            logger.warn("Could not initialize lazy collections for role: {}", role.getRoleId(), e);
        }
    }
}