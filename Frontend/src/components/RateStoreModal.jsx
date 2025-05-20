import React, { useState } from "react";
import "../index.css";

export default function RateStoreModal({ storeName, onClose, onSubmit }) {
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async () => {
        if (rating < 1 || rating > 5) {
            setError("Rating must be between 1 and 5");
            return;
        }

        setIsSubmitting(true);

        try {
            await onSubmit(rating, comment);
            // The parent component will handle closing the modal
        } catch (err) {
            setError(`Failed to submit rating: ${err.message || "Unknown error"}`);
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Rate {storeName}</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {error && <div className="error-text">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="rating">Rating (1-5 stars)</label>
                        <div className="rating-input">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <span
                                    key={star}
                                    className={`star ${star <= rating ? "selected" : ""}`}
                                    onClick={() => setRating(star)}
                                >
                  ★
                </span>
                            ))}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="comment">Your Review (Optional)</label>
                        <textarea
                            id="comment"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Share your experience with this store..."
                            rows={4}
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
                        {isSubmitting ? "Submitting..." : "Submit Rating"}
                    </button>
                </div>
            </div>
        </div>
    );
}