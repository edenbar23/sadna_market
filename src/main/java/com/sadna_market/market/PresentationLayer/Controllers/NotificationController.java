package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.DTOs.NotificationDTO;
import com.sadna_market.market.ApplicationLayer.NotificationService;
import com.sadna_market.market.ApplicationLayer.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * WebSocket message mapping for user connection
     */
    @MessageMapping("/connect")
    public void handleUserConnect(@Payload Map<String, String> payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = payload.get("username");

            if (username != null) {
                // Store username in session
                headerAccessor.getSessionAttributes().put("username", username);

                // Send offline notifications to user
                notificationService.sendOfflineNotifications(username);

                logger.info("User {} connected to WebSocket and received offline notifications", username);
            }
        } catch (Exception e) {
            logger.error("Error handling user connection: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all notifications for authenticated user
     */
    @GetMapping("/{username}")
    public ResponseEntity<Response<List<NotificationDTO>>> getUserNotifications(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        try {
            // TODO: Add token validation here if needed

            List<NotificationDTO> notifications = notificationService.getUserNotifications(username);

            return ResponseEntity.ok(Response.success(notifications));

        } catch (Exception e) {
            logger.error("Failed to get notifications for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to get notifications: " + e.getMessage()));
        }
    }

    /**
     * Get unread notifications for authenticated user
     */
    @GetMapping("/{username}/unread")
    public ResponseEntity<Response<List<NotificationDTO>>> getUnreadNotifications(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(username);

            return ResponseEntity.ok(Response.success(notifications));

        } catch (Exception e) {
            logger.error("Failed to get unread notifications for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to get unread notifications: " + e.getMessage()));
        }
    }

    /**
     * Get unread notification count for authenticated user
     */
    @GetMapping("/{username}/unread/count")
    public ResponseEntity<Response<Integer>> getUnreadCount(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        try {
            int count = notificationService.getUnreadCount(username);

            return ResponseEntity.ok(Response.success(count));

        } catch (Exception e) {
            logger.error("Failed to get unread count for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to get unread count: " + e.getMessage()));
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Response<String>> markAsRead(
            @PathVariable UUID notificationId,
            @RequestHeader("Authorization") String token) {

        try {
            notificationService.markAsRead(notificationId);

            return ResponseEntity.ok(Response.success("Notification marked as read"));

        } catch (Exception e) {
            logger.error("Failed to mark notification {} as read: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to mark notification as read: " + e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read for user
     */
    @PutMapping("/{username}/read-all")
    public ResponseEntity<Response<String>> markAllAsRead(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        try {
            notificationService.markAllAsRead(username);

            return ResponseEntity.ok(Response.success("All notifications marked as read"));

        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to mark all notifications as read: " + e.getMessage()));
        }
    }

    /**
     * Send test notification (for debugging)
     */
    @PostMapping("/test/{username}")
    public ResponseEntity<Response<String>> sendTestNotification(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        try {
            notificationService.sendNotification(
                    username,
                    "Test Notification",
                    "This is a test notification to verify the system is working.",
                    com.sadna_market.market.DomainLayer.NotificationType.SYSTEM_ANNOUNCEMENT,
                    null,
                    null,
                    null
            );

            return ResponseEntity.ok(Response.success("Test notification sent"));

        } catch (Exception e) {
            logger.error("Failed to send test notification to user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Response.error("Failed to send test notification: " + e.getMessage()));
        }
    }
}