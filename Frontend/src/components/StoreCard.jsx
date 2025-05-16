import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/components.css";

export default function StoreCard({ store }) {
  const navigate = useNavigate();

  const handleNavigate = () => {
    navigate(`/store/${store.id}`);
  };


  return (
    <div className="card">
      <div className="clickable-area" onClick={handleNavigate}>
      <img src={store.logo} alt={store.name} className="card-img" />
      <h3 className="card-title">{store.name}</h3>
        <p className="card-text">Rating: {store.rating}</p>
        </div>
    </div>
  );
}
