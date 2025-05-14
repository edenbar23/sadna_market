package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {

    @Getter
    private final UUID messageId;

    @Getter
    private final String senderUsername;

    @Getter
    private final UUID storeId;

    @Getter
    private final String content;

    @Getter
    private final LocalDateTime timestamp;

    @Getter @Setter
    private String reply;

    @Getter @Setter
    private LocalDateTime replyTimestamp;

    @Getter @Setter
    private String replyAuthor;

    @Getter @Setter
    private boolean isRead;

    /**
     * Constructor for a new message
     *
     * @param senderUsername The username of the sender
     * @param storeId The ID of the store the message is addressed to
     * @param content The message content
     */
    public Message(String senderUsername, UUID storeId, String content) {
        this.messageId = UUID.randomUUID();
        this.senderUsername = senderUsername;
        this.storeId = storeId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.reply = null;
        this.replyTimestamp = null;
        this.replyAuthor = null;
        this.isRead = false;
    }

    /**
     * Constructor for reconstructing a message from repository
     */
    public Message(UUID messageId, String senderUsername, UUID storeId, String content,
                   LocalDateTime timestamp, String reply, LocalDateTime replyTimestamp,
                   String replyAuthor, boolean isRead) {
        this.messageId = messageId;
        this.senderUsername = senderUsername;
        this.storeId = storeId;
        this.content = content;
        this.timestamp = timestamp;
        this.reply = reply;
        this.replyTimestamp = replyTimestamp;
        this.replyAuthor = replyAuthor;
        this.isRead = isRead;
    }

    /**
     * Adds a reply to this message
     *
     * @param replyAuthor The username of the user replying
     * @param replyText The content of the reply
     */
    public void addReply(String replyAuthor, String replyText) {
        this.reply = replyText;
        this.replyAuthor = replyAuthor;
        this.replyTimestamp = LocalDateTime.now();
    }

    /**
     * Marks this message as read
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Checks if the message has been replied to
     *
     * @return true if the message has a reply
     */
    public boolean hasReply() {
        return reply != null && !reply.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Message message = (Message) obj;
        return messageId.equals(message.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message[id=").append(messageId)
                .append(", from=").append(senderUsername)
                .append(", to store=").append(storeId)
                .append(", time=").append(timestamp).append("]\n")
                .append("Content: ").append(content);

        if (hasReply()) {
            sb.append("\nReply from ").append(replyAuthor)
                    .append(" at ").append(replyTimestamp).append(": ")
                    .append(reply);
        }

        return sb.toString();
    }



}
