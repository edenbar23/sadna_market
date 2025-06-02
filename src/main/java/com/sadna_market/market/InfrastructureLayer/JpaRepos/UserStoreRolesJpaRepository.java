package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.UserStoreRoles;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.DomainLayer.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStoreRolesJpaRepository extends JpaRepository<UserStoreRoles, UUID> {

    /**
     * Find all roles for a specific user.
     */
    List<UserStoreRoles> findByUsername(String username);

    /**
     * Find all roles for a specific store.
     */
    List<UserStoreRoles> findByStoreId(UUID storeId);

    /**
     * Find a specific role for a user in a store.
     */
    Optional<UserStoreRoles> findByUsernameAndStoreId(String username, UUID storeId);

    /**
     * Find all roles of a specific type.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE TYPE(r) = :roleClass")
    List<UserStoreRoles> findByRoleType(@Param("roleClass") Class<? extends UserStoreRoles> roleClass);

    /**
     * Alternative method using discriminator value directly.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.class = :roleType")
    List<UserStoreRoles> findByDiscriminatorValue(@Param("roleType") String roleType);

    /**
     * Find all roles appointed by a specific user.
     */
    List<UserStoreRoles> findByAppointedBy(String appointedBy);

    /**
     * Find all users who have been appointed by a specific user.
     * This finds roles where the appointer has others in their appointees list.
     */
    @Query("SELECT r FROM UserStoreRoles r JOIN r.appointees a WHERE a = :appointerUsername")
    List<UserStoreRoles> findAppointeesOf(@Param("appointerUsername") String appointerUsername);

    /**
     * Find the founder of a specific store.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.storeId = :storeId AND TYPE(r) = StoreFounder")
    Optional<UserStoreRoles> findFounderByStoreId(@Param("storeId") UUID storeId);

    /**
     * Find all store owners for a specific store.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.storeId = :storeId AND TYPE(r) = StoreOwner")
    List<UserStoreRoles> findOwnersByStoreId(@Param("storeId") UUID storeId);

    /**
     * Find all store managers for a specific store.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.storeId = :storeId AND TYPE(r) = StoreManager")
    List<UserStoreRoles> findManagersByStoreId(@Param("storeId") UUID storeId);

    /**
     * Find all store founders.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE TYPE(r) = StoreFounder")
    List<UserStoreRoles> findAllFounders();

    /**
     * Find all roles that have a specific permission.
     */
    @Query("SELECT r FROM UserStoreRoles r JOIN r.permissions p WHERE p = :permission")
    List<UserStoreRoles> findByPermission(@Param("permission") Permission permission);

    /**
     * Check if a user has a specific role type in a store.
     */
    @Query("SELECT COUNT(r) > 0 FROM UserStoreRoles r WHERE r.username = :username AND r.storeId = :storeId AND TYPE(r) = :roleClass")
    boolean hasRole(@Param("username") String username, @Param("storeId") UUID storeId, @Param("roleClass") Class<? extends UserStoreRoles> roleClass);

    /**
     * Check if a user has any role in a store.
     */
    boolean existsByUsernameAndStoreId(String username, UUID storeId);

    /**
     * Delete all roles for a specific user.
     */
    void deleteByUsername(String username);

    /**
     * Delete all roles for a specific store.
     */
    void deleteByStoreId(UUID storeId);

    /**
     * Delete a specific role by username, store, and type.
     */
    @Query("DELETE FROM UserStoreRoles r WHERE r.username = :username AND r.storeId = :storeId AND TYPE(r) = :roleClass")
    void deleteByUsernameAndStoreIdAndRoleType(@Param("username") String username, @Param("storeId") UUID storeId, @Param("roleClass") Class<? extends UserStoreRoles> roleClass);

    /**
     * Count roles by type.
     */
    @Query("SELECT COUNT(r) FROM UserStoreRoles r WHERE TYPE(r) = :roleClass")
    long countByRoleType(@Param("roleClass") Class<? extends UserStoreRoles> roleClass);

    /**
     * Count roles for a specific store.
     */
    long countByStoreId(UUID storeId);

    /**
     * Count roles for a specific user.
     */
    long countByUsername(String username);

    /**
     * Find roles by username and store with specific role type.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.username = :username AND r.storeId = :storeId AND TYPE(r) = :roleClass")
    Optional<UserStoreRoles> findByUsernameAndStoreIdAndRoleType(@Param("username") String username, @Param("storeId") UUID storeId, @Param("roleClass") Class<? extends UserStoreRoles> roleClass);

    /**
     * Find all stores where a user has any role.
     */
    @Query("SELECT DISTINCT r.storeId FROM UserStoreRoles r WHERE r.username = :username")
    List<UUID> findStoreIdsByUsername(@Param("username") String username);

    /**
     * Find all users who have roles in a specific store.
     */
    @Query("SELECT DISTINCT r.username FROM UserStoreRoles r WHERE r.storeId = :storeId")
    List<String> findUsernamesByStoreId(@Param("storeId") UUID storeId);

    /**
     * Find roles with appointment hierarchy - who appointed whom.
     */
    @Query("SELECT r FROM UserStoreRoles r WHERE r.appointedBy IS NOT NULL ORDER BY r.appointedBy, r.username")
    List<UserStoreRoles> findAllWithAppointmentHierarchy();
}