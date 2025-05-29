package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.CheckoutResultDTO;
import com.sadna_market.market.ApplicationLayer.Requests.CheckoutRequest;
import com.sadna_market.market.ApplicationLayer.Requests.GuestCheckoutRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.OrderProcessingService;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentResult;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentService;
import com.sadna_market.market.InfrastructureLayer.Supply.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Application Service responsible for orchestrating the complete checkout process.
 * This service coordinates between domain services and infrastructure services
 * to handle payment processing, supply arrangement, and order finalization.
 */
@Service
public class CheckoutApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutApplicationService.class);

    // Domain Services
    private final OrderProcessingService orderProcessingService;
    private final UserAccessService userAccessService;

    // Infrastructure Services
    private final PaymentService paymentService;
    private final SupplyService supplyService;
    private final AuthenticationAdapter authentication;

    // Repositories
    private final IUserRepository userRepository;

    @Autowired
    public CheckoutApplicationService(
            OrderProcessingService orderProcessingService,
            UserAccessService userAccessService,
            PaymentService paymentService,
            SupplyService supplyService,
            AuthenticationAdapter authentication,
            IUserRepository userRepository) {
        this.orderProcessingService = orderProcessingService;
        this.userAccessService = userAccessService;
        this.paymentService = paymentService;
        this.supplyService = supplyService;
        this.authentication = authentication;
        this.userRepository = userRepository;

        logger.info("CheckoutApplicationService initialized");
    }

    /**
     * Processes checkout for a registered user
     */
    public Response<CheckoutResultDTO> processUserCheckout(String username, String token, CheckoutRequest request) {
        logger.info("Processing checkout for user: {}", username);

        try {
            // 1. Validate authentication
            logger.info("Validating token for user: {}", username);
            authentication.validateToken(username, token);

            // 2. Get user and cart
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            Cart cart = user.getCart();
            if (cart.isEmpty()) {
                logger.warn("Cannot process empty cart for user: {}", username);
                return Response.error("Cannot checkout with empty cart");
            }

            // 3. Create pending orders through domain service
            logger.info("Creating pending orders for user: {}", username);
            List<Order> orders = orderProcessingService.createPendingOrders(username, cart);

            // 4. Process payment through infrastructure service
            double totalAmount = calculateTotalAmount(orders);
            logger.info("Processing payment for amount: {}", totalAmount);
            PaymentResult paymentResult = paymentService.processPayment(request.getPaymentMethod(), totalAmount);

            if (paymentResult.isFailure()) {
                logger.error("Payment failed: {}", paymentResult.getErrorMessage());
                rollbackOrders(orders);
                return Response.error("Payment failed: " + paymentResult.getErrorMessage());
            }

            // 5. Arrange supply through infrastructure service
            logger.info("Arranging supply for {} orders", orders.size());
            List<SupplyResult> supplyResults = processSupplyForOrders(orders, request.getSupplyMethod());

            // Check if any supply failed
            for (SupplyResult supplyResult : supplyResults) {
                if (supplyResult.isFailure()) {
                    logger.error("Supply arrangement failed: {}", supplyResult.getErrorMessage());
                    // Full rollback: refund payment and cancel orders
                    performFullRollback(orders, paymentResult, supplyResults);
                    return Response.error("Supply arrangement failed: " + supplyResult.getErrorMessage());
                }
            }

            // 6. Finalize orders through domain service
            logger.info("Finalizing orders with payment and supply information");
            List<Integer> supplyTransactionIds = supplyResults.stream()
                    .map(SupplyResult::getTransactionId)
                    .toList();
            orderProcessingService.finalizeOrders(orders, paymentResult.getTransactionId(), supplyTransactionIds);

            // 7. Clear user cart
            logger.info("Clearing cart for user: {}", username);
            clearUserCart(user);

            // 8. Create success response
            CheckoutResultDTO result = createCheckoutResult(orders, paymentResult, supplyResults);
            logger.info("Checkout completed successfully for user: {}", username);

            return Response.success(result);

        } catch (Exception e) {
            logger.error("Checkout failed for user {}: {}", username, e.getMessage(), e);
            return Response.error("Checkout failed: " + e.getMessage());
        }
    }

    /**
     * Processes checkout for a guest user
     */
    public Response<CheckoutResultDTO> processGuestCheckout(GuestCheckoutRequest request) {
        logger.info("Processing checkout for guest");

        try {
            // 1. Validate cart
            Cart cart = new Cart(request.getCartItems());
            if (cart.isEmpty()) {
                logger.warn("Cannot process empty cart for guest");
                return Response.error("Cannot checkout with empty cart");
            }

            // 2. Create pending orders for guest through domain service
            logger.info("Creating pending orders for guest");
            List<Order> orders = orderProcessingService.createGuestPendingOrders(cart);

            // 3. Process payment through infrastructure service
            double totalAmount = calculateTotalAmount(orders);
            logger.info("Processing payment for guest, amount: {}", totalAmount);
            PaymentResult paymentResult = paymentService.processPayment(request.getPaymentMethod(), totalAmount);

            if (paymentResult.isFailure()) {
                logger.error("Guest payment failed: {}", paymentResult.getErrorMessage());
                rollbackOrders(orders);
                return Response.error("Payment failed: " + paymentResult.getErrorMessage());
            }

            // 4. Arrange supply through infrastructure service
            logger.info("Arranging supply for guest orders");
            List<SupplyResult> supplyResults = processSupplyForOrders(orders, request.getSupplyMethod());

            // Check if any supply failed
            for (SupplyResult supplyResult : supplyResults) {
                if (supplyResult.isFailure()) {
                    logger.error("Guest supply arrangement failed: {}", supplyResult.getErrorMessage());
                    // Full rollback: refund payment and cancel orders
                    performFullRollback(orders, paymentResult, supplyResults);
                    return Response.error("Supply arrangement failed: " + supplyResult.getErrorMessage());
                }
            }

            // 5. Finalize orders through domain service
            logger.info("Finalizing guest orders");
            List<Integer> supplyTransactionIds = supplyResults.stream()
                    .map(SupplyResult::getTransactionId)
                    .toList();
            orderProcessingService.finalizeOrders(orders, paymentResult.getTransactionId(), supplyTransactionIds);

            // 6. Create success response
            CheckoutResultDTO result = createCheckoutResult(orders, paymentResult, supplyResults);
            logger.info("Guest checkout completed successfully");

            return Response.success(result);

        } catch (Exception e) {
            logger.error("Guest checkout failed: {}", e.getMessage(), e);
            return Response.error("Checkout failed: " + e.getMessage());
        }
    }

    /**
     * Processes supply arrangements for all orders
     */
    private List<SupplyResult> processSupplyForOrders(List<Order> orders, SupplyMethod supplyMethod) {
        return orders.stream().map(order -> {
            try {
                // Create shipment details for each order
                ShipmentDetails shipmentDetails = createShipmentDetails(order);
                double weight = calculateOrderWeight(order);

                logger.debug("Processing supply for order: {}", order.getOrderId());
                return supplyService.processShipment(supplyMethod, shipmentDetails, weight);

            } catch (Exception e) {
                logger.error("Failed to process supply for order {}: {}", order.getOrderId(), e.getMessage());
                return SupplyResult.failure("Supply processing failed: " + e.getMessage(), supplyMethod, null);
            }
        }).toList();
    }

    /**
     * Creates shipment details from order information
     */
    private ShipmentDetails createShipmentDetails(Order order) {
        // Extract shipping information from order
        // For now, using default values - in real system, this would come from user input
        String shipmentId = "SHIP-" + order.getOrderId().toString().substring(0, 8);
        String address = "Default Address"; // This should come from user/order data
        int totalQuantity = order.getProductsMap().values().stream().mapToInt(Integer::intValue).sum();

        boolean isGuest = order.getUserName().contains("-"); // Simple guest detection
        String username = isGuest ? null : order.getUserName();

        return new ShipmentDetails(shipmentId, address, totalQuantity, username, isGuest);
    }

    /**
     * Calculates total weight for an order
     */
    private double calculateOrderWeight(Order order) {
        // Simple calculation - in real system, would get actual product weights
        int totalItems = order.getProductsMap().values().stream().mapToInt(Integer::intValue).sum();
        return totalItems * 0.5; // Assume 0.5kg per item average
    }

    // Remove this method - it's now handled by OrderProcessingService.finalizeOrders()
    // private void finalizeOrders(List<Order> orders, PaymentResult paymentResult, List<SupplyResult> supplyResults)

    /**
     * Calculates total amount for all orders
     */
    private double calculateTotalAmount(List<Order> orders) {
        return orders.stream()
                .mapToDouble(Order::getFinalPrice)
                .sum();
    }

    /**
     * Performs full rollback when supply fails after payment succeeds
     * Refunds payment, cancels supply transactions, and cancels orders
     */
    private void performFullRollback(List<Order> orders, PaymentResult paymentResult, List<SupplyResult> supplyResults) {
        logger.warn("Performing full rollback for {} orders", orders.size());

        // 1. Cancel any successful supply transactions
        rollbackSupplyTransactions(supplyResults);

        // 2. Refund the payment
        rollbackPayment(paymentResult);

        // 3. Cancel orders and restore inventory
        rollbackOrders(orders);

        logger.info("Full rollback completed");
    }

    /**
     * Rolls back payment transaction
     */
    private void rollbackPayment(PaymentResult paymentResult) {
        if (paymentResult != null && paymentResult.isSuccess()) {
            try {
                logger.info("Refunding payment transaction: {}", paymentResult.getTransactionId());
                boolean refunded = paymentService.cancelPayment(paymentResult.getTransactionId());

                if (refunded) {
                    logger.info("Payment refunded successfully: {}", paymentResult.getTransactionId());
                } else {
                    logger.error("Failed to refund payment: {}", paymentResult.getTransactionId());
                }
            } catch (Exception e) {
                logger.error("Error refunding payment {}: {}", paymentResult.getTransactionId(), e.getMessage());
            }
        }
    }

    /**
     * Rolls back supply transactions
     */
    private void rollbackSupplyTransactions(List<SupplyResult> supplyResults) {
        if (supplyResults == null) {
            return;
        }

        for (SupplyResult supplyResult : supplyResults) {
            if (supplyResult.isSuccess()) {
                try {
                    logger.info("Cancelling supply transaction: {}", supplyResult.getTransactionId());
                    boolean cancelled = supplyService.cancelShipment(supplyResult.getTransactionId());

                    if (cancelled) {
                        logger.info("Supply transaction cancelled: {}", supplyResult.getTransactionId());
                    } else {
                        logger.error("Failed to cancel supply transaction: {}", supplyResult.getTransactionId());
                    }
                } catch (Exception e) {
                    logger.error("Error cancelling supply transaction {}: {}",
                            supplyResult.getTransactionId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Handles supply failure by keeping payment but notifying about shipping issues
     * DEPRECATED: Use performFullRollback instead for better customer experience
     */
    @Deprecated
    private void handleSupplyFailure(List<Order> orders, PaymentResult paymentResult) {
        logger.warn("Supply failed but payment succeeded. Keeping orders as paid.");

        // Update orders to reflect payment success but supply failure
        for (Order order : orders) {
            order.updateStatus(OrderStatus.PAID); // Keep as paid, supply will be arranged later
        }

        // Could send notification to customer about shipping delay
        // Could create a task for customer service to follow up
    }

    /**
     * Rolls back orders by canceling them through domain service
     */
    private void rollbackOrders(List<Order> orders) {
        logger.warn("Rolling back {} orders", orders.size());

        orderProcessingService.cancelOrders(orders);
    }

    /**
     * Clears user cart after successful checkout
     */
    private void clearUserCart(User user) {
        try {
            user.clearCart();
            userRepository.update(user);
            logger.debug("Cleared cart for user: {}", user.getUserName());
        } catch (Exception e) {
            logger.error("Failed to clear cart for user {}: {}", user.getUserName(), e.getMessage());
            // Non-critical error, don't fail the checkout
        }
    }

    /**
     * Creates checkout result DTO
     */
    private CheckoutResultDTO createCheckoutResult(List<Order> orders, PaymentResult paymentResult, List<SupplyResult> supplyResults) {
        List<String> orderIds = orders.stream()
                .map(order -> order.getOrderId().toString())
                .toList();

        List<String> trackingNumbers = supplyResults.stream()
                .map(SupplyResult::getTrackingInfo)
                .toList();

        return new CheckoutResultDTO(
                orderIds,
                paymentResult.getTransactionId(),
                trackingNumbers,
                calculateTotalAmount(orders),
                "Checkout completed successfully"
        );
    }
}