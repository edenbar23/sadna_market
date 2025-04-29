package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMessageRepository {

    /**
     * Saves a message to the repository
     *
     * @param message The message to save
     * @return The saved message
     */
    Message save(Message message);

    /**
     * Finds a message by its ID
     *
     * @param messageId The message ID to look for
     * @return Optional containing the message if found
     */
    Optional<Message> findById(UUID messageId);

    /**
     * Finds all messages sent by a specific user
     *
     * @param username The username of the sender
     * @return List of messages sent by the user
     */
    List<Message> findBySender(String username);

    /**
     * Finds all messages sent to a specific store
     *
     * @param storeId The store ID
     * @return List of messages sent to the store
     */
    List<Message> findByStore(UUID storeId);

    /**
     * Finds all messages between a specific user and store
     *
     * @param username The username of the sender
     * @param storeId The store ID
     * @return List of messages between the user and the store
     */
    List<Message> findByUserAndStore(String username, UUID storeId);

    /**
     * Deletes a message by its ID
     *
     * @param messageId The message ID to delete
     */
    void deleteById(UUID messageId);

    /**
     * Adds a reply to an existing message
     *
     * @param messageId The ID of the message to reply to
     * @param replyAuthor The username of the user replying
     * @param replyText The content of the reply
     * @return true if the reply was added successfully
     */
    boolean addReply(UUID messageId, String replyAuthor, String replyText);

    /**
     * Gets all unread messages for a specific store
     *
     * @param storeId The store ID
     * @return List of unread messages for the store
     */
    List<Message> getUnreadMessagesForStore(UUID storeId);

    /**
     * Gets all unanswered messages for a specific store
     *
     * @param storeId The store ID
     * @return List of unanswered messages for the store
     */
    List<Message> getUnansweredMessagesForStore(UUID storeId);

    /**
     * Marks a message as read
     *
     * @param messageId The ID of the message to mark as read
     * @return true if the message was marked as read successfully
     */
    boolean markMessageAsRead(UUID messageId);

    /**
     * Marks all messages for a store as read
     *
     * @param storeId The ID of the store
     * @return The number of messages marked as read
     */
    int markAllStoreMessagesAsRead(UUID storeId);
}