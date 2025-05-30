import React, { useState } from "react";
import '../../styles/payment-form.css'; // We'll create this new CSS file

export default function PaymentForm({ onComplete }) {
  const [formData, setFormData] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    cardHolder: "",
    cardType: "credit", // credit, debit
    billingAddress: {
      addressLine1: "",
      addressLine2: "",
      city: "",
      state: "",
      postalCode: "",
      country: "United States"
    },
    savePaymentMethod: false
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name.startsWith('billing.')) {
      const billingField = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        billingAddress: {
          ...prev.billingAddress,
          [billingField]: value
        }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: type === 'checkbox' ? checked : value
      }));
    }

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const formatCardNumber = (value) => {
    // Remove all non-digits
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');

    // Add spaces every 4 digits
    const matches = v.match(/\d{4,16}/g);
    const match = matches && matches[0] || '';
    const parts = [];

    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }

    if (parts.length) {
      return parts.join(' ');
    } else {
      return v;
    }
  };

  const formatExpiryDate = (value) => {
    // Remove all non-digits
    const v = value.replace(/\D/g, '');

    // Add slash after 2 digits
    if (v.length >= 2) {
      return v.slice(0, 2) + '/' + v.slice(2, 4);
    }

    return v;
  };

  const handleCardNumberChange = (e) => {
    const formatted = formatCardNumber(e.target.value);
    setFormData(prev => ({ ...prev, cardNumber: formatted }));
  };

  const handleExpiryChange = (e) => {
    const formatted = formatExpiryDate(e.target.value);
    setFormData(prev => ({ ...prev, expiryDate: formatted }));
  };

  const detectCardType = (number) => {
    const cleanNumber = number.replace(/\s/g, '');

    if (/^4/.test(cleanNumber)) return 'visa';
    if (/^5[1-5]/.test(cleanNumber)) return 'mastercard';
    if (/^3[47]/.test(cleanNumber)) return 'amex';
    if (/^6/.test(cleanNumber)) return 'discover';

    return 'unknown';
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.cardHolder.trim()) {
      newErrors.cardHolder = 'Cardholder name is required';
    }

    const cleanCardNumber = formData.cardNumber.replace(/\s/g, '');
    if (!cleanCardNumber) {
      newErrors.cardNumber = 'Card number is required';
    } else if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
      newErrors.cardNumber = 'Invalid card number';
    }

    if (!formData.expiryDate) {
      newErrors.expiryDate = 'Expiry date is required';
    } else if (!/^\d{2}\/\d{2}$/.test(formData.expiryDate)) {
      newErrors.expiryDate = 'Invalid expiry date format (MM/YY)';
    } else {
      const [month, year] = formData.expiryDate.split('/');
      const currentDate = new Date();
      const currentYear = currentDate.getFullYear() % 100;
      const currentMonth = currentDate.getMonth() + 1;

      if (parseInt(month) < 1 || parseInt(month) > 12) {
        newErrors.expiryDate = 'Invalid month';
      } else if (parseInt(year) < currentYear || (parseInt(year) === currentYear && parseInt(month) < currentMonth)) {
        newErrors.expiryDate = 'Card has expired';
      }
    }

    if (!formData.cvv) {
      newErrors.cvv = 'CVV is required';
    } else if (formData.cvv.length < 3 || formData.cvv.length > 4) {
      newErrors.cvv = 'Invalid CVV';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      // Clean the data before sending
      const cleanData = {
        cardNumber: formData.cardNumber.replace(/\s/g, ''),
        expiryDate: formData.expiryDate,
        cvv: formData.cvv,
        cardHolder: formData.cardHolder,
        cardType: formData.cardType,
        billingAddress: formData.billingAddress
      };

      onComplete(cleanData);
    } catch (error) {
      setErrors({ form: 'Failed to process payment information. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const cardType = detectCardType(formData.cardNumber);

  return (
      <div className="payment-form-container">
        <form onSubmit={handleSubmit} className="payment-form">
          <h3 className="payment-form-title">Payment Information</h3>

          {errors.form && <div className="error-text">{errors.form}</div>}

          {/* Card Information Section */}
          <div className="payment-section">
            <h4 className="section-title">Card Details</h4>

            <div className="form-group">
              <label htmlFor="cardHolder">Cardholder Name *</label>
              <input
                  type="text"
                  id="cardHolder"
                  name="cardHolder"
                  value={formData.cardHolder}
                  onChange={handleChange}
                  className={errors.cardHolder ? 'error' : ''}
                  placeholder="Enter name as shown on card"
                  disabled={isSubmitting}
              />
              {errors.cardHolder && <span className="error-text">{errors.cardHolder}</span>}
            </div>

            <div className="form-group card-number-group">
              <label htmlFor="cardNumber">Card Number *</label>
              <div className="card-input-wrapper">
                <input
                    type="text"
                    id="cardNumber"
                    name="cardNumber"
                    value={formData.cardNumber}
                    onChange={handleCardNumberChange}
                    className={errors.cardNumber ? 'error' : ''}
                    placeholder="1234 5678 9012 3456"
                    maxLength="19"
                    disabled={isSubmitting}
                />
                <div className={`card-type-icon ${cardType}`}>
                  {cardType === 'visa' && '💳'}
                  {cardType === 'mastercard' && '💳'}
                  {cardType === 'amex' && '💳'}
                  {cardType === 'discover' && '💳'}
                  {cardType === 'unknown' && '💳'}
                </div>
              </div>
              {errors.cardNumber && <span className="error-text">{errors.cardNumber}</span>}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="expiryDate">Expiry Date *</label>
                <input
                    type="text"
                    id="expiryDate"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleExpiryChange}
                    className={errors.expiryDate ? 'error' : ''}
                    placeholder="MM/YY"
                    maxLength="5"
                    disabled={isSubmitting}
                />
                {errors.expiryDate && <span className="error-text">{errors.expiryDate}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="cvv">CVV *</label>
                <input
                    type="text"
                    id="cvv"
                    name="cvv"
                    value={formData.cvv}
                    onChange={handleChange}
                    className={errors.cvv ? 'error' : ''}
                    placeholder="123"
                    maxLength="4"
                    disabled={isSubmitting}
                />
                {errors.cvv && <span className="error-text">{errors.cvv}</span>}
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="cardType">Card Type</label>
              <select
                  id="cardType"
                  name="cardType"
                  value={formData.cardType}
                  onChange={handleChange}
                  disabled={isSubmitting}
              >
                <option value="credit">Credit Card</option>
                <option value="debit">Debit Card</option>
              </select>
            </div>
          </div>

          {/* Billing Address Section */}
          <div className="payment-section">
            <h4 className="section-title">Billing Address</h4>

            <div className="form-group">
              <label htmlFor="billing.addressLine1">Address Line 1 *</label>
              <input
                  type="text"
                  id="billing.addressLine1"
                  name="billing.addressLine1"
                  value={formData.billingAddress.addressLine1}
                  onChange={handleChange}
                  placeholder="Enter street address"
                  disabled={isSubmitting}
              />
            </div>

            <div className="form-group">
              <label htmlFor="billing.addressLine2">Address Line 2</label>
              <input
                  type="text"
                  id="billing.addressLine2"
                  name="billing.addressLine2"
                  value={formData.billingAddress.addressLine2}
                  onChange={handleChange}
                  placeholder="Apartment, suite, etc. (optional)"
                  disabled={isSubmitting}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="billing.city">City *</label>
                <input
                    type="text"
                    id="billing.city"
                    name="billing.city"
                    value={formData.billingAddress.city}
                    onChange={handleChange}
                    placeholder="Enter city"
                    disabled={isSubmitting}
                />
              </div>

              {/* Only show state field for countries that have states */}
              {(formData.billingAddress.country === 'United States' ||
                  formData.billingAddress.country === 'Canada' ||
                  formData.billingAddress.country === 'Australia') && (
                  <div className="form-group">
                    <label htmlFor="billing.state">
                      {formData.billingAddress.country === 'Canada' ? 'Province' : 'State'} *
                    </label>
                    <input
                        type="text"
                        id="billing.state"
                        name="billing.state"
                        value={formData.billingAddress.state}
                        onChange={handleChange}
                        placeholder={`Enter ${formData.billingAddress.country === 'Canada' ? 'province' : 'state'}`}
                        disabled={isSubmitting}
                    />
                  </div>
              )}

              <div className="form-group">
                <label htmlFor="billing.postalCode">
                  {formData.billingAddress.country === 'United Kingdom' ? 'Postcode' :
                      formData.billingAddress.country === 'Israel' ? 'Postal Code' : 'Postal Code'} *
                </label>
                <input
                    type="text"
                    id="billing.postalCode"
                    name="billing.postalCode"
                    value={formData.billingAddress.postalCode}
                    onChange={handleChange}
                    placeholder={`Enter ${formData.billingAddress.country === 'United Kingdom' ? 'postcode' : 'postal code'}`}
                    disabled={isSubmitting}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="billing.country">Country *</label>
              <select
                  id="billing.country"
                  name="billing.country"
                  value={formData.billingAddress.country}
                  onChange={handleChange}
                  disabled={isSubmitting}
              >
                <option value="United States">United States</option>
                <option value="Canada">Canada</option>
                <option value="United Kingdom">United Kingdom</option>
                <option value="Germany">Germany</option>
                <option value="France">France</option>
                <option value="Australia">Australia</option>
                <option value="Israel">Israel</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>

          {/* Options */}
          <div className="form-group checkbox-group">
            <label className="checkbox-label">
              <input
                  type="checkbox"
                  name="savePaymentMethod"
                  checked={formData.savePaymentMethod}
                  onChange={handleChange}
                  disabled={isSubmitting}
              />
              <span className="checkmark"></span>
              Save this payment method for future purchases
            </label>
          </div>

          <div className="form-actions">
            <button
                type="submit"
                className="btn btn-primary"
                disabled={isSubmitting}
            >
              {isSubmitting ? 'Processing...' : 'Save Payment Method'}
            </button>
          </div>
        </form>
      </div>
  );
}