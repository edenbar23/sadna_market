import React, { useState } from "react";
import MessageModal from "./MessageModal.jsx";
import ReportModal from "./ReportModal.jsx"; // <-- Import
import "../index.css";

export default function StoreActionPanel({ store, currentUser, productId }) {
    const [showMessageModal, setShowMessageModal] = useState(false);
    const [showReportModal, setShowReportModal] = useState(false);

    return (
        <div>
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
                    {currentUser &&
                        (<div className="store-buttons">
                            <button onClick={() => setShowMessageModal(true)}>Message Store</button>
                            <button className="report-btn" onClick={() => setShowReportModal(true)}>
                                Report Violation
                            </button>
                        </div>

                    )}
                </div>
            </div>

            {showMessageModal && (
                <MessageModal store={store} onClose={() => setShowMessageModal(false)} />
            )}

            {showReportModal && (
                <ReportModal
                    store={store}
                    currentUser={currentUser}
                    //productId={productId}
                    onClose={() => setShowReportModal(false)}
                />
            )}
        </div>
    );
}
