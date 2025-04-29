package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.DTOs.CartDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.UserDTO;
import com.sadna_market.market.ApplicationLayer.Requests.CartRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RateRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ReviewRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
//import com.sadna_market.market.DomainLayer.DomainServices

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static UserService instance;
    // here we will implement the user service logic
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    //private final IUserRepository userRepository;
    private UserAccessService userAccessService;
    private ObjectMapper objectMapper;


    private UserService(UserAccessService userAccessService) {
        this.objectMapper = new ObjectMapper();
        this.userAccessService = userAccessService;
    }

    public static synchronized UserService getInstance(UserAccessService userAccessService) {
        if (instance == null) {
            instance = new UserService(userAccessService);
        }
        return instance;
    }

    //admin functions:
    public Response deleteUser(String adminUser, String userToDelete) {
        // Here we would implement the logic to delete a user
        try {
            logger.info("Deleting user with username: {}", userToDelete);
            userAccessService.deleteUser(adminUser, userToDelete);
            logger.info("User deleted successfully");
            return Response.success("User deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //Guest functions:
    public Response registerUser(RegisterRequest user) {
        // Here we would implement the logic to register a user
        try {
            logger.info("Registering user with username: {}", user);
            userAccessService.registerUser(user.getUserName(), user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
            logger.info("User registered successfully");
            return Response.success("User registered successfully");
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response addToCart(CartRequest cart, UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to add a product to a user's cart
        logger.info("Adding product with ID: {} to guest: {}", productId);
        //maybe add here a domainService to make sure product in stock
        try {
            cart.addToCartRequest(storeId, productId, quantity);
            String json = objectMapper.writeValueAsString(cart);
            logger.info("Product added to cart successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error processing cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response updateCart(CartRequest cart,UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to update a product in a user's cart
        logger.info("Updating product with ID: {} in user with username: {}", productId);
        try {
            cart.updateItem(storeId, productId, quantity);
            String json = objectMapper.writeValueAsString(cart);
            logger.info("Product updated in cart successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error updating product in cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response viewCart(CartRequest cart) {
        // Here we would implement the logic to view a user's cart
        logger.info("Viewing cart for guest");
        //maybe use a domainService to check all products still in stock and update it if needed
        try {
            String json = objectMapper.writeValueAsString(cart);
            logger.info("Cart viewed successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error viewing cart: {}", e.getMessage());
            return null;
        }
    }

    public Response removeFromCart(CartRequest cart, UUID storeId, UUID productId) {
        // Here we would implement the logic to remove a product from a user's cart
        logger.info("Removing product with ID: {} from guest", productId);
        try {
            cart.removeFromCartRequest(storeId, productId);
            String json = objectMapper.writeValueAsString(cart);
            logger.info("Product removed from cart successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    /**
     * This method returns session token if logged in successfully
     */
    public Response loginUser(String username, String password) {
        try {
            logger.info("Logging in user with username: {}", username);
            userAccessService.loginUser(username, password);
            logger.info("User logged in successfully");
            return Response.success("User logged in successfully");
        } catch (Exception e) {
            logger.error("Error logging in user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
        //should return a response object of token
    }




    //Registered functions:
    public Response logoutUser(String username) {
        try {
            logger.info("Logging out user with username: {}", username);
            userAccessService.logoutUser(username);
            return Response.success("User logged out successfully");
        } catch (Exception e) {
            logger.error("Error logging out user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response addToCart(String username, UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to add a product to a user's cart
        logger.info("Adding product with ID: {} to user with username: {}", productId, username);
        try {
            userAccessService.addToCart(username, storeId, productId, quantity);
            logger.info("Product added to cart successfully");
            return Response.success("Product added to cart successfully");
        } catch (Exception e) {
            logger.error("Error adding product to cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response viewCart(String username) {
        // Here we would implement the logic to view a user's cart
        logger.info("Viewing cart for user with username: {}", username);
        try {
            Cart cart = userAccessService.getCart(username);
            CartDTO cartDTO = new CartDTO(cart);
            String json = objectMapper.writeValueAsString(cartDTO);
            logger.info("Cart viewed successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error viewing cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }


    public Response getOrdersHistory(String username) {
        // Here we would implement the logic to get a user's order history
        logger.info("Getting order history for user with username: {}", username);
        try {
            List<UUID> orders = userAccessService.getOrdersHistory(username);
            String json = objectMapper.writeValueAsString(orders);
            logger.info("Order history retrieved successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error getting order history: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response removeFromCart(String username, UUID storeId, UUID productId) {
        // Here we would implement the logic to remove a product from a user's cart
        logger.info("Removing product with ID: {} from user with username: {}", productId, username);
        try {
            Cart cartUpdated = userAccessService.removeFromCart(username, storeId, productId);
            CartDTO cartDTO = new CartDTO(cartUpdated);
            String json = objectMapper.writeValueAsString(cartDTO);
            logger.info("Product removed from cart successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response updateCart(String userName,UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to update a product in a user's cart
        logger.info("Updating product with ID: {} in user with username: {}", productId, userName);
        try {
            Cart updatedCart = userAccessService.updateCart(userName,storeId, productId, quantity);
            CartDTO cartDTO = new CartDTO(updatedCart);
            String json = objectMapper.writeValueAsString(cartDTO);
            logger.info("Product updated in cart successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error updating product in cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //checkout of guest:
    public Response checkout(CartRequest cartReq, PaymentMethod pm) {
        // Here we would implement the logic to checkout a user's cart
        logger.info("Checking out cart for user with username: {}");
        try {
            Cart cart = new Cart(cartReq.getBaskets());
            userAccessService.checkoutGuest(cart,pm);
            logger.info("checkout successfully");
            return Response.success("checkout successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }
    //checkout of user:
    public Response checkout(String userName, PaymentMethod pm) {
        // Here we would implement the logic to checkout a user's cart
        logger.info("Checking out cart for user with username: {}", userName);
        try {
            userAccessService.checkout(userName,pm);
            logger.info("checkout successfully");
            return Response.success("checkout successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response saveReview(ReviewRequest review) {
        // Here we would implement the logic to save a review
        logger.info("Saving review for product with ID: {}", review.getProductId());
        try {
            userAccessService.saveReview(review.getUsername(),review.getStoreId(), review.getProductId(), review.getRating(), review.getComment());
            logger.info("Review saved successfully");
            return Response.success("Review saved successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response saveRate(RateRequest rate) {
        // Here we would implement the logic to save a rate
        logger.info("Saving rate for product with ID: {}", rate.getProductId());
        try {
            userAccessService.saveRate(rate.getUsername(), rate.getStore(), rate.getProductId(), rate.getRating());
            logger.info("Rate saved successfully");
            return Response.success("Rate saved successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response sendMessage(String username, UUID storeId, String message) {
        // Here we would implement the logic to send a message to a store
        logger.info("Sending message to store with ID: {} from user with username: {}", storeId, username);
        try {
            userAccessService.sendMessage(username, storeId, message);
            logger.info("Message sent successfully");
            return Response.success("Message sent successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response reportViolation(String username, ReviewRequest report) {
        // Here we would implement the logic to report a violation
        logger.info("Reporting violation for review with ID: {}", report.getProductId());
        try {
            userAccessService.reportViolation(username, report.getStoreId(), report.getProductId(), report.getComment());
            logger.info("Violation reported successfully");
            return Response.success("Violation reported successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response returnInfo(String username) {
        // Here we would implement the logic to return a user's information
        logger.info("Returning info for user with username: {}", username);
        try {
            User user = userAccessService.returnInfo(username);
            UserDTO userDTO = new UserDTO(user);
            String json = objectMapper.writeValueAsString(userDTO);
            logger.info("Returning info successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response changeUserInfo(String username, RegisterRequest user) {
        // Here we would implement the logic to change a user's information
        logger.info("Changing info for user with username: {}", username);
        try {
            User userObject = userAccessService.changeUserInfo(username, user.getUserName(), user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
            UserDTO userDTO = new UserDTO(userObject);
            String json = objectMapper.writeValueAsString(userDTO);
            logger.info("Returning info successfully");
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //StoreOwner functions:
    public Response canAddProductToStore(String username, UUID storeId) {
        // Here we would implement the logic to check if a user can add a product to a store
        logger.info("Checking if user with username: {} can add product to store with ID: {}", username, storeId);
        try {
            boolean isOk = userAccessService.canAddToStore(username, storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(isOk);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }
    public Response canRemoveProductToStore(String username, UUID storeId) {
        // Here we would implement the logic to check if a user can remove a product from a store
        logger.info("Checking if user with username: {} can remove product from store with ID: {}", username, storeId);
        try {
            boolean isOk = userAccessService.canRemoveToStore(username,storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(isOk);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response canUpdateProductToStore(String username, UUID storeId) {
        // Here we would implement the logic to check if a user can edit a product in a store
        logger.info("Checking if user with username: {} can edit product in store with ID: {}", username, storeId);
        try {
            boolean isOk = userAccessService.canUpdateProductToStore(username,storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(isOk);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response canUpdateStoreDiscountPolicy(String username, UUID storeId) {
        // Here we would implement the logic to check if a user can update the store discount policy
        logger.info("Checking if user with username: {} can update store discount policy for store with ID: {}", username, storeId);
        try {
            boolean isOk = userAccessService.canUpdateStoreDiscount(username,storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(isOk);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response canUpdateStorePurchasePolicy(String username, UUID storeId) {
        // Here we would implement the logic to check if a user can update the store purchase policy
        logger.info("Checking if user with username: {} can update store purchase policy for store with ID: {}", username, storeId);
        try {
            boolean isOk = userAccessService.canUpdateStorePurchasePolicy(username,storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(isOk);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response getStoreManagerPermissions(String username, UUID storeId) {
        // Here we would implement the logic to get a store manager's permissions
        logger.info("Getting store manager permissions for user with username: {} and store ID: {}", username, storeId);
        // Here we would implement the logic to check if a user can update the store purchase policy
        try {
            List<Permission> permissions = userAccessService.getStoreManagerPermissions(username,storeId);
            logger.info("Check result returned successfully");
            String json = objectMapper.writeValueAsString(permissions);
            return Response.success(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public static synchronized void reset() {
        instance = null;
    }
}
