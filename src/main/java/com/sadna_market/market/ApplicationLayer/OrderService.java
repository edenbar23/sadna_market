package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.OrderDTO;
import com.sadna_market.market.DomainLayer.DomainServices.OrderProcessingService;
import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
import com.sadna_market.market.DomainLayer.Events.DomainEventPublisher;
import com.sadna_market.market.DomainLayer.Events.OrderProcessedEvent;
import com.sadna_market.market.DomainLayer.IOrderRepository;
import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final AuthenticationAdapter authentication;
    private final IOrderRepository orderRepository;
    private final OrderProcessingService orderProcessingService;
    private final RatingService ratingService;


    public Response<OrderDTO> getOrder(UUID orderId) {
        logger.info("Fetching order details for orderId: {}", orderId);
        try {
//            logger.info("Validating token for user with username: {}", username);
//            authentication.validateToken(username, token);
            logger.info("Getting order details for orderId: {}", orderId);
            Order order = orderProcessingService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            OrderDTO orderDto = new OrderDTO(order);
            return Response.success(orderDto);
        } catch (Exception e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return Response.error("Failed to fetch order: " + e.getMessage());
        }
    }

    public Response<List<OrderDTO>> getUserOrderHistory(String username, String token) {
        //logger.info("Fetching order history for user: {}", username);
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);
            logger.info("Getting order history for user with username: {}", username);
            List<Order> orders = orderProcessingService.getOrdersByUser(username);
            List<OrderDTO> ordersDto = orders.stream()
                    .map(OrderDTO::new)
                    .toList();
            return Response.success(ordersDto);
        } catch (Exception e) {
            logger.error("Error fetching order history for user {}: {}", username, e.getMessage());
            return Response.error("Failed to fetch order history: " + e.getMessage());
        }
    }

    public Response<OrderStatus> getOrderStatus(UUID orderId) {
        try {
            OrderStatus status = orderProcessingService.getOrderStatus(orderId);
            return Response.success(status);
        }
        catch (Exception e) {
            logger.error("Error fetching order status", e.getMessage());
            return Response.error("Failed to fetch order history: " + e.getMessage());
        }
    }

    public Response<String> markOrderAsCompleted(String username, String token, UUID orderId) {
        logger.info("User {} attempting to mark order {} as completed", username, orderId);

        try {
            // Validate authentication token
            logger.info("Validating token for user: {}", username);
            authentication.validateToken(username, token);

            // Get the order to verify ownership and status
            logger.info("Retrieving order {} for validation", orderId);
            Optional<Order> orderOpt = orderProcessingService.getOrderById(orderId);

            if (orderOpt.isEmpty()) {
                logger.warn("Order not found: {}", orderId);
                return Response.error("Order not found");
            }

            Order order = orderOpt.get();

            // Verify that the user owns this order
            if (!order.getUserName().equals(username)) {
                logger.warn("User {} attempted to mark order {} owned by {} as completed",
                        username, orderId, order.getUserName());
                return Response.error("You can only mark your own orders as completed");
            }

            // Verify that the order is in SHIPPED status
            if (order.getStatus() != OrderStatus.SHIPPED) {
                logger.warn("Order {} is not in SHIPPED status. Current status: {}",
                        orderId, order.getStatus());
                return Response.error("Only shipped orders can be marked as completed. Current status: " +
                        order.getStatus());
            }

            // Mark the order as completed using the domain service
            logger.info("Marking order {} as completed", orderId);
            boolean success = orderProcessingService.markOrderAsCompleted(orderId);

            if (success) {
                orderProcessingService.getOrderById(orderId).ifPresent(orderNot -> DomainEventPublisher.publish(new OrderProcessedEvent(
                        username, orderId, order.getStoreId()
                )));
                logger.info("Successfully marked order {} as completed by user {}", orderId, username);
                return Response.success("Order marked as completed successfully");
            } else {
                logger.error("Failed to mark order {} as completed", orderId);
                return Response.error("Failed to mark order as completed");
            }

        } catch (Exception e) {
            logger.error("Error marking order {} as completed by user {}: {}",
                    orderId, username, e.getMessage());
            return Response.error("Failed to mark order as completed: " + e.getMessage());
        }
    }
}
