package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * UserAccessService is responsible for managing user access and permissions in the system.
 * It handles user registration, authentication, authorization, and other user-related operations.
 */
@Service
public class UserAccessService {
    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IReportRepository reportRepository;
    private final Logger logger = LoggerFactory.getLogger(UserAccessService.class);
    private final String realAdmin;
    private final Queue<LocalDateTime> transactionTimestamps;
    private final Queue<LocalDateTime> subscriptionTimestamps;

    @Autowired
    public UserAccessService(IUserRepository userRepository,
                             IStoreRepository storeRepository,
                             IReportRepository reportRepository,
                             @Value("${market.admin.username:admin}") String adminUsername) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.reportRepository = reportRepository;
        this.realAdmin = adminUsername;
        this.transactionTimestamps = new ConcurrentLinkedQueue<>();
        this.subscriptionTimestamps = new ConcurrentLinkedQueue<>();
    }

    /*
     * Registers a new user in the system.
     * Business rules:
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

        // Record subscription
        recordSubscription();

        return user;
    }

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
            checkIfLoggedIn(username);
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
            checkIfLoggedIn(username);
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
        checkIfLoggedIn(adminUser);
        if(userToDelete.equals(adminUser)) {
            throw new IllegalArgumentException("Admin can't delete himself!");
        }
        try {
            userRepository.delete(userToDelete);
            // Additional cleanup can happen here
            logger.info("User {} deleted successfully by admin {}", userToDelete, adminUser);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete user {}: {}", userToDelete, e.getMessage());
            throw new RuntimeException("Failed to delete user: " + userToDelete);
        }
    }

    public void loginUser(String username, String password) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            user.login(username,password);
            userRepository.update(user);
            logger.info("User {} logged in successfully", username);
        }
        catch (Exception e) {
            logger.error("Failed to login user: {}", username);
            throw new RuntimeException("Failed to login user: " + username);
        }
    }

    // Registered functions here:
    public Cart addToCart(String username, UUID storeId, UUID productId, int quantity) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            checkIfLoggedIn(username);
            if(storeRepository.hasProductInStock(storeId, productId, quantity)) {
                Cart updatedCart = user.addToCart(storeId, productId, quantity);
                userRepository.update(user);
                logger.info("Product added to cart successfully for user: {}", username);
                logger.info("Current Cart: {}", updatedCart.toString());
                return updatedCart;
            }
            else throw new IllegalArgumentException("Store does not have product in stock");
        }
        catch(Exception e){
            logger.error("Failed to add to cart: {}", e.getMessage());
            throw new RuntimeException("Failed to add to cart: " + e.getMessage());
        }
    }

    public Cart removeFromCart(String username, UUID storeId, UUID productId) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            checkIfLoggedIn(username);
            // find product in store
            if (!storeRepository.hasProductInStock(storeId, productId, 1)) {
                throw new IllegalArgumentException("Store does not have product in stock");
            }
            Cart updatedCart = user.removeFromCart(storeId, productId);
            logger.info("Successfully removed from cart for user: {}", username);
            userRepository.update(user);
            return updatedCart;
        }
        catch (Exception e) {
            logger.error("Failed to remove from cart for user: {}", username);
            throw new RuntimeException("Failed to remove from cart for user: " + username);
        }
    }

    public Cart updateCart(String username, UUID storeId, UUID productId, int quantity) {
        try {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            // find product in store
            if (!storeRepository.hasProductInStock(storeId, productId, quantity)) {
                throw new IllegalArgumentException("Store does not have product in stock");
            }
            checkIfLoggedIn(username);
            Cart updatedCart = user.updateCart(storeId, productId, quantity);
            userRepository.update(user);
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
            checkIfLoggedIn(username);
            return user.getCart();
        }
        catch (Exception e) {
            logger.error("Failed to get cart for user: {}", username);
            throw new RuntimeException("Failed to get cart for user: " + username);
        }
    }

    public List<UUID> getOrdersHistory(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        checkIfLoggedIn(username);
        return user.getOrdersHistory();
    }

    public void saveReview(String username, UUID storeId, UUID productId, String reviewText) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        checkIfLoggedIn(username);
        // Implementation logic for saving review
        logger.info("Saved review for product {} by user {}", productId, username);
    }

    // Checkout for guest:
    public void checkoutGuest(Cart cart, PaymentMethod pm) {
        try {
            if (cart.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }

            // Publish event instead of direct service call
            DomainEventPublisher.publish(
                    new CheckoutInitiatedEvent(null, cart, pm, true)
            );

            recordTransaction();
            logger.info("Checkout event published for guest");
        } catch (Exception e) {
            logger.error("Failed to checkout for guest: {}", e.getMessage());
            throw new RuntimeException("Failed to checkout for guest: " + e.getMessage());
        }
    }

    // Checkout for user:
    public void checkout(String username, PaymentMethod pm) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("user not found!"));

            checkIfLoggedIn(username);
            Cart cart = user.getCart();
            System.out.println("printing cart.toString()");
            System.out.println(cart.toString());
            if (cart.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }

            // Publish event instead of direct service call
            DomainEventPublisher.publish(
                    new CheckoutInitiatedEvent(username, cart, pm, false)
            );

            recordTransaction();
            logger.info("Checkout event published for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to checkout for user: {}", username);
            throw new RuntimeException("Failed to checkout for user: " + username);
        }
    }

    public void saveRate(String username, UUID storeId, UUID productId, int rating) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        checkIfLoggedIn(username);
        // Implementation for saving product rating
        logger.info("Saved rating {} for product {} by user {}", rating, productId, username);
    }

    public void reportViolation(String username, UUID storeId, UUID productId, String comment) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found!"));
        checkIfLoggedIn(username);
        // Create the report first
        Report report = new Report(username, comment, storeId, productId);
        user.addReport(report.getReportId());
        reportRepository.save(report);

        // Then publish the event
        DomainEventPublisher.publish(
                new ViolationReportedEvent(username, storeId, productId, comment)
        );

        logger.info("Violation report created and event published for user: {}", username);
    }

    public User returnInfo(String username) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            checkIfLoggedIn(username);
            return user;
        } catch (Exception e) {
            logger.error("Failed to get user info: {}", username);
            throw new RuntimeException("Failed to get user info: " + username);
        }
    }

    public User changeUserInfo(String username, String userName, String password, String email, String firstName, String lastName) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
            checkIfLoggedIn(username);
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
            logger.info("User information updated successfully for user: {}", username);
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
        return user.hasPermission(storeId, Permission.MANAGE_DISCOUNT_POLICY);
    }

    public boolean canUpdateStorePurchasePolicy(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.hasPermission(storeId, Permission.MANAGE_PURCHASE_POLICY);
    }

    public List<Permission> getStoreManagerPermissions(String username, UUID storeId) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        return user.getStoreManagerPermissions(storeId);
    }

    public List<Report> getViolationReports(String admin) {
        validateAdmin(admin);
        checkIfLoggedIn(admin);
        List<Report> reports = reportRepository.getAllReports();
        if (reports.isEmpty()) {
            logger.info("No violation reports found");
            return new ArrayList<>();
        }
        return reports;
    }

    public double getTransactionsRatePerHour(String admin) {
        validateAdmin(admin);
        checkIfLoggedIn(admin);
        return cleanupAndCount(transactionTimestamps);
    }

    public double getSubscriptionsRatePerHour(String admin) {
        validateAdmin(admin);
        checkIfLoggedIn(admin);
        return cleanupAndCount(subscriptionTimestamps);
    }

    public void replyViolationReport(String admin, UUID reportId, String user, String message) {
        validateAdmin(admin);
        checkIfLoggedIn(admin);
        // Publish a message for the report reply instead of direct call
        DomainEventPublisher.publish(
                new ViolationReplyEvent(admin, reportId, user, message)
        );
        logger.info("Violation report reply event published by admin: {}", admin);
    }

    public void sendMessageToUser(String admin, String addressee, String message) {
        validateAdmin(admin);
        checkIfLoggedIn(admin);
        // Publish direct message event instead of direct call
        DomainEventPublisher.publish(
                new DirectMessageEvent(admin, addressee, message)
        );
        logger.info("Direct message event published from admin to user: {}", addressee);
    }

    // Record transaction and subscription methods
    public void recordTransaction() {
        transactionTimestamps.add(LocalDateTime.now());
    }

    public void recordSubscription() {
        subscriptionTimestamps.add(LocalDateTime.now());
    }

    // Helper methods for validation
    private void validateAdmin(String username) {
        if(!realAdmin.equals(username))
            throw new IllegalArgumentException("Not authorized! only admin can operate this!");
    }

    private double cleanupAndCount(Queue<LocalDateTime> timestamps) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        while (!timestamps.isEmpty() && timestamps.peek().isBefore(oneHourAgo)) {
            timestamps.poll(); // remove old entries
        }
        return timestamps.size(); // rate = count in past hour
    }

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
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            logger.error("Password must contain at least one special character");
            return false;
        }
        return true;
    }

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

    public void clear() {
        reportRepository.clear();
        userRepository.clear();
        storeRepository.clear();
    }

    private void checkIfLoggedIn(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("user not found!"));
        if (!user.isLoggedIn()) {
            throw new IllegalArgumentException("User is not logged in");
        }
    }
}