// src/main/java/com/sadna_market/market/ApplicationLayer/NotificationEventListeners.java
package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.Events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class NotificationEventListeners {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListeners.class);

    private final NotificationService notificationService;
    private final IStoreRepository storeRepository;
    private final IUserRepository userRepository;

    @Autowired
    public NotificationEventListeners(NotificationService notificationService,
                                      IStoreRepository storeRepository,
                                      IUserRepository userRepository) {
        this.notificationService = notificationService;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Subscribe to order processed events
        DomainEventPublisher.subscribe(OrderProcessedEvent.class, this::handleOrderProcessed);

        // Subscribe to store closed/reopened events
        DomainEventPublisher.subscribe(StoreCreatedEvent.class, this::handleStoreCreated);
        DomainEventPublisher.subscribe(StoreClosedEvent.class, this::handleStoreClosed);
        DomainEventPublisher.subscribe(StoreReopenedEvent.class, this::handleStoreReopened);

        // Subscribe to message events
        DomainEventPublisher.subscribe(MessageSentEvent.class, this::handleMessageSent);
        DomainEventPublisher.subscribe(DirectMessageEvent.class, this::handleDirectMessage);

        // Subscribe to violation events
        DomainEventPublisher.subscribe(ViolationReplyEvent.class, this::handleViolationReply);

        // Subscribe to role management events
        DomainEventPublisher.subscribe(RoleAssignedEvent.class, this::handleRoleAssigned);
        DomainEventPublisher.subscribe(RoleRemovedEvent.class, this::handleRoleRemoved);

        logger.info("Notification event listeners initialized and subscribed to domain events");
    }

    /**
     * Handle store created - notify all admins
     */
    private void handleStoreCreated(StoreCreatedEvent event) {
        try {
            String title = "New Store Created!";
            String message = String.format("User %s has created a new store: '%s'",
                    event.getFounderUsername(), event.getStoreName());

            // Get all admin users and notify them
            List<User> adminUsers = userRepository.findByIsAdmin(true);

            for (User admin : adminUsers) {
                notificationService.sendNotification(
                        admin.getUserName(),
                        title,
                        message,
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        event.getStoreId(),
                        null,
                        null
                );
            }

            logger.info("Store creation notifications sent for store: {}", event.getStoreName());

        } catch (Exception e) {
            logger.error("Failed to handle store created event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle order processed - notify store owners/managers
     */
    private void handleOrderProcessed(OrderProcessedEvent event) {
        try {
            Store store = storeRepository.findById(event.getStoreId())
                    .orElse(null);

            if (store == null) {
                logger.warn("Store not found for order processed event: {}", event.getStoreId());
                return;
            }

            String title = "New Order Received!";
            String message = String.format("Customer %s placed an order in your store.",
                    event.getUsername());

            // Notify store founder
            if (store.getFounderUserName() != null) {
                notificationService.sendNotification(
                        store.getFounderUserName(),
                        title,
                        message,
                        NotificationType.ORDER_RECEIVED,
                        event.getStoreId(),
                        event.getOrderId(),
                        null
                );
            }

            // Notify store owners
            for (String ownerUsername : store.getOwnerUsernames()) {
                if (!ownerUsername.equals(store.getFounderUserName())) {
                    notificationService.sendNotification(
                            ownerUsername,
                            title,
                            message,
                            NotificationType.ORDER_RECEIVED,
                            event.getStoreId(),
                            event.getOrderId(),
                            null
                    );
                }
            }

            // Notify store managers
            for (String managerUsername : store.getManagerUsernames()) {
                notificationService.sendNotification(
                        managerUsername,
                        title,
                        message,
                        NotificationType.ORDER_RECEIVED,
                        event.getStoreId(),
                        event.getOrderId(),
                        null
                );
            }

        } catch (Exception e) {
            logger.error("Failed to handle order processed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle store closed - notify all store personnel
     */
    private void handleStoreClosed(StoreClosedEvent event) {
        try {
            Store store = storeRepository.findById(event.getStoreId())
                    .orElse(null);

            if (store == null) {
                logger.warn("Store not found for store closed event: {}", event.getStoreId());
                return;
            }

            String title = "Store Closed";
            String message = String.format("The store '%s' has been closed.", store.getName());

            // Notify all store personnel (founders, owners, managers)
            notifyStorePersonnel(store, title, message, NotificationType.STORE_CLOSED);

        } catch (Exception e) {
            logger.error("Failed to handle store closed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle store reopened - notify all store personnel
     */
    private void handleStoreReopened(StoreReopenedEvent event) {
        try {
            Store store = storeRepository.findById(event.getStoreId())
                    .orElse(null);

            if (store == null) {
                logger.warn("Store not found for store reopened event: {}", event.getStoreId());
                return;
            }

            String title = "Store Reopened!";
            String message = String.format("The store '%s' has been reopened and is now active.",
                    store.getName());

            // Notify all store personnel
            notifyStorePersonnel(store, title, message, NotificationType.STORE_REOPENED);

        } catch (Exception e) {
            logger.error("Failed to handle store reopened event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle message sent to store - notify store personnel who can view messages
     */
    private void handleMessageSent(MessageSentEvent event) {
        try {
            Store store = storeRepository.findById(event.getStoreId())
                    .orElse(null);

            if (store == null) {
                logger.warn("Store not found for message sent event: {}", event.getStoreId());
                return;
            }

            String title = "New Message Received";
            String message = String.format("You have received a new message from %s in store '%s'.",
                    event.getSenderUsername(), store.getName());

            // Notify store personnel who can view messages
            notifyStorePersonnel(store, title, message, NotificationType.MESSAGE_RECEIVED);

        } catch (Exception e) {
            logger.error("Failed to handle message sent event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle direct message between users
     */
    private void handleDirectMessage(DirectMessageEvent event) {
        try {
            String title = "New Direct Message";
            String message = String.format("You have received a new message from %s.",
                    event.getSender());

            notificationService.sendNotification(
                    event.getRecipient(),
                    title,
                    message,
                    NotificationType.MESSAGE_RECEIVED,
                    null,
                    null,
                    null
            );

        } catch (Exception e) {
            logger.error("Failed to handle direct message event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle violation reply from admin
     */
    private void handleViolationReply(ViolationReplyEvent event) {
        try {
            String title = "Admin Response to Your Report";
            String message = String.format("An administrator has replied to your violation report: %s",
                    event.getMessage());

            notificationService.sendNotification(
                    event.getUser(),
                    title,
                    message,
                    NotificationType.VIOLATION_REPLY,
                    null,
                    null,
                    null
            );

        } catch (Exception e) {
            logger.error("Failed to handle violation reply event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle role assigned - notify user when they get a new role
     */
    private void handleRoleAssigned(RoleAssignedEvent event) {
        try {
            String title = "New Role Assigned!";
            String message = String.format("You have been assigned as %s for store '%s' by %s.",
                    event.getRoleType().toString().toLowerCase().replace("_", " "),
                    event.getStoreName(),
                    event.getAssignedBy());

            notificationService.sendNotification(
                    event.getUsername(),
                    title,
                    message,
                    NotificationType.ROLE_ASSIGNED,
                    event.getStoreId(),
                    null,
                    null
            );

        } catch (Exception e) {
            logger.error("Failed to handle role assigned event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle role removed - notify user when their role is removed
     */
    private void handleRoleRemoved(RoleRemovedEvent event) {
        try {
            String title = "Role Removed";
            String message = String.format("Your %s role for store '%s' has been removed by %s.",
                    event.getRoleType().toString().toLowerCase().replace("_", " "),
                    event.getStoreName(),
                    event.getRemovedBy());

            notificationService.sendNotification(
                    event.getUsername(),
                    title,
                    message,
                    NotificationType.ROLE_REMOVED,
                    event.getStoreId(),
                    null,
                    null
            );

        } catch (Exception e) {
            logger.error("Failed to handle role removed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to notify all store personnel
     */
    private void notifyStorePersonnel(Store store, String title, String message, NotificationType type) {
        // Notify founder
        if (store.getFounderUserName() != null) {
            notificationService.sendNotification(
                    store.getFounderUserName(),
                    title,
                    message,
                    type,
                    store.getStoreId(),
                    null,
                    null
            );
        }

        // Notify owners (excluding founder to avoid duplicates)
        for (String ownerUsername : store.getOwnerUsernames()) {
            if (!ownerUsername.equals(store.getFounderUserName())) {
                notificationService.sendNotification(
                        ownerUsername,
                        title,
                        message,
                        type,
                        store.getStoreId(),
                        null,
                        null
                );
            }
        }

        // Notify managers
        for (String managerUsername : store.getManagerUsernames()) {
            notificationService.sendNotification(
                    managerUsername,
                    title,
                    message,
                    type,
                    store.getStoreId(),
                    null,
                    null
            );
        }
    }
}