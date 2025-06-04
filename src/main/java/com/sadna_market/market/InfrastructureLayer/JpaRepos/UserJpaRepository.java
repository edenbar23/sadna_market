package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {

    /**
     * Find a user by username (this is the primary key).
     */
    Optional<User> findByUserName(String userName);

    /**
     * Check if a user exists by username.
     */
    boolean existsByUserName(String userName);

    /**
     * Find users by email address.
     */
    List<User> findByEmail(String email);

    /**
     * Find users by email (case insensitive).
     */
    List<User> findByEmailIgnoreCase(String email);

    /**
     * Find all currently logged in users.
     */
    List<User> findByIsLoggedInTrue();

    /**
     * Find users who have roles in a specific store.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userStoreRoles usr WHERE usr.storeId = :storeId")
    List<User> findByStoreId(@Param("storeId") UUID storeId);

    /**
     * Find users by first and last name.
     */
    List<User> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find users by first name containing (case insensitive).
     */
    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Find users by last name containing (case insensitive).
     */
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT u FROM User u WHERE u.isAdmin = :isAdmin")
    List<User> findByIsAdmin(@Param("isAdmin") boolean isAdmin);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isAdmin = :isAdmin")
    long countByIsAdmin(@Param("isAdmin") boolean isAdmin);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isLoggedIn = true")
    int countActiveUsers();


}