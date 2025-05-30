import OrderCard from "../components/OrderCard";
import { useState, useEffect } from "react";
import { fetchOrderHistory } from "../api/order";
import { fetchStoreById, rateStore } from "../api/store";
import { useAuthContext } from "../context/AuthContext";
import MessageModal from "../components/MessageModal";
import "../index.css";

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
      } else if (ratingType === 'product') {
        // TODO: Implement API call to rate product
        console.log('Rating product:', {
          productId: selectedProduct.id,
          rating,
          comment,
          userId: user.id
        });
        // await rateProduct(selectedProduct.id, { rating, comment, userId: user.id });
        alert('Product rating functionality coming soon!');
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

  if (loading) {
    return (
      <div className="orders-page">
        <h1 className="orders-title">Loading orders...</h1>
      </div>
    );
  }

  if (!orders || !orders.length) {
    return (
      <div className="orders-page">
        <h1 className="orders-title">No orders found.</h1>
      </div>
    );
  }

  return (
    <div className="orders-page">
      <h1 className="orders-title">My Orders</h1>
      <div className="orders-list">
        {orders.map((order) => {
          const store = stores[order.storeId];
          const isCompleted = order.status === 'COMPLETED';
          
          return (
            <div key={order.orderId} className="order-card">
              <div className="order-content">
                <OrderCard order={order} />
              </div>
              
              <div className="order-actions">
                <button
                  className="message-store-btn"
                  onClick={() => handleMessageStore(order)}
                >
                  Message {store?.data?.name || store?.name || "Store"}
                </button>
                {isCompleted && (
                  <button
                    className="rate-store-btn"
                    onClick={() => handleRateStore(order)}
                  >
                    Rate Store
                  </button>
                )}
              </div>
            </div>
          );
        })}
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
                <label>Rating:</label>
                <StarRating rating={rating} onRatingChange={setRating} />
              </div>
              <div className="comment-section">
                <label htmlFor="rating-comment">Comment (optional):</label>
                <textarea
                  id="rating-comment"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Share your experience..."
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