import React, { useState } from "react";
import "../styles/messages.css"; // Assume you'll style accordingly

const mockConversations = [
  {
    id: 1,
    with: "Tech Store",
    type: "store",
    messages: [
      { sender: "store", text: "Your order has shipped.", time: "10:01 AM" },
      { sender: "user", text: "Thanks! When will it arrive?", time: "10:05 AM" },
    ],
  },
  {
    id: 2,
    with: "Admin",
    type: "admin",
    messages: [
      { sender: "admin", text: "Your refund is approved.", time: "Yesterday" },
    ],
  },
];

export default function MessagesPage() {
  const [conversations, setConversations] = useState(mockConversations);
  const [selectedConv, setSelectedConv] = useState(conversations[0]);
  const [newMsg, setNewMsg] = useState("");
  const [showNewForm, setShowNewForm] = useState(false);
  const [newFormData, setNewFormData] = useState({ toType: "store", to: "", text: "" });

  const handleSend = () => {
    if (newMsg.trim() === "") return;
    const updated = conversations.map((conv) =>
      conv.id === selectedConv.id
        ? {
            ...conv,
            messages: [
              ...conv.messages,
              { sender: "user", text: newMsg, time: "Now" },
            ],
          }
        : conv
    );
    setConversations(updated);
    setSelectedConv(updated.find((c) => c.id === selectedConv.id));
    setNewMsg("");
  };

  const handleNewMessage = () => {
    const { to, toType, text } = newFormData;
    if (!to || !text) return;

    const newConv = {
      id: Date.now(),
      with: to,
      type: toType,
      messages: [{ sender: "user", text, time: "Now" }],
    };
    setConversations([newConv, ...conversations]);
    setSelectedConv(newConv);
    setShowNewForm(false);
    setNewFormData({ toType: "store", to: "", text: "" });
  };

  return (
    <div className="messages-page">
      <div className="sidebar">
        <h3>Messages</h3>
        <button className="new-message-btn" onClick={() => setShowNewForm(true)}>+ New Message</button>
        <ul>
          {conversations.map((conv) => (
            <li key={conv.id} onClick={() => setSelectedConv(conv)} className={conv.id === selectedConv?.id ? "active" : ""}>
              <strong>{conv.with}</strong> ({conv.type})
            </li>
          ))}
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
                    <span className="sender">{msg.sender}</span>
                    <span>{msg.text}</span>
                    <span className="time">{msg.time}</span>
                  </div>
                </div>
              ))}
            </div>
            <div className="chat-input">
              <input value={newMsg} onChange={(e) => setNewMsg(e.target.value)} placeholder="Type a reply..." />
              <button onClick={handleSend}>Send</button>
            </div>
          </>
        ) : (
          <p>Select a conversation</p>
        )}
      </div>

      {showNewForm && (
        <div className="new-message-modal">
          <h3>New Message</h3>
          <label>
            To Type:
            <select value={newFormData.toType} onChange={(e) => setNewFormData({ ...newFormData, toType: e.target.value })}>
              <option value="store">Store</option>
              <option value="user">User</option>
            </select>
          </label>
          <label>
            Name:
            <input value={newFormData.to} onChange={(e) => setNewFormData({ ...newFormData, to: e.target.value })} />
          </label>
          <label>
            Message:
            <textarea value={newFormData.text} onChange={(e) => setNewFormData({ ...newFormData, text: e.target.value })} />
          </label>
          <button onClick={handleNewMessage}>Send</button>
          <button onClick={() => setShowNewForm(false)}>Cancel</button>
        </div>
      )}
    </div>
  );
}
