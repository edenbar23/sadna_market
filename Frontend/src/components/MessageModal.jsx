import React, { useState, useContext } from "react";
import "../index.css";
import { sendMessage } from "../api/message";
import { AuthContext } from "../context/AuthContext";

export default function MessageModal({ store, onClose }) {
    const [message, setMessage] = useState("");
    const [status, setStatus] = useState("idle");
    const { user, token, isAuthenticated } = useContext(AuthContext);

    const handleSubmit = async () => {
        if (!message.trim()) return;
        if (!isAuthenticated) {
            alert("You must be logged in to send a message.");
            setStatus("error");
            return;
        }

        setStatus("sending");
        try {
            await sendMessage(user.username, store.id, message, token);
            setStatus("sent");
            setTimeout(onClose, 1500);
        } catch (error) {
            console.error(error);
            setStatus("error");
        }
    };

    return (
        <div className="modal-backdrop">
            <div className="modal-window">
                <h3 className="modal-title">Message {store.name}</h3>
                <textarea
                    className="modal-textarea"
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Write your message here..."
                />
                <div className="modal-actions">
                    <button className="modal-send-btn" onClick={handleSubmit} disabled={status === "sending"}>
                        {status === "sending" ? "Sending..." : "Send"}
                    </button>
                    <button className="modal-cancel-btn" onClick={onClose}>Cancel</button>
                </div>
                {status === "sent" && <p className="modal-success">Message sent!</p>}
                {status === "error" && <p className="modal-error">Failed to send.</p>}
            </div>
        </div>
    );
}