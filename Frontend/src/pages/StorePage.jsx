// src/pages/StorePage.jsx
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import SendMessageModal from "../components/SendMessageModal";
import RateStoreModal from "../components/RateStoreModal";
import { useStoreManagement } from "@/hooks/index.js";
import "../index.css";

export default function StorePage({ user }) {
    const { storeId } = useParams();
    const navigate = useNavigate();
    const [showMessageModal, setShowMessageModal] = useState(false);
    const [showRateModal, setShowRateModal] = useState(false);

    // Redirect if storeId is undefined
    useEffect(() => {
        if (!storeId) {
            navigate("/");
        }
    }, [storeId, navigate]);

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
            // Send message logic would go here
            console.log("Sending message to store:", message);
            setShowMessageModal(false);
        } catch (err) {
            console.error("Failed to send message:", err);
        }
    };

    const handleRateStore = async (rating, comment) => {
        try {
            // Rate store logic would go here
            console.log("Rating store:", { rating, comment });
            setShowRateModal(false);
        } catch (err) {
            console.error("Failed to rate store:", err);
        }
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
            <div className="store-header">
                <img
                    src={store.logo || "/assets/blank_store.png"}
                    alt={`${store.name} logo`}
                    className="store-logo"
                />
                <div className="store-header-info">
                    <h2>{store.name}</h2>
                    <p className="store-rating">Rating: {store.rating || 'N/A'} ⭐</p>
                    {store.description && <p className="store-description">{store.description}</p>}
                </div>
            </div>

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

            <h3 className="store-products-title">Store Products</h3>
            <div className="store-products-scroll">
                {products.length > 0 ? (
                    products.map((product) => (
                        <ProductCard key={product.productId} product={product} />
                    ))
                ) : (
                    <p className="no-products-message">This store has no products yet.</p>
                )}
            </div>

            {/* Modals */}
            {showMessageModal && (
                <SendMessageModal
                    recipient={store.name}
                    onClose={() => setShowMessageModal(false)}
                    onSubmit={handleSendMessage}
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