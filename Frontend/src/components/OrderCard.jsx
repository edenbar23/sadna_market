import React from "react";
import "../styles/components.css";

export default function OrderCard({ order }) {
  const {
    storeName,
    products,
    paymentMethod,
    deliveryAddress,
    totalPrice,
    status,
  } = order;

  return (
    <div className="order-card">
      <h2>{storeName}</h2>
      <ul>
        {products.map((p, index) => (
          <li key={index}>{p.name} × {p.quantity}</li>
        ))}
      </ul>
      <p><strong>Payment:</strong> {paymentMethod}</p>
      <p><strong>Shipping:</strong> {deliveryAddress}</p>
      <p><strong>Total:</strong> ${totalPrice.toFixed(2)}</p>
      <p><strong>Status:</strong> {status}</p>
    </div>
  );
}
