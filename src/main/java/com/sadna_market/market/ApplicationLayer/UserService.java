package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    // here we will implement the user service logic
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //admin functions:
    public void deleteUser(String adminUser,String adminToken,String userToDelete) {
        // Here we would implement the logic to delete a user
        logger.info("Deleting user with username: {}", userToDelete);
        Optional<User> user = userRepository.findByUsername(userToDelete);
        if (user.isPresent()) {
            //if user is really admin then:
            User delete = (User) user.get();
            userRepository.delete(delete.getUserName());
            logger.info("User deleted successfully");
        } else {
            logger.error("User not found");
        }
    }

    //Guest functions:
    public void registerUser(RegisterRequest user) {
        // Here we would implement the logic to register a user
        logger.info("Registering user with username: {}", user);
        //user.setPassword(user.getPassword()); // Hash the password before saving
        if(userRepository.contains(user.getUserName())) {
            logger.error("User already exists");
            throw new IllegalArgumentException("User already exists");
        }
        try {
            User newUser = new User(user.getUserName(), user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
            userRepository.save(newUser);
        }
        catch (Exception e) {
           logger.error("Error saving user: {}", e.getMessage());
           throw new RuntimeException("Error saving user");
        }
        //return new Response(true);
    }

    public void loginUser(String username, String password) {
        // Here we would implement the logic to log in a user
        logger.info("Logging in user with username: {}", username);
        Optional<User> getUser = userRepository.findByUsername(username);
        if (getUser.isPresent()) {
            User user = (User) getUser.get();
            logger.info("User logged in successfully");
        } else {
            logger.error("Invalid username or password");
        }
        //should return a response object of token
    }

    public void addToCart(CartRequest cart, UUID productId, int quantity) {
    }


    public void viewCart(CartRequest cart) {
    }


    //Registered functions:
    public void logoutUser(String username) {
        // Here we would implement the logic to log out a user
        logger.info("Logging out user with username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User loggedOutUser = (User) user.get();
            loggedOutUser.logout();
            logger.info("User logged out successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void addToCart(String username, UUID productId, int quantity) {
        // Here we would implement the logic to add a product to a user's cart
        logger.info("Adding product with ID: {} to user with username: {}", productId, username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.addToCart(productId, quantity);
            logger.info("Product added to cart successfully");
        } else {
            logger.error("User not found");
        }
    }
    public CartDTO viewCart(String username) {
        // Here we would implement the logic to view a user's cart
        logger.info("Viewing cart for user with username: {}", username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            return user.getCart();
            logger.info("Cart viewed successfully");
        } else {
            logger.error("User not found");
        }
    }


    public HashMap<UUID,OrderDTO> getOrdersHistory(String username) {
        // Here we would implement the logic to get a user's order history
        logger.info("Getting order history for user with username: {}", username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            logger.info("Order history retrieved successfully");
            return user.getOrdersHistory();
        } else {
            logger.error("User not found");
            throw new IllegalArgumentException("User not found");
        }
    }

    public void removeFromCart(String userName, UUID productId) {
        // Here we would implement the logic to remove a product from a user's cart
        logger.info("Removing product with ID: {} from user with username: {}", productId, userName);
        Optional<User> user_ = userRepository.findByUsername(userName);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.removeFromCart(productId);
            logger.info("Product removed from cart successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void updateCart(String userName, UUID productId, int quantity) {
        // Here we would implement the logic to update a product in a user's cart
        logger.info("Updating product with ID: {} in user with username: {}", productId, userName);
        Optional<User> user_ = userRepository.findByUsername(userName);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.updateCart(productId, quantity);
            logger.info("Product updated in cart successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void checkout(String userName) {
        // Here we would implement the logic to checkout a user's cart
        logger.info("Checking out cart for user with username: {}", userName);
        Optional<User> user_ = userRepository.findByUsername(userName);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.checkout();
            logger.info("Cart checked out successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void saveReview(ReviewRequest review) {
        // Here we would implement the logic to save a review
        logger.info("Saving review for product with ID: {}", review.getProductId());
        Optional<User> user_ = userRepository.findByUsername(review.getUserName());
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.saveReview(review.getStore(), review.getProductId(), review.getRating(), review.getComment());
            logger.info("Review saved successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void saveRate(RateRequest rate) {
        // Here we would implement the logic to save a rate
        logger.info("Saving rate for product with ID: {}", rate.getProductId());
        Optional<User> user_ = userRepository.findByUsername(rate.getUserName());
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.saveRate(rate.getStore(), rate.getProductId(), rate.getRating());
            logger.info("Rate saved successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void sendMessage(String username, UUID storeId, String message) {
        // Here we would implement the logic to send a message to a store
        logger.info("Sending message to store with ID: {} from user with username: {}", storeId, username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.sendMessage(storeId, message);
            //maybe add here a DomainService of store to send the message to the store
            logger.info("Message sent successfully");
        } else {
            logger.error("User not found");
        }
    }

    public void reportViolation(String username, ReviewRequest report) {
        // Here we would implement the logic to report a violation
        logger.info("Reporting violation for review with ID: {}", report.getProductId());
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            user.reportViolation(report.getStore(), report.getProductId(), report.getComment());
            logger.info("Violation reported successfully");
        } else {
            logger.error("User not found");
        }
    }

//    public void updateUser(RegisterRequest user) {
//        // Here we would implement the logic to update a user's password
//        logger.info("Updating user info with username: {}", user);
//        Optional<User> updateUser = userRepository.findByUsername(user.getUserName());
//        if (updateUser.isPresent()) {
//            User userToUpdate = (User) updateUser.get();
//            userToUpdate.updateInfo(user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
//            userRepository.save(userToUpdate);
//            logger.info("User password updated successfully");
//        } else {
//            logger.error("User not found");
//        }
//    }

    public UserDTO returnInfo(String username) {
        // Here we would implement the logic to return a user's information
        logger.info("Returning info for user with username: {}", username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            return new UserDTO(user);
        } else {
            logger.error("User not found");
            return null;
        }
    }

    public void changeUserInfo(String username, RegisterRequest user) {
        // Here we would implement the logic to change a user's information
        logger.info("Changing info for user with username: {}", username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User userToUpdate = (User) user_.get();
            userToUpdate.setUserName(user.getUserName());
            userToUpdate.setPassword(user.getPassword());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFirstName(user.getFirstName());
            userToUpdate.setLastName(user.getLastName());
            userRepository.update(userToUpdate);
            logger.info("User info updated successfully");
        } else {
            logger.error("User not found");
        }
    }
}
