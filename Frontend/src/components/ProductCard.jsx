import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/components.css";

export default function ProductCard({ product }) {
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);

  const handleNavigate = () => {
    navigate(`/product/${product.id}`);
  };

  const incrementQty = () => setQuantity((q) => q + 1);
  const decrementQty = () => setQuantity((q) => (q > 1 ? q - 1 : 1));

  const handleAddToCart = () => {
    console.log("Adding to cart:", { productId: product.id, quantity });
    // You could call context, Redux, or backend here
  };

  return (
    <div className="card">
      <div className="clickable-area" onClick={handleNavigate}>
        <img src={product.image} alt={product.name} className="card-img" />
        <h3 className="card-title">{product.name}</h3>
        <p className="card-text">Store: {product.store || "N/A"}</p>
        <p className="card-text">Rating: {product.rating} ⭐</p>
        <p className="card-price">${product.price}</p>
      </div>

      <div className="card-actions">
        <div className="quantity-controls">
          <button onClick={decrementQty}>-</button>
          <span>{quantity}</span>
          <button onClick={incrementQty}>+</button>
        </div>
        <button className="add-to-cart-btn" onClick={handleAddToCart}>
          Add to Cart
        </button>
      </div>
    </div>
  );
}
