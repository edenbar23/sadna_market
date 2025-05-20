import React, { useState } from "react";
import "../index.css";

export default function SendMessageModal({ recipient, onClose, onSubmit }) {
    const [message, setMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async () => {
        if (!message.trim()) {
            setError("Message cannot be empty");
            return;
        }

        setIsSubmitting(true);

        try {
            await onSubmit(message);
            // The parent component will handle closing the modal
        } catch (err) {
            setError(`Failed to send message: ${err.message || "Unknown error"}`);
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Message to {recipient}</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {error && <div className="error-text">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="message">Your Message</label>
                        <textarea
                            id="message"
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Type your message here..."
                            rows={5}
                            disabled={isSubmitting}
                        />
                    </div>
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={onClose}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Sending..." : "Send Message"}
                    </button>
                </div>
            </div>
        </div>
    );
}