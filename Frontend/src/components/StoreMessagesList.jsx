import React, { useState, useEffect } from "react";
import { useMessages } from "../hooks/useMessages";
import "../styles/components.css";

export default function StoreMessagesList({ storeId }) {
    const [messages, setMessages] = useState([]);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [replyText, setReplyText] = useState("");
    const [showReplyForm, setShowReplyForm] = useState(false);

    const {
        getStoreMessagesList,
        replyToStoreMessage,
        markAsRead,
        loading,
        error
    } = useMessages();

    // Load store messages
    useEffect(() => {
        if (!storeId) return;

        const fetchMessages = async () => {
            try {
                const response = await getStoreMessagesList(storeId);

                let data;
                if (response && response.data) {
                    data = response.data;
                } else {
                    data = response || [];
                }

                if (!Array.isArray(data)) {
                    console.error("Expected an array of messages but got:", data);
                    return;
                }

                const sorted = data
                    .filter(msg => msg && msg.timestamp)
                    .sort((a, b) => {
                        const timeA = new Date(a.timestamp);
                        const timeB = new Date(b.timestamp);
                        return timeB - timeA; // newest first
                    });

                setMessages(sorted);
            } catch (err) {
                console.error("Failed to fetch store messages:", err);
            }
        };

        fetchMessages();
    }, [storeId, getStoreMessagesList]);

    const handleSelectMessage = async (message) => {
        setSelectedMessage(message);
        setShowReplyForm(true);

        if (!message.read) {
            try {
                const success = await markAsRead(message.messageId);
                if (success) {
                    setMessages(prev =>
                        prev.map(m => m.messageId === message.messageId ? { ...m, read: true } : m)
                    );
                }
            } catch (err) {
                console.error("Failed to mark message as read:", err);
            }
        }
    };

    const handleReply = async () => {
        if (!selectedMessage || !replyText.trim()) return;

        try {
            const success = await replyToStoreMessage(selectedMessage.messageId, replyText.trim());
            if (success) {
                // Update the message in the local state to show the reply
                setMessages(prev =>
                    prev.map(m =>
                        m.messageId === selectedMessage.messageId
                            ? {
                                ...m,
                                reply: replyText.trim(),
                                hasReply: true,
                                replyTimestamp: new Date().toISOString(),
                                replyAuthor: "Store" // This should be the actual username, but for display purposes
                            }
                            : m
                    )
                );
                setReplyText("");
                setShowReplyForm(false);
                setSelectedMessage(null);
            }
        } catch (err) {
            console.error("Reply failed:", err);
            alert("Failed to send reply. Please try again.");
        }
    };

    const formatTimestamp = (ts) => {
        if (!ts) return "";
        try {
            let date;
            if (Array.isArray(ts)) {
                // Handle array format [year, month, day, hour, minute, second]
                const [year, month, day, hour = 0, minute = 0, second = 0] = ts;
                date = new Date(year, month - 1, day, hour, minute, second);
            } else {
                date = new Date(ts);
            }

            if (isNaN(date.getTime())) {
                return ts.toString();
            }

            return date.toLocaleString("en-US", {
                year: "numeric",
                month: "short",
                day: "numeric",
                hour: "numeric",
                minute: "2-digit",
                hour12: true
            });
        } catch {
            return ts.toString();
        }
    };

    if (loading && messages.length === 0) {
        return <div className="loading-indicator">Loading store messages...</div>;
    }

    if (error && messages.length === 0) {
        return <div className="error-message">Error: {error}</div>;
    }

    if (messages.length === 0) {
        return <div className="no-data-message">No messages yet.</div>;
    }

    return (
        <div className="store-messages-list">
            <div className="messages-scroll">
                {messages.map((msg) => (
                    <div
                        key={msg.messageId}
                        className={`message-bubble ${msg.read || msg.isRead ? "" : "unread"}`}
                        onClick={() => handleSelectMessage(msg)}
                    >
                        <div className="message-header">
                            <span className="message-sender">{msg.senderUsername}</span>
                            <span className="message-time">{formatTimestamp(msg.timestamp)}</span>
                        </div>
                        <div className="message-content">{msg.content}</div>

                        {msg.hasReply && msg.reply && (
                            <div className="message-reply">
                                <div className="reply-header">
                                    <span className="reply-author">{msg.replyAuthor || "Store"}</span>
                                    <span className="reply-time">{formatTimestamp(msg.replyTimestamp)}</span>
                                </div>
                                <div className="reply-content">{msg.reply}</div>
                            </div>
                        )}

                        {!msg.hasReply && (
                            <button
                                className="reply-button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleSelectMessage(msg);
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
                                setSelectedMessage(null);
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