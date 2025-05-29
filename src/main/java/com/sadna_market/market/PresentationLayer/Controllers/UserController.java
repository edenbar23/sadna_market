package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.DTOs.CartDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.StoreDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.UserDTO;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.ApplicationLayer.UserService;
import com.sadna_market.market.DomainLayer.Report;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for handling user-related operations
 * Provides endpoints for guest users, registered users, and admin functions
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // For development - you might want to restrict this in production
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //---------------------------
    // Authentication Endpoints
    //---------------------------

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<Response<String>> registerUser(@RequestBody RegisterRequest request) {
        Response<String> response = userService.registerUser(request);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login a user and get authentication token
     */
    @PostMapping("/login")
    public ResponseEntity<Response<String>> loginUser(
            @RequestParam String username,
            @RequestParam String password) {

        Response<String> response = userService.loginUser(username, password);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Logout a user
     */
    @PostMapping("/{username}/logout")
    public ResponseEntity<Response<String>> logoutUser(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        Response<String> response = userService.logoutUser(username, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    //---------------------------
    // Guest Cart Endpoints
    //---------------------------

    /**
     * Add product to guest cart
     */
    @PostMapping("/guest/cart")
    public ResponseEntity<Response<CartRequest>> addToGuestCart(
            @RequestBody CartRequest cart,
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam int quantity) {

        Response<CartRequest> response = userService.addToCart(cart, storeId, productId, quantity);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * View guest cart
     */
    @GetMapping("/guest/cart")
    public ResponseEntity<Response<CartRequest>> viewGuestCart(@RequestBody CartRequest cart) {
        Response<CartRequest> response = userService.viewCart(cart);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Update guest cart item quantity
     */
    @PutMapping("/guest/cart")
    public ResponseEntity<Response<CartRequest>> updateGuestCart(
            @RequestBody CartRequest cart,
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam int quantity) {

        Response<CartRequest> response = userService.updateCart(cart, storeId, productId, quantity);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Remove item from guest cart
     */
    @DeleteMapping("/guest/cart")
    public ResponseEntity<Response<CartRequest>> removeFromGuestCart(
            @RequestBody CartRequest cart,
            @RequestParam UUID storeId,
            @RequestParam UUID productId) {

        Response<CartRequest> response = userService.removeFromCart(cart, storeId, productId);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

//    /**
//     * Checkout as guest
//     */
//    @PostMapping("/guest/checkout")
//    public ResponseEntity<Response<String>> guestCheckout(
//            @RequestBody CartRequest cart,
//            @RequestBody PaymentMethod paymentMethod) {
//
//        Response<String> response = userService.checkout(cart, paymentMethod);
//
//        return response.isError()
//                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
//                : ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    //---------------------------
    // Registered User Cart Endpoints
    //---------------------------

    /**
     * Add product to user cart
     */
    @PostMapping("/{username}/cart")
    public ResponseEntity<Response<String>> addToUserCart(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam int quantity) {

        Response<String> response = userService.addToCart(username, token, storeId, productId, quantity);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * View user cart
     */
    @GetMapping("/{username}/cart")
    public ResponseEntity<Response<CartDTO>> viewUserCart(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        Response<CartDTO> response = userService.viewCart(username, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Update user cart item quantity
     */
    @PutMapping("/{username}/cart")
    public ResponseEntity<Response<CartDTO>> updateUserCart(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam int quantity) {

        Response<CartDTO> response = userService.updateCart(username, token, storeId, productId, quantity);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Remove item from user cart
     */
    @DeleteMapping("/{username}/cart")
    public ResponseEntity<Response<CartDTO>> removeFromUserCart(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestParam UUID storeId,
            @RequestParam UUID productId) {

        Response<CartDTO> response = userService.removeFromCart(username, token, storeId, productId);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

//    /**
//     * Checkout user cart
//     */
//    @PostMapping("/{username}/checkout")
//    public ResponseEntity<Response<String>> userCheckout(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token,
//            @RequestBody PaymentMethod paymentMethod) {
//
//        Response<String> response = userService.checkout(username, token, paymentMethod);
//
//        return response.isError()
//                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
//                : ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    //---------------------------
    // User Profile Endpoints
    //---------------------------

    /**
     * Get user profile information
     */
    @GetMapping("/{username}")
    public ResponseEntity<Response<UserDTO>> getUserProfile(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        Response<UserDTO> response = userService.returnInfo(username, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Update user profile information
     */
    @PutMapping("/{username}")
    public ResponseEntity<Response<UserDTO>> updateUserProfile(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterRequest updateRequest) {

        Response<UserDTO> response = userService.changeUserInfo(username, token, updateRequest);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }


    /**
     * Get user's stores id's
     */

    @GetMapping("/user/{username}/stores")
    public ResponseEntity<Response<List<UUID>>> getUserStores(
        @PathVariable String username,
        @RequestHeader("Authorization") String token) {

        Response<List<UUID>> response = userService.getStoresIds(username, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);

        }


    /**
     * Get user's order history
     */
    @GetMapping("/{username}/orders")
    public ResponseEntity<Response<List<UUID>>> getUserOrderHistory(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        Response<List<UUID>> response = userService.getOrdersHistory(username, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    //---------------------------
    // User Interaction Endpoints
    //---------------------------

    /**
     * Submit a product review
     */
    @PostMapping("/{username}/reviews")
    public ResponseEntity<Response<String>> addProductReview(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody ProductReviewRequest reviewRequest) {

        Response<String> response = userService.saveReview(token, reviewRequest);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Rate a product
     */
    @PostMapping("/{username}/ratings")
    public ResponseEntity<Response<String>> rateProduct(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRateRequest rateRequest) {

        Response<String> response = userService.saveRate(token, rateRequest);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Report a violation
     */
    @PostMapping("/{username}/violations")
    public ResponseEntity<Response<String>> reportViolation(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody ReviewRequest violationReport) {

        Response<String> response = userService.reportViolation(username, token, violationReport);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //---------------------------
    // Admin Endpoints
    //---------------------------

    /**
     * Admin: Delete a user
     */
    @DeleteMapping("/admin/{adminUsername}/users/{username}")
    public ResponseEntity<Response<String>> adminDeleteUser(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable String username) {

        Response<String> response = userService.deleteUser(adminUsername, token, username);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Get all violation reports
     */
    @GetMapping("/admin/{adminUsername}/violations")
    public ResponseEntity<Response<List<Report>>> getViolationReports(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<List<Report>> response = userService.getViolationReports(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Reply to a violation report
     */
    @PostMapping("/admin/{adminUsername}/violations/{reportId}/reply")
    public ResponseEntity<Response<String>> replyToViolation(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable UUID reportId,
            @RequestParam String user,
            @RequestParam String message) {

        Response<String> response = userService.replyViolationReport(adminUsername, token, reportId, user, message);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Send a message to a user
     */
    @PostMapping("/admin/{adminUsername}/messages")
    public ResponseEntity<Response<String>> sendAdminMessage(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @RequestParam String recipient,
            @RequestParam String message) {

        Response<String> response = userService.sendMessageToUser(adminUsername, token, recipient, message);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin: Get a user's purchase history
     */
    @GetMapping("/admin/{adminUsername}/users/{username}/purchases")
    public ResponseEntity<Response<List<UUID>>> getUserPurchases(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable String username) {

        Response<List<UUID>> response = userService.getUserPurchasedHistory(adminUsername, token, username);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Get transaction rate
     */
    @GetMapping("/admin/{adminUsername}/stats/transactions")
    public ResponseEntity<Response<Double>> getTransactionRate(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<Double> response = userService.getTransactionsRate(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Get subscription rate
     */
    @GetMapping("/admin/{adminUsername}/stats/subscriptions")
    public ResponseEntity<Response<Double>> getSubscriptionRate(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<Double> response = userService.getSubscriptionsRate(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }
}