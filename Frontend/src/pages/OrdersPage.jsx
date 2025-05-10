import React from "react";
import OrderCard from "../components/OrderCard";
import "../index.css";

// Dummy orders data
const orders = [
  {
    id: 1,
    storeName: "Tech Store",
    products: [
      { name: "Laptop", quantity: 1 },
      { name: "Mouse", quantity: 2 },
    ],
    paymentMethod: "Credit Card",
    deliveryAddress: "123 Main St, Tel Aviv",
    totalPrice: 1250,
    status: "Shipped",
  },
  {
    id: 2,
    storeName: "Book Shop",
    products: [
      { name: "React Handbook", quantity: 1 },
    ],
    paymentMethod: "PayPal",
    deliveryAddress: "45 Rothschild Blvd, Tel Aviv",
    totalPrice: 30,
    status: "Processing",
  },
];

export default function OrdersPage() {
    const handleMessageStore = (storeName) => {
        alert(`Messaging ${storeName}... (simulate messaging logic here)`);
    };
  return (
    <div className="orders-page">
    <h1 className="orders-title">My Orders</h1>
    <div className="orders-list">
      {orders.map((order) => (
        <div key={order.id} className="order-card">
          <OrderCard order={order} />
          <button
            className="message-store-btn"
            onClick={() => handleMessageStore(order.storeName)}
          >
            Message {order.storeName}
          </button>
        </div>
      ))}
    </div>
  </div>
  );
}
