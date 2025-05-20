import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/components.css";

export default function StoreCard({ store }) {
    const navigate = useNavigate();

    const handleNavigate = () => {
        navigate(`/store/${store.storeId}`);
    };

    // Use a default image if no logo is provided
    const logoSrc = store.logo || "/assets/blank_store.png";

    // Format the rating to one decimal place
    const formattedRating = store.rating
        ? Number(store.rating).toFixed(1)
        : 'N/A';

    return (
        <div className="card store-card">
            <div className="clickable-area" onClick={handleNavigate}>
                <img
                    src={logoSrc}
                    alt={`${store.name} logo`}
                    className="card-img"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/assets/blank_store.png";
                    }}
                />
                <h3 className="card-title">{store.name}</h3>
                <p className="card-text">
                    Rating: {formattedRating} {formattedRating !== 'N/A' && '⭐'}
                </p>

                {store.active !== undefined && (
                    <span className={`store-status-indicator ${store.active ? "active" : "closed"}`}>
            {store.active ? "Active" : "Closed"}
          </span>
                )}

                {store.productsCount !== undefined && (
                    <p className="card-text products-count">
                        {store.productsCount} Products
                    </p>
                )}
            </div>
        </div>
    );
}