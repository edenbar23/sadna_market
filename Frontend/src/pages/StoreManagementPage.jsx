import React, { useEffect, useState } from "react";
import { fetchUserStores } from "../api/user";
import StoreControlPanel from "../components/StoreControlPanel";

export default function StoreManagementPage({ user }) {
  const [stores, setStores] = useState([]);

  useEffect(() => {
    fetchUserStores(user.username).then(setStores).catch(console.error);
  }, [user]);

  return (
    <div className="store-management-container">
          <h1 className="store-management-title">My Stores</h1>
          <div className="create-store-button" title="Create new store" onClick={() => { /* handle navigation */ }}>
  +
</div>

      <div className="store-grid">
        {stores.map((store) => (
          <div key={store.id} className="store-card">
            <StoreControlPanel store={store} />
          </div>
        ))}
          </div>
          
      </div>
      
  );
}
