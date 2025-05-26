package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.ApplicationLayer.MessageApplicationService;
import com.sadna_market.market.ApplicationLayer.DTOs.MessageDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // Update for production
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageApplicationService messageApplicationService;

    @Autowired
    public MessageController(MessageApplicationService messageApplicationService) {
        this.messageApplicationService = messageApplicationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Response<MessageDTO>> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Use the SendMessageRequest overload directly
            Response<MessageDTO> response = messageApplicationService.sendMessage(
                    request.getSenderUsername(),
                    token,
                    request
            );

            if (response.isError()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<Response<String>> replyToMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageReplyRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Replying to message {} by user {}", messageId, request.getSenderUsername());

        try {
            // Set the messageId in the request if not already set
            request.setMessageId(messageId);

            Response<String> response = messageApplicationService.replyToMessage(
                    request.getSenderUsername(),
                    token,
                    request
            );

            if (response.isError()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error replying to message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to reply to message: " + e.getMessage()));
        }
    }

    @GetMapping("/user-to-store")
    public ResponseEntity<Response<List<MessageDTO>>> getMessagesBetweenUserAndStore(
            @RequestParam String username,
            @RequestParam UUID storeID,
            @RequestHeader("Authorization") String token) {
        logger.info("Fetching messages between user {} and store {}", username, storeID);
        try {
            Response<List<MessageDTO>> response = messageApplicationService.getUserStoreConversation(
                    username, token, storeID);

            if (response.isError()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch messages: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{username}/sent")
    public ResponseEntity<Response<List<MessageDTO>>> getAllSentMessagesByUser(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {
        logger.info("Fetching sent messages for user {}", username);
        try {
            Response<List<MessageDTO>> response = messageApplicationService.getUserMessages(username, token);

            if (response.isError()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching sent messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch sent messages: " + e.getMessage()));
        }
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Response<String>> markMessageAsRead(
            @PathVariable UUID messageId,
            @RequestBody MarkMessageReadRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("Marking message {} as read by user {}", messageId, request.getUsername());
        try {
            Response<String> response = messageApplicationService.markMessageAsRead(
                    request.getUsername(), token, messageId);

            if (response.isError()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking message as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to mark message as read: " + e.getMessage()));
        }
    }

    @PatchMapping("/{messageId}/report-violation")
    public ResponseEntity<Response<String>> reportViolation(
            @PathVariable UUID messageId,
            @RequestBody ReportViolationRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("User {} is reporting violation on message {}", request.getReporterUsername(), messageId);
        try {
            // Implementation would call a violation reporting service
            // For now, return success
            return ResponseEntity.ok(Response.success("Violation reported successfully"));
        } catch (Exception e) {
            logger.error("Error reporting message violation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to report violation: " + e.getMessage()));
        }
    }
}