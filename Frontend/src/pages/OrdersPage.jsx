import OrderCard from "../components/OrderCard";
import { useState, useEffect } from "react";
import { fetchOrderHistory, markOrderAsCompleted } from "../api/order";
import { fetchStoreById, rateStore } from "../api/store";
import { useAuthContext } from "../context/AuthContext";
import MessageModal from "../components/MessageModal";
import { rateProduct } from "../api/product";

import "../styles/orders-page.css";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedStore, setSelectedStore] = useState(null);
  const [stores, setStores] = useState({}); // Cache for store data
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [ratingType, setRatingType] = useState(null); // 'store' or 'product'
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [isSubmittingRating, setIsSubmittingRating] = useState(false);


  const [showCelebration, setShowCelebration] = useState(false);
  const [completingOrderId, setCompletingOrderId] = useState(null);

  const { user, token } = useAuthContext();

  useEffect(() => {
    const fetchOrders = async () => {
      if (!user?.username || !token) {
        console.error("Username or token not found in AuthContext");
        setLoading(false);
        return;
      }

      try {
        const ordersData = await fetchOrderHistory(user.username);
        console.log("Fetched orders:", ordersData);

        // Fetch store data for each unique storeId
        const storeIds = [...new Set(ordersData.map(order => order.storeId))];
        const storePromises = storeIds.map(async (storeId) => {
          try {
            const storeData = await fetchStoreById(storeId);
            return { storeId, storeData };
          } catch (error) {
            console.error(`Failed to fetch store ${storeId}:`, error);
            return { storeId, storeData: { name: "Unknown Store" } };
          }
        });

        const storeResults = await Promise.all(storePromises);
        const storesMap = {};
        storeResults.forEach(({ storeId, storeData }) => {
          storesMap[storeId] = storeData;
        });

        setStores(storesMap);
        setOrders(ordersData);
      } catch (error) {
        console.error("Failed to fetch orders:", error);
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [user, token]);


  const handleMarkOrderAsCompleted = async (orderId) => {
    if (!user?.username || !token) {
      alert('Please login to mark orders as completed');
      return;
    }

    setCompletingOrderId(orderId);

    try {
      const response = await markOrderAsCompleted(orderId, user.username, token);

      if (response.success) {
        // Update the order status locally
        setOrders(prevOrders =>
            prevOrders.map(order =>
                order.orderId === orderId
                    ? { ...order, status: 'COMPLETED' }
                    : order
            )
        );

        // Trigger celebration animation
        setShowCelebration(true);
        setTimeout(() => setShowCelebration(false), 4000);

        // Show success message after a brief delay
        setTimeout(() => {
          alert('🎉 Order marked as completed! Thank you for shopping with us!');
        }, 1500);
      }
    } catch (error) {
      console.error('Error marking order as completed:', error);
      alert(error.message || 'An error occurred while marking the order as completed. Please try again.');
    } finally {
      setCompletingOrderId(null);
    }
  };

  const handleMessageStore = (order) => {
    const store = stores[order.storeId];
    store.id = store.storeId;
    setSelectedStore(store);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setSelectedStore(null);
  };

  const handleRateStore = (order) => {
    setSelectedOrder(order);
    setRatingType('store');
    setShowRatingModal(true);
    setRating(0);
    setComment("");
  };

  const handleRateProduct = (product, order) => {
    setSelectedProduct(product);
    setSelectedOrder(order);
    setRatingType("product");
    setShowRatingModal(true);
    setRating(0);
    setComment("");
  };


  const handleSubmitRating = async () => {
    if (rating === 0) {
      alert('Please select a rating before submitting.');
      return;
    }

    setIsSubmittingRating(true);

    try {
      if (ratingType === 'store') {
        const result = await rateStore(
            selectedOrder.storeId,
            rating,
            comment,
            token,
            user.username
        );
        console.log('Store rating submitted successfully:', result);
        alert('Store rating submitted successfully!');
      } else if (ratingType === "product") {
        const rateData = {
          productId: selectedProduct.productId,
          storeId: selectedOrder.storeId,
          username: user.username,
          rating: rating
        };

          await rateProduct(rateData, token);
          alert("Product rating submitted successfully!");
      }

      // Close modal and reset state
      setShowRatingModal(false);
      setSelectedOrder(null);
      setSelectedProduct(null);
      setRating(0);
      setComment("");

    } catch (error) {
      console.error('Failed to submit rating:', error);

      // Show more specific error message based on error type
      let errorMessage = 'Failed to submit rating. Please try again.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      alert(errorMessage);
    } finally {
      setIsSubmittingRating(false);
    }
  };

  const handleCloseRatingModal = () => {
    setShowRatingModal(false);
    setSelectedOrder(null);
    setSelectedProduct(null);
    setRating(0);
    setComment("");
  };

  const StarRating = ({ rating, onRatingChange }) => {
    return (
        <div className="star-rating">
          {[1, 2, 3, 4, 5].map((star) => (
              <button
                  key={star}
                  type="button"
                  className={`star ${star <= rating ? 'star-filled' : 'star-empty'}`}
                  onClick={() => onRatingChange(star)}
              >
                ★
              </button>
          ))}
        </div>
    );
  };

  // Calculate order statistics
  const orderStats = {
    total: orders.length,
    completed: orders.filter(order => order.status === 'COMPLETED').length,
    pending: orders.filter(order => order.status === 'PENDING').length,
    shipped: orders.filter(order => order.status === 'SHIPPED').length, // ✅ NEW: Track shipped orders
    totalSpent: orders.reduce((sum, order) => sum + (order.finalPrice || order.totalPrice || 0), 0)
  };

  if (loading) {
    return (
        <div className="orders-page">
          <div className="orders-loading">
            <div className="loading-spinner"></div>
            <p className="loading-text">Loading your orders...</p>
          </div>
        </div>
    );
  }

  if (!orders || !orders.length) {
    return (
        <div className="orders-page">
          <div className="orders-page-header">
            <h1 className="orders-title">My Orders</h1>
            <p className="orders-subtitle">Track and manage your order history</p>
          </div>

          <div className="no-orders">
            <div className="no-orders-icon">📦</div>
            <h2>No Orders Yet</h2>
            <p>You haven't placed any orders yet. Start shopping to see your orders here!</p>
            <button
                className="start-shopping-btn"
                onClick={() => window.location.href = '/'}
            >
              Start Shopping
            </button>
          </div>
        </div>
    );
  }

  return (
      <div className="orders-page">
        {showCelebration && (
            <div className="celebration-overlay">
              <div className="celebration-content">
                <div className="celebration-emoji">🎉</div>
                <h2>Order Completed!</h2>
                <p>Thank you for confirming delivery!</p>
                <div className="confetti-container">
                  {Array.from({ length: 50 }).map((_, i) => (
                      <div
                          key={i}
                          className={`confetti-piece confetti-${i % 6}`}
                          style={{
                            left: `${Math.random() * 100}%`,
                            animationDelay: `${Math.random() * 2}s`,
                            animationDuration: `${2 + Math.random() * 2}s`
                          }}
                      ></div>
                  ))}
                </div>
              </div>
            </div>
        )}

        <div className="orders-page-header">
          <h1 className="orders-title">My Orders</h1>
          <p className="orders-subtitle">Track and manage your order history</p>
        </div>

        {/* Order Statistics */}
        <div className="orders-stats">
          <div className="stat-card">
            <div className="stat-number">{orderStats.total}</div>
            <div className="stat-label">Total Orders</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{orderStats.completed}</div>
            <div className="stat-label">Completed</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{orderStats.shipped}</div>
            <div className="stat-label">Shipped</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">${orderStats.totalSpent.toFixed(2)}</div>
            <div className="stat-label">Total Spent</div>
          </div>
        </div>

        <div className="orders-container">
          <div className="orders-list">
            {orders.map((order) => {
              const store = stores[order.storeId];
              const isCompleted = order.status === 'COMPLETED';
              const isShipped = order.status === 'SHIPPED';
              const isCompletingThisOrder = completingOrderId === order.orderId;

              return (
                  <div key={order.orderId} className="order-card">
                    {/* Order Header */}
                    <div className="order-header">
                      <div className="order-meta">
                        <div className="order-id">#{order.orderId.substring(0, 8)}...</div>
                        <div className="order-date">
                          {new Date(order.orderDate).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                      <div className="order-summary">
                        <div className="order-total">${(order.finalPrice || order.totalPrice || 0).toFixed(2)}</div>
                        <div className={`order-status ${order.status?.toLowerCase() || 'pending'}`}>
                          {order.status || 'PENDING'}
                        </div>
                      </div>
                    </div>

                    <div className="order-content">
                      {/* Store Information */}
                      <div className="store-info_orders">
                        <div className="store-name">
                          {store?.data?.name || store?.name || order.storeName || "Unknown Store"}
                        </div>
                        <div className="store-details">
                          Order from this store • {order.products?.length || 0} items
                        </div>
                      </div>

                      {/* Products Section */}
                      <div className="products-section">
                        <div className="products-header">
                          <h4 className="products-title">Items Ordered</h4>
                          <span className="products-count">{order.products?.length || 0} items</span>
                        </div>
                        <ul className="products-list">
                          {order.products?.map((product, index) => (
                              <li key={index} className="product-item">
                                <div className="product-info">
                                  <div className="product-details">
                                    <span className="product-name">{product.name}</span>
                                    <span className="product-quantity">Quantity: {product.quantity}</span>
                                  </div>
                                </div>
                                <div className="product-price">
                                  ${(product.price * product.quantity).toFixed(2)}
                                </div>
                                {isCompleted && (
                                    <button
                                        className="rate-product-btn"
                                        onClick={() => handleRateProduct(product, order)}
                                    >
                                      Rate Product
                                    </button>
                                )}
                              </li>
                          ))}
                        </ul>
                      </div>

                      {/* Order Details */}
                      <div className="order-details">
                        <div className="detail-item">
                          <span className="detail-label">Payment Method</span>
                          <span className="detail-value">{order.paymentMethod || 'N/A'}</span>
                        </div>
                        <div className="detail-item">
                          <span className="detail-label">Delivery Address</span>
                          <span className="detail-value">{order.deliveryAddress || 'N/A'}</span>
                        </div>
                        <div className="detail-item">
                          <span className="detail-label">Order Total</span>
                          <span className="detail-value">${(order.totalPrice || 0).toFixed(2)}</span>
                        </div>
                        <div className="detail-item">
                          <span className="detail-label">Final Price</span>
                          <span className="detail-value">${(order.finalPrice || order.totalPrice || 0).toFixed(2)}</span>
                        </div>
                      </div>
                    </div>

                    {/* Order Actions */}
                    <div className="order-actions">
                      <button
                          className="message-store-btn"
                          onClick={() => handleMessageStore(order)}
                      >
                        💬 Message {store?.data?.name || store?.name || "Store"}
                      </button>


                      {isShipped && (
                          <button
                              className="mark-completed-btn"
                              onClick={() => handleMarkOrderAsCompleted(order.orderId)}
                              disabled={isCompletingThisOrder}
                          >
                            {isCompletingThisOrder ? (
                                <>
                                  <span className="spinner"></span>
                                  Marking as Completed...
                                </>
                            ) : (
                                <>
                                  📦➡️✅ Mark as Delivered
                                </>
                            )}
                          </button>
                      )}

                      {isCompleted && (
                          <>
                            <button
                                className="rate-store-btn"
                                onClick={() => handleRateStore(order)}
                            >
                              ⭐ Rate Store
                            </button>

                            <div className="completed-message">
                              <span className="celebration-icon">🎉</span>
                              Order completed! Thank you for your purchase!
                            </div>
                          </>
                      )}
                    </div>
                  </div>
              );
            })}
          </div>
        </div>

        {/* Message Modal */}
        {showModal && selectedStore && (
            <MessageModal
                store={selectedStore}
                onClose={handleCloseModal}
            />
        )}

        {/* Rating Modal */}
        {showRatingModal && (
            <div className="modal-overlay">
              <div className="rating-modal">
                <div className="modal-header">
                  <h3>
                    Rate {ratingType === 'store'
                      ? (stores[selectedOrder?.storeId]?.data?.name || stores[selectedOrder?.storeId]?.name || 'Store')
                      : (selectedProduct?.name || 'Product')
                  }
                  </h3>
                  <button className="modal-close" onClick={handleCloseRatingModal}>×</button>
                </div>
                <div className="modal-body">
                  <div className="rating-section">
                    <label>How would you rate your experience?</label>
                    <StarRating rating={rating} onRatingChange={setRating} />
                  </div>
                  <div className="comment-section">
                    <label htmlFor="rating-comment">Share your thoughts (optional):</label>
                    <textarea
                        id="rating-comment"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="Tell us about your experience..."
                        rows="4"
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button className="cancel-btn" onClick={handleCloseRatingModal}>
                    Cancel
                  </button>
                  <button
                      className="submit-btn"
                      onClick={handleSubmitRating}
                      disabled={rating === 0 || isSubmittingRating}
                  >
                    {isSubmittingRating ? 'Submitting...' : 'Submit Rating'}
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}