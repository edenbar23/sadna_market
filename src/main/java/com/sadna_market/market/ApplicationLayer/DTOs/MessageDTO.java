package com.sadna_market.market.ApplicationLayer.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sadna_market.market.DomainLayer.Message;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageDTO {

    @Getter
    private UUID messageId;

    @Getter
    private String senderUsername;

    @Getter
    private UUID storeId;

    @Getter
    private String content;

    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Getter
    private String reply;

    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTimestamp;

    @Getter
    private String replyAuthor;

    @Getter
    private boolean hasReply;

    @Getter
    private boolean isRead;

    /**
     * Constructs a MessageDTO from a Message domain entity
     *
     * @param message The message entity
     */
    public MessageDTO(Message message) {
        this.messageId = message.getMessageId();
        this.senderUsername = message.getSenderUsername();
        this.storeId = message.getStoreId();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
        this.reply = message.getReply();
        this.replyTimestamp = message.getReplyTimestamp();
        this.replyAuthor = message.getReplyAuthor();
        this.hasReply = message.hasReply();
        this.isRead = message.isRead();
    }

    /**
     * Alternative constructor with all fields explicitly set
     */
    public MessageDTO(UUID messageId, String senderUsername, UUID storeId, String content,
                      LocalDateTime timestamp, String reply, LocalDateTime replyTimestamp,
                      String replyAuthor, boolean hasReply, boolean isRead) {
        this.messageId = messageId;
        this.senderUsername = senderUsername;
        this.storeId = storeId;
        this.content = content;
        this.timestamp = timestamp;
        this.reply = reply;
        this.replyTimestamp = replyTimestamp;
        this.replyAuthor = replyAuthor;
        this.hasReply = hasReply;
        this.isRead = isRead;
    }
}