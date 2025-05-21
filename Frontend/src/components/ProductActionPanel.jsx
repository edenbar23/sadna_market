import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../hooks/useCart";
import { useAuthContext } from "../context/AuthContext";
import "../index.css";

export default function ProductActionPanel({ product }) {
    const navigate = useNavigate();
    const { user } = useAuthContext();
    const { addToCart, loading } = useCart();
    const [quantity, setQuantity] = useState(1);
    const [addingToCart, setAddingToCart] = useState(false);
    const [error, setError] = useState(null);

    const handleQuantityChange = (newQuantity) => {
        if (newQuantity < 1) return;
        setQuantity(newQuantity);
    };

    const handleAddToCart = async () => {
        if (!product || !product.productId || !product.storeId) {
            setError("Invalid product data");
            return;
        }

        setAddingToCart(true);
        setError(null);

        try {
            // If user is logged in, use user cart, otherwise use guest cart
            if (user) {
                await addToCart(product.storeId, product.productId, quantity);
            } else {
                // Get guest cart from localStorage or create new one
                let guestCart = JSON.parse(localStorage.getItem('guestCart')) || { baskets: {} };

                // Check if store exists in baskets
                if (!guestCart.baskets[product.storeId]) {
                    guestCart.baskets[product.storeId] = {};
                }

                // Add or update product quantity
                guestCart.baskets[product.storeId][product.productId] = quantity;

                // Save updated cart to localStorage
                localStorage.setItem('guestCart', JSON.stringify(guestCart));
            }

            // Show success feedback
            alert(`Added ${quantity} ${product.name} to cart`);
        } catch (err) {
            console.error("Error adding to cart:", err);
            setError("Failed to add to cart. Please try again.");
        } finally {
            setAddingToCart(false);
        }
    };

    const navigateToReviews = () => {
        if (product && product.productId) {
            navigate(`/product/${product.productId}/reviews`);
        }
    };

    const navigateToPhotos = () => {
        if (product && product.productId) {
            navigate(`/product/${product.productId}/photos`);
        }
    };

    return (
        <div className="product-action-panel">
            <div className="quantity-controls">
                <button
                    onClick={() => handleQuantityChange(quantity - 1)}
                    disabled={quantity <= 1 || addingToCart}
                >-</button>
                <span>{quantity}</span>
                <button
                    onClick={() => handleQuantityChange(quantity + 1)}
                    disabled={addingToCart}
                >+</button>
            </div>

            <button
                className="add-to-cart-btn"
                onClick={handleAddToCart}
                disabled={addingToCart || loading}
            >
                {addingToCart ? "Adding..." : "Add to Cart"}
            </button>

            {error && <p className="error-text">{error}</p>}

            <div className="product-buttons">
                <button onClick={navigateToPhotos}>Photos</button>
                <button onClick={navigateToReviews}>Reviews</button>
            </div>
        </div>
    );
}