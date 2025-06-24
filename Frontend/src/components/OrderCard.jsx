import React, { useState } from "react";
import "../styles/components.css";
import { rateProduct } from "../api/product";
import { useAuthContext } from "../context/AuthContext";
import ErrorAlert from "./ErrorAlert";

export default function OrderCard({ order }) {
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [isSubmittingRating, setIsSubmittingRating] = useState(false);
  const [ratingError, setRatingError] = useState("");

  const { user, token } = useAuthContext();

  const {
    storeName,
    products,
    paymentMethod,
    deliveryAddress,
    totalPrice,
    status,
  } = order;

  const isCompleted = status === 'COMPLETED';
  console.log("OrderCard rendered with order:", order);
  console.log("OrderCard isCompleted:", isCompleted);

  const handleRateProduct = (product) => {
    setSelectedProduct(product);
    setShowRatingModal(true);
    setRating(0);
    setComment("");
  };

  const handleSubmitRating = async () => {
    if (rating === 0) {
      setRatingError('Please select a rating before submitting.');
      return;
    }
    setIsSubmittingRating(true);
    setRatingError("");
    try {
      const rateRequest = {
        productId: selectedProduct.productId,
        storeId: order.storeId,
        rating: rating,
        comment: comment,
        username: user.username
      };
      const result = await rateProduct(rateRequest, token);
      if (result && (result.error || (result.data && result.data.error))) {
        setRatingError(result.error || result.data.error);
        return;
      }
      setShowRatingModal(false);
      setSelectedProduct(null);
      setRating(0);
      setComment("");
      alert('Product rating submitted successfully!');
    } catch (error) {
      console.error('Failed to submit product rating:', error);
      let errorMessage = 'Failed to submit rating. Please try again.';
      if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.message) {
        errorMessage = error.message;
      }
      setRatingError(errorMessage);
    } finally {
      setIsSubmittingRating(false);
    }
  };

  const handleCloseRatingModal = () => {
    setShowRatingModal(false);
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

  return (
    <>
      <div className="order-card">
        <h2>{storeName}</h2>
        <div className="products-section">
          <ul className="products-list">
            {products.map((p, index) => (
              <li key={index} className="product-item">
                <div className="product-info">
                  <span className="product-details">
                    {p.name} × {p.quantity}
                  </span>
                  {p.price && (
                    <span className="product-price">
                      ${(p.price * p.quantity).toFixed(2)}
                    </span>
                  )}
                </div>
                {isCompleted && (
                  <button
                    className="rate-product-btn"
                    onClick={() => handleRateProduct(p)}
                  >
                    Rate Product
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>
        <div className="order-details">
          <p><strong>Payment:</strong> {paymentMethod}</p>
          <p><strong>Shipping:</strong> {deliveryAddress}</p>
          <p><strong>Total:</strong> ${totalPrice.toFixed(2)}</p>
          <p><strong>Status:</strong> {status}</p>
        </div>
      </div>

      {/* Product Rating Modal */}
      {showRatingModal && selectedProduct && (
        <div className="modal-overlay">
          <div className="rating-modal">
            <div className="modal-header">
              <h3>Rate {selectedProduct.name}</h3>
              <button className="modal-close" onClick={handleCloseRatingModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="rating-section">
                <label>Rating:</label>
                <StarRating rating={rating} onRatingChange={setRating} />
              </div>
              <div className="comment-section">
                <label htmlFor="product-rating-comment">Comment (optional):</label>
                <textarea
                  id="product-rating-comment"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Share your experience with this product..."
                  rows="4"
                />
              </div>
              {ratingError && <ErrorAlert message={ratingError} onClose={() => setRatingError("")} />}
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
    </>
  );
}