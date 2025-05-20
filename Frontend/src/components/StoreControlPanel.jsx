import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useStoreOperations } from "../hooks/useStoreOperations";

export default function StoreControlPanel({ store, onUpdate, user }) {
    const navigate = useNavigate();
    const { handleToggleStoreStatus, handleUpdateStore, isLoading, error } = useStoreOperations(user);
    const [isRenaming, setIsRenaming] = useState(false);
    const [newStoreName, setNewStoreName] = useState(store.name);
    const [localError, setLocalError] = useState("");

    const handleStatusToggle = async () => {
        try {
            await handleToggleStoreStatus(store.id, store.active);
            // Call the onUpdate function to refresh the list of stores
            if (onUpdate) onUpdate();
        } catch (err) {
            setLocalError("Failed to toggle store status");
        }
    };

    const startRenaming = () => {
        setNewStoreName(store.name);
        setIsRenaming(true);
    };

    const cancelRenaming = () => {
        setIsRenaming(false);
        setLocalError("");
    };

    const submitRename = async () => {
        if (!newStoreName.trim()) {
            setLocalError("Store name cannot be empty");
            return;
        }

        try {
            await handleUpdateStore(store.id, { ...store, name: newStoreName });
            setIsRenaming(false);
            // Call the onUpdate function to refresh the list of stores
            if (onUpdate) onUpdate();
        } catch (err) {
            setLocalError("Failed to rename store");
        }
    };

    return (
        <div className="store-control-card">
            <div className="store-header">
                {isRenaming ? (
                    <div className="rename-container">
                        <input
                            type="text"
                            value={newStoreName}
                            onChange={(e) => setNewStoreName(e.target.value)}
                            className="rename-input"
                            autoFocus
                        />
                        {localError && <div className="error-text">{localError}</div>}
                        <div className="rename-buttons">
                            <button onClick={submitRename} disabled={isLoading}>Save</button>
                            <button onClick={cancelRenaming}>Cancel</button>
                        </div>
                    </div>
                ) : (
                    <h2 className="store-name">{store.name}</h2>
                )}
                <span className={`store-status ${store.active ? "active" : "closed"}`}>
          {store.active ? "Active" : "Closed"}
        </span>
            </div>

            <div className="store-buttons">
                {!isRenaming && (
                    <>
                        <button className="store-button" onClick={startRenaming} disabled={isLoading}>
                            Rename
                        </button>
                        <button
                            className="store-button"
                            onClick={handleStatusToggle}
                            disabled={isLoading}
                        >
                            {store.active ? "Close" : "Activate"}
                        </button>
                        <button
                            className="store-button"
                            onClick={() => navigate(`/store/${store.id}/appoint`)}
                            disabled={isLoading}
                        >
                            Manage Users
                        </button>
                        <Link to={`/store-manage/${store.id}`}>
                            <button className="store-button primary">Manage Store</button>
                        </Link>
                    </>
                )}
            </div>

            {error && !localError && <div className="error-text">{error}</div>}
        </div>
    );
}