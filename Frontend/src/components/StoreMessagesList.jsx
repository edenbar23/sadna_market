import React, { useState, useEffect } from "react";
import { useMessages } from "../hooks/useMessages";
import "../styles/components.css";

export default function StoreMessagesList({ storeId }) {
    const [messages, setMessages] = useState([]);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [replyText, setReplyText] = useState("");
    const [showReplyForm, setShowReplyForm] = useState(false);
    const { getStoreMessagesList, replyToStoreMessage, markAsRead, loading, error } = useMessages();

    // Load store messages when component mounts or storeId changes
    useEffect(() => {
        const loadMessages = async () => {
            if (!storeId) {
                console.warn("No storeId provided to StoreMessagesList");
                return;
            }

            try {
                console.log("Fetching messages for store:", storeId);
                const messagesData = await getStoreMessagesList(storeId);
                if (messagesData) {
                    // Sort messages by timestamp (newest first)
                    const sortedMessages = messagesData.sort((a, b) =>
                        new Date(b.timestamp) - new Date(a.timestamp)
                    );
                    setMessages(sortedMessages);
                    console.log("Successfully loaded messages:", sortedMessages.length);
                } else {
                    console.warn("No message data returned from API");
                    setMessages([]);
                }
            } catch (err) {
                console.error("Failed to load store messages:", err);
                // Don't set messages to empty array in case of error, to preserve existing messages
            }
        };

        loadMessages();
    }, [storeId, getStoreMessagesList]);

    // Handle sending a reply to a message
    const handleReply = async () => {
        if (!selectedMessage || !replyText.trim()) return;

        try {
            const success = await replyToStoreMessage(selectedMessage.messageId, replyText);

            if (success) {
                // Update the message in our local state
                const updatedMessages = messages.map(msg =>
                    msg.messageId === selectedMessage.messageId
                        ? {
                            ...msg,
                            reply: replyText,
                            hasReply: true,
                            replyTimestamp: new Date().toISOString()
                        }
                        : msg
                );

                setMessages(updatedMessages);
                setReplyText("");
                setShowReplyForm(false);
            }
        } catch (err) {
            console.error("Failed to send reply:", err);
            alert("Failed to send reply. Please try again.");
        }
    };

    // Mark a message as read when opened
    const handleSelectMessage = async (message) => {
        setSelectedMessage(message);

        if (!message.isRead) {
            try {
                const success = await markAsRead(message.messageId);

                if (success) {
                    // Update the message in our local state
                    const updatedMessages = messages.map(msg =>
                        msg.messageId === message.messageId
                            ? { ...msg, isRead: true }
                            : msg
                    );

                    setMessages(updatedMessages);
                }
            } catch (err) {
                console.error("Failed to mark message as read:", err);
            }
        }

        setShowReplyForm(true);
    };

    // Format timestamp to readable date/time using native JavaScript
    const formatTimestamp = (timestamp) => {
        if (!timestamp) return "";
        try {
            const date = new Date(timestamp);
            return date.toLocaleString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
                hour: 'numeric',
                minute: 'numeric',
                hour12: true
            });
        } catch (e) {
            return timestamp;
        }
    };

    if (loading && messages.length === 0) {
        return <div className="loading-indicator">Loading store messages...</div>;
    }

    if (error && messages.length === 0) {
        return <div className="error-message">{error}</div>;
    }

    if (messages.length === 0) {
        return <div className="no-data-message">No messages yet.</div>;
    }

    return (
        <div className="store-messages-list">
            <div className="messages-scroll">
                {messages.map((message) => (
                    <div
                        key={message.messageId}
                        className={`message-bubble ${!message.isRead ? 'unread' : ''}`}
                        onClick={() => handleSelectMessage(message)}
                    >
                        <div className="message-header">
                            <span className="message-sender">{message.senderUsername}</span>
                            <span className="message-time">{formatTimestamp(message.timestamp)}</span>
                        </div>
                        <div className="message-content">{message.content}</div>

                        {message.hasReply && (
                            <div className="message-reply">
                                <div className="reply-header">
                                    <span className="reply-author">{message.replyAuthor}</span>
                                    <span className="reply-time">{formatTimestamp(message.replyTimestamp)}</span>
                                </div>
                                <div className="reply-content">{message.reply}</div>
                            </div>
                        )}

                        {!message.hasReply && (
                            <button
                                className="reply-button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleSelectMessage(message);
                                }}
                            >
                                Reply
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {showReplyForm && selectedMessage && (
                <div className="reply-form">
                    <h4>Reply to {selectedMessage.senderUsername}</h4>
                    <div className="original-message">
                        <p><strong>Original message:</strong> {selectedMessage.content}</p>
                    </div>
                    <textarea
                        value={replyText}
                        onChange={(e) => setReplyText(e.target.value)}
                        placeholder="Type your reply here..."
                        rows={3}
                    />
                    <div className="reply-actions">
                        <button
                            className="cancel-button"
                            onClick={() => {
                                setShowReplyForm(false);
                                setReplyText("");
                            }}
                        >
                            Cancel
                        </button>
                        <button
                            className="send-button"
                            onClick={handleReply}
                            disabled={!replyText.trim() || loading}
                        >
                            {loading ? "Sending..." : "Send Reply"}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}