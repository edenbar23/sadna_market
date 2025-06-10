package com.sadna_market.market.PresentationLayer.Controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sadna_market.market.ApplicationLayer.DTOs.OrderDTO;
import com.sadna_market.market.ApplicationLayer.OrderService;
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
@RequestMapping("api/orders")
@CrossOrigin(origins = "*") // For development - you might want to restrict this in production
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

//    private final OrderProcessingService orderProcessingService;
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderProcessingService orderProcessingService,
                           OrderService orderService) {
//        this.orderProcessingService = orderProcessingService;
        this.orderService = orderService;
    }

    // Endpoint: Get order details by orderId
    @GetMapping("/{orderId}")
    public ResponseEntity<Response<OrderDTO>> getOrder(@PathVariable UUID orderId) {
        logger.info("Fetching order details for {}", orderId);
        try {
            Response<OrderDTO> response = orderService.getOrder(orderId);
            if (response.isError()) {
                logger.error("Failed to fetch order {}: {}", orderId, response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.error("Order not found: " + response.getErrorMessage()));
            } else {
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Error fetching order: " + e.getMessage()));
        }
    }

//    @PostMapping("/{orderId}/cancel")
//    public ResponseEntity<Response<String>> cancelOrder(@PathVariable UUID orderId) {
//        logger.info("Cancelling order {}", orderId);
//        try {
//            // This assumes you add a public method in service to cancel by id
//            orderProcessingService.cancelOrder(orderId);
//            return ResponseEntity.ok(Response.success("Order cancelled successfully"));
//        } catch (Exception e) {
//            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Response.error("Failed to cancel order: " + e.getMessage()));
//        }
//    }

    @GetMapping("/history/{username}")
    public ResponseEntity<Response<List<OrderDTO>>> getUserOrderHistory(@PathVariable String username,@RequestHeader("Authorization") String token) {
        logger.info("Fetching order history for user {}", username);
        try {
            Response<List<OrderDTO>> response = orderService.getUserOrderHistory(username,token);
            if (response.isError()) {
                logger.error("Failed to fetch user history {}: {}", username, response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.error("Order not found: " + response.getErrorMessage()));
            } else {
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error fetching order history for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch order history: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<Response<OrderStatus>> getOrderStatus(@PathVariable UUID orderId) {
        try {
            Response<OrderStatus> response = orderService.getOrderStatus(orderId);
            if (response.isError()) {
                logger.error("Failed to fetch order {}: {}", orderId, response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.error("Order not found: " + response.getErrorMessage()));
            } else {
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error getting order status for order {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to get order status: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<Response<String>> markOrderAsCompleted(
            @PathVariable UUID orderId,
            @RequestParam String username,
            @RequestHeader("Authorization") String token) {

        logger.info("User {} requesting to mark order {} as completed", username, orderId);

        try {
            Response<String> response = orderService.markOrderAsCompleted(username, token, orderId);

            if (response.isError()) {
                logger.error("Failed to mark order {} as completed: {}", orderId, response.getErrorMessage());

                if (response.getErrorMessage().contains("not found")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                } else if (response.getErrorMessage().contains("own orders") ||
                        response.getErrorMessage().contains("token")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                } else if (response.getErrorMessage().contains("status")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            logger.info("Successfully marked order {} as completed by user {}", orderId, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking order {} as completed: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Unexpected error: " + e.getMessage()));
        }
    }

}
