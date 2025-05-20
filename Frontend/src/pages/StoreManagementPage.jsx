import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUserStores } from "@/hooks/index.js";
import { useStoreOperations } from "@/hooks/index.js";
import StoreControlPanel from "../components/StoreControlPanel";
import CreateStoreModal from "../components/CreateStoreModal";
import { fetchStoreById } from "@/api/store"; // Adjust the path if needed

export default function StoreManagementPage({ user }) {
    const navigate = useNavigate();
    const { stores, isLoading, error, loadUserStores } = useUserStores(user);
    const { handleCreateStore } = useStoreOperations(user);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [storeDetails, setStoreDetails] = useState([]);
    const [loadingDetails, setLoadingDetails] = useState(false);

    useEffect(() => {
        const fetchAndSetStoreDetails = async () => {
            try {
                setLoadingDetails(true);
                await loadUserStores(); // Load basic store list (IDs)
                if (stores.length > 0) {
                    const detailedStores = await Promise.all(
                        stores.map((store) => fetchStoreById(store.storeId))
                    );
                    setStoreDetails(detailedStores);
                } else {
                    setStoreDetails([]);
                }
            } catch (err) {
                console.error("Error loading store details:", err);
            } finally {
                setLoadingDetails(false);
            }
        };

        fetchAndSetStoreDetails();
    }, [user]);

    const handleCreateStoreSubmit = async (storeData) => {
        try {
            await handleCreateStore(storeData);
            await loadUserStores();
            const detailedStores = await Promise.all(
                stores.map((store) => fetchStoreById(store.storeId))
            );
            setStoreDetails(detailedStores);
            setShowCreateModal(false);
        } catch (err) {
            console.error("Failed to create store:", err);
        }
    };

    if (isLoading || loadingDetails) {
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
                {storeDetails.length > 0 ? (
                    storeDetails.map((store) => (
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
                    isLoading={isLoading || loadingDetails}
                />
            )}
        </div>
    );
}
