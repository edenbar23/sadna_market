import React, { useState, useEffect } from "react";
import "../styles/messages.css";
import { useAuthContext } from "../context/AuthContext";
import { getUserMessages, getUserStoreConversation, sendMessage, replyToMessage, markMessageAsRead } from "../api/message";
import { fetchStoreById } from "../api/store";

export default function MessagesPage() {
  const { user, token } = useAuthContext();
  const [conversations, setConversations] = useState([]);
  const [selectedConv, setSelectedConv] = useState(null);
  const [newMsg, setNewMsg] = useState("");
  const [showNewForm, setShowNewForm] = useState(false);
  const [newFormData, setNewFormData] = useState({ toType: "store", to: "", text: "" });
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState(null);

  // Debug: Component render counter
  const renderRef = React.useRef(0);
  renderRef.current += 1;
  console.log(`🔄 MessagesPage render #${renderRef.current}`);

  // Load user messages when component mounts
  useEffect(() => {
    console.log("📥 useEffect loadMessages triggered", { user: user?.username, hasToken: !!token });

    const loadMessages = async () => {
      if (!user || !token) {
        console.log("⚠️ No user or token, skipping load messages");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        console.log("🔍 Fetching user messages...");
        const response = await getUserMessages(user.username, token);
        console.log("📨 getUserMessages response:", response);

        // Group messages by conversation (store)
        const convMap = new Map();
        const storeNames = new Map(); // Cache for store names

        if (response && response.data) {
          // First, collect all unique store IDs
          const storeIds = [...new Set(response.data.map(message => message.storeId))];
          console.log("🏪 Unique store IDs:", storeIds);

          // Fetch store names for all stores
          for (const storeId of storeIds) {
            try {
              const storeResponse = await fetchStoreById(storeId);
              if (storeResponse && storeResponse.name) {
                storeNames.set(storeId, storeResponse.name);
              } else {
                storeNames.set(storeId, `Store ${storeId.substring(0, 8)}...`);
              }
            } catch (err) {
              console.error(`Failed to fetch store name for ${storeId}:`, err);
              storeNames.set(storeId, `Store ${storeId.substring(0, 8)}...`);
            }
          }

          response.data.forEach(message => {
            const storeId = message.storeId;
            const storeName = storeNames.get(storeId) || `Store ${storeId.substring(0, 8)}...`;

            if (!convMap.has(storeId)) {
              convMap.set(storeId, {
                id: storeId,
                with: storeName,
                type: "store",
                messages: []
              });
            }

            const conv = convMap.get(storeId);

            // Add the original message
            const messageItem = {
              id: message.messageId,
              sender: "user", // This is always from the user since we're getting sent messages
              text: message.content,
              time: formatMessageTime(message.timestamp),
              hasReply: message.hasReply,
              isRead: message.isRead,
              originalMessageId: message.messageId
            };

            conv.messages.push(messageItem);

            // If there's a reply, add it as a separate message in the conversation
            if (message.hasReply && message.reply) {
              const replyMessage = {
                id: `${message.messageId}-reply`,
                sender: "store",
                text: message.reply,
                time: formatMessageTime(message.replyTimestamp),
                replyAuthor: "Store",
                isReply: true,
                replyingTo: message.messageId
              };
              conv.messages.push(replyMessage);
            }
          });
        }

        const sortedConversations = Array.from(convMap.values()).map(conv => {
          // Sort messages by timestamp
          conv.messages.sort((a, b) => {
            const timeA = new Date(a.time);
            const timeB = new Date(b.time);
            return timeA - timeB;
          });
          return conv;
        });

        console.log("💬 Setting conversations:", sortedConversations);
        setConversations(sortedConversations);

        // Select the first conversation by default if available
        if (sortedConversations.length > 0) {
          console.log("🎯 Auto-selecting first conversation:", sortedConversations[0].id);
          setSelectedConv(sortedConversations[0]);
        }
      } catch (err) {
        console.error("❌ Failed to load messages:", err);
        setError("Failed to load messages. Please try again.");
      } finally {
        setIsLoading(false);
      }
    };

    loadMessages();
  }, [user, token]);

  // Helper function to format message timestamps
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
      return timestamp;
    }
  };

  // Handle sending a message in existing conversation
  const handleSend = async () => {
    const timestamp = Date.now();
    console.log(`🚀 handleSend called at ${timestamp}`, {
      messageText: newMsg,
      selectedConvId: selectedConv?.id,
      isSending,
      hasUser: !!user,
      hasToken: !!token
    });

    if (newMsg.trim() === "" || !selectedConv || !user || !token || isSending) {
      console.log("⛔ handleSend early return", {
        emptyMessage: newMsg.trim() === "",
        noSelectedConv: !selectedConv,
        noUser: !user,
        noToken: !token,
        alreadySending: isSending
      });
      return;
    }

    console.log(`🔒 Setting isSending to true at ${timestamp}`);
    setIsSending(true);
    const messageText = newMsg; // Capture the message text
    console.log(`📝 Captured message text: "${messageText}"`);

    console.log(`🧹 Clearing input field at ${timestamp}`);
    setNewMsg(""); // Clear input immediately to prevent double-sends

    try {
      console.log(`📤 Calling sendMessage API at ${timestamp}`, {
        username: user.username,
        storeId: selectedConv.id,
        message: messageText
      });

      // Send message to the API
      const apiResponse = await sendMessage(
          user.username,
          selectedConv.id,
          messageText,
          token
      );

      console.log(`✅ sendMessage API response at ${timestamp}:`, apiResponse);

      // Update the UI
      const newMessage = {
        id: `temp-${timestamp}`,
        sender: "user",
        text: messageText,
        time: new Date().toLocaleString('en-US', {
          month: 'short',
          day: 'numeric',
          year: 'numeric',
          hour: 'numeric',
          minute: 'numeric',
          hour12: true
        }),
        hasReply: false,
        isRead: false
      };

      console.log(`🎨 Created new message object at ${timestamp}:`, newMessage);

      console.log(`🔄 Updating conversations state at ${timestamp}`);
      setConversations(prevConversations => {
        console.log(`📊 Previous conversations:`, prevConversations.length);
        const updated = prevConversations.map((conv) =>
            conv.id === selectedConv.id
                ? {
                  ...conv,
                  messages: [...conv.messages, newMessage],
                }
                : conv
        );

        console.log(`📊 Updated conversations:`, updated.length);
        console.log(`💬 Updated selected conv messages count:`,
            updated.find(c => c.id === selectedConv.id)?.messages.length
        );

        // Update selected conversation
        const newSelectedConv = updated.find((c) => c.id === selectedConv.id);
        console.log(`🎯 Setting new selected conversation at ${timestamp}:`, newSelectedConv?.id);
        setSelectedConv(newSelectedConv);
        return updated;
      });

      console.log(`✅ Message send completed successfully at ${timestamp}`);

    } catch (err) {
      console.error(`❌ Failed to send message at ${timestamp}:`, err);
      alert("Failed to send message. Please try again.");
      console.log(`🔄 Restoring message text: "${messageText}"`);
      setNewMsg(messageText); // Restore message on error
    } finally {
      console.log(`🔓 Setting isSending to false at ${timestamp}`);
      setIsSending(false);
    }
  };

  // Handle starting a new conversation
  const handleNewMessage = async () => {
    console.log("🆕 handleNewMessage called", newFormData);

    const { to, toType, text } = newFormData;
    if (!to || !text || !user || !token) {
      console.log("⛔ handleNewMessage early return - missing data");
      return;
    }

    try {
      console.log("📤 Sending new message via API");
      await sendMessage(
          user.username,
          to, // This should be a storeId
          text,
          token
      );

      // Try to get the store name
      let storeName = `Store ${to.substring(0, 8)}...`;
      try {
        const storeResponse = await fetchStoreById(to);
        if (storeResponse && storeResponse.name) {
          storeName = storeResponse.name;
        }
      } catch (err) {
        console.error("Failed to fetch store name:", err);
      }

      // Create a new conversation object
      const newConv = {
        id: to,
        with: storeName,
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
            }),
            hasReply: false,
            isRead: false
          }
        ],
      };

      console.log("🆕 Created new conversation:", newConv);
      setConversations([newConv, ...conversations]);
      setSelectedConv(newConv);
      setShowNewForm(false);
      setNewFormData({ toType: "store", to: "", text: "" });
    } catch (err) {
      console.error("❌ Failed to start new conversation:", err);
      alert("Failed to send message. Please try again.");
    }
  };

  // Mark messages as read when viewing a conversation
  useEffect(() => {
    console.log("👁️ useEffect markMessagesRead triggered", { selectedConvId: selectedConv?.id });

    const markMessagesRead = async () => {
      if (!selectedConv || !user || !token) {
        console.log("⚠️ Skipping mark as read - missing requirements");
        return;
      }

      const unreadMessages = selectedConv.messages.filter(
          msg => msg.sender !== "user" && !msg.isRead && msg.originalMessageId
      );

      console.log(`📖 Found ${unreadMessages.length} unread messages to mark as read`);

      for (const msg of unreadMessages) {
        try {
          await markMessageAsRead(msg.originalMessageId, user.username, token);
          msg.isRead = true;
          console.log(`✅ Marked message ${msg.id} as read`);
        } catch (err) {
          console.error(`❌ Failed to mark message ${msg.id} as read:`, err);
        }
      }

      if (unreadMessages.length > 0) {
        console.log("🔄 Updating conversations after marking as read");
        setConversations([...conversations]);
      }
    };

    markMessagesRead();
  }, [selectedConv, user, token]);

  // Debug: Log key press events
  const handleKeyDown = (e) => {
    console.log(`⌨️ Key pressed: ${e.key}`, {
      target: e.target.tagName,
      isSending,
      messageLength: newMsg.length
    });

    if (e.key === 'Enter') {
      console.log("🎯 Enter key detected - calling handleSend");
      e.preventDefault();
      handleSend();
    }
  };

  // Debug: Log button clicks
  const handleSendClick = () => {
    console.log("🖱️ Send button clicked", { isSending, messageLength: newMsg.length });
    handleSend();
  };

  if (isLoading) {
    return <div className="messages-page loading">Loading messages...</div>;
  }

  if (error) {
    return <div className="messages-page error">{error}</div>;
  }

  console.log("🎨 Rendering component", {
    conversationsCount: conversations.length,
    selectedConvId: selectedConv?.id,
    isSending
  });

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
                        onClick={() => {
                          console.log(`🎯 Selecting conversation: ${conv.id}`);
                          setSelectedConv(conv);
                        }}
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
                      <div key={msg.id} className={`message ${msg.sender}`}>
                        <div className="message-content">
                          <span className="sender">
                            {msg.sender === "user" ? "You" :
                                msg.isReply ? (msg.replyAuthor || "Store") : selectedConv.with}
                          </span>
                          <span className="message-text">{msg.text}</span>
                          <span className="time">{msg.time}</span>
                        </div>
                      </div>
                  ))}
                </div>
                <div className="chat-input">
                  <input
                      value={newMsg}
                      onChange={(e) => {
                        console.log(`📝 Input changed: "${e.target.value}"`);
                        setNewMsg(e.target.value);
                      }}
                      placeholder="Type a reply..."
                      onKeyDown={handleKeyDown}
                      disabled={isSending}
                  />
                  <button onClick={handleSendClick} disabled={isSending}>
                    {isSending ? 'Sending...' : 'Send'}
                  </button>
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