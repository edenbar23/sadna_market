package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationService.class);

    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IOrderRepository orderRepository;
    private final IMessageRepository messageRepository;

    @Autowired
    public OrderNotificationService(
            IUserRepository userRepository,
            IStoreRepository storeRepository,
            IOrderRepository orderRepository,
            IMessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        logger.info("OrderNotificationService initialized");
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Subscribe to order-related events
        DomainEventPublisher.subscribe(OrderProcessedEvent.class, this::handleOrderProcessed);
        logger.info("OrderNotificationService subscribed to OrderProcessedEvent");
    }

    /**
     * Event handler for OrderProcessedEvent
     * Performs post-purchase actions like notifications
     */
    private void handleOrderProcessed(OrderProcessedEvent event) {
        logger.info("Handling order processed event for order: {}", event.getOrderId());

        try {
            // Get the order details
            Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
            if (orderOpt.isEmpty()) {
                logger.error("Order not found: {}", event.getOrderId());
                return;
            }

            Order order = orderOpt.get();

            notifyBuyer(order);
            notifyStore(order);

            logger.info("Order processed notifications sent successfully for order: {}", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error handling order processed event: {}", e.getMessage(), e);
        }
    }

    private void notifyBuyer(Order order) {
        if (order.getUserName() == null || order.getUserName().isEmpty() ||
                order.getUserName().startsWith(UUID.randomUUID().toString())) {
            logger.info("Skipping notification for guest user");
            return;
        }

        Optional<User> userOpt = userRepository.findByUsername(order.getUserName());
        if (userOpt.isEmpty()) {
            logger.warn("User not found for notification: {}", order.getUserName());
            return;
        }

        // Get store name for better notification
        String storeName = "a store";
        Optional<Store> storeOpt = storeRepository.findById(order.getStoreId());
        if (storeOpt.isPresent()) {
            storeName = storeOpt.get().getName();
        }

        // Create a message/notification to the user
        String messageContent = String.format(
                "Your order #%s from %s has been processed successfully. Total: $%.2f",
                order.getOrderId().toString().substring(0, 8),
                storeName,
                order.getFinalPrice()
        );

        // Create a direct message to the user
        Message message = new Message("System", UUID.randomUUID(), messageContent);
        messageRepository.save(message);

        logger.info("Notification sent to buyer: {}", order.getUserName());
    }

    private void notifyStore(Order order) {
        Optional<Store> storeOpt = storeRepository.findById(order.getStoreId());
        if (storeOpt.isEmpty()) {
            logger.warn("Store not found for notification: {}", order.getStoreId());
            return;
        }

        Store store = storeOpt.get();

        // Create a single message for the store
        String messageContent = String.format(
                "New order #%s has been processed. Customer: %s, Total: $%.2f",
                order.getOrderId().toString().substring(0, 8),
                order.getUserName(),
                order.getFinalPrice()
        );

        // Send a single message to the store
        Message message = new Message("System", store.getStoreId(), messageContent);
        messageRepository.save(message);

        logger.info("Order notification sent to store: {}", store.getName());
    }

}