// This is a partial update to StorePage.jsx
// Focus on the handleSendMessage function and related state

import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import SendMessageModal from "../components/SendMessageModal";
import RateStoreModal from "../components/RateStoreModal";
import { useStoreManagement } from "@/hooks/index.js";
import { useMessages } from "../hooks/useMessages";
import "../index.css";

export default function StorePage({ user }) {
    const { storeId } = useParams();
    const navigate = useNavigate();
    const [showMessageModal, setShowMessageModal] = useState(false);
    const [showRateModal, setShowRateModal] = useState(false);
    const [messageSent, setMessageSent] = useState(false);
    const { sendMessageToStore } = useMessages();

    // Use store management hook to get store data
    const {
        store,
        products,
        isLoading,
        error
    } = useStoreManagement(storeId, user);

    // Check if user has completed a purchase from this store
    const hasCompletedPurchase = user && user.orders && user.orders.some(
        order => order.storeId === storeId && order.status === "Complete"
    );

    const handleSendMessage = async (message) => {
        try {
            await sendMessageToStore(storeId, message);
            setMessageSent(true);
            setTimeout(() => setMessageSent(false), 3000); // Show success message for 3 seconds
            setShowMessageModal(false);
        } catch (err) {
            console.error("Failed to send message:", err);
            // Error handling is done by the hook
        }
    };

    const handleRateStore = async (rating, comment) => {
        // Existing code for rating store...
    };

    if (!storeId) {
        return <div className="loading-message">Redirecting...</div>;
    }

    if (isLoading) {
        return <div className="store-page loading">Loading store information...</div>;
    }

    if (error) {
        return (
            <div className="store-page error">
                <h2>Error loading store</h2>
                <p>{error}</p>
                <button onClick={() => navigate("/")} className="btn">
                    Back to Home
                </button>
            </div>
        );
    }

    if (!store) {
        return <div className="store-page not-found">Store not found</div>;
    }

    return (
        <div className="store-page">
            {/* Store header section */}
            <div className="store-header">
                {/* ... existing code ... */}
            </div>

            {/* Show success message if message was sent */}
            {messageSent && (
                <div className="success-message">
                    Message sent successfully! The store will respond soon.
                </div>
            )}

            <div className="store-actions">
                {user && (
                    <>
                        <button
                            className="button"
                            onClick={() => setShowMessageModal(true)}
                        >
                            Message Seller
                        </button>

                        {hasCompletedPurchase && (
                            <button
                                className="button"
                                onClick={() => setShowRateModal(true)}
                            >
                                Rate Store
                            </button>
                        )}
                    </>
                )}
            </div>

            {/* Products section */}
            <h3 className="store-products-title">Store Products</h3>
            <div className="store-products-scroll">
                {/* ... existing code ... */}
            </div>

            {/* Modals */}
            {showMessageModal && (
                <SendMessageModal
                    storeId={storeId}
                    storeName={store.name}
                    onClose={() => setShowMessageModal(false)}
                    onSuccess={() => {
                        setMessageSent(true);
                        setTimeout(() => setMessageSent(false), 3000);
                    }}
                />
            )}

            {showRateModal && (
                <RateStoreModal
                    storeName={store.name}
                    onClose={() => setShowRateModal(false)}
                    onSubmit={handleRateStore}
                />
            )}
        </div>
    );
}