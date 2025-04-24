package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sadna_market.market.DomainLayer.User;

import java.util.Optional;

@Service
public class UserService {
    // here we will implement the user service logic
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

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


    //User's basic functionality
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

    public void updateUser(RegisterRequest user) {
        // Here we would implement the logic to update a user's password
        logger.info("Updating password for user with username: {}", user);
        Optional<User> updateUser = userRepository.findByUsername(user.getUserName());
        if (updateUser.isPresent()) {
            User userToUpdate = (User) updateUser.get();
            userRepository.save(userToUpdate);
            logger.info("User password updated successfully");
         } else {
             logger.error("User not found");
         }
    }
    //Guest functionality
    public void addToCart(CartRequest cart, String productId, int quantity) {
    }


    public void viewCart(CartRequest cart) {
    }


    //Registered functionality
    public void addToCart(String username, String productId, int quantity) {
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
    public void viewCart(String username) {
        // Here we would implement the logic to view a user's cart
        logger.info("Viewing cart for user with username: {}", username);
        Optional<User> user_ = userRepository.findByUsername(username);
        if (user_.isPresent()) {
            User user = (User) user_.get();
            //return user.getCart();
            logger.info("Cart viewed successfully");
        } else {
            logger.error("User not found");
        }
    }




}
