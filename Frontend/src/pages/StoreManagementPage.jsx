import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import StoreControlPanel from "../components/StoreControlPanel";
import CreateStoreModal from "../components/CreateStoreModal";
import { useStoreOperations } from "../hooks/useStoreOperations";
import { fetchUserStores } from "../api/user";
import { fetchStoreById } from "../api/store";

export default function StoreManagementPage() {
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const { handleCreateStore } = useStoreOperations(user);

    const [stores, setStores] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);

    const reloadStores = async () => {
        if (!user || !user.username || !user.token) {
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const response = await fetchUserStores(user.username, user.token);

            if (response?.data && Array.isArray(response.data)) {
                const storeIds = response.data;

                const storePromises = storeIds.map(storeId =>
                    fetchStoreById(storeId).catch(err => {
                        console.error(`Error fetching store ${storeId}:`, err);
                        return null;
                    })
                );

                const storeDetails = await Promise.all(storePromises);
                const validStores = storeDetails.filter(store => store != null);
                setStores(validStores);
            } else {
                console.error("Invalid response format:", response);
                setStores([]);
            }
        } catch (err) {
            console.error("Error loading stores:", err);
            setError(err.message || "Failed to load stores");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        reloadStores();
    }, [user]);

    return (
        <div className="store-management-container">
            <h1 className="store-management-title">My Stores</h1>

            {error && (
                <div className="error-message">
                    Error loading stores: {error}
                </div>
            )}

            {isLoading ? (
                <div className="loading-indicator">Loading your stores...</div>
            ) : (
                <div className="store-grid">
                    {stores?.length > 0 ? (
                        stores.map((store) => (
                            <div key={store.storeId || store.id} className="store-card">
                                <StoreControlPanel
                                    store={store}
                                    onUpdate={reloadStores}
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
            )}

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
                    handleCreateStore={handleCreateStore}
                    reloadStores={reloadStores}
                />
            )}
        </div>
    );
}
