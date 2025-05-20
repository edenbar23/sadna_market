import React, { useState } from "react";
import "../index.css";
import { useMessages } from "../hooks/useMessages";

export default function SendMessageModal({ storeId, storeName, onClose, onSuccess }) {
    const [message, setMessage] = useState("");
    const { sendMessageToStore, loading, error } = useMessages();
    const [localError, setLocalError] = useState("");

    const handleSubmit = async () => {
        if (!message.trim()) {
            setLocalError("Message cannot be empty");
            return;
        }

        try {
            await sendMessageToStore(storeId, message);
            onSuccess?.();
            onClose();
        } catch (err) {
            setLocalError(error || "Failed to send message. Please try again.");
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Message to {storeName}</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {(localError || error) && <div className="error-text">{localError || error}</div>}

                    <div className="form-group">
                        <label htmlFor="message">Your Message</label>
                        <textarea
                            id="message"
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Type your message here..."
                            rows={5}
                            disabled={loading}
                        />
                    </div>
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={onClose}
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        disabled={loading}
                    >
                        {loading ? "Sending..." : "Send Message"}
                    </button>
                </div>
            </div>
        </div>
    );
}