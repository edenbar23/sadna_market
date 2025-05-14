import React, { useState } from "react";
import "../index.css";

export default function ProductActionPanel({ product }) {
  const [quantity, setQuantity] = useState(1);

  return (
    <div className="product-action-panel">
      <div className="quantity-controls">
        <button onClick={() => setQuantity(Math.max(1, quantity - 1))}>-</button>
        <span>{quantity}</span>
        <button onClick={() => setQuantity(quantity + 1)}>+</button>
      </div>
      <button className="add-to-cart-btn">Add to Cart</button>
      <div className="product-buttons">
        <button>Photos</button>
        <button>Reviews</button>
      </div>
    </div>
  );
}
