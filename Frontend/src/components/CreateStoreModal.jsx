import React, { useState } from "react";
import "../index.css";

export default function CreateStoreModal({ onClose, onSubmit, isLoading }) {
    const [storeName, setStoreName] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = () => {
        // Basic validation
        if (!storeName.trim()) {
            setError("Store name is required");
            return;
        }

        // Submit the form data
        onSubmit({
            storeName,
            storeDescription: "", // Optional additional fields
            category: "",
        });
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Create New Store</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    <div className="form-group">
                        <label htmlFor="storeName">Store Name</label>
                        <input
                            type="text"
                            id="storeName"
                            value={storeName}
                            onChange={(e) => setStoreName(e.target.value)}
                            placeholder="Enter store name"
                            disabled={isLoading}
                        />
                        {error && <div className="error-text">{error}</div>}
                    </div>

                    {/* Additional fields could be added here */}
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={onClose}
                        disabled={isLoading}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        disabled={isLoading}
                    >
                        {isLoading ? "Creating..." : "Create Store"}
                    </button>
                </div>
            </div>
        </div>
    );
}