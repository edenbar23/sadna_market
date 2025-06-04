package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.User;
import com.sadna_market.market.DomainLayer.RoleType;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserJpaAdapter implements IUserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserJpaAdapter.class);

    private final UserJpaRepository userJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        logger.info("UserJpaAdapter initialized with JPA repository");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        logger.info("Finding user by username: {}", username);
        if (username == null || username.isEmpty()) {
            logger.error("Cannot find user with null or empty username");
            return Optional.empty();
        }

        try {
            Optional<User> user = userJpaRepository.findByUserName(username);
            if (user.isPresent()) {
                logger.info("User found: {}", user.get().getUserName());
                // Initialize lazy collections if needed
                initializeLazyCollections(user.get());
            } else {
                logger.warn("User not found with username: {}", username);
            }
            return user;
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean contains(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot check existence of user with null or empty username");
            return false;
        }

        try {
            boolean exists = userJpaRepository.existsByUserName(username);
            logger.debug("Checking if user exists with username {}: {}", username, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking if user exists: {}", username, e);
            return false;
        }
    }

    @Override
    @Transactional
    public void save(User user) {
        if (user == null) {
            logger.error("Cannot save null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            logger.error("Cannot save user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            logger.debug("Saving user: {}", user.getUserName());
            userJpaRepository.save(user);
            logger.info("User saved successfully: {}", user.getUserName());
        } catch (Exception e) {
            logger.error("Error saving user: {}", user.getUserName(), e);
            throw new RuntimeException("Failed to save user: " + user.getUserName(), e);
        }
    }

    @Override
    @Transactional
    public void update(User user) {
        if (user == null) {
            logger.error("Cannot update null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        String username = user.getUserName();
        if (username == null || username.isEmpty()) {
            logger.error("Cannot update user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            if (!userJpaRepository.existsByUserName(username)) {
                logger.error("Cannot update non-existent user: {}", username);
                throw new IllegalArgumentException("User does not exist: " + username);
            }

            logger.debug("Updating user: {}", username);
            userJpaRepository.save(user); // JPA save handles both insert and update
            logger.info("User updated successfully: {}", username);
        } catch (Exception e) {
            logger.error("Error updating user: {}", username, e);
            throw new RuntimeException("Failed to update user: " + username, e);
        }
    }

    @Override
    @Transactional
    public void delete(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot delete user with null or empty username");
            return;
        }

        try {
            logger.info("Starting cascade delete for user: {}", username);

            // Find the user first to trigger all lazy loading
            Optional<User> userOpt = userJpaRepository.findByUserName(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found for deletion: {}", username);
                return;
            }

            User user = userOpt.get();

            initializeLazyCollections(user);

            logger.debug("Clearing user collections before delete");
            user.getUserStoreRoles().clear();
            user.getOrdersHistory().clear();
            user.getAddressIds().clear();

            // Clear cart and its baskets
            if (user.getCart() != null) {
                user.getCart().getShoppingBasketsList().clear();
            }

            entityManager.flush();

            // Now delete the user - JPA cascade should handle the rest
            logger.debug("Deleting user entity: {}", username);
            userJpaRepository.delete(user);

            // Final flush to ensure everything is committed
            entityManager.flush();

            logger.info("User deleted successfully with all related data: {}", username);

        } catch (Exception e) {
            logger.error("Error deleting user: {}", username, e);
            throw new RuntimeException("Failed to delete user: " + username, e);
        }
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<User> findAll() {
//        try {
//            logger.debug("Getting all users");
//            List<User> users = userJpaRepository.findAll();
//            logger.debug("Found {} users", users.size());
//
//            // Initialize lazy collections for all users
//            users.forEach(this::initializeLazyCollections);
//
//            return users;
//        } catch (Exception e) {
//            logger.error("Error finding all users", e);
//            throw new RuntimeException("Failed to find all users", e);
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.error("Cannot find users with null or empty email");
            return List.of();
        }

        try {
            logger.debug("Finding users with email: {}", email);
            List<User> users = userJpaRepository.findByEmail(email);
            logger.debug("Found {} users with email: {}", users.size(), email);

            // Initialize lazy collections for all users
            users.forEach(this::initializeLazyCollections);

            return users;
        } catch (Exception e) {
            logger.error("Error finding users by email: {}", email, e);
            return List.of();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        try {
            logger.debug("Finding active users");
            List<User> users = userJpaRepository.findByIsLoggedInTrue();
            logger.debug("Found {} active users", users.size());

            // Initialize lazy collections for all users
            users.forEach(this::initializeLazyCollections);

            return users;
        } catch (Exception e) {
            logger.error("Error finding active users", e);
            return List.of();
        }
    }


    @Override
    @Transactional
    public void clear() {
        try {
            logger.info("Starting to clear all users with cascade delete");


            List<User> allUsers = userJpaRepository.findAll();
            logger.info("Found {} users to delete", allUsers.size());

            for (User user : allUsers) {

                initializeLazyCollections(user);


                user.getUserStoreRoles().clear();
                user.getOrdersHistory().clear();
                user.getAddressIds().clear();

                if (user.getCart() != null) {
                    user.getCart().getShoppingBasketsList().clear();
                }
            }


            entityManager.flush();


            userJpaRepository.deleteAll();
            entityManager.flush();

            logger.info("All users and their data cleared successfully");

        } catch (Exception e) {
            logger.error("Error clearing user repository", e);
            throw new RuntimeException("Failed to clear user repository", e);
        }
    }

    /**
     * Helper method to initialize lazy collections to avoid LazyInitializationException.
     * This is called when we need to access collections outside of the transaction context.
     */
    private void initializeLazyCollections(User user) {
        try {
            // Initialize cart and its shopping baskets
            if (user.getCart() != null) {
                user.getCart().getShoppingBasketsList().size(); // Force initialization
                user.getCart().getShoppingBasketsList().forEach(basket -> {
                    basket.getProductsList().size(); // Force initialization of products map
                });
            }

            // Initialize other collections
            user.getUserStoreRoles().size();
            user.getOrdersHistory().size();
            user.getAddressIds().size();

        } catch (Exception e) {
            logger.warn("Could not initialize lazy collections for user: {}", user.getUserName(), e);
        }
    }



    @Override
    public int countAll() {
        return Math.toIntExact(userJpaRepository.count());
    }

    @Override
    public int countActiveUsers() {
        return userJpaRepository.countActiveUsers();
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public boolean existsByIsAdmin(boolean isAdmin) {
        return userJpaRepository.countByIsAdmin(isAdmin) > 0;
    }

    @Override
    public List<User> findByIsAdmin(boolean isAdmin) {
        return userJpaRepository.findByIsAdmin(isAdmin);
    }

    @Override
    public long countByIsAdmin(boolean isAdmin) {
        return userJpaRepository.countByIsAdmin(isAdmin);
    }

}