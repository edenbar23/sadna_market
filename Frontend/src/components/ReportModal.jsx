import React, { useState } from "react";
import { reportViolation } from "../api/user";
import { useAuthContext } from "../context/authContext"; // <-- ✅ import context
import "../index.css";

export default function ReportModal({ store, productId, onClose }) {
    const [comment, setComment] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const { user, token } = useAuthContext(); // <-- ✅ use context

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        if (!user || !token) {
            setError("You must be logged in to submit a report.");
            setSubmitting(false);
            return;
        }

        const report = {
            storeId: store.id,
            productId: productId,
            comment: comment,
            rating: null,
            username: user.username,
            timestamp: new Date().toISOString(),
            isViolationReport: true
        };

        try {
            const response = await reportViolation(user.username, `Bearer ${token}`, report);
            if (response.status >= 200 && response.status < 300) {
                setSuccess(true);
            } else {
                setError("Failed to report violation.");
            }
        } catch (err) {
            setError(err.response?.data?.message || "An unexpected error occurred.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal">
                <h2>Report Violation</h2>
                {success ? (
                    <p className="success">Violation report submitted successfully.</p>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <textarea
                            placeholder="Describe the violation..."
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            required
                            rows={5}
                        />
                        {error && <p className="error">{error}</p>}
                        <div className="modal-buttons">
                            <button type="submit" disabled={submitting}>
                                {submitting ? "Submitting..." : "Submit Report"}
                            </button>
                            <button type="button" onClick={onClose}>Cancel</button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}
