import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import StoreControlPanel from "../components/StoreControlPanel";
import CreateStoreModal from "../components/CreateStoreModal";
import { useStoreOperations } from "../hooks/useStoreOperations";
import { fetchUserStores } from "../api/user";
import { fetchStoreById } from "../api/store";

export default function StoreManagementPage() {
    // Get user from auth context instead of props
    const { user } = useAuthContext();
    const navigate = useNavigate();
    const { handleCreateStore } = useStoreOperations(user);

    const [stores, setStores] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);

    // Load stores directly in this component
    useEffect(() => {
        const loadStores = async () => {
            if (!user || !user.username || !user.token) {
                setIsLoading(false);
                return;
            }

            setIsLoading(true);
            setError(null);

            try {
                // Fetch store IDs first
                const response = await fetchUserStores(user.username, user.token);

                // Check if we got a valid response with data
                if (response && response.data && Array.isArray(response.data)) {
                    const storeIds = response.data;

                    if (storeIds.length > 0) {
                        // Then fetch store details
                        const storePromises = storeIds.map(storeId =>
                            fetchStoreById(storeId).catch(err => {
                                console.error(`Error fetching store ${storeId}:`, err);
                                return null;
                            })
                        );

                        const storeDetails = await Promise.all(storePromises);
                        // Filter out any null results (failed fetches)
                        const validStores = storeDetails.filter(store => store != null);

                        setStores(validStores);
                    } else {
                        setStores([]);
                    }
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

        loadStores();
    }, [user]);

    const handleCreateStoreSubmit = async (storeData) => {
        try {
            await handleCreateStore(storeData);

            // Reload stores after creating a new one
            setIsLoading(true);
            const response = await fetchUserStores(user.username, user.token);

            if (response && response.data && Array.isArray(response.data)) {
                const storeIds = response.data;
                const storePromises = storeIds.map(storeId => fetchStoreById(storeId));
                const storeDetails = await Promise.all(storePromises);
                setStores(storeDetails);
            }

            setShowCreateModal(false);
            setIsLoading(false);
        } catch (err) {
            console.error("Failed to create store:", err);
            // You might want to show an error message here
        }
    };

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
                    {stores && stores.length > 0 ? (
                        stores.map((store) => (
                            <div key={store.storeId || store.id} className="store-card">
                                <StoreControlPanel
                                    store={store}
                                    onUpdate={() => {
                                        // Refresh stores when updated
                                        setIsLoading(true);
                                        loadStores();
                                    }}
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
                    onSubmit={handleCreateStoreSubmit}
                    isLoading={isLoading}
                />
            )}
        </div>
    );
}