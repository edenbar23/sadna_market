import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { fetchOrderById } from '../api/order';
import { getProductInfo } from '../api/product';
import '../styles/order-confirmation.css';

export default function OrderConfirmationPage() {
  const { orderId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isMultipleOrders, setIsMultipleOrders] = useState(false);

  useEffect(() => {
    const loadOrders = async () => {
      try {
        setLoading(true);
        console.log('=== ORDER CONFIRMATION DEBUG ===');
        console.log('URL orderId:', orderId);
        console.log('URL searchParams:', Object.fromEntries(searchParams));

        // Get order IDs from multiple sources
        let orderIds = [];

        // Method 1: Multiple order IDs from URL search params (our new approach)
        const orderIdsParam = searchParams.get('orderIds');
        if (orderIdsParam) {
          try {
            orderIds = JSON.parse(decodeURIComponent(orderIdsParam));
            console.log('✅ Order IDs from URL params:', orderIds);
          } catch (e) {
            console.error('❌ Failed to parse orderIds from URL:', e);
          }
        }

        // Method 2: Single order ID from URL path
        if (orderIds.length === 0 && orderId) {
          orderIds = [orderId];
          console.log('✅ Single order ID from URL path:', orderIds);
        }

        // Method 3: Fallback to sessionStorage (backup method)
        if (orderIds.length === 0) {
          const storedIds = sessionStorage.getItem('checkoutOrderIds');
          if (storedIds) {
            try {
              orderIds = JSON.parse(storedIds);
              sessionStorage.removeItem('checkoutOrderIds'); // Clean up
              console.log('✅ Order IDs from sessionStorage:', orderIds);
            } catch (e) {
              console.error('❌ Failed to parse stored order IDs:', e);
            }
          }
        }

        if (orderIds.length === 0) {
          throw new Error('No order IDs found. Please check your order history.');
        }

        console.log('📋 Final order IDs to load:', orderIds);
        setIsMultipleOrders(orderIds.length > 1);

        // Load all orders in parallel
        const orderPromises = orderIds.map(async (id, index) => {
          try {
            console.log(`📦 Loading order ${index + 1}/${orderIds.length}: ${id}`);

            const response = await fetchOrderById(id);
            if (!response || !response.data) {
              throw new Error(`No data received for order ${id}`);
            }

            const orderData = response.data;
            console.log(`✅ Order ${id} loaded:`, orderData);

            // Process products for this order
            let processedProducts = [];
            if (orderData.products && typeof orderData.products === 'object') {
              const productEntries = Object.entries(orderData.products);
              console.log(`🛍️ Processing ${productEntries.length} products for order ${id}`);

              const productPromises = productEntries.map(async ([productId, quantity]) => {
                try {
                  const productResponse = await getProductInfo(productId);
                  const productData = productResponse.data || productResponse;

                  return {
                    productId,
                    name: productData.name || `Product ${productId.substring(0, 8)}...`,
                    description: productData.description || 'No description available',
                    price: productData.price || 0,
                    category: productData.category || 'Unknown',
                    quantity: parseInt(quantity) || 0,
                    imageUrl: productData.imageUrl || '/assets/blank_product.png'
                  };
                } catch (err) {
                  console.warn(`⚠️ Failed to load product ${productId}:`, err.message);
                  return {
                    productId,
                    name: `Product ${productId.substring(0, 8)}...`,
                    description: 'Product details unavailable',
                    price: 0,
                    category: 'Unknown',
                    quantity: parseInt(quantity) || 0,
                    imageUrl: '/assets/blank_product.png'
                  };
                }
              });

              processedProducts = await Promise.all(productPromises);
            }

            return {
              ...orderData,
              products: processedProducts,
              orderNumber: index + 1 // For display purposes
            };

          } catch (err) {
            console.error(`❌ Failed to load order ${id}:`, err);
            return {
              orderId: id,
              orderDate: new Date().toISOString(),
              status: 'ERROR',
              totalPrice: 0,
              finalPrice: 0,
              products: [],
              error: err.message,
              orderNumber: index + 1
            };
          }
        });

        const resolvedOrders = await Promise.all(orderPromises);
        console.log('🎉 All orders processed:', resolvedOrders);

        // Filter out completely failed orders
        const validOrders = resolvedOrders.filter(order => order.orderId);
        setOrders(validOrders);

        if (validOrders.length === 0) {
          throw new Error('Failed to load any order details');
        }

      } catch (err) {
        console.error('💥 Order loading failed:', err);
        setError(err.message || 'Failed to load order details');
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, [orderId, searchParams]);

  const formatDate = (dateValue) => {
    if (!dateValue) return 'Unknown date';

    try {
      let date;
      if (Array.isArray(dateValue)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue;
        date = new Date(year, month - 1, day, hour, minute, second);
      } else {
        date = new Date(dateValue);
      }

      return isNaN(date.getTime()) ? 'Invalid date' : date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });
    } catch (e) {
      return 'Date format error';
    }
  };

  const calculateTotals = () => {
    const totalAmount = orders.reduce((sum, order) => sum + (order.finalPrice || order.totalPrice || 0), 0);
    const totalItems = orders.reduce((sum, order) => {
      return sum + order.products.reduce((orderSum, product) => orderSum + product.quantity, 0);
    }, 0);
    return { totalAmount, totalItems };
  };

  const handleContinueShopping = () => {
    console.log('🛒 Continue Shopping clicked');
    navigate('/');
  };

  const handleViewAllOrders = () => {
    console.log('📋 View All Orders clicked');
    navigate('/orders');
  };

  if (loading) {
    return (
        <div className="order-confirmation-page">
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading your order details...</p>
          </div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="order-confirmation-page">
          <div className="error-container">
            <div className="error-icon">❌</div>
            <h2>Unable to Load Orders</h2>
            <p>{error}</p>
            <div className="error-actions">
              <button
                  onClick={handleViewAllOrders}
                  className="secondary-button"
              >
                Go to Order History
              </button>
              <button
                  onClick={handleContinueShopping}
                  className="primary-button"
              >
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
    );
  }

  if (orders.length === 0) {
    return (
        <div className="order-confirmation-page">
          <div className="error-container">
            <div className="error-icon">📦</div>
            <h2>No Orders Found</h2>
            <p>We couldn't find any order details to display.</p>
            <button onClick={handleViewAllOrders} className="primary-button">
              Go to Order History
            </button>
          </div>
        </div>
    );
  }

  const { totalAmount, totalItems } = calculateTotals();

  return (
      <div className="order-confirmation-page">
        <div className="confirmation-container">
          {/* Header */}
          <div className="confirmation-header">
            <div className="success-icon">✅</div>
            <h1>{isMultipleOrders ? 'Orders Confirmed!' : 'Order Confirmed!'}</h1>
            <p>Thank you for your purchase!</p>

            {isMultipleOrders && (
                <div className="summary-stats">
                  <div className="stat-item">
                    <span className="stat-number">{orders.length}</span>
                    <span className="stat-label">Orders</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-number">{totalItems}</span>
                    <span className="stat-label">Items</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-number">${totalAmount.toFixed(2)}</span>
                    <span className="stat-label">Total</span>
                  </div>
                </div>
            )}
          </div>

          {/* Orders Content */}
          <div className="orders-content">
            {isMultipleOrders ? (
                // Multiple Orders Layout
                <div className="multiple-orders-layout">
                  <h2>Your {orders.length} Orders</h2>
                  {orders.map((order, index) => (
                      <div key={order.orderId} className="order-card">
                        <div className="order-header">
                          <div className="order-title">
                            <h3>Order #{order.orderNumber}</h3>
                            <span className="order-id">{order.orderId}</span>
                          </div>
                          <div className="order-summary">
                            <span className="order-amount">${(order.finalPrice || order.totalPrice || 0).toFixed(2)}</span>
                            <span className={`order-status status-${(order.status || 'pending').toLowerCase()}`}>
                        {order.status || 'PENDING'}
                      </span>
                          </div>
                        </div>

                        <div className="order-body">
                          <div className="order-info">
                            <div className="info-grid">
                              <div className="info-item">
                                <span className="info-label">Date:</span>
                                <span className="info-value">{formatDate(order.orderDate)}</span>
                              </div>
                              {order.storeName && (
                                  <div className="info-item">
                                    <span className="info-label">Store:</span>
                                    <span className="info-value">{order.storeName}</span>
                                  </div>
                              )}
                            </div>
                          </div>

                          <div className="order-products">
                            <h4>{order.products.length} Items</h4>
                            <div className="products-grid">
                              {order.products.map(product => (
                                  <div key={product.productId} className="product-item">
                                    <img
                                        src={product.imageUrl}
                                        alt={product.name}
                                        className="product-image"
                                        onError={(e) => {
                                          e.target.src = '/assets/blank_product.png';
                                        }}
                                    />
                                    <div className="product-details">
                                      <h5>{product.name}</h5>
                                      <div className="product-meta">
                                        <span>Qty: {product.quantity}</span>
                                        <span>${(product.price * product.quantity).toFixed(2)}</span>
                                      </div>
                                    </div>
                                  </div>
                              ))}
                            </div>
                          </div>
                        </div>
                      </div>
                  ))}
                </div>
            ) : (
                // Single Order Layout
                <div className="single-order-layout">
                  {orders.map(order => (
                      <div key={order.orderId}>
                        <div className="order-details-section">
                          <h2>Order Details</h2>
                          <div className="details-grid">
                            <div className="detail-row">
                              <span className="detail-label">Order ID:</span>
                              <span className="detail-value">{order.orderId}</span>
                            </div>
                            <div className="detail-row">
                              <span className="detail-label">Date:</span>
                              <span className="detail-value">{formatDate(order.orderDate)}</span>
                            </div>
                            <div className="detail-row">
                              <span className="detail-label">Status:</span>
                              <span className={`detail-value status-badge status-${(order.status || 'pending').toLowerCase()}`}>
                          {order.status || 'PENDING'}
                        </span>
                            </div>
                            <div className="detail-row">
                              <span className="detail-label">Total:</span>
                              <span className="detail-value price-highlight">
                          ${(order.finalPrice || order.totalPrice || 0).toFixed(2)}
                        </span>
                            </div>
                            {order.storeName && (
                                <div className="detail-row">
                                  <span className="detail-label">Store:</span>
                                  <span className="detail-value">{order.storeName}</span>
                                </div>
                            )}
                          </div>
                        </div>

                        <div className="order-items-section">
                          <h2>Items ({order.products.length})</h2>
                          <div className="items-list">
                            {order.products.map(product => (
                                <div key={product.productId} className="item-card-detailed">
                                  <img
                                      src={product.imageUrl}
                                      alt={product.name}
                                      className="item-image"
                                      onError={(e) => {
                                        e.target.src = '/assets/blank_product.png';
                                      }}
                                  />
                                  <div className="item-info">
                                    <h3>{product.name}</h3>
                                    <p className="item-description">{product.description}</p>
                                    {product.category && (
                                        <p className="item-category">Category: {product.category}</p>
                                    )}
                                    <div className="item-pricing">
                                      <span className="item-quantity">Qty: {product.quantity}</span>
                                      <span className="item-unit-price">${product.price.toFixed(2)} each</span>
                                      <span className="item-total-price">${(product.price * product.quantity).toFixed(2)}</span>
                                    </div>
                                  </div>
                                </div>
                            ))}
                          </div>
                        </div>
                      </div>
                  ))}
                </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="action-buttons">
            <button
                className="primary-button"
                onClick={handleContinueShopping}
            >
              Continue Shopping
            </button>
            <button
                className="secondary-button"
                onClick={handleViewAllOrders}
            >
              View All Orders
            </button>
          </div>
        </div>
      </div>
  );
}