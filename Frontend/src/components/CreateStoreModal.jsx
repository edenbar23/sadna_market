import React, { useState } from "react";
import ReactDOM from "react-dom";
import "../index.css";

const modalRoot = document.getElementById("root");

export default function CreateStoreModal({ onClose, handleCreateStore, reloadStores, onSuccess }) {
    const [storeName, setStoreName] = useState("");
    const [description, setDescription] = useState("");
    const [address, setAddress] = useState("");
    const [email, setEmail] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async () => {
        if (!storeName.trim()) {
            setError("Store name is required");
            return;
        }
        if (!email.trim()) {
            setError("Email is required");
            return;
        }

        setIsLoading(true);
        setError("");

        try {
            await handleCreateStore({
                storeName,
                description,
                address,
                email,
                phoneNumber,
            });
            if(reloadStores) { await reloadStores(); }
            onClose();
            if (onSuccess) onSuccess();
        } catch (err) {
            console.error("Failed to create store:", err);
            setError("Failed to create store. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const modal = (
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
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <input
                            type="text"
                            id="description"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Enter store description"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="address">Address</label>
                        <input
                            type="text"
                            id="address"
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            placeholder="Enter address"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Enter email"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phoneNumber">Phone Number</label>
                        <input
                            type="tel"
                            id="phoneNumber"
                            value={phoneNumber}
                            onChange={(e) => setPhoneNumber(e.target.value)}
                            placeholder="Enter phone number"
                            disabled={isLoading}
                        />
                    </div>

                    {error && <div className="error-text">{error}</div>}
                </div>

                <div className="modal-footer">
                    <button className="btn-cancel" onClick={onClose} disabled={isLoading}>
                        Cancel
                    </button>
                    <button className="btn-submit" onClick={handleSubmit} disabled={isLoading}>
                        {isLoading ? "Creating..." : "Create Store"}
                    </button>
                </div>
            </div>
        </div>
    );

    return modalRoot ? ReactDOM.createPortal(modal, modalRoot) : null;
}
