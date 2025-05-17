import React from "react";
import { Link } from "react-router-dom";

export default function StoreControlPanel({ store }) {
  return (
    <div className="store-control-card">
      <div className="store-header">
        <h2 className="store-name">{store.name}</h2>
        <span className={`store-status ${store.active ? "active" : "closed"}`}>
          {store.active ? "Active" : "Closed"}
        </span>
      </div>

      <div className="store-buttons">
        <button className="store-button">Rename</button>
        <button className="store-button">
          {store.active ? "Close" : "Activate"}
        </button>
        <button className="store-button">Appoint User</button>
        <Link to={`/store-manage/${store.id}`}>
          <button className="store-button primary">Manage Store</button>
        </Link>
      </div>
    </div>
  );
}
