package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.NotificationDTO;
import com.sadna_market.market.DomainLayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final INotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(INotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send real-time notification to user
     */
    public void sendNotification(String username, String title, String message,
                                 NotificationType type, UUID relatedStoreId,
                                 UUID relatedOrderId, UUID relatedProductId) {
        try {
            // Create and save notification
            Notification notification = new Notification(
                    username, title, message, type,
                    relatedStoreId, relatedOrderId, relatedProductId
            );
            notificationRepository.save(notification);

            // Convert to DTO for frontend
            NotificationDTO notificationDTO = convertToDTO(notification);

            // Send real-time notification via WebSocket
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    notificationDTO
            );

            logger.info("Notification sent to user {}: {}", username, title);

        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", username, e.getMessage(), e);
        }
    }

    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getUserNotifications(String username) {
        try {
            List<Notification> notifications = notificationRepository.findByRecipientUsername(username);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get notifications for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to get notifications", e);
        }
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationDTO> getUnreadNotifications(String username) {
        try {
            List<Notification> notifications = notificationRepository.findUnreadByRecipientUsername(username);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get unread notifications for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to get unread notifications", e);
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(UUID notificationId) {
        try {
            notificationRepository.markAsRead(notificationId);
            logger.info("Notification {} marked as read", notificationId);
        } catch (Exception e) {
            logger.error("Failed to mark notification {} as read: {}", notificationId, e.getMessage(), e);
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(String username) {
        try {
            notificationRepository.markAllAsReadForUser(username);
            logger.info("All notifications marked as read for user {}", username);
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to mark all notifications as read", e);
        }
    }

    /**
     * Get unread notification count for a user
     */
    public int getUnreadCount(String username) {
        try {
            return notificationRepository.countUnreadForUser(username);
        } catch (Exception e) {
            logger.error("Failed to get unread count for user {}: {}", username, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send offline notifications when user comes online
     */
    public void sendOfflineNotifications(String username) {
        try {
            List<NotificationDTO> unreadNotifications = getUnreadNotifications(username);

            for (NotificationDTO notification : unreadNotifications) {
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/notifications",
                        notification
                );
            }

            logger.info("Sent {} offline notifications to user {}", unreadNotifications.size(), username);

        } catch (Exception e) {
            logger.error("Failed to send offline notifications to user {}: {}", username, e.getMessage(), e);
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getNotificationId(),
                notification.getRecipientUsername(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getRelatedStoreId(),
                notification.getRelatedOrderId(),
                notification.getRelatedProductId()
        );
    }
}