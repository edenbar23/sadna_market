package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor // Required by JPA
public class Message {

    @Id
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID messageId;

    @Column(name = "sender_username", nullable = false, length = 50)
    private String senderUsername;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Setter
    @Column(name = "reply", length = 2000)
    private String reply;

    @Setter
    @Column(name = "reply_timestamp")
    private LocalDateTime replyTimestamp;

    @Setter
    @Column(name = "reply_author", length = 50)
    private String replyAuthor;

    @Setter
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Setter
    @Column(name = "is_violation_reported", nullable = false)
    private boolean isViolationReported;

    @Setter
    @Column(name = "violation_reason", length = 500)
    private String violationReason;

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
        this.isViolationReported = false;
        this.violationReason = null;
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
        this.isViolationReported = false;
        this.violationReason = null;
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

    /**
     * Reports a violation on this message.
     *
     * @param reason The reason for reporting the message
     */
    public void reportViolation(String reason) {
        this.isViolationReported = true;
        this.violationReason = reason;
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
