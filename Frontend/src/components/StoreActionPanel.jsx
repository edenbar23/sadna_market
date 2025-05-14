import React from "react";
import "../index.css";

export default function StoreActionPanel({ store }) {
  return (
    <div className="store-info-card">
      <img src={store.logo} alt={`${store.name} logo`} className="store-logo" />
      <div className="store-info-text">
        <h3>{store.name}</h3>
        <p>Rating: {store.rating} ⭐</p>
        <div className="store-buttons">
          <button>Message Store</button>
          <button className="report-btn">Report Violation</button>
        </div>
      </div>
    </div>
  );
}
