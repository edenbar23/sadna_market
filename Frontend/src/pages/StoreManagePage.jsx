import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import StoreControlPanel from "../components/StoreControlPanel";
import {
  fetchStoreById,
  fetchStoreProducts,
  fetchStoreMessages,
  fetchStoreOrders,
} from "../api/store";

export default function StoreManagePage() {
    const { storeId } = useParams();
    const [store, setStore] = useState(null);
   const [products, setProducts] = useState([]);
    const [orders, setOrders] = useState([]);
    const [messages, setMessages] = useState([]);

    useEffect(() => {
      const loadStoreData = async () => {
        try {
            const storeData = await fetchStoreById(storeId);
           const storeProducts = await fetchStoreProducts(storeId);
            const storeOrders = await fetchStoreOrders(storeId);
            const storeMsgs = await fetchStoreMessages(storeId);

            setStore(storeData);
           setProducts(storeProducts);
            setOrders(storeOrders);
            setMessages(storeMsgs);
          } catch (error) {
            console.error("Failed to load store data:", error);
          }
        };
      
        loadStoreData();
      }, [storeId]);

    if (!store) return <p>Loading...</p>;

    return (
        <div className="store-manage-container">
            <div className="store-header">
                <h1 className="store-title">{store.name}</h1>
                <span className={`store-status ${store.active ? "active" : "inactive"}`}>
                    {store.active ? "Active" : "Closed"}
                </span>
            </div>
            <div className="store-actions">
              <button className="btn">Appoint Role</button>
              <button className="btn">
                {store.active ? "Close Store" : "Activate Store"}
              </button>
              <button className="btn">Change Name</button>
        </div>
        
        <section className="store-section">
        <h2 className="store-section-title">Store Crew</h2>
        <p className="text-muted">No members assigned yet.</p>
      </section>

      <div className="store-section">
  <h2 className="store-section-title">Manage Products</h2>
  <div className="products-scroll">
    {products.map((product) => (
      <div key={product.id} className="product-card">
        <div className="product-info">
          <h3 className="product-name">{product.name}</h3>
          <p className="product-price">Price: ${product.price}</p>
          <p className="product-stock">Stock: {product.stock}</p>
        </div>
        <div className="product-actions">
          <button className="product-edit">Edit</button>
          <button className="product-delete">Delete</button>
        </div>
      </div>
    ))}
  </div>
</div>

  
            <section className="store-section">
  <h2 className="store-section-title">Store Messages</h2>
  <div className="messages-scroll">
    {messages.map((msg) => (
      <div key={msg.id} className="message-bubble">
        <div className="message-header">
          <span className="message-sender">{msg.sender}</span>
          <span className="message-time">{msg.timestamp}</span>
        </div>
        <div className="message-content">{msg.content}</div>
        <button className="reply-button">Reply</button>
      </div>
    ))}
  </div>
</section>


<section className="store-section">
  <h2 className="store-section-title">Store Orders</h2>

  {/* Pending Orders */}
  <div className="order-subsection">
    <h3 className="order-subsection-title">Pending Orders</h3>
    <div className="orders-scroll">
      {orders.filter(o => o.status === "Pending").map(order => (
        <div key={order.id} className="order-card">
          <div className="order-header">
            <span className="order-date">{order.orderedAt}</span>
            <span className="order-buyer">Buyer: {order.buyer}</span>
          </div>
          <ul className="order-items">
            {order.items.map((item, idx) => (
              <li key={idx}>
                {item.product} - Qty: {item.quantity}, ${item.price}
              </li>
            ))}
          </ul>
          <div className="order-footer">
            <span className="order-total">Total: ${order.total.toFixed(2)}</span>
            <div className="order-actions">
              <button className="btn-approve">Approve</button>
              <button className="btn-decline">Decline</button>
            </div>
          </div>
        </div>
      ))}
    </div>
  </div>

  {/* In Process Orders */}
  <div className="order-subsection">
    <h3 className="order-subsection-title">In Process</h3>
    <div className="orders-scroll">
      {orders.filter(o => o.status !== "Complete" && o.status !== "Pending").map(order => (
        <div key={order.id} className="order-card">
          <div className="order-header">
            <span className="order-date">{order.orderedAt}</span>
            <span className="order-buyer">Buyer: {order.buyer}</span>
          </div>
          <ul className="order-items">
            {order.items.map((item, idx) => (
              <li key={idx}>
                {item.product} - Qty: {item.quantity}, ${item.price}
              </li>
            ))}
          </ul>
          <div className="order-footer">
            <span className="order-total">Total: ${order.total.toFixed(2)}</span>
            <span className="order-status">Status: In Process</span>
          </div>
        </div>
      ))}
    </div>
  </div>

  {/* Completed Orders */}
  <div className="order-subsection">
    <h3 className="order-subsection-title">Completed</h3>
    <div className="orders-scroll">
      {orders.filter(o => o.status === "Complete").map(order => (
        <div key={order.id} className="order-card">
          <div className="order-header">
            <span className="order-date">{order.orderedAt}</span>
            <span className="order-buyer">Buyer: {order.buyer}</span>
          </div>
          <ul className="order-items">
            {order.items.map((item, idx) => (
              <li key={idx}>
                {item.product} - Qty: {item.quantity}, ${item.price}
              </li>
            ))}
          </ul>
          <div className="order-footer">
            <span className="order-total">Total: ${order.total.toFixed(2)}</span>
            <span className="order-status">Status: Completed</span>
          </div>
        </div>
      ))}
    </div>
  </div>
</section>


        </div>
    );
}  
