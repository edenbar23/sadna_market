package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.IOrderRepository;
import com.sadna_market.market.DomainLayer.Order;
import com.sadna_market.market.DomainLayer.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("test")
public class InMemoryOrderRepository implements IOrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrderRepository.class);

    // Thread-safe map to store orders by ID
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    public InMemoryOrderRepository() {
        logger.info("InMemoryOrderRepository initialized");
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            logger.error("Cannot save null order");
            throw new IllegalArgumentException("Order cannot be null");
        }

        logger.debug("Saving order: {}", order.getOrderId());
        orders.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        if (orderId == null) {
            logger.error("Cannot find order with null ID");
            return Optional.empty();
        }

        logger.debug("Finding order by ID: {}", orderId);
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findAll() {
        logger.debug("Getting all orders (total: {})", orders.size());
        return new ArrayList<>(orders.values());
    }

    @Override
    public void deleteById(UUID orderId) {
        if (orderId == null) {
            logger.error("Cannot delete order with null ID");
            return;
        }

        logger.debug("Deleting order with ID: {}", orderId);
        orders.remove(orderId);
    }

    @Override
    public boolean exists(UUID orderId) {
        if (orderId == null) {
            logger.error("Cannot check existence of order with null ID");
            return false;
        }

        boolean exists = orders.containsKey(orderId);
        logger.debug("Checking if order exists with ID {}: {}", orderId, exists);
        return exists;
    }

    @Override
    public UUID createOrder(UUID storeId, String userName, Map<UUID, Integer> products,
                            double totalPrice, double finalPrice, LocalDateTime orderDate,
                            OrderStatus status, int transactionId){
        // Validate input parameters
        if (storeId == null) {
            logger.error("Cannot create order with null store ID");
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (userName == null || userName.isEmpty()) {
            logger.error("Cannot create order with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (products == null || products.isEmpty()) {
            logger.error("Cannot create order with null or empty products");
            throw new IllegalArgumentException("Products cannot be null or empty");
        }

        if (orderDate == null) {
            logger.error("Cannot create order with null order date");
            throw new IllegalArgumentException("Order date cannot be null");
        }

        if (status == null) {
            logger.error("Cannot create order with null status");
            throw new IllegalArgumentException("Order status cannot be null");
        }

        logger.info("Creating new order for user: {} in store: {}", userName, storeId);

        // Create a defensive copy of the products map
        HashMap<UUID, Integer> productsCopy = new HashMap<>(products);

        Order order = new Order(storeId, userName, productsCopy, totalPrice, finalPrice,
                orderDate, status, transactionId);

        orders.put(order.getOrderId(), order);
        logger.info("Order created with ID: {}", order.getOrderId());

        return order.getOrderId();
    }

    @Override
    public UUID createOrderWithDetails(UUID storeId, String userName, Map<UUID, Integer> products,
                                       double totalPrice, double finalPrice, LocalDateTime orderDate,
                                       OrderStatus status, int transactionId, String storeName,
                                       String paymentMethod, String deliveryAddress) {
        // Validate input parameters
        if (storeId == null) {
            logger.error("Cannot create order with null store ID");
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        if (userName == null || userName.isEmpty()) {
            logger.error("Cannot create order with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (products == null || products.isEmpty()) {
            logger.error("Cannot create order with null or empty products");
            throw new IllegalArgumentException("Products cannot be null or empty");
        }

        if (orderDate == null) {
            logger.error("Cannot create order with null order date");
            throw new IllegalArgumentException("Order date cannot be null");
        }

        if (status == null) {
            logger.error("Cannot create order with null status");
            throw new IllegalArgumentException("Order status cannot be null");
        }

        logger.info("Creating new order with details for user: {} in store: {} ({})",
                userName, storeId, storeName);

        // Create a defensive copy of the products map
        HashMap<UUID, Integer> productsCopy = new HashMap<>(products);

        // Use the enhanced constructor with additional details
        Order order = new Order(storeId, userName, productsCopy, totalPrice, finalPrice,
                orderDate, status, transactionId, storeName, paymentMethod, deliveryAddress);

        orders.put(order.getOrderId(), order);
        logger.info("Order created with enhanced details. ID: {}, Store: {}, Payment: {}",
                order.getOrderId(), storeName, paymentMethod);

        return order.getOrderId();
    }

    @Override
    public boolean updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        if (orderId == null) {
            logger.error("Cannot update status for order with null ID");
            return false;
        }

        if (newStatus == null) {
            logger.error("Cannot update order status to null");
            return false;
        }

        logger.debug("Updating order status. ID: {}, newStatus: {}", orderId, newStatus);

        Order order = orders.get(orderId);
        if (order == null) {
            logger.warn("Cannot update status - order not found with ID: {}", orderId);
            return false;
        }

        boolean updated = order.updateStatus(newStatus);
        if (updated) {
            logger.info("Order status updated. ID: {}, status: {}", orderId, newStatus);
        } else {
            logger.warn("Invalid status transition for order {}: {} to {}",
                    orderId, order.getStatus(), newStatus);
        }

        return updated;
    }

    @Override
    public boolean setDeliveryId(UUID orderId, UUID deliveryId) {
        if (orderId == null) {
            logger.error("Cannot set delivery ID for order with null ID");
            return false;
        }

        logger.debug("Setting delivery ID for order: {}, deliveryId: {}", orderId, deliveryId);

        Order order = orders.get(orderId);
        if (order == null) {
            logger.warn("Cannot set delivery ID - order not found with ID: {}", orderId);
            return false;
        }

        boolean updated = order.setDeliveryTracking(deliveryId);
        if (updated) {
            logger.info("Delivery ID set for order: {}, deliveryId: {}", orderId, deliveryId);
        } else {
            logger.warn("Could not set delivery ID for order {}, invalid status: {}",
                    orderId, order.getStatus());
        }

        return updated;
    }

    @Override
    public boolean updateOrderDetails(UUID orderId, String storeName, String paymentMethod, String deliveryAddress) {
        if (orderId == null) {
            logger.error("Cannot update details for order with null ID");
            return false;
        }

        logger.debug("Updating order details for ID: {}", orderId);

        Order order = orders.get(orderId);
        if (order == null) {
            logger.warn("Cannot update details - order not found with ID: {}", orderId);
            return false;
        }

        // Update only non-null values
        if (storeName != null) {
            order.setStoreName(storeName);
            logger.debug("Updated store name for order {}: {}", orderId, storeName);
        }

        if (paymentMethod != null) {
            order.setPaymentMethod(paymentMethod);
            logger.debug("Updated payment method for order {}: {}", orderId, paymentMethod);
        }

        if (deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
            logger.debug("Updated delivery address for order {}: {}", orderId, deliveryAddress);
        }

        logger.info("Order details updated for ID: {}", orderId);
        return true;
    }

    @Override
    public List<Order> findByStoreId(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot find orders for null store ID");
            return Collections.emptyList();
        }

        logger.debug("Finding orders for store: {}", storeId);

        return orders.values().stream()
                .filter(order -> storeId.equals(order.getStoreId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            logger.error("Cannot find orders for null or empty username");
            return Collections.emptyList();
        }

        logger.debug("Finding orders for user: {}", userName);

        return orders.values().stream()
                .filter(order -> userName.equals(order.getUserName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            logger.error("Cannot find orders with null status");
            return Collections.emptyList();
        }

        logger.debug("Finding orders with status: {}", status);

        return orders.values().stream()
                .filter(order -> status == order.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            logger.error("Cannot find orders with null date range parameters");
            return Collections.emptyList();
        }

        if (startDate.isAfter(endDate)) {
            logger.error("Invalid date range: start date is after end date");
            return Collections.emptyList();
        }

        logger.debug("Finding orders between dates: {} and {}", startDate, endDate);

        return orders.values().stream()
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    return (orderDate.isEqual(startDate) || orderDate.isAfter(startDate)) &&
                            (orderDate.isEqual(endDate) || orderDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getUserPurchaseHistory(String userName) {
        if (userName == null || userName.isEmpty()) {
            logger.error("Cannot get purchase history for null or empty username");
            return Collections.emptyList();
        }

        logger.info("Getting purchase history for user: {}", userName);

        List<Order> userOrders = findByUserName(userName);

        // Sort by date, newest first
        userOrders.sort(Comparator.comparing(Order::getOrderDate).reversed());

        logger.info("Found {} orders for user: {}", userOrders.size(), userName);
        return userOrders;
    }

    @Override
    public List<Order> getStorePurchaseHistory(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot get purchase history for null store ID");
            return Collections.emptyList();
        }

        logger.info("Getting purchase history for store: {}", storeId);

        List<Order> storeOrders = findByStoreId(storeId);

        // Sort by date, newest first
        storeOrders.sort(Comparator.comparing(Order::getOrderDate).reversed());

        logger.info("Found {} orders for store: {}", storeOrders.size(), storeId);
        return storeOrders;
    }

    @Override
    public int countAll() {
        // TODO
        // implement this
        return 0;
    }

    @Override
    public double calculateTotalRevenue() {
        // TODO
        // implement this
        return 0;
    }

    @Override
    public void clear() {
        orders.clear();
        logger.info("Order repository cleared");
    }

    @Override
    public List<Order> findOrdersByUser(String username){
        if (username == null || username.isEmpty()) {
            logger.error("Cannot find orders for null or empty username");
            return Collections.emptyList();
        }

        logger.debug("Finding orders for user: {}", username);

        return orders.values().stream()
                .filter(order -> username.equals(order.getUserName()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasUserPurchasedProduct(String username, UUID productId) {
        logger.info("Checking if user {} has purchased product {}", username, productId);
        List<Order> orders = findOrdersByUser(username);
        logger.info("Found {} orders for user {}", orders.size(), username);
        for (Order order : orders) {
            logger.info("Checking order {} with status {}", order.getOrderId(), order.getStatus());
            if (order.getProductsMap().containsKey(productId) && order.getStatus() == OrderStatus.COMPLETED) {
                logger.info("User {} has purchased product {}", username, productId);
                return true;
            }
        }
        logger.info("User {} has not purchased product {}", username, productId);
        return false;
    }
}