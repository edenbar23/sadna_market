package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.DomainLayer.DomainServices.MessageService;
import com.sadna_market.market.DomainLayer.Message;

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
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<Response<String>> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            messageService.sendMessage(
                    request.getSenderUsername(),
                    request.getReceiverStoreId(),
                    request.getContent()
            );
            return ResponseEntity.ok(Response.success("Message sent successfully"));
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to send message: " + e.getMessage()));
        }
    }
    

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<Response<Boolean>> replyToMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageReplyRequest request) {
        logger.info("Replying to message {} by user {}", messageId, request.getSenderUsername());
        try {
            boolean success = messageService.replyToMessage(
                messageId,
                request.getSenderUsername(),
                request.getContent()
            );
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Response.success(true));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Response.error("Failed to reply to message"));
            }
        } catch (Exception e) {
            logger.error("Error replying to message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to reply to message: " + e.getMessage()));
        }
    }

    

    @GetMapping("/user-to-store")
    public ResponseEntity<Response<List<Message>>> getMessagesBetweenUserAndStore(
            @RequestParam String username,
            @RequestParam UUID storeID) {
        logger.info("Fetching messages between user {} and store {}", username, storeID);
        try {
            List<Message> messages = messageService.getUserStoreConversation(username, storeID);
            return ResponseEntity.ok(Response.success(messages));
        } catch (Exception e) {
            logger.error("Error fetching messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch messages: " + e.getMessage()));
        }
    }

    @GetMapping("/store/{storeName}/received")
    public ResponseEntity<Response<List<Message>>> getAllReceivedMessagesForStore(
            @PathVariable UUID storeID,
            @RequestParam String username) {
        logger.info("Fetching received messages for store {} by user {}", storeID, username);
        try {
            List<Message> messages = messageService.getStoreMessages(username, storeID);
            return ResponseEntity.ok(Response.success(messages));
        } catch (IllegalArgumentException e) {
            logger.error("Store not found", e);
            return ResponseEntity.badRequest().body(Response.error(e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Permission denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching store messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch store messages: " + e.getMessage()));
        }
    }
    

    @GetMapping("/user/{username}/sent")
    public ResponseEntity<Response<List<Message>>> getAllSentMessagesByUser(@PathVariable String username) {
        logger.info("Fetching sent messages for user {}", username);
        try {
            List<Message> messages = messageService.getUserMessages(username);
            return ResponseEntity.ok(Response.success(messages));
        } catch (Exception e) {
            logger.error("Error fetching sent messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch sent messages: " + e.getMessage()));
        }
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Response<String>> markMessageAsRead(
            @PathVariable UUID messageId,
            @RequestParam String username) {
        logger.info("Marking message {} as read by user {}", messageId, username);
        try {
            messageService.markMessageAsRead(username, messageId);
            return ResponseEntity.ok(Response.success("Message marked as read"));
        } catch (Exception e) {
            logger.error("Error marking message as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to mark message as read: " + e.getMessage()));
        }
    }

    @PatchMapping("/{messageId}/report-violation")
    public ResponseEntity<Response<String>> reportViolation(
            @PathVariable UUID messageId,
            @RequestBody ReportViolationRequest request) {
        logger.info("User {} is reporting violation on message {}", request.getReporterUsername(), messageId);
        try {
            messageService.reportViolation(messageId, request.getReporterUsername(), request.getReason());
            return ResponseEntity.ok(Response.success("Violation reported successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for reporting violation", e);
            return ResponseEntity.badRequest().body(Response.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reporting message violation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to report violation: " + e.getMessage()));
        }
    }
}

