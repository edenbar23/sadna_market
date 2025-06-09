package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.OrderJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@Profile({"dev", "prod", "default"})
public class OrderJpaAdapter implements IOrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderJpaAdapter.class);

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    // ================================================================================
    // BASIC CRUD OPERATIONS (from IOrderRepository)
    // ================================================================================

    @Override
    @Transactional
    public Order save(Order order) {
        logger.debug("Saving order: {}", order.getOrderId());
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        logger.debug("Finding order by ID: {}", orderId);
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public List<Order> findAll() {
        logger.debug("Finding all orders");
        return orderJpaRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(UUID orderId) {
        logger.debug("Deleting order: {}", orderId);
        orderJpaRepository.deleteById(orderId);
    }

    @Override
    public boolean exists(UUID orderId) {
        return orderJpaRepository.existsById(orderId);
    }

    @Override
    @Transactional
    public void clear() {
        logger.info("Clearing all orders");
        orderJpaRepository.deleteAll();
    }

    // ================================================================================
    // ORDER CREATION METHODS
    // ================================================================================

    @Override
    @Transactional
    public UUID createOrder(UUID storeId, String userName, Map<UUID, Integer> products,
                            double totalPrice, double finalPrice, LocalDateTime orderDate,
                            OrderStatus status, int transactionId) {
        logger.info("Creating order for user: {} in store: {}", userName, storeId);

        // Create the order entity using the @ElementCollection approach
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStoreId(storeId);
        order.setUserName(userName);
        order.setProducts(new HashMap<>(products)); // Direct map assignment!
        order.setTotalPrice(totalPrice);
        order.setFinalPrice(finalPrice);
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setTransactionId(transactionId);
        order.setStoreName("Unknown Store");
        order.setPaymentMethod("Unknown Payment Method");
        order.setDeliveryAddress("Not specified");

        // Save and return the ID
        orderJpaRepository.save(order);
        logger.info("Order created successfully: {}", orderId);
        return orderId;
    }

    @Override
    @Transactional
    public UUID createOrderWithDetails(UUID storeId, String userName, Map<UUID, Integer> products,
                                       double totalPrice, double finalPrice, LocalDateTime orderDate,
                                       OrderStatus status, int transactionId, String storeName,
                                       String paymentMethod, String deliveryAddress) {
        logger.info("Creating order with details for user: {} in store: {}", userName, storeId);

        // Create the order entity with enhanced details
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStoreId(storeId);
        order.setUserName(userName);
        order.setProducts(new HashMap<>(products)); // Direct map assignment!
        order.setTotalPrice(totalPrice);
        order.setFinalPrice(finalPrice);
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setTransactionId(transactionId);
        order.setStoreName(storeName != null ? storeName : "Unknown Store");
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "Unknown Payment Method");
        order.setDeliveryAddress(deliveryAddress != null ? deliveryAddress : "Not specified");

        // Save and return the ID
        orderJpaRepository.save(order);
        logger.info("Order with details created successfully: {}", orderId);
        return orderId;
    }

    // ================================================================================
    // ORDER UPDATE METHODS
    // ================================================================================

    @Override
    @Transactional
    public boolean updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);

        Optional<Order> orderOpt = orderJpaRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            boolean updated = order.updateStatus(newStatus);
            if (updated) {
                orderJpaRepository.save(order);
                logger.info("Order {} status updated successfully", orderId);
                return true;
            } else {
                logger.warn("Invalid status transition for order {}", orderId);
                return false;
            }
        } else {
            logger.error("Order not found: {}", orderId);
            return false;
        }
    }


    @Override
    @Transactional
    public boolean updeteOrderTransactionId(UUID orderId, int transactionId) {
        logger.info("Updating transaction ID for order {} to {}", orderId, transactionId);

        Optional<Order> orderOpt = orderJpaRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            boolean updated = order.updateTransactionId(transactionId);;
            if (updated) {
                orderJpaRepository.save(order);
                logger.info("Transaction ID updated successfully for order {}", orderId);
                return true;
            } else {
                logger.warn("Cannot update transaction ID for order {} in current status", orderId);
                return false;
            }
        } else {
            logger.error("Order not found: {}", orderId);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean setDeliveryId(UUID orderId, UUID deliveryId) {
        logger.info("Setting delivery ID {} for order {}", deliveryId, orderId);

        Optional<Order> orderOpt = orderJpaRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            boolean updated = order.setDeliveryTracking(deliveryId);
            if (updated) {
                orderJpaRepository.save(order);
                logger.info("Delivery ID set successfully for order {}", orderId);
                return true;
            } else {
                logger.warn("Cannot set delivery ID for order {} in current status", orderId);
                return false;
            }
        } else {
            logger.error("Order not found: {}", orderId);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateOrderDetails(UUID orderId, String storeName, String paymentMethod, String deliveryAddress) {
        logger.info("Updating details for order: {}", orderId);

        Optional<Order> orderOpt = orderJpaRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            if (storeName != null) {
                order.setStoreName(storeName);
            }
            if (paymentMethod != null) {
                order.setPaymentMethod(paymentMethod);
            }
            if (deliveryAddress != null) {
                order.setDeliveryAddress(deliveryAddress);
            }

            orderJpaRepository.save(order);
            logger.info("Order details updated successfully for order {}", orderId);
            return true;
        } else {
            logger.error("Order not found: {}", orderId);
            return false;
        }
    }

    // ================================================================================
    // FINDER METHODS (from IOrderRepository)
    // ================================================================================

    @Override
    public List<Order> findByStoreId(UUID storeId) {
        logger.debug("Finding orders by store ID: {}", storeId);
        return orderJpaRepository.findByStoreId(storeId);
    }

    @Override
    public List<Order> findByUserName(String userName) {
        logger.debug("Finding orders by username: {}", userName);
        return orderJpaRepository.findByUserName(userName);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        logger.debug("Finding orders by status: {}", status);
        return orderJpaRepository.findByStatus(status);
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding orders by date range: {} to {}", startDate, endDate);
        return orderJpaRepository.findByOrderDateBetween(startDate, endDate);
    }

    @Override
    public List<Order> getUserPurchaseHistory(String userName) {
        logger.debug("Getting purchase history for user: {}", userName);
        return orderJpaRepository.findByUserNameOrderByOrderDateDesc(userName);
    }

    @Override
    public List<Order> getStorePurchaseHistory(UUID storeId) {
        logger.debug("Getting purchase history for store: {}", storeId);
        return orderJpaRepository.findByStoreIdOrderByOrderDateDesc(storeId);
    }

    @Override
    public List<Order> findOrdersByUser(String username) {
        logger.debug("Finding orders by user: {}", username);
        return orderJpaRepository.findByUserName(username);
    }

    // ================================================================================
    // SPECIAL BUSINESS LOGIC METHODS
    // ================================================================================

    @Override
    public boolean hasUserPurchasedProduct(String username, UUID productId) {
        logger.debug("Checking if user {} has purchased product {}", username, productId);

        try {
            // Use the @ElementCollection query we created in the repository
            return orderJpaRepository.hasUserPurchasedProduct(username, productId);
        } catch (Exception e) {
            logger.error("Error checking if user {} purchased product {}: {}", username, productId, e.getMessage());
            return false;
        }
    }

    // ================================================================================
    // BONUS: ADDITIONAL USEFUL METHODS (using @ElementCollection power)
    // ================================================================================

    /**
     * Find orders containing a specific product (bonus method)
     */
    public List<Order> findOrdersContainingProduct(UUID productId) {
        logger.debug("Finding orders containing product: {}", productId);
        return orderJpaRepository.findOrdersContainingProduct(productId);
    }

    /**
     * Get simple order statistics for a store (bonus method)
     */
    public Map<String, Object> getSimpleStoreStats(UUID storeId) {
        logger.debug("Getting simple statistics for store: {}", storeId);

        List<Order> allOrders = orderJpaRepository.findByStoreId(storeId);
        List<Order> completedOrders = orderJpaRepository.findByStatus(OrderStatus.COMPLETED)
                .stream()
                .filter(order -> order.getStoreId().equals(storeId))
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("completedOrders", completedOrders.size());
        stats.put("totalRevenue", completedOrders.stream()
                .mapToDouble(Order::getFinalPrice)
                .sum());

        return stats;
    }

    /**
     * Get simple order statistics for a user (bonus method)
     */
    public Map<String, Object> getSimpleUserStats(String username) {
        logger.debug("Getting simple statistics for user: {}", username);

        List<Order> allOrders = orderJpaRepository.findByUserName(username);
        List<Order> completedOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("completedOrders", completedOrders.size());
        stats.put("totalSpent", completedOrders.stream()
                .mapToDouble(Order::getFinalPrice)
                .sum());

        return stats;
    }

    @Override
    public int countAll() {
        return Math.toIntExact(orderJpaRepository.count());
    }

    @Override
    public double calculateTotalRevenue() {
        return orderJpaRepository.calculateTotalRevenue();
    }

}