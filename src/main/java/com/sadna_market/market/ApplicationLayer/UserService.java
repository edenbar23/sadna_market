package com.sadna_market.market.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sadna_market.market.DomainLayer.User;
@Service
public class UserService {
    // here we will implement the user service logic
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    //private final UserRepository userRepository;

    public UserService() {
        //this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        // Here we would implement the logic to register a user
        logger.info("Registering user with username: {}", user);
        //user.setPassword(user.getPassword()); // Hash the password before saving

        // userRepository.save(user);
        // userRepository.save(new User(username, password));
    }

    public void loginUser(String username, String password) {
        // Here we would implement the logic to log in a user
        logger.info("Logging in user with username: {}", username);
        // User user = userRepository.findByUsername(username);
        // if (user != null && user.getPassword().equals(password)) {
        //     logger.info("User logged in successfully");
        // } else {
        //     logger.error("Invalid username or password");
        // }
    }

    public void logoutUser(String username) {
        // Here we would implement the logic to log out a user
        logger.info("Logging out user with username: {}", username);
        // User user = userRepository.findByUsername(username);
        // if (user != null) {
        //     logger.info("User logged out successfully");
        // } else {
        //     logger.error("User not found");
        // }
    }

    public void deleteUser(String username) {
        // Here we would implement the logic to delete a user
        logger.info("Deleting user with username: {}", username);
        // User user = userRepository.findByUsername(username);
        // if (user != null) {
        //     userRepository.delete(user);
        //     logger.info("User deleted successfully");
        // } else {
        //     logger.error("User not found");
        // }
    }

    public void updateUser(String username, String newPassword) {
        // Here we would implement the logic to update a user's password
        logger.info("Updating password for user with username: {}", username);
        // User user = userRepository.findByUsername(username);
        // if (user != null) {
        //     user.setPassword(newPassword);
        //     userRepository.save(user);
        //     logger.info("User password updated successfully");
        // } else {
        //     logger.error("User not found");
        // }
    }

    public void getUser(String username) {
        // Here we would implement the logic to get a user's details
        logger.info("Getting user with username: {}", username);
        // User user = userRepository.findByUsername(username);
        // if (user != null) {
        //     logger.info("User found: {}", user);
        // } else {
        //     logger.error("User not found");
        // }
        // return user;
    }
}
