package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.StoreRepository;
import com.sadna_market.market.InfrastructureLayer.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserAccessService is responsible for managing user access and permissions in the system.
 * It handles user registration, authentication, authorization, and other user-related operations.
 */
public class UserAccessService {
    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final Logger logger = LoggerFactory.getLogger(UserAccessService.class);
    private final String realAdmin;

    public UserAccessService(IUserRepository userRepository, IStoreRepository storeRepository, String realAdmin) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.realAdmin = realAdmin;
    }


    /*
     * Registers a new user in the system.
     * Buisness rules:
     * 1. The username must be unique.
     * 2. The password must meet the strength requirements.
     * 3. The email must be valid.
     * 4. The first name and last name must not be empty.
     */
    public User registerUser(String username, String password, String email, String firstName, String lastName) {
        logger.info("Registering new user: {}", username);
        if (userRepository.contains(username)) {
            logger.error("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        if (!isValidEmail(email)) {
            logger.error("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isValidPassword(password)) {
            logger.error("Password does not meet strength requirements");
            throw new IllegalArgumentException("Password does not meet strength requirements");
        }
        if (firstName.isEmpty() || lastName.isEmpty()) {
            logger.error("First name and last name must not be empty");
            throw new IllegalArgumentException("First name and last name must not be empty");
        }
        User user = new User(username, password, email, firstName, lastName);
        userRepository.save(user);
        return user;
    }


//    /*
//     * Authenticates a user by checking the username and password.
//     * Buisness rules:
//     * 1. The username must exist in the system.
//     * 2. The password must match the stored password for the user.
//     */
//    public User authenticateUser(String username, String password) {
//        logger.info("Authenticating user: {}", username);
//        Optional<User> userOptional = userRepository.findByUsername(username);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            if (user.getPassword().equals(password)) {
//                logger.info("User authenticated successfully: {}", username);
//                return user;
//            } else {
//                logger.error("Invalid password for user: {}", username);
//                throw new IllegalArgumentException("Invalid password");
//            }
//        } else {
//            logger.error("User not found: {}", username);
//            throw new IllegalArgumentException("User not found");
//        }
//    }

    /*
     * Logs out a user by invalidating their session.
     * Business rules:
     * 1. The user must be logged in to log out.
     */
    public void logoutUser(String username) {
        logger.info("Logging out user: {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Invalidate the user's session (implementation depends on session management)
            user.logout();
            userRepository.update(user);
            logger.info("User logged out successfully: {}", username);
        } else {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
    }

    // Authorization checks
    /*
     * Checks if a user has permission to perform an action on a store.
     * Business rules:
     * 1. The user must be logged in.
     * 2. The user must have the required permission for the store.
     */
    public boolean hasStorePermission(String username, UUID storeId, Permission permission) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.hasPermission(storeId, permission))
                logger.error("User {} does not have permission {} for store {}", username, permission, storeId);
            return false;
        } else {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
    }

    // Password management
    /*
     * Changes the password for a user.
     * Business rules:
     * 1. The user must be logged in.
     * 2. The old password must match the stored password for the user.
     * 3. The new password must be different from the old password.
     * 4. The new password must meet the strength requirements.
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for user: {}", username);
        // Check if the user exists
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check if the old password matches
            if (!user.getPassword().equals(oldPassword)) {
                logger.error("Old password does not match for user: {}", username);
                throw new IllegalArgumentException("Old password does not match");
            }
            // Check if the new password is different from the old password
            if (newPassword.equals(oldPassword)) {
                logger.error("New password must be different from the old password");
                throw new IllegalArgumentException("New password must be different from the old password");
            }
            // Check if the new password meets the strength requirements
            if (!isValidPassword(newPassword)) {
                logger.error("New password does not meet strength requirements");
                throw new IllegalArgumentException("New password does not meet strength requirements");
            }
            user.setPassword(newPassword);
            userRepository.update(user);
            logger.info("Password changed successfully for user: {}", username);
        }
    }

    public boolean deleteUser(String adminUser, String userToDelete) {
        validateAdmin(adminUser);
        if(userToDelete.equals(adminUser)) {
            throw new IllegalArgumentException("Admin can't delete himself!");
        }
        try {
            userRepository.delete(userToDelete);
            //remove him from all stores and remove everyone he appointed
            //storeRepository.deleteUserFromStores(userToDelete);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + userToDelete);
        }
        return true;
    }

    public void loginUser(String username, String password) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow("user not found!");
            user.login(username,password);
            userRepository.update(user);
        }
        catch (Exception e) {
            logger.error("Failed to login user: {}", username);
            throw new RuntimeException("Failed to login user: " + username);
        }
    }


    //Registered functions here:
    public Cart addToCart(String username,UUID storeId, UUID productId, int quantity) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(IllegalArgumentException("user not found!"));
            if(storeRepository.hasProductInStock(storeId,productId,quantity)) {
                Cart updatedCart = user.addToCart(storeId, productId, quantity);
                userRepository.update(user);
                return updatedCart;
            }
            else throw new IllegalArgumentException("Store does not have product in stock");
        }
        catch(Exception e){
            throw new RuntimeException("Failed to add to cart: " + e.getMessage());
        }
    }

    public Cart removeFromCart(String username,UUID storeId, UUID productId) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            Cart updatedCart = user.removeFromCart(storeId, productId);
            logger.info("Successfully removed from cart for user: {}", username);
            userRepository.update(user);
            return updatedCart;
        }
        catch (Exception e) {}
                logger.error("Failed to remove from cart for user: {}", username);
                throw new RuntimeException("Failed to remove from cart for user: " + username);
    }

    public Cart updateCart(String username,UUID storeId, UUID productId, int quantity) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            Cart updatedCart = user.updateCart(storeId, productId, quantity);
            logger.info("Successfully updated cart for user: {}", username);
            return updatedCart;
        }
        catch (Exception e) {
            logger.error("Failed to update cart for user: {}", username);
            throw new RuntimeException("Failed to update cart for user: " + username);
        }
    }

    public Cart getCart(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            return user.getCart();
        }
        catch (Exception e) {
            logger.error("Failed to get cart for user: {}", username);
            throw new RuntimeException("Failed to get cart for user: " + username);
        }
    }

    public List<UUID> getOrdersHistory(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.getOrdersHistory();
    }

    public void saveReview(String username, UUID storeId, UUID productId, int rating, String comment) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));

    }

    public void checkout(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            Cart cart = user.getCart();
            if (cart.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }
            // Process the checkout (e.g., create an order, charge payment, etc.)
            // This is not implemented here yet.
            logger.info("Checkout successful for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to checkout for user: {}", username);
            throw new RuntimeException("Failed to checkout for user: " + username);
        }
    }

    public void saveRate(String username,UUID storeId, UUID productId, int rating) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new IllegalArgumentException("store not found!"));
        //Product product = productRepository.getProduct(productId).orElseThrow("product not found!");
        //product.addRating(rating);
        //storeRepository.update(store);
    }

    public void sendMessage(String username, UUID storeId, String message) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));

    }

    public void reportViolation(String username, UUID storeId, UUID productId, String comment) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));

    }

    public User returnInfo(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            return user;
        } catch (Exception e) {
            logger.error("Failed to get user info: {}", username);
            throw new RuntimeException("Failed to get user info: " + username);
        }
    }

    public User changeUserInfo(String username, String userName, String password, String email, String firstName, String lastName) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            if (password != null) {
                user.setPassword(password);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (firstName != null) {
                user.setFirstName(firstName);
            }
            if (lastName != null) {
                user.setLastName(lastName);
            }
            userRepository.update(user);
            return user;
        } catch (Exception e) {
            logger.error("Failed to change user info: {}", username);
            throw new RuntimeException("Failed to change user info: " + username);
        }
    }

    public boolean canAddToStore(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId, Permission.ADD_PRODUCT);
    }

    public boolean canRemoveToStore(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId, Permission.REMOVE_PRODUCT);
    }

    public boolean canUpdateProductToStore(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId, Permission.UPDATE_PRODUCT);
    }

    public boolean canUpdateStoreDiscount(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId,Permission.MANAGE_DISCOUNT_POLICY);
    }

    public boolean canUpdateStorePurchasePolicy(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId,Permission.MANAGE_PURCHASE_POLICY);
    }

    public boolean getStoreManagerPermissions(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.getStoreManagerPermissions(storeId);
    }

    //Validation functions here:

    /*
     * Validates the strength of a password.
     * Business rules:
     * 1. The password must contain at least one uppercase letter.
     * 2. The password must contain at least one lowercase letter.
     * 3. The password must contain at least one digit.
     * 4. The password must contain at least one special character.
     * 5. The password must be at least 8 characters long.
     */
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            logger.error("Password must be at least 8 characters long");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            logger.error("Password must contain at least one uppercase letter");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            logger.error("Password must contain at least one lowercase letter");
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            logger.error("Password must contain at least one digit");
            return false;
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            logger.error("Password must contain at least one special character");
            return false;
        }
        return true;
    }

    /*
     * Validates the format of an email address.
     * Business rules:
     * 1. The email must contain an "@" symbol.
     * 2. The email must contain a domain name.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.error("Email cannot be null or empty");
            return false;
        }
        if (!email.contains("@")) {
            logger.error("Email must contain an '@' symbol");
            return false;
        }
        String domain = email.substring(email.indexOf("@") + 1);
        if (domain.isEmpty() || !domain.contains(".")) {
            logger.error("Email must contain a valid domain name");
            return false;
        }
        return true;
    }

    /**
     *
     * @param username
     * validate that username equals admin's username
     */
    private void validateAdmin(String username) {
        if(!realAdmin.equals(username))
            throw new IllegalArgumentException("Not authorized! only admin can operate this!");
    }



}