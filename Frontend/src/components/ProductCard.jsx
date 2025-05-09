import React from "react";
import "../styles/components.css";

export default function ProductCard({ product }) {
  return (
    <div className="card">
      <img src={product.image} alt={product.name} className="card-img" />
      <h3 className="card-title">{product.name}</h3>
      <p className="card-text">Store: {product.store}</p>
      <p className="card-text">Rating: {product.rating}</p>
      <p className="card-price">${product.price}</p>
    </div>
  );
}
