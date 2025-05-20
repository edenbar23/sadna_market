package com.sadna_market.market.PresentationLayer.Controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.DomainLayer.DomainServices.OrderProcessingService;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import com.sadna_market.market.DomainLayer.User;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderProcessingService orderProcessingService;
    private final IUserRepository userRepository; // for some user-related endpoints, if needed

    @Autowired
    public OrderController(OrderProcessingService orderProcessingService,
                           IUserRepository userRepository) {
        this.orderProcessingService = orderProcessingService;
        this.userRepository = userRepository;
    }

    // Endpoint: User checkout (registered user)
    @PostMapping("/checkout/{username}")
    public ResponseEntity<Response<List<Order>>> checkoutUser(
            @PathVariable String username,
            @RequestBody CheckoutRequest checkoutRequest) {

        logger.info("User {} initiating checkout", username);

        try {
            List<Order> orders = orderProcessingService.processPurchase(username, checkoutRequest.getCart(), checkoutRequest.getPaymentMethod());
            return ResponseEntity.ok(Response.success(orders));
        } catch (Exception e) {
            logger.error("Checkout failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Checkout failed: " + e.getMessage()));
        }
    }

    // Endpoint: Guest checkout
    @PostMapping("/checkout/guest")
    public ResponseEntity<Response<List<Order>>> checkoutGuest(@RequestBody CheckoutRequest checkoutRequest) {
        logger.info("Guest initiating checkout");

        try {
            List<Order> orders = orderProcessingService.processGuestPurchase(checkoutRequest.getCart(), checkoutRequest.getPaymentMethod());
            return ResponseEntity.ok(Response.success(orders));
        } catch (Exception e) {
            logger.error("Guest checkout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Guest checkout failed: " + e.getMessage()));
        }
    }

    // Endpoint: Get order details by orderId
    @GetMapping("/{orderId}")
    public ResponseEntity<Response<Order>> getOrder(@PathVariable UUID orderId) {
        logger.info("Fetching order details for {}", orderId);
        try {
            Optional<Order> order = orderProcessingService.getOrderById(orderId);
            if (order.isPresent()) {
                return ResponseEntity.ok(Response.success(order.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.error("Order not found"));
            }
        } catch (Exception e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Error fetching order: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Response<String>> cancelOrder(@PathVariable UUID orderId) {
        logger.info("Cancelling order {}", orderId);
        try {
            // This assumes you add a public method in service to cancel by id
            orderProcessingService.cancelOrder(orderId);
            return ResponseEntity.ok(Response.success("Order cancelled successfully"));
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    // Optional: Get user order history
    @GetMapping("/history/{username}")
    public ResponseEntity<Response<List<Order>>> getUserOrderHistory(@PathVariable String username) {
        logger.info("Fetching order history for user {}", username);
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.error("User not found"));
            }
            List<Order> orders = orderProcessingService.getOrdersByUser(username);
            return ResponseEntity.ok(Response.success(orders));
        } catch (Exception e) {
            logger.error("Error fetching order history for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch order history: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<Response<OrderStatus>> getOrderStatus(@PathVariable UUID orderId) {
        try {
            OrderStatus status = orderProcessingService.getOrderStatus(orderId);
            return ResponseEntity.ok(Response.success(status));
        } catch (Exception e) {
            logger.error("Error getting order status for order {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to get order status: " + e.getMessage()));
        }
    }
}
