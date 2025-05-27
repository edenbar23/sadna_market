import React, { useState } from "react";
import MessageModal from "./MessageModal.jsx";
import "../index.css";

export default function StoreActionPanel({ store }) {
    const [showModal, setShowModal] = useState(false);

    return (
        <div className="store-info-card">
            <img src={store.logo} alt={`${store.name} logo`} className="store-logo" />
            <div className="store-info-text">
                <h3>{store.name}</h3>
                <p>Rating: {store.rating} ⭐</p>
                <div className="store-buttons">
                    <button onClick={() => setShowModal(true)}>Message Store</button>
                    <button className="report-btn">Report Violation</button>
                </div>
            </div>
            {showModal && <MessageModal store={store} onClose={() => setShowModal(false)} />}
        </div>
    );
}