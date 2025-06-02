package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserStoreRoles entities.
 * Defines the contract for data access operations on role objects.
 */
public interface IUserStoreRolesRepository {

    /**
     * Find a role by its unique ID.
     * @param roleId the role ID to search for
     * @return Optional containing the role if found, empty otherwise
     */
    Optional<UserStoreRoles> findById(UUID roleId);

    /**
     * Find all roles for a specific user.
     * @param username the username to search for
     * @return list of roles for the user
     */
    List<UserStoreRoles> findByUsername(String username);

    /**
     * Find all roles for a specific store.
     * @param storeId the store ID to search for
     * @return list of roles for the store
     */
    List<UserStoreRoles> findByStoreId(UUID storeId);

    /**
     * Find a specific role for a user in a store.
     * @param username the username
     * @param storeId the store ID
     * @param roleType the role type
     * @return Optional containing the role if found, empty otherwise
     */
    Optional<UserStoreRoles> findByUsernameAndStoreIdAndRoleType(String username, UUID storeId, RoleType roleType);

    /**
     * Find all roles of a specific type.
     * @param roleType the role type to search for
     * @return list of roles of the specified type
     */
    List<UserStoreRoles> findByRoleType(RoleType roleType);

    /**
     * Find all roles appointed by a specific user.
     * @param appointedBy the username of the appointer
     * @return list of roles appointed by the user
     */
    List<UserStoreRoles> findByAppointedBy(String appointedBy);

    /**
     * Find all users who have been appointed by a specific user.
     * @param appointerUsername the username of the appointer
     * @return list of roles where the appointer has appointed others
     */
    List<UserStoreRoles> findAppointeesOf(String appointerUsername);

    /**
     * Find all store founders.
     * @return list of all store founder roles
     */
    List<UserStoreRoles> findAllFounders();

    /**
     * Find all store owners for a specific store.
     * @param storeId the store ID
     * @return list of store owner roles for the store
     */
    List<UserStoreRoles> findOwnersByStoreId(UUID storeId);

    /**
     * Find all store managers for a specific store.
     * @param storeId the store ID
     * @return list of store manager roles for the store
     */
    List<UserStoreRoles> findManagersByStoreId(UUID storeId);

    /**
     * Find the founder of a specific store.
     * @param storeId the store ID
     * @return Optional containing the founder role if found, empty otherwise
     */
    Optional<UserStoreRoles> findFounderByStoreId(UUID storeId);

    /**
     * Find all roles that have a specific permission.
     * @param permission the permission to search for
     * @return list of roles that have the permission
     */
    List<UserStoreRoles> findByPermission(Permission permission);

    /**
     * Check if a user has a specific role in a store.
     * @param username the username
     * @param storeId the store ID
     * @param roleType the role type
     * @return true if the user has the role, false otherwise
     */
    boolean hasRole(String username, UUID storeId, RoleType roleType);

    /**
     * Check if a user has any role in a store.
     * @param username the username
     * @param storeId the store ID
     * @return true if the user has any role in the store, false otherwise
     */
    boolean hasAnyRoleInStore(String username, UUID storeId);

    /**
     * Save a new role or update an existing one.
     * @param role the role to save
     */
    void save(UserStoreRoles role);

    /**
     * Update an existing role.
     * @param role the role to update
     * @throws IllegalArgumentException if role doesn't exist
     */
    void update(UserStoreRoles role);

    /**
     * Delete a role by its ID.
     * @param roleId the role ID to delete
     */
    void delete(UUID roleId);

    /**
     * Delete all roles for a specific user.
     * @param username the username whose roles to delete
     */
    void deleteByUsername(String username);

    /**
     * Delete all roles for a specific store.
     * @param storeId the store ID whose roles to delete
     */
    void deleteByStoreId(UUID storeId);

    /**
     * Delete a specific role for a user in a store.
     * @param username the username
     * @param storeId the store ID
     * @param roleType the role type to delete
     */
    void deleteByUsernameAndStoreIdAndRoleType(String username, UUID storeId, RoleType roleType);

    /**
     * Find all roles in the system.
     * @return list of all roles
     */
    List<UserStoreRoles> findAll();

    /**
     * Count total number of roles.
     * @return total count of roles
     */
    long count();

    /**
     * Clear all roles from the repository.
     * Used mainly for testing purposes.
     */
    void clear();
}