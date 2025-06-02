package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.MessageDTO;
import com.sadna_market.market.ApplicationLayer.Requests.MessageReplyRequest;
import com.sadna_market.market.ApplicationLayer.Requests.MessageRequest;
import com.sadna_market.market.ApplicationLayer.Requests.SendMessageRequest;
import com.sadna_market.market.DomainLayer.DomainServices.MessageService;
import com.sadna_market.market.DomainLayer.Events.DomainEventPublisher;
import com.sadna_market.market.DomainLayer.Events.MessageSentEvent;
import com.sadna_market.market.DomainLayer.Message;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(MessageApplicationService.class);

    private final AuthenticationAdapter authentication;
    private final MessageService messageService;

    @Autowired
    public MessageApplicationService(AuthenticationAdapter authentication,
                                     MessageService messageService) {
        this.authentication = authentication;
        this.messageService = messageService;
    }

    /**
     * Sends a message from a user to a store using SendMessageRequest
     *
     * @param username The username of the sender
     * @param token Authentication token
     * @param request The send message request containing store ID and content
     * @return Response object with success/error status and message data
     */
    public Response<MessageDTO> sendMessage(String username, String token, SendMessageRequest request) {
        logger.info("Processing send message request from user: {} to store: {}", username, request.getReceiverStoreId());

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

//            // Publish the event
//            DomainEventPublisher.publish(
//                    new MessageSentEvent(username, request.getReceiverStoreId(), request.getContent())
//            );

            // Use the domain service to send the message
            Message message = messageService.sendMessage(username, request.getReceiverStoreId(), request.getContent());
            MessageDTO messageDTO = new MessageDTO(message);

            logger.info("Message sent successfully by user: {}", username);
            return Response.success(messageDTO);
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            return Response.error("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Sends a message from a user to a store using MessageRequest
     *
     * @param username The username of the sender
     * @param token Authentication token
     * @param request The message request containing store ID and content
     * @return Response object with success/error status and message data
     */
    public Response<MessageDTO> sendMessage(String username, String token, MessageRequest request) {
        logger.info("Processing send message request from user: {} to store: {}", username, request.getStoreId());

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

//            // Publish the event
//            DomainEventPublisher.publish(
//                    new MessageSentEvent(username, request.getStoreId(), request.getContent())
//            );

            // Use the domain service to send the message
            Message message = messageService.sendMessage(username, request.getStoreId(), request.getContent());
            MessageDTO messageDTO = new MessageDTO(message);

            logger.info("Message sent successfully by user: {}", username);
            return Response.success(messageDTO);
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            return Response.error("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Replies to a message
     *
     * @param username The username of the replier
     * @param token Authentication token
     * @param request The reply request containing message ID and reply content
     * @return Response object with success/error status
     */
    public Response<String> replyToMessage(String username, String token, MessageReplyRequest request) {
        logger.info("Processing reply to message request from user: {} to message: {}", username, request.getMessageId());

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            boolean success = messageService.replyToMessage(
                    request.getMessageId(), username, request.getContent());

            if (success) {
                logger.info("Reply sent successfully by user: {}", username);
                return Response.success("Reply sent successfully");
            } else {
                logger.error("Failed to send reply");
                return Response.error("Failed to send reply");
            }
        } catch (Exception e) {
            logger.error("Error sending reply: {}", e.getMessage(), e);
            return Response.error("Failed to send reply: " + e.getMessage());
        }
    }

    /**
     * Gets all messages sent by a user
     *
     * @param username The username of the user
     * @param token Authentication token
     * @return Response object with success/error status and messages data
     */
    public Response<List<MessageDTO>> getUserMessages(String username, String token) {
        logger.info("Getting messages for user: {}", username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            List<Message> messages = messageService.getUserMessages(username);
            List<MessageDTO> messageDTOs = convertToMessageDTOs(messages);

            logger.info("Retrieved {} messages for user: {}", messages.size(), username);
            return Response.success(messageDTOs);
        } catch (Exception e) {
            logger.error("Error getting user messages: {}", e.getMessage(), e);
            return Response.error("Failed to get messages: " + e.getMessage());
        }
    }

    /**
     * Gets all messages for a store
     *
     * @param username The username of the requesting user (must be a store owner/manager)
     * @param token Authentication token
     * @param storeId The ID of the store
     * @return Response object with success/error status and messages data
     */
    public Response<List<MessageDTO>> getStoreMessages(String username, String token, UUID storeId) {
        logger.info("Getting messages for store: {} requested by user: {}", storeId, username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            List<Message> messages = messageService.getStoreMessages(username, storeId);
            List<MessageDTO> messageDTOs = convertToMessageDTOs(messages);

            logger.info("Retrieved {} messages for store: {}", messages.size(), storeId);
            return Response.success(messageDTOs);
        } catch (Exception e) {
            logger.error("Error getting store messages: {}", e.getMessage(), e);
            return Response.error("Failed to get store messages: " + e.getMessage());
        }
    }

    /**
     * Gets the conversation between a user and a store
     *
     * @param username The username of the user
     * @param token Authentication token
     * @param storeId The ID of the store
     * @return Response object with success/error status and conversation data
     */
    public Response<List<MessageDTO>> getUserStoreConversation(String username, String token, UUID storeId) {
        logger.info("Getting conversation between user: {} and store: {}", username, storeId);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            List<Message> messages = messageService.getUserStoreConversation(username, storeId);
            List<MessageDTO> messageDTOs = convertToMessageDTOs(messages);

            logger.info("Retrieved {} messages in conversation", messages.size());
            return Response.success(messageDTOs);
        } catch (Exception e) {
            logger.error("Error getting conversation: {}", e.getMessage(), e);
            return Response.error("Failed to get conversation: " + e.getMessage());
        }
    }

    /**
     * Gets all unanswered messages for a store
     *
     * @param username The username of the requesting user (must be a store owner/manager)
     * @param token Authentication token
     * @param storeId The ID of the store
     * @return Response object with success/error status and messages data
     */
    public Response<List<MessageDTO>> getUnansweredStoreMessages(String username, String token, UUID storeId) {
        logger.info("Getting unanswered messages for store: {} requested by user: {}", storeId, username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            List<Message> messages = messageService.getUnansweredStoreMessages(username, storeId);
            List<MessageDTO> messageDTOs = convertToMessageDTOs(messages);

            logger.info("Retrieved {} unanswered messages for store: {}", messages.size(), storeId);
            return Response.success(messageDTOs);
        } catch (Exception e) {
            logger.error("Error getting unanswered store messages: {}", e.getMessage(), e);
            return Response.error("Failed to get unanswered store messages: " + e.getMessage());
        }
    }

    /**
     * Gets all unread messages for a store
     *
     * @param username The username of the requesting user (must be a store owner/manager)
     * @param token Authentication token
     * @param storeId The ID of the store
     * @return Response object with success/error status and messages data
     */
    public Response<List<MessageDTO>> getUnreadStoreMessages(String username, String token, UUID storeId) {
        logger.info("Getting unread messages for store: {} requested by user: {}", storeId, username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            List<Message> messages = messageService.getUnreadMessagesForStore(username, storeId);
            List<MessageDTO> messageDTOs = convertToMessageDTOs(messages);

            logger.info("Retrieved {} unread messages for store: {}", messages.size(), storeId);
            return Response.success(messageDTOs);
        } catch (Exception e) {
            logger.error("Error getting unread store messages: {}", e.getMessage(), e);
            return Response.error("Failed to get unread store messages: " + e.getMessage());
        }
    }

    /**
     * Deletes a message
     *
     * @param username The username of the requesting user
     * @param token Authentication token
     * @param messageId The ID of the message to delete
     * @return Response object with success/error status
     */
    public Response<String> deleteMessage(String username, String token, UUID messageId) {
        logger.info("Processing delete message request from user: {} for message: {}", username, messageId);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            boolean success = messageService.deleteMessage(username, messageId);

            if (success) {
                logger.info("Message deleted successfully by user: {}", username);
                return Response.success("Message deleted successfully");
            } else {
                logger.error("Failed to delete message");
                return Response.error("Failed to delete message");
            }
        } catch (Exception e) {
            logger.error("Error deleting message: {}", e.getMessage(), e);
            return Response.error("Failed to delete message: " + e.getMessage());
        }
    }

    /**
     * Marks a message as read
     *
     * @param username The username of the requesting user
     * @param token Authentication token
     * @param messageId The ID of the message to mark as read
     * @return Response object with success/error status
     */
    public Response<String> markMessageAsRead(String username, String token, UUID messageId) {
        logger.info("Processing mark message as read request from user: {} for message: {}",
                username, messageId);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            boolean success = messageService.markMessageAsRead(username, messageId);

            if (success) {
                logger.info("Message marked as read successfully by user: {}", username);
                return Response.success("Message marked as read successfully");
            } else {
                logger.error("Failed to mark message as read");
                return Response.error("Failed to mark message as read");
            }
        } catch (Exception e) {
            logger.error("Error marking message as read: {}", e.getMessage(), e);
            return Response.error("Failed to mark message as read: " + e.getMessage());
        }
    }

    /**
     * Marks all messages for a store as read
     *
     * @param username The username of the requesting user
     * @param token Authentication token
     * @param storeId The ID of the store
     * @return Response object with success/error status
     */
    public Response<String> markAllStoreMessagesAsRead(String username, String token, UUID storeId) {
        logger.info("Processing mark all store messages as read request from user: {} for store: {}",
                username, storeId);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            int count = messageService.markAllStoreMessagesAsRead(username, storeId);
            logger.info("{} messages marked as read for store {} by user {}",
                    count, storeId, username);
            return Response.success(count + " messages marked as read successfully");
        } catch (Exception e) {
            logger.error("Error marking all store messages as read: {}", e.getMessage(), e);
            return Response.error("Failed to mark all store messages as read: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert a list of Message entities to MessageDTOs
     *
     * @param messages List of Message entities
     * @return List of MessageDTOs
     */
    private List<MessageDTO> convertToMessageDTOs(List<Message> messages) {
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }

    public void clear() {
        messageService.clear();
    }
}