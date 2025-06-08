import React, { useState } from "react";
import MessageModal from "./MessageModal.jsx";
import "../index.css";

export default function StoreActionPanel({ store }) {
    const [showModal, setShowModal] = useState(false);

    return (
        <div >
            <div className="store-header">
                <img
                    src={store.logo || "/assets/blank_store.png"}
                    alt={`${store.name} logo`}
                    className="store-logo"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/assets/blank_store.png";
                    }}
                />
                <div className="store-info">
                    <h1 className="store-name">{store.name}</h1>
                    <p className="store-rating">
                        Rating: {store.rating ? `${store.rating.toFixed(1)} ⭐` : 'Not rated yet'}
                    </p>
                    {store.description && <p className="store-description">{store.description}</p>}
                    <div className="store-buttons">
                        <button onClick={() => setShowModal(true)}>Message Store</button>
                        <button className="report-btn">Report Violation</button>
                    </div>
                </div>
            </div>

            {showModal && (
                <MessageModal store={store} onClose={() => setShowModal(false)} />
            )}
        </div>
    );
}