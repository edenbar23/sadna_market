import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { fetchOrderById } from '../api/order';
import '../styles/order-confirmation.css';

export default function OrderConfirmationPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadOrder = async () => {
      try {
        setLoading(true);
        const response = await fetchOrderById(orderId);
        setOrder(response.data);
      } catch (err) {
        console.error('Failed to load order:', err);
        setError('Failed to load order details. Please check your order history.');
      } finally {
        setLoading(false);
      }
    };

    if (orderId) {
      loadOrder();
    }
  }, [orderId]);

  if (loading) {
    return (
      <div className="order-confirmation-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading order details...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="order-confirmation-page">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => window.location.href = '/orders'}>
            Go to Order History
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="order-confirmation-page">
      <div className="confirmation-container">
        <div className="confirmation-header">
          <div className="success-icon">✓</div>
          <h1>Order Confirmed!</h1>
          <p>Thank you for your purchase</p>
        </div>

        <div className="order-details">
          <h2>Order Details</h2>
          <div className="order-info">
            <div className="info-row">
              <span className="info-label">Order ID:</span>
              <span className="info-value">{order.orderId}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Date:</span>
              <span className="info-value">{new Date(order.orderDate).toLocaleString()}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Status:</span>
              <span className="info-value status-badge">{order.status}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Total:</span>
              <span className="info-value price">${order.finalPrice.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <div className="order-items">
          <h2>Items</h2>
          <div className="items-list">
            {order.products.map(product => (
              <div key={product.productId} className="item-card">
                <div className="item-details">
                  <h3>{product.name}</h3>
                  <p className="item-description">{product.description}</p>
                  <div className="item-meta">
                    <span className="item-quantity">Qty: {product.quantity}</span>
                    <span className="item-price">${(product.price * product.quantity).toFixed(2)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="action-buttons">
          <button className="primary-button" onClick={() => window.location.href = '/'}>
            Continue Shopping
          </button>
          <button className="secondary-button" onClick={() => window.location.href = '/orders'}>
            View All Orders
          </button>
        </div>
      </div>
    </div>
  );
}