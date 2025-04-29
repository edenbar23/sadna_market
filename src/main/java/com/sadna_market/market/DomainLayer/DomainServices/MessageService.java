package com.sadna_market.market.DomainLayer.DomainServices;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private static MessageService instance;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final IMessageRepository messageRepository;
    private final IStoreRepository storeRepository;
    private final IUserRepository userRepository;
    private RepositoryConfiguration RC;

    private MessageService(RepositoryConfiguration RC) {
        this.messageRepository = RC.messageRepository();
        this.storeRepository = RC.storeRepository();
        this.userRepository = RC.userRepository();
    }

    public static MessageService getInstance(RepositoryConfiguration RC) {
        if (instance == null) {
            instance = new MessageService( RC);
        }
        return instance;
    }

    /**
     * Sends a message from a user to a store
     *
     * @param senderUsername The username of the sender
     * @param storeId The ID of the store
     * @param content The message content
     * @return The created message
     * @throws IllegalArgumentException if the user or store doesn't exist
     */
    public Message sendMessage(String senderUsername, UUID storeId, String content) {
        logger.info("User {} sending message to store {}", senderUsername, storeId);

        // Verify user exists
        if (!userRepository.contains(senderUsername)) {
            logger.error("User {} not found", senderUsername);
            throw new IllegalArgumentException("User not found: " + senderUsername);
        }

        // Verify store exists and is active
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store {} not found", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        if (!store.isActive()) {
            logger.error("Store {} is not active", storeId);
            throw new IllegalStateException("Cannot send message to inactive store");
        }

        // Create and save the message
        Message message = new Message(senderUsername, storeId, content);
        messageRepository.save(message);

        logger.info("Message sent successfully: {}", message.getMessageId());
        return message;
    }

    /**
     * Replies to a message
     *
     * @param messageId The ID of the message to reply to
     * @param replyAuthor The username of the reply author
     * @param replyContent The content of the reply
     * @return true if the reply was added successfully
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean replyToMessage(UUID messageId, String replyAuthor, String replyContent) {
        logger.info("User {} replying to message {}", replyAuthor, messageId);

        // Verify user exists
        if (!userRepository.contains(replyAuthor)) {
            logger.error("User {} not found", replyAuthor);
            throw new IllegalArgumentException("User not found: " + replyAuthor);
        }

        // Verify the message exists
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message {} not found", messageId);
                    return new IllegalArgumentException("Message not found: " + messageId);
                });

        // Verify the store exists and is active
        Store store = storeRepository.findById(message.getStoreId())
                .orElseThrow(() -> {
                    logger.error("Store {} not found", message.getStoreId());
                    return new IllegalArgumentException("Store not found: " + message.getStoreId());
                });

        if (!store.isActive()) {
            logger.error("Store {} is not active", message.getStoreId());
            throw new IllegalStateException("Cannot reply to message for inactive store");
        }

        // Verify the user has permission to reply (either the sender or a store owner/manager)
        if (!replyAuthor.equals(message.getSenderUsername()) &&
                !store.isStoreOwner(replyAuthor) &&
                !store.isStoreManager(replyAuthor)) {

            logger.error("User {} does not have permission to reply to message {}",
                    replyAuthor, messageId);
            throw new IllegalStateException("User does not have permission to reply to this message");
        }

        // Add the reply
        boolean success = messageRepository.addReply(messageId, replyAuthor, replyContent);

        if (success) {
            logger.info("Reply added successfully to message {}", messageId);
        } else {
            logger.error("Failed to add reply to message {}", messageId);
        }

        return success;
    }

    /**
     * Gets all messages sent by a user
     *
     * @param username The username of the sender
     * @return List of messages sent by the user
     */
    public List<Message> getUserMessages(String username) {
        logger.info("Getting messages for user {}", username);
        return messageRepository.findBySender(username);
    }

    /**
     * Gets all messages for a store
     *
     * @param username The username of the requesting user
     * @param storeId The ID of the store
     * @return List of messages for the store
     * @throws IllegalStateException if the user doesn't have permission
     */
    public List<Message> getStoreMessages(String username, UUID storeId) {
        logger.info("User {} getting messages for store {}", username, storeId);

        // Verify the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store {} not found", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        // Verify the user has permission to view store messages
        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            logger.error("User {} does not have permission to view messages for store {}",
                    username, storeId);
            throw new IllegalStateException("User does not have permission to view store messages");
        }

        return messageRepository.findByStore(storeId);
    }

    /**
     * Gets all unanswered messages for a store
     *
     * @param username The username of the requesting user
     * @param storeId The ID of the store
     * @return List of unanswered messages for the store
     * @throws IllegalStateException if the user doesn't have permission
     */
    public List<Message> getUnansweredStoreMessages(String username, UUID storeId) {
        logger.info("User {} getting unanswered messages for store {}", username, storeId);

        // Verify the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store {} not found", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        // Verify the user has permission to view store messages
        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            logger.error("User {} does not have permission to view messages for store {}",
                    username, storeId);
            throw new IllegalStateException("User does not have permission to view store messages");
        }

        return messageRepository.getUnansweredMessagesForStore(storeId);
    }

    /**
     * Gets all unread messages for a store
     *
     * @param username The username of the requesting user
     * @param storeId The ID of the store
     * @return List of unread messages for the store
     * @throws IllegalStateException if the user doesn't have permission
     */
    public List<Message> getUnreadMessagesForStore(String username, UUID storeId) {
        logger.info("User {} getting unread messages for store {}", username, storeId);

        // Verify the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store {} not found", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        // Verify the user has permission to view store messages
        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            logger.error("User {} does not have permission to view messages for store {}",
                    username, storeId);
            throw new IllegalStateException("User does not have permission to view store messages");
        }

        return messageRepository.getUnreadMessagesForStore(storeId);
    }

    /**
     * Gets all messages between a user and a store
     *
     * @param username The username of the user
     * @param storeId The ID of the store
     * @return List of messages between the user and the store
     */
    public List<Message> getUserStoreConversation(String username, UUID storeId) {
        logger.info("Getting conversation between user {} and store {}", username, storeId);

        // Verify the store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store {} not found", storeId);
            throw new IllegalArgumentException("Store not found: " + storeId);
        }

        return messageRepository.findByUserAndStore(username, storeId);
    }

    /**
     * Deletes a message
     *
     * @param username The username of the requesting user
     * @param messageId The ID of the message to delete
     * @return true if the message was deleted successfully
     * @throws IllegalStateException if the user doesn't have permission
     */
    public boolean deleteMessage(String username, UUID messageId) {
        logger.info("User {} attempting to delete message {}", username, messageId);

        // Verify the message exists
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message {} not found", messageId);
                    return new IllegalArgumentException("Message not found: " + messageId);
                });

        // Verify the user has permission to delete the message
        Store store = storeRepository.findById(message.getStoreId())
                .orElseThrow(() -> {
                    logger.error("Store {} not found", message.getStoreId());
                    return new IllegalArgumentException("Store not found: " + message.getStoreId());
                });

        // Only the sender, store owners, or system admins can delete messages
        if (!username.equals(message.getSenderUsername()) &&
                !store.isStoreOwner(username)) {

            logger.error("User {} does not have permission to delete message {}",
                    username, messageId);
            throw new IllegalStateException("User does not have permission to delete this message");
        }

        // Delete the message
        messageRepository.deleteById(messageId);
        logger.info("Message {} deleted successfully", messageId);
        return true;
    }

    /**
     * Marks a message as read
     *
     * @param username The username of the requesting user
     * @param messageId The ID of the message to mark as read
     * @return true if the message was marked as read successfully
     * @throws IllegalStateException if the user doesn't have permission
     */
    public boolean markMessageAsRead(String username, UUID messageId) {
        logger.info("User {} attempting to mark message {} as read", username, messageId);

        // Verify the message exists
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message {} not found", messageId);
                    return new IllegalArgumentException("Message not found: " + messageId);
                });

        // Verify the store exists
        Store store = storeRepository.findById(message.getStoreId())
                .orElseThrow(() -> {
                    logger.error("Store {} not found", message.getStoreId());
                    return new IllegalArgumentException("Store not found: " + message.getStoreId());
                });

        // Verify the user has permission to mark the message as read
        // Only store owners/managers can mark messages as read (unless they're the sender)
        if (!username.equals(message.getSenderUsername()) &&
                !store.isStoreOwner(username) &&
                !store.isStoreManager(username)) {

            logger.error("User {} does not have permission to mark message {} as read",
                    username, messageId);
            throw new IllegalStateException("User does not have permission to mark this message as read");
        }

        // Mark the message as read
        boolean success = messageRepository.markMessageAsRead(messageId);
        if (success) {
            logger.info("Message {} marked as read by user {}", messageId, username);
        } else {
            logger.error("Failed to mark message {} as read", messageId);
        }

        return success;
    }

    /**
     * Marks all messages for a store as read
     *
     * @param username The username of the requesting user
     * @param storeId The ID of the store
     * @return The number of messages marked as read
     * @throws IllegalStateException if the user doesn't have permission
     */
    public int markAllStoreMessagesAsRead(String username, UUID storeId) {
        logger.info("User {} attempting to mark all messages for store {} as read", username, storeId);

        // Verify the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    logger.error("Store {} not found", storeId);
                    return new IllegalArgumentException("Store not found: " + storeId);
                });

        // Verify the user has permission to mark messages as read for this store
        if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
            logger.error("User {} does not have permission to mark messages for store {} as read",
                    username, storeId);
            throw new IllegalStateException("User does not have permission to mark messages for this store as read");
        }

        // Mark all messages for the store as read
        int count = messageRepository.markAllStoreMessagesAsRead(storeId);
        logger.info("User {} marked {} messages as read for store {}", username, count, storeId);

        return count;
    }
}