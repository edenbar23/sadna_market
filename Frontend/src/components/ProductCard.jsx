import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import { useCart } from "../hooks/useCart";
import "../styles/components.css";

export default function ProductCard({ product }) {
  const navigate = useNavigate();
  const { user, token } = useAuthContext();
  const { addToCart, loading } = useCart();
  const [quantity, setQuantity] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [error, setError] = useState(null);

  const handleNavigate = () => {
    navigate(`/product/${product.productId}`);
  };

  const incrementQty = () => setQuantity((q) => q + 1);
  const decrementQty = () => setQuantity((q) => (q > 1 ? q - 1 : 1));

  const handleAddToCart = async () => {
    setAddingToCart(true);
    setError(null);

    try {
      // Add to cart using the useCart hook (which will use CartContext for guests)
      await addToCart(product.storeId, product.productId, quantity);
      window.dispatchEvent(new Event('cartUpdated'));


      // Show success feedback (you could add a toast notification here)
      alert(`Added ${quantity} ${product.name} to cart`);
    } catch (err) {
      console.error("Error adding to cart:", err);
      setError("Failed to add to cart. Please try again.");
    } finally {
      setAddingToCart(false);
    }
  };

  return (
      <div className="card">
        <div className="clickable-area" onClick={handleNavigate}>
          <img
              src={product.imageUrl || "/assets/blank_product.png"}
              alt={product.name}
              className="card-img"
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = "/assets/blank_product.png";
              }}
          />
          <h3 className="card-title">{product.name}</h3>
          {product.storeName && (
              <p className="card-text">Store: {product.storeName}</p>
          )}
          <p className="card-text">
            Rating: {product.rating ? `${product.rating} ⭐` : 'Not rated yet'}
          </p>
          <p className="card-price">${parseFloat(product.price).toFixed(2)}</p>
        </div>

        <div className="card-actions">
          <div className="quantity-controls">
            <button onClick={decrementQty} disabled={addingToCart}>-</button>
            <span>{quantity}</span>
            <button onClick={incrementQty} disabled={addingToCart}>+</button>
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
      </div>
  );
}
