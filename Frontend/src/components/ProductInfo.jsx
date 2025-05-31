import React, { useState } from "react";
import { useCart } from "../hooks/useCart";
import { useAuthContext } from "../context/AuthContext";
import "../index.css";

export default function ProductInfo({ product, onRate, canRate }) {
    const { user } = useAuthContext();
    const { addToCart, loading } = useCart();
    const [quantity, setQuantity] = useState(1);
    const [addingToCart, setAddingToCart] = useState(false);
    const [error, setError] = useState(null);
    const [ratingHover, setRatingHover] = useState(0);

    const handleIncrement = () => {
        setQuantity(prev => prev + 1);
    };

    const handleDecrement = () => {
        setQuantity(prev => (prev > 1 ? prev - 1 : 1));
    };

    const handleAddToCart = async () => {
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
                if (guestCart.baskets[product.storeId][product.productId]) {
                    guestCart.baskets[product.storeId][product.productId] += quantity;
                } else {
                    guestCart.baskets[product.storeId][product.productId] = quantity;
                }

                // Save updated cart to localStorage
                localStorage.setItem('guestCart', JSON.stringify(guestCart));
            }

            // FIXED: Dispatch cart update event
            console.log("🔄 ProductInfo: Dispatching cart update event");
            window.dispatchEvent(new Event('cartUpdated'));

            // Show success feedback
            alert(`Added ${quantity} ${product.name} to cart`);
        } catch (err) {
            console.error("Error adding to cart:", err);
            setError("Failed to add to cart. Please try again.");
        } finally {
            setAddingToCart(false);
        }
    };

    const renderStars = (rating) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span
                    key={i}
                    className={`star ${i <= rating ? 'filled' : ''}`}
                    onClick={() => canRate && onRate && onRate(i)}
                    onMouseEnter={() => canRate && setRatingHover(i)}
                    onMouseLeave={() => canRate && setRatingHover(0)}
                >
          ★
        </span>
            );
        }
        return stars;
    };

    return (
        <div className="product-info-container">
            <div className="product-image-section">
                <img
                    src={product.imageUrl || "/assets/blank_product.png"}
                    alt={product.name}
                    className="product-image"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/assets/blank_product.png";
                    }}
                />
            </div>

            <div className="product-details-section">
                <h1>{product.name}</h1>

                <div className="product-rating">
                    <span>Rating: </span>
                    <div className="stars-container">
                        {renderStars(ratingHover || product.rating || 0)}
                    </div>
                    {product.rating ? (
                        <span className="rating-value">({product.rating.toFixed(1)})</span>
                    ) : (
                        <span className="rating-value">(Not rated yet)</span>
                    )}

                    {canRate && (
                        <span className="rating-prompt">
              {product.hasRated ? "You've rated this product" : "Click to rate"}
            </span>
                    )}
                </div>

                {product.category && (
                    <p className="product-category">Category: {product.category}</p>
                )}

                {product.description && (
                    <p className="product-description">{product.description}</p>
                )}

                <p className="product-price">${parseFloat(product.price).toFixed(2)}</p>

                <div className="product-action-panel">
                    <div className="quantity-controls">
                        <button onClick={handleDecrement} disabled={addingToCart}>-</button>
                        <span>{quantity}</span>
                        <button onClick={handleIncrement} disabled={addingToCart}>+</button>
                    </div>

                    <button
                        className="add-to-cart-btn"
                        onClick={handleAddToCart}
                        disabled={addingToCart || loading}
                    >
                        {addingToCart ? "Adding..." : "Add to Cart"}
                    </button>

                    {error && <p className="error-text">{error}</p>}
                </div>

                {product.storeName && (
                    <p className="product-store">Sold by: {product.storeName}</p>
                )}
            </div>
        </div>
    );
}