package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.CheckoutApplicationService;
import com.sadna_market.market.ApplicationLayer.DTOs.CheckoutResultDTO;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.ApplicationLayer.Requests.CheckoutRequest;
import com.sadna_market.market.ApplicationLayer.Requests.GuestCheckoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for handling checkout operations
 */
@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutApplicationService checkoutService;

    @Autowired
    public CheckoutController(CheckoutApplicationService checkoutService) {
        this.checkoutService = checkoutService;
        logger.info("CheckoutController initialized");
    }

    /**
     * Processes checkout for a registered user
     *
     * @param username Username from path
     * @param token Authorization token from header
     * @param request Checkout request with payment and supply methods
     * @return Response with checkout result
     */
    @PostMapping("/user/{username}")
    public ResponseEntity<Response<CheckoutResultDTO>> processUserCheckout(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody CheckoutRequest request) {

        logger.info("Received checkout request for user: {}", username);

        try {
            // Validate request
            if (request == null) {
                logger.error("Checkout request is null for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(Response.error("Checkout request cannot be empty"));
            }

            if (request.getPaymentMethod() == null) {
                logger.error("Payment method is null for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(Response.error("Payment method is required"));
            }

            if (request.getSupplyMethod() == null) {
                logger.error("Supply method is null for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(Response.error("Supply method is required"));
            }

            // Process checkout
            Response<CheckoutResultDTO> response = checkoutService.processUserCheckout(username, token, request);

            // Return appropriate HTTP status
            if (response.isError()) {
                logger.warn("Checkout failed for user {}: {}", username, response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                logger.info("Checkout successful for user: {}", username);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during checkout for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Internal server error during checkout"));
        }
    }

    /**
     * Processes checkout for a guest user
     *
     * @param request Guest checkout request with cart, payment and supply methods
     * @return Response with checkout result
     */
    @PostMapping("/guest")
    public ResponseEntity<Response<CheckoutResultDTO>> processGuestCheckout(
            @RequestBody GuestCheckoutRequest request) {

        logger.info("Received guest checkout request");

        try {
            // Validate request
            if (request == null) {
                logger.error("Guest checkout request is null");
                return ResponseEntity.badRequest()
                        .body(Response.error("Checkout request cannot be empty"));
            }

            if (request.getCartItems() == null || request.getCartItems().isEmpty()) {
                logger.error("Guest cart is empty");
                return ResponseEntity.badRequest()
                        .body(Response.error("Cart cannot be empty"));
            }

            if (request.getPaymentMethod() == null) {
                logger.error("Payment method is null for guest");
                return ResponseEntity.badRequest()
                        .body(Response.error("Payment method is required"));
            }

            if (request.getSupplyMethod() == null) {
                logger.error("Supply method is null for guest");
                return ResponseEntity.badRequest()
                        .body(Response.error("Supply method is required"));
            }

            if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
                logger.error("Shipping address is null for guest");
                return ResponseEntity.badRequest()
                        .body(Response.error("Shipping address is required for guest checkout"));
            }

            if (request.getContactEmail() == null || request.getContactEmail().trim().isEmpty()) {
                logger.error("Contact email is null for guest");
                return ResponseEntity.badRequest()
                        .body(Response.error("Contact email is required for guest checkout"));
            }

            // Process checkout
            Response<CheckoutResultDTO> response = checkoutService.processGuestCheckout(request);

            // Return appropriate HTTP status
            if (response.isError()) {
                logger.warn("Guest checkout failed: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                logger.info("Guest checkout successful");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during guest checkout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Internal server error during checkout"));
        }
    }

    /**
     * Health check endpoint for checkout service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Checkout service is running");
    }
}