import React, { useState, useEffect } from "react";
import "../index.css";

export default function UpdateStoreModal({ store, onClose, onSubmit, isLoading }) {
    const [storeData, setStoreData] = useState({
        name: "",
        description: "",
        category: "",
        logoUrl: ""
    });

    const [error, setError] = useState("");

    // Initialize form with store data when component mounts
    useEffect(() => {
        if (store) {
            setStoreData({
                name: store.name || "",
                description: store.description || "",
                category: store.category || "",
                logoUrl: store.logo || ""
            });
        }
    }, [store]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setStoreData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = () => {
        // Basic validation
        if (!storeData.name.trim()) {
            setError("Store name is required");
            return;
        }

        // Submit the form data
        onSubmit({
            ...store,
            name: storeData.name,
            description: storeData.description,
            category: storeData.category,
            logo: storeData.logoUrl
        });
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Update Store</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {error && <div className="error-text">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="name">Store Name*</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={storeData.name}
                            onChange={handleChange}
                            placeholder="Enter store name"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <textarea
                            id="description"
                            name="description"
                            value={storeData.description}
                            onChange={handleChange}
                            placeholder="Enter store description"
                            rows={3}
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="category">Category</label>
                        <input
                            type="text"
                            id="category"
                            name="category"
                            value={storeData.category}
                            onChange={handleChange}
                            placeholder="Enter store category"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="logoUrl">Logo URL</label>
                        <input
                            type="text"
                            id="logoUrl"
                            name="logoUrl"
                            value={storeData.logoUrl}
                            onChange={handleChange}
                            placeholder="Enter logo URL"
                            disabled={isLoading}
                        />
                        {storeData.logoUrl && (
                            <div className="logo-preview">
                                <img
                                    src={storeData.logoUrl}
                                    alt="Logo preview"
                                    onError={(e) => {
                                        e.target.onerror = null;
                                        e.target.src = "/assets/blank_store.png";
                                    }}
                                />
                            </div>
                        )}
                    </div>
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
                        {isLoading ? "Updating..." : "Update Store"}
                    </button>
                </div>
            </div>
        </div>
    );
}