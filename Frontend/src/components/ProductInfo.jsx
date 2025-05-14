import React from "react";
import ProductActionPanel from "./ProductActionPanel"; // Import the panel

import "../index.css";

export default function ProductInfo({ product }) {
  return (
<div className="product-info-container">
      <div className="product-image-section">
        <img
          src={product.imageUrl || "/placeholder-product.jpg"}
          alt={product.name}
          className="product-image"
        />
      </div>

      <div className="product-details-section">
        <h1>{product.name}</h1>
        <p className="product-rating">Rating: {product.rating} ⭐</p>
        <p className="product-description">{product.description}</p>
        <p className="product-type">Purchase Type: {product.type}</p>
        <p className="product-price">${product.price.toFixed(2)}</p>
        <ProductActionPanel product={product} />
      </div>
    </div>
  );
}