package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.IMessageRepository;
import com.sadna_market.market.DomainLayer.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryMessageRepository implements IMessageRepository {

    private static final Logger logger = LogManager.getLogger(InMemoryMessageRepository.class);

    // Thread-safe map to store messages by ID
    private final Map<UUID, Message> messages = new ConcurrentHashMap<>();

    public InMemoryMessageRepository() {
        logger.info("InMemoryMessageRepository initialized");
    }

    @Override
    public Message save(Message message) {
        if (message == null) {
            logger.error("Cannot save null message");
            throw new IllegalArgumentException("Message cannot be null");
        }

        logger.debug("Saving message: {}", message.getMessageId());
        messages.put(message.getMessageId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID messageId) {
        if (messageId == null) {
            logger.error("Cannot find message with null ID");
            return Optional.empty();
        }

        logger.debug("Finding message by ID: {}", messageId);
        return Optional.ofNullable(messages.get(messageId));
    }

    @Override
    public List<Message> findBySender(String username) {
        if (username == null || username.isEmpty()) {
            logger.error("Cannot find messages with null or empty username");
            return Collections.emptyList();
        }

        logger.debug("Finding messages sent by user: {}", username);

        return messages.values().stream()
                .filter(message -> username.equals(message.getSenderUsername()))
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByStore(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot find messages with null store ID");
            return Collections.emptyList();
        }

        logger.debug("Finding messages for store: {}", storeId);

        return messages.values().stream()
                .filter(message -> storeId.equals(message.getStoreId()))
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByUserAndStore(String username, UUID storeId) {
        if (username == null || username.isEmpty() || storeId == null) {
            logger.error("Cannot find messages with null or empty parameters");
            return Collections.emptyList();
        }

        logger.debug("Finding messages between user: {} and store: {}", username, storeId);

        return messages.values().stream()
                .filter(message -> (username.equals(message.getSenderUsername()) &&
                        storeId.equals(message.getStoreId())) ||
                        (storeId.equals(message.getStoreId()) &&
                                username.equals(message.getReplyAuthor())))
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID messageId) {
        if (messageId == null) {
            logger.error("Cannot delete message with null ID");
            return;
        }

        logger.debug("Deleting message with ID: {}", messageId);
        messages.remove(messageId);
    }

    @Override
    public boolean addReply(UUID messageId, String replyAuthor, String replyText) {
        if (messageId == null || replyAuthor == null || replyAuthor.isEmpty() ||
                replyText == null || replyText.isEmpty()) {
            logger.error("Cannot add reply with null or empty parameters");
            return false;
        }

        logger.debug("Adding reply to message: {}", messageId);

        Message message = messages.get(messageId);
        if (message == null) {
            logger.warn("Cannot add reply - message not found with ID: {}", messageId);
            return false;
        }

        message.addReply(replyAuthor, replyText);
        logger.info("Reply added to message: {}", messageId);
        return true;
    }

    @Override
    public List<Message> getUnreadMessagesForStore(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot find unread messages with null store ID");
            return Collections.emptyList();
        }

        logger.debug("Finding unread messages for store: {}", storeId);

        return messages.values().stream()
                .filter(message -> storeId.equals(message.getStoreId()) && !message.isRead())
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean markMessageAsRead(UUID messageId) {
        if (messageId == null) {
            logger.error("Cannot mark message with null ID as read");
            return false;
        }

        Message message = messages.get(messageId);
        if (message == null) {
            logger.warn("Cannot mark non-existent message as read: {}", messageId);
            return false;
        }

        message.markAsRead();
        logger.debug("Message {} marked as read", messageId);
        return true;
    }

    @Override
    public int markAllStoreMessagesAsRead(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot mark messages for null store ID as read");
            return 0;
        }

        logger.debug("Marking all messages for store {} as read", storeId);

        int count = 0;
        for (Message message : messages.values()) {
            if (storeId.equals(message.getStoreId()) && !message.isRead()) {
                message.markAsRead();
                count++;
            }
        }

        logger.info("Marked {} messages as read for store {}", count, storeId);
        return count;
    }

    @Override
    public List<Message> getUnansweredMessagesForStore(UUID storeId) {
        if (storeId == null) {
            logger.error("Cannot find unanswered messages with null store ID");
            return Collections.emptyList();
        }

        logger.debug("Finding unanswered messages for store: {}", storeId);

        return messages.values().stream()
                .filter(message -> storeId.equals(message.getStoreId()) && !message.hasReply())
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        messages.clear();
        logger.info("Message repository cleared");
    }
}