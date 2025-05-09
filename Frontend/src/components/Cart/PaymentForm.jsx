import React, { useState } from "react";
import '../../index.css';

export default function PaymentForm({ onComplete }) {
  const [formData, setFormData] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    cardHolder: "",
  });

  const isFormComplete = Object.values(formData).every((val) => val.trim() !== "");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (isFormComplete) {
      onComplete(formData);
    }
  };

  return (
    <div className="form-container">
      <h3>Payment Method</h3>
      <form onSubmit={handleSubmit} className="form">
        <input
          type="text"
          name="cardNumber"
          placeholder="Card Number"
          value={formData.cardNumber}
          onChange={handleChange}
        />
        <input
          type="text"
          name="expiryDate"
          placeholder="Expiry Date (MM/YY)"
          value={formData.expiryDate}
          onChange={handleChange}
        />
        <input
          type="text"
          name="cvv"
          placeholder="CVV"
          value={formData.cvv}
          onChange={handleChange}
        />
        <input
          type="text"
          name="cardHolder"
          placeholder="Card Holder Name"
          value={formData.cardHolder}
          onChange={handleChange}
        />
        <button type="submit" className="form-submit-btn" disabled={!isFormComplete}>
          Save Payment Method
        </button>
      </form>
    </div>
  );
} 
