import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import StoreControlPanel from "../components/StoreControlPanel";
import CreateStoreModal from "../components/CreateStoreModal";
import ManageUsersModal from "../components/ManageUsersModal";
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
  const [showManageUsersModal, setShowManageUsersModal] = useState(false);
  const [selectedStoreId, setSelectedStoreId] = useState(null);
  const [selectedPersonnel, setSelectedPersonnel] = useState([]);

  // Derive personnel and their permissions for the modal
  const derivePersonnel = (store) => {
    if (!store) return [];
    const permMap = store.permMap || {};
    const personnel = [];
    if (Array.isArray(store.ownerUsernames)) {
      store.ownerUsernames.forEach(u =>
        personnel.push({
          username: u,
          role: "Owner",
          permissions: permMap[u] || [],
        })
      );
    }
    if (Array.isArray(store.managerUsernames)) {
      store.managerUsernames.forEach(u =>
        personnel.push({
          username: u,
          role: "Manager",
          permissions: permMap[u] || [],
        })
      );
    }
    return personnel;
  };

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
        const storeDetails = await Promise.all(
          storeIds.map((storeId) =>
            fetchStoreById(storeId).catch((err) => {
              console.error(`Error fetching store ${storeId}:`, err);
              return null;
            })
          )
        );
        const validStores = storeDetails.filter((s) => s);
        setStores(validStores);
      } else {
        setStores([]);
      }
    } catch (err) {
      setError(err.message || "Failed to load stores");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    reloadStores();
    // eslint-disable-next-line
  }, [user]);

  return (
    <div className="store-management-container">
      <h1 className="store-management-title">My Stores</h1>
      {error && <div className="error-message">Error loading stores: {error}</div>}
      {isLoading ? (
        <div className="loading-indicator">Loading your stores...</div>
      ) : (
        <div className="store-grid">
          {stores.length > 0 ? (
            stores.map((store) => (
              <div key={store.storeId || store.id} className="store-card">
                <StoreControlPanel
                  store={store}
                  onUpdate={reloadStores}
                  user={user}
                  onManageUsers={() => {
                    setSelectedStoreId(store.storeId);
                    setSelectedPersonnel(derivePersonnel(store));
                    setShowManageUsersModal(true);
                  }}
                />
              </div>
            ))
          ) : (
            <div className="no-stores-message">
              <p>
                You don't have any stores yet. Create your first store to get started!
              </p>
            </div>
          )}
        </div>
      )}

      {showCreateModal && (
        <CreateStoreModal
          onClose={() => setShowCreateModal(false)}
          handleCreateStore={handleCreateStore}
          reloadStores={reloadStores}
        />
      )}

      {showManageUsersModal && (
        <ManageUsersModal
          storeId={selectedStoreId}
          isOpen={showManageUsersModal}
          onClose={() => {
            setShowManageUsersModal(false);
            setSelectedStoreId(null);
            setSelectedPersonnel([]);
            reloadStores(); // To refresh new permissions
          }}
          personnel={selectedPersonnel}
        />
      )}

      <div
        className="create-store-button"
        title="Create new store"
        onClick={() => setShowCreateModal(true)}
      >
        +
      </div>
    </div>
  );
}
