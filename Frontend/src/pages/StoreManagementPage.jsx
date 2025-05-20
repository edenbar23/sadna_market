import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUserStores } from "@/hooks/index.js";
import { useStoreOperations } from "@/hooks/index.js";
import StoreControlPanel from "../components/StoreControlPanel";
import CreateStoreModal from "../components/CreateStoreModal";

export default function StoreManagementPage({ user }) {
    const navigate = useNavigate();
    const { stores, isLoading, error, loadUserStores } = useUserStores(user);
    const { handleCreateStore } = useStoreOperations(user);
    const [showCreateModal, setShowCreateModal] = useState(false);

    useEffect(() => {
        // Load user's stores when component mounts
        loadUserStores();
    }, [user]);

    const handleCreateStoreSubmit = async (storeData) => {
        try {
            await handleCreateStore(storeData);
            // Refresh the stores list after creating a new store
            await loadUserStores();
            setShowCreateModal(false);
        } catch (err) {
            console.error("Failed to create store:", err);
            // Error is handled by the hook and will be available in the error state
        }
    };

    if (isLoading) {
        return <div className="store-management-container">Loading your stores...</div>;
    }

    return (
        <div className="store-management-container">
            <h1 className="store-management-title">My Stores</h1>

            {error && (
                <div className="error-message">
                    Error loading stores: {error}
                </div>
            )}

            <div className="store-grid">
                {stores.length > 0 ? (
                    stores.map((store) => (
                        <div key={store.id} className="store-card">
                            <StoreControlPanel
                                store={store}
                                onUpdate={loadUserStores}
                                user={user}
                            />
                        </div>
                    ))
                ) : (
                    <div className="no-stores-message">
                        <p>You don't have any stores yet. Create your first store to get started!</p>
                    </div>
                )}
            </div>

            <div
                className="create-store-button"
                title="Create new store"
                onClick={() => setShowCreateModal(true)}
            >
                +
            </div>

            {showCreateModal && (
                <CreateStoreModal
                    onClose={() => setShowCreateModal(false)}
                    onSubmit={handleCreateStoreSubmit}
                    isLoading={isLoading}
                />
            )}
        </div>
    );
}