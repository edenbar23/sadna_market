import React, { useState, useEffect } from "react";
import "../styles/messages.css";
import { useAuthContext } from "../context/AuthContext";
import { getUserMessages, getUserStoreConversation, sendMessage, replyToMessage, markMessageAsRead } from "../api/message";

export default function MessagesPage() {
  const { user, token } = useAuthContext();
  const [conversations, setConversations] = useState([]);
  const [selectedConv, setSelectedConv] = useState(null);
  const [newMsg, setNewMsg] = useState("");
  const [showNewForm, setShowNewForm] = useState(false);
  const [newFormData, setNewFormData] = useState({ toType: "store", to: "", text: "" });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load user messages when component mounts
  useEffect(() => {
    const loadMessages = async () => {
      if (!user || !token) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        const response = await getUserMessages(user.username, token);

        // Group messages by conversation (store)
        const convMap = new Map();

        if (response && response.data) {
          response.data.forEach(message => {
            const storeId = message.storeId;
            const storeName = message.storeId; // We might need to fetch store names separately

            if (!convMap.has(storeId)) {
              convMap.set(storeId, {
                id: storeId,
                with: storeName,
                type: "store",
                messages: []
              });
            }

            const conv = convMap.get(storeId);
            conv.messages.push({
              id: message.messageId,
              sender: message.senderUsername === user.username ? "user" : "store",
              text: message.content,
              time: formatMessageTime(message.timestamp),
              hasReply: message.hasReply,
              reply: message.reply,
              replyTimestamp: message.replyTimestamp ? formatMessageTime(message.replyTimestamp) : null,
              replyAuthor: message.replyAuthor,
              isRead: message.isRead
            });
          });
        }

        const sortedConversations = Array.from(convMap.values()).map(conv => {
          // Sort messages by timestamp
          conv.messages.sort((a, b) => new Date(a.time) - new Date(b.time));
          return conv;
        });

        setConversations(sortedConversations);

        // Select the first conversation by default if available
        if (sortedConversations.length > 0) {
          setSelectedConv(sortedConversations[0]);
        }
      } catch (err) {
        console.error("Failed to load messages:", err);
        setError("Failed to load messages. Please try again.");
      } finally {
        setIsLoading(false);
      }
    };

    loadMessages();
  }, [user, token]);

  // Helper function to format message timestamps using native JavaScript
  const formatMessageTime = (timestamp) => {
    if (!timestamp) return "Unknown";

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
      console.error("Error formatting date:", e);
      return timestamp; // Return the original timestamp if formatting fails
    }
  };

  // Handle sending a message in existing conversation
  const handleSend = async () => {
    if (newMsg.trim() === "" || !selectedConv || !user || !token) return;

    try {
      // Send message to the API
      await sendMessage(
          user.username,
          selectedConv.id, // storeId
          newMsg,
          token
      );

      // Update the UI
      const updated = conversations.map((conv) =>
          conv.id === selectedConv.id
              ? {
                ...conv,
                messages: [
                  ...conv.messages,
                  {
                    id: `temp-${Date.now()}`,
                    sender: "user",
                    text: newMsg,
                    time: new Date().toLocaleString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric',
                      hour: 'numeric',
                      minute: 'numeric',
                      hour12: true
                    })
                  },
                ],
              }
              : conv
      );

      setConversations(updated);
      setSelectedConv(updated.find((c) => c.id === selectedConv.id));
      setNewMsg("");
    } catch (err) {
      console.error("Failed to send message:", err);
      alert("Failed to send message. Please try again.");
    }
  };

  // Handle starting a new conversation
  const handleNewMessage = async () => {
    const { to, toType, text } = newFormData;
    if (!to || !text || !user || !token) return;

    try {
      // In a real implementation, you'd need to:
      // 1. Convert the store name to a storeId
      // 2. Send the message with the storeId

      // For now, we'll pretend 'to' is the storeId for simplicity
      await sendMessage(
          user.username,
          to, // This should be a storeId
          text,
          token
      );

      // Create a new conversation object
      const newConv = {
        id: to, // Using 'to' as storeId for now
        with: to, // Ideally this would be the store name
        type: toType,
        messages: [
          {
            id: `temp-${Date.now()}`,
            sender: "user",
            text,
            time: new Date().toLocaleString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
              hour: 'numeric',
              minute: 'numeric',
              hour12: true
            })
          }
        ],
      };

      setConversations([newConv, ...conversations]);
      setSelectedConv(newConv);
      setShowNewForm(false);
      setNewFormData({ toType: "store", to: "", text: "" });
    } catch (err) {
      console.error("Failed to start new conversation:", err);
      alert("Failed to send message. Please try again.");
    }
  };

  // Handle replying to a message
  const handleReply = async (messageId, replyText) => {
    if (!replyText.trim() || !user || !token) return;

    try {
      // Send reply to the API
      await replyToMessage(messageId, user.username, replyText, token);

      // Reload conversations to get updated message with reply
      // Ideally this would be more efficient, updating only the specific message
      const response = await getUserMessages(user.username, token);
      // Process response similarly to the useEffect...
    } catch (err) {
      console.error("Failed to reply to message:", err);
      alert("Failed to reply to message. Please try again.");
    }
  };

  // Mark a message as read when viewing it
  useEffect(() => {
    const markMessagesRead = async () => {
      if (!selectedConv || !user || !token) return;

      const unreadMessages = selectedConv.messages.filter(
          msg => msg.sender !== "user" && !msg.isRead
      );

      for (const msg of unreadMessages) {
        try {
          await markMessageAsRead(msg.id, user.username, token);
          // Update local state
          msg.isRead = true;
        } catch (err) {
          console.error(`Failed to mark message ${msg.id} as read:`, err);
        }
      }

      // Force a re-render by creating a new array
      setConversations([...conversations]);
    };

    markMessagesRead();
  }, [selectedConv, user, token]);

  if (isLoading) {
    return <div className="messages-page loading">Loading messages...</div>;
  }

  if (error) {
    return <div className="messages-page error">{error}</div>;
  }

  return (
      <div className="messages-page">
        <div className="sidebar">
          <h3>Messages</h3>
          <button className="new-message-btn" onClick={() => setShowNewForm(true)}>+ New Message</button>
          <ul>
            {conversations.length > 0 ? (
                conversations.map((conv) => (
                    <li
                        key={conv.id}
                        onClick={() => setSelectedConv(conv)}
                        className={conv.id === selectedConv?.id ? "active" : ""}
                    >
                      <strong>{conv.with}</strong> ({conv.type})
                      {conv.messages.some(m => m.sender !== "user" && !m.isRead) && (
                          <span className="unread-badge">New</span>
                      )}
                    </li>
                ))
            ) : (
                <li className="no-messages">No messages yet</li>
            )}
          </ul>
        </div>

        <div className="chat-window">
          {selectedConv ? (
              <>
                <h4>Chat with {selectedConv.with} ({selectedConv.type})</h4>
                <div className="chat-messages">
                  {selectedConv.messages.map((msg, idx) => (
                      <div key={idx} className={`message ${msg.sender}`}>
                        <div className="message-content">
                          <span className="sender">{msg.sender === "user" ? "You" : selectedConv.with}</span>
                          <span>{msg.text}</span>
                          <span className="time">{msg.time}</span>

                          {/* Show reply if exists */}
                          {msg.hasReply && (
                              <div className="message-reply">
                                <span className="reply-author">{msg.replyAuthor}</span>
                                <span>{msg.reply}</span>
                                <span className="time">{msg.replyTimestamp}</span>
                              </div>
                          )}
                        </div>
                      </div>
                  ))}
                </div>
                <div className="chat-input">
                  <input
                      value={newMsg}
                      onChange={(e) => setNewMsg(e.target.value)}
                      placeholder="Type a reply..."
                      onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                  />
                  <button onClick={handleSend}>Send</button>
                </div>
              </>
          ) : (
              <p>Select a conversation or start a new one</p>
          )}
        </div>

        {showNewForm && (
            <div className="new-message-modal">
              <h3>New Message</h3>
              <label>
                To Type:
                <select
                    value={newFormData.toType}
                    onChange={(e) => setNewFormData({ ...newFormData, toType: e.target.value })}
                >
                  <option value="store">Store</option>
                  {/* We could include more options like admin if needed */}
                </select>
              </label>
              <label>
                Store ID:
                <input
                    value={newFormData.to}
                    onChange={(e) => setNewFormData({ ...newFormData, to: e.target.value })}
                    placeholder="Enter store ID"
                />
              </label>
              <label>
                Message:
                <textarea
                    value={newFormData.text}
                    onChange={(e) => setNewFormData({ ...newFormData, text: e.target.value })}
                    placeholder="Type your message here..."
                />
              </label>
              <button onClick={handleNewMessage}>Send</button>
              <button onClick={() => setShowNewForm(false)}>Cancel</button>
            </div>
        )}
      </div>
  );
}