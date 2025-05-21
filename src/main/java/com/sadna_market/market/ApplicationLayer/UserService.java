package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.CartDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.UserDTO;
import com.sadna_market.market.ApplicationLayer.Requests.CartRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRateRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductReviewRequest;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ReviewRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.OrderProcessingService;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final AuthenticationAdapter authentication;
    private final UserAccessService userAccessService;
    private final InventoryManagementService inventoryManagementService;
    private final OrderProcessingService orderProcessingService;

    @Autowired
    public UserService(AuthenticationAdapter authentication,
                       UserAccessService userAccessService,
                       InventoryManagementService inventoryManagementService, OrderProcessingService orderProcessingService) {
        this.authentication = authentication;
        this.userAccessService = userAccessService;
        this.inventoryManagementService = inventoryManagementService;
        this.orderProcessingService = orderProcessingService;
    }

    //Guest functions here:
    //req 1.3
    public Response<String> registerUser(RegisterRequest user) {
        // Here we would implement the logic to register a user
        try {
            logger.info("Registering user with username: {}", user.getUserName());
            userAccessService.registerUser(user.getUserName(), user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
            logger.info("User registered successfully");
            authentication.saveUser(user.getUserName(), user.getPassword());
            return Response.success("User registered successfully");
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    /**
     * This method returns session token if logged in successfully
     */
    //req 1.4
    public Response<String> loginUser(String username, String password) {
        try {
            logger.info("Logging in user with username: {}", username);
            userAccessService.loginUser(username, password);
            logger.info("User logged in successfully");
            //auth return token
            String token = authentication.createUserSessionToken(username, password);
            logger.info("Token generated successfully: {}", token);
            return Response.success(token);
        } catch (Exception e) {
            logger.error("Error logging in user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
        //should return a response object of token
    }

    //req 2.3
    public Response<CartRequest> addToCart(CartRequest cart, UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to add a product to a user's cart
        logger.info("Adding product with ID: {} to guest cart", productId);
        //maybe add here a domainService to make sure product in stock
        try {
            if (!inventoryManagementService.checkProductAvailability(storeId, productId, quantity)) {
                return Response.error("Product not available");
            }
            cart.addToCartRequest(storeId, productId, quantity);
            logger.info("Product added to cart successfully");
            return Response.success(cart);
        } catch (Exception e) {
            logger.error("Error processing cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 2.4 (a)
    public Response<CartRequest> viewCart(CartRequest cart) {
        // Here we would implement the logic to view a user's cart
        logger.info("Viewing cart for guest");
        //maybe use a domainService to check all products still in stock and update it if needed
        try {
            logger.info("Cart viewed successfully");
            return Response.success(cart);
        } catch (Exception e) {
            logger.error("Error viewing cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 2.4 (b)
    public Response<CartRequest> updateCart(CartRequest cart, UUID storeId, UUID productId, int quantity) {
        // Here we would implement the logic to update a product in a user's cart
        logger.info("Updating product with ID: {} in guest cart", productId);
        try {
            cart.updateItem(storeId, productId, quantity);
            logger.info("Product updated in cart successfully");
            return Response.success(cart);
        } catch (Exception e) {
            logger.error("Error updating product in cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 2.4 (c)
    public Response<CartRequest> removeFromCart(CartRequest cart, UUID storeId, UUID productId) {
        // Here we would implement the logic to remove a product from a user's cart
        logger.info("Removing product with ID: {} from guest cart", productId);
        try {
            cart.removeFromCartRequest(storeId, productId);
            logger.info("Product removed from cart successfully");
            return Response.success(cart);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 2.5
    public Response<String> checkout(CartRequest cartReq, PaymentMethod pm) { //checkout of guest:
        // Here we would implement the logic to checkout a user's cart
        logger.info("Checking out cart for guest");
        try {
            Cart cart = new Cart(cartReq.getBaskets());
            userAccessService.checkoutGuest(cart, pm);
            logger.info("checkout successfully");
            return Response.success("checkout successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //Registered functions:
    //req 3.1
    public Response<String> logoutUser(String username, String token) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            // Logout in the authentication bridge - passing the token to invalidate it
            authentication.logout(username, token);

            logger.info("Logging out user with username: {}", username);
            userAccessService.logoutUser(username);

            return Response.success("User logged out successfully");
        } catch (Exception e) {
            logger.error("Error logging out user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 2.1 - 2.5 for registered users
    public Response<String> addToCart(String username, String token, UUID storeId, UUID productId, int quantity) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Adding product with ID: {} to user with username: {}", productId, username);
            userAccessService.addToCart(username, storeId, productId, quantity);
            logger.info("Product added to cart successfully");
            return Response.success("Product added to cart successfully");
        } catch (Exception e) {
            logger.error("Error adding product to cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<CartDTO> viewCart(String username, String token) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Viewing cart for user with username: {}", username);
            Cart cart = userAccessService.getCart(username);
            CartDTO cartDTO = new CartDTO(cart);
            logger.info("Cart viewed successfully");
            return Response.success(cartDTO);
        } catch (Exception e) {
            logger.error("Error viewing cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<CartDTO> removeFromCart(String username, String token, UUID storeId, UUID productId) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Removing product with ID: {} from user with username: {}", productId, username);
            Cart cartUpdated = userAccessService.removeFromCart(username, storeId, productId);
            CartDTO cartDTO = new CartDTO(cartUpdated);
            logger.info("Product removed from cart successfully");
            return Response.success(cartDTO);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<CartDTO> updateCart(String username, String token, UUID storeId, UUID productId, int quantity) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Updating product with ID: {} in user with username: {}", productId, username);
            Cart updatedCart = userAccessService.updateCart(username, storeId, productId, quantity);
            CartDTO cartDTO = new CartDTO(updatedCart);
            logger.info("Product updated in cart successfully");
            return Response.success(cartDTO);
        } catch (Exception e) {
            logger.error("Error updating product in cart: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //checkout of user:
    public Response<String> checkout(String username, String token, PaymentMethod pm) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Checking out cart for user with username: {}", username);
            userAccessService.checkout(username, pm);
            logger.info("checkout successfully");
            return Response.success("checkout successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.3
    public Response<String> saveReview(String token, ProductReviewRequest review) {
        try {
            logger.info("Validating token for user with username: {}", review.getUsername());
            authentication.validateToken(review.getUsername(), token);
            logger.info("Saving review for product with ID: {}", review.getProductId());
            userAccessService.saveReview(review.getUsername(), review.getStoreId(), review.getProductId(), review.getReviewText());
            logger.info("Review saved successfully");
            return Response.success("Review saved successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.4
    public Response<String> saveRate(String token, ProductRateRequest rate) {
        try {
            logger.info("Validating token for user with username: {}", rate.getUsername());
            authentication.validateToken(rate.getUsername(), token);
            logger.info("Saving rate for product with ID: {}", rate.getProductId());
            userAccessService.saveRate(rate.getUsername(), rate.getStoreId(), rate.getProductId(), rate.getRating());
            logger.info("Rate saved successfully");
            return Response.success("Rate saved successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.6
    public Response<String> reportViolation(String username, String token, ReviewRequest report) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Reporting violation for review with ID: {}", report.getProductId());
            userAccessService.reportViolation(username, report.getStoreId(), report.getProductId(), report.getComment());
            logger.info("Violation reported successfully");
            return Response.success("Violation reported successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.7
    public Response<List<UUID>> getOrdersHistory(String username, String token) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Getting order history for user with username: {}", username);
            List<UUID> orders = userAccessService.getOrdersHistory(username);
            logger.info("Order history retrieved successfully");
            return Response.success(orders);
        } catch (Exception e) {
            logger.error("Error getting order history: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.8 (a)
    public Response<UserDTO> returnInfo(String username, String token) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Returning info for user with username: {}", username);
            User user = userAccessService.returnInfo(username);
            UserDTO userDTO = new UserDTO(user);
            logger.info("Returning info successfully");
            return Response.success(userDTO);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 3.8 (b)
    public Response<UserDTO> changeUserInfo(String username, String token, RegisterRequest user) {
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Changing info for user with username: {}", username);
            User userObject = userAccessService.changeUserInfo(username, user.getUserName(), user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName());
            UserDTO userDTO = new UserDTO(userObject);
            logger.info("Returning info successfully");
            return Response.success(userDTO);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //SystemAdmin functions here:
    //req 6.2
    public Response<String> deleteUser(String adminUser, String token, String userToDelete) {
        try {
            logger.info("Validating token for user with username: {}", adminUser);
            authentication.validateToken(adminUser, token);
            logger.info("Deleting user with username: {}", userToDelete);
            userAccessService.deleteUser(adminUser, userToDelete);
            logger.info("User deleted successfully");
            return Response.success("User deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.3 (a)
    public Response<List<Report>> getViolationReports(String admin, String token) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Getting violation reports for admin with username: {}", admin);
            List<Report> reports = userAccessService.getViolationReports(admin);
            logger.info("Violation reports retrieved successfully");
            return Response.success(reports);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.3 (b)
    public Response<String> replyViolationReport(String admin, String token, UUID reportId, String user, String message) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Replying to violation report with ID: {} for admin with username: {}", reportId, admin);
            userAccessService.replyViolationReport(admin, reportId, user, message);
            logger.info("Reply sent successfully");
            return Response.success("Reply sent successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.3 (c)
    public Response<String> sendMessageToUser(String admin, String token, String addressee, String message) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Sending message to user with username: {} from admin with username: {}", addressee, admin);
            userAccessService.sendMessageToUser(admin, addressee, message);
            logger.info("Message sent successfully");
            return Response.success("Message sent successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.4 (a)
    public Response<List<UUID>> getUserPurchasedHistory(String admin, String token, String username) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Getting purchase history for user with username: {}", username);
            List<UUID> orders = userAccessService.getOrdersHistory(username);
            logger.info("Purchase history retrieved successfully");
            return Response.success(orders);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.5 (b)
    public Response<Double> getTransactionsRate(String admin, String token) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Getting transactions rate for admin with username: {}", admin);
            double transactionsRate = userAccessService.getTransactionsRatePerHour(admin);
            logger.info("Transactions rate retrieved successfully:{}", transactionsRate);
            return Response.success(transactionsRate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //req 6.5 (c)
    public Response<Double> getSubscriptionsRate(String admin, String token) {
        try {
            logger.info("Validating token for user with username: {}", admin);
            authentication.validateToken(admin, token);
            logger.info("Getting subscriptions rate for admin with username: {}", admin);
            double subsRate = userAccessService.getSubscriptionsRatePerHour(admin);
            logger.info("Subscriptions rate retrieved successfully");
            return Response.success(subsRate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<List<UUID>> getStoresIds (String username, String token){
        try{
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Getting stores ids for user with username: {}", username);
            List<UUID> storesIds = userAccessService.getStoresIds(username);
            logger.info("StoresIds retrieved successfully");
            return Response.success(storesIds);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.error(e.getMessage());
        }


    }

//    //System functions here:
//    public Response<String> openMarket(String admin, String token) {
//        //open the market
//        //initialize the system
//        //validate admin user exists
//        logger.info("Validating token for user with username: {}", admin);
//        authentication.validateToken(admin, token);
//        //initialize the supply system
//        //initialize the payment system
//        //open market
//        return Response.error("not implemented yet");
//    }
//
//    public Response<String> closeMarket(String admin, String token) {
//        try {
//            logger.info("Validating token for user with username: {}", admin);
//            authentication.validateToken(admin, token);
//        } catch (IllegalArgumentException e) {
//            return Response.error(e.getMessage());
//        }
//        //close the market (not allowing anyone to access market)
//        return Response.error("not implemented yet");
//    }

    public void clear() {
        authentication.clear();
        userAccessService.clear();
    }
}