package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing orders in the market system.
 * Provides methods for creating, retrieving, updating and managing orders.
 */
public interface IOrderRepository {
    
    /**
     * Saves an order to the repository
     * 
     * @param order The order to save
     * @return The saved order
     */
    Order save(Order order);
    
    /**
     * Finds an order by its ID
     * 
     * @param orderId The order ID to look for
     * @return Optional containing the order if found
     */
    Optional<Order> findById(UUID orderId);
    
    /**
     * Gets all orders in the system
     * 
     * @return List of all orders
     */
    List<Order> findAll();
    
    /**
     * Deletes an order by its ID
     * 
     * @param orderId The order ID to delete
     */
    void deleteById(UUID orderId);
    
    /**
     * Checks if an order exists
     * 
     * @param orderId The order ID to check
     * @return true if the order exists
     */
    boolean exists(UUID orderId);
    
    /**
     * Creates a new order with the given information
     * 
     * @param storeId The ID of the store
     * @param userName The username of the buyer
     * @param products Map of product IDs to quantities
     * @param totalPrice The total price before discounts
     * @param finalPrice The final price after discounts
     * @param orderDate The date and time when the order was placed
     * @param status The initial status of the order
     * @param paymentId The payment transaction ID (if any)
     * @return The ID of the newly created order
     */

    UUID createOrder(UUID storeId, String userName, Map<UUID, Integer> products,
                 double totalPrice, double finalPrice, LocalDateTime orderDate, 
                 OrderStatus status, String paymentId);
    
    /**
     * Updates the status of an order
     * 
     * @param orderId The order ID
     * @param newStatus The new status
     * @return true if the update was successful
     */
    boolean updateOrderStatus(UUID orderId, OrderStatus newStatus);
    
    /**
     * Sets the delivery ID for an order
     * 
     * @param orderId The order ID
     * @param deliveryId The delivery tracking ID
     * @return true if the update was successful
     */
    boolean setDeliveryId(UUID orderId, UUID deliveryId);
    
    /**
     * Finds orders by store ID
     * 
     * @param storeId The store ID
     * @return List of orders for the specified store
     */
    List<Order> findByStoreId(UUID storeId);
    
    /**
     * Finds orders by username (buyer)
     * 
     * @param userName The username of the buyer
     * @return List of orders placed by the specified user
     */
    List<Order> findByUserName(String userName);
    
    /**
     * Finds orders by status
     * 
     * @param status The order status
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Finds orders placed within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of orders placed within the date range
     */
    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets the purchase history of a user
     * 
     * @param userName The username of the buyer
     * @return List of orders placed by the user
     */
    List<Order> getUserPurchaseHistory(String userName);
    
    /**
     * Gets the purchase history of a store
     * 
     * @param storeId The store ID
     * @return List of orders placed at the store
     */
    List<Order> getStorePurchaseHistory(UUID storeId);
}