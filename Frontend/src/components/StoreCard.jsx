import React from "react";
import "../styles/components.css";

export default function StoreCard({ store }) {
  return (
    <div className="card">
      <img src={store.logo} alt={store.name} className="card-img" />
      <h3 className="card-title">{store.name}</h3>
      <p className="card-text">Rating: {store.rating}</p>
    </div>
  );
}
