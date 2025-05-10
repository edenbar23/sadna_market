import React, { useState } from "react";
import '../../index.css';

export default function ShippingForm({ onComplete }) {
  const [formData, setFormData] = useState({
    fullName: "",
    address: "",
    city: "",
    postalCode: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const allFilled = Object.values(formData).every((val) => val.trim() !== "");
    if (allFilled) {
      onComplete(formData);
    } else {
      alert("Please fill in all fields.");
    }
  };

  return (
    <form className="shipping-form" onSubmit={handleSubmit}>
      <h3>Shipping Address</h3>
      <input type="text" name="fullName" placeholder="Full Name" value={formData.fullName} onChange={handleChange} />
      <input type="text" name="address" placeholder="Address" value={formData.address} onChange={handleChange} />
      <input type="text" name="city" placeholder="City" value={formData.city} onChange={handleChange} />
      <input type="text" name="postalCode" placeholder="Postal Code" value={formData.postalCode} onChange={handleChange} />
      <button type="submit">Save Shipping</button>
    </form>
  );
}
