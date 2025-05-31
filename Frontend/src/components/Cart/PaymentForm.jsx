// src/components/Cart/PaymentForm.jsx
import React, { useState, useEffect } from "react";
import { useSavedCards } from '../../hooks/useSavedCards';
import { useAuthContext } from '../../context/AuthContext';
import '../../styles/payment-form.css';

export default function PaymentForm({ onComplete }) {
  const { user } = useAuthContext();
  const {
    savedCards,
    loading: cardsLoading,
    error: cardsError,
    saveCard,
    getCardForPayment,
    deleteCard,
    updateCardNickname
  } = useSavedCards();

  // FIXED: Better initial state management
  const [paymentMethod, setPaymentMethod] = useState('new'); // Always start with 'new'
  const [selectedCardId, setSelectedCardId] = useState('');
  const [showCardNicknameEdit, setShowCardNicknameEdit] = useState('');
  const [newNickname, setNewNickname] = useState('');

  const [formData, setFormData] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    cardHolder: "",
    cardType: "credit",
    billingAddress: {
      addressLine1: "",
      addressLine2: "",
      city: "",
      state: "",
      postalCode: "",
      country: "United States"
    },
    savePaymentMethod: false,
    cardNickname: ""
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // FIXED: Only auto-select saved card if user explicitly chooses it
  useEffect(() => {
    if (savedCards.length > 0 && paymentMethod === 'saved' && !selectedCardId) {
      setSelectedCardId(savedCards[0].id);
    }
  }, [savedCards, paymentMethod, selectedCardId]);

  // FIXED: Clear form when switching between payment methods
  const handlePaymentMethodChange = (method) => {
    setPaymentMethod(method);
    setErrors({}); // Clear any errors

    if (method === 'saved' && savedCards.length > 0) {
      setSelectedCardId(savedCards[0].id);
    } else if (method === 'new') {
      setSelectedCardId('');
      // Optionally clear the form data when switching to new card
      setFormData(prev => ({
        ...prev,
        cardNumber: "",
        expiryDate: "",
        cvv: "",
        cardHolder: "",
        cardType: "credit"
      }));
    }
  };

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
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
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
    const v = value.replace(/\D/g, '');
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

  const validateNewCardForm = () => {
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

  const validateSavedCardForm = () => {
    if (!selectedCardId) {
      setErrors({ form: 'Please select a saved card' });
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (paymentMethod === 'saved') {
      if (!validateSavedCardForm()) return;
    } else {
      if (!validateNewCardForm()) return;
    }

    setIsSubmitting(true);

    try {
      let paymentData;

      if (paymentMethod === 'saved') {
        // Get saved card data
        paymentData = getCardForPayment(selectedCardId);
      } else {
        // Use new card data
        paymentData = {
          cardNumber: formData.cardNumber.replace(/\s/g, ''),
          expiryDate: formData.expiryDate,
          cvv: formData.cvv,
          cardHolder: formData.cardHolder,
          cardType: formData.cardType,
          billingAddress: formData.billingAddress
        };

        // Save card if requested
        if (formData.savePaymentMethod) {
          try {
            const nickname = formData.cardNickname.trim() ||
                `${formData.cardHolder}'s Card`;

            await saveCard(paymentData, nickname);

            // Show success message
            alert('Payment method saved for future use!');
          } catch (saveError) {
            console.warn('Failed to save card:', saveError);
            // Don't prevent checkout if saving fails
            alert('Payment will proceed, but card could not be saved for future use.');
          }
        }
      }

      onComplete(paymentData);
    } catch (error) {
      setErrors({ form: 'Failed to process payment information. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteCard = async (cardId, e) => {
    e.stopPropagation();

    if (!confirm('Are you sure you want to delete this payment method?')) {
      return;
    }

    try {
      await deleteCard(cardId);

      // If deleted card was selected, switch to first available or new card
      if (selectedCardId === cardId) {
        if (savedCards.length > 1) {
          const remainingCards = savedCards.filter(card => card.id !== cardId);
          setSelectedCardId(remainingCards[0].id);
        } else {
          // FIXED: If no cards left, switch to new card method
          setPaymentMethod('new');
          setSelectedCardId('');
        }
      }
    } catch (error) {
      alert('Failed to delete payment method: ' + error.message);
    }
  };

  const handleNicknameEdit = async (cardId) => {
    if (!newNickname.trim()) {
      setShowCardNicknameEdit('');
      return;
    }

    try {
      await updateCardNickname(cardId, newNickname.trim());
      setShowCardNicknameEdit('');
      setNewNickname('');
    } catch (error) {
      alert('Failed to update nickname: ' + error.message);
    }
  };

  const cardType = detectCardType(formData.cardNumber);

  return (
      <div className="payment-form-container">
        <form onSubmit={handleSubmit} className="payment-form">
          <h3 className="payment-form-title">Payment Information</h3>

          {errors.form && <div className="error-text">{errors.form}</div>}
          {cardsError && <div className="error-text">Card loading error: {cardsError}</div>}

          {/* Payment Method Selection */}
          <div className="payment-section">
            <h4 className="section-title">Payment Method</h4>

            {/* Show saved cards if available */}
            {savedCards.length > 0 && (
                <div className="payment-method-option">
                  <label className="radio-option">
                    <input
                        type="radio"
                        name="paymentMethod"
                        value="saved"
                        checked={paymentMethod === 'saved'}
                        onChange={(e) => handlePaymentMethodChange(e.target.value)}
                    />
                    <span>Use saved payment method ({savedCards.length} available)</span>
                  </label>

                  {paymentMethod === 'saved' && (
                      <div className="saved-cards-list">
                        {savedCards.map(card => (
                            <div
                                key={card.id}
                                className={`saved-card-option ${selectedCardId === card.id ? 'selected' : ''}`}
                                onClick={() => setSelectedCardId(card.id)}
                            >
                              <div className="card-selection">
                                <input
                                    type="radio"
                                    name="selectedCard"
                                    value={card.id}
                                    checked={selectedCardId === card.id}
                                    onChange={() => setSelectedCardId(card.id)}
                                />

                                <div className="card-info">
                                  <div className="card-primary-info">
                                    {showCardNicknameEdit === card.id ? (
                                        <div className="nickname-edit">
                                          <input
                                              type="text"
                                              value={newNickname}
                                              onChange={(e) => setNewNickname(e.target.value)}
                                              onBlur={() => handleNicknameEdit(card.id)}
                                              onKeyDown={(e) => {
                                                if (e.key === 'Enter') handleNicknameEdit(card.id);
                                                if (e.key === 'Escape') {
                                                  setShowCardNicknameEdit('');
                                                  setNewNickname('');
                                                }
                                              }}
                                              autoFocus
                                              className="nickname-input"
                                          />
                                        </div>
                                    ) : (
                                        <span className="card-nickname">{card.nickname}</span>
                                    )}
                                    <span className="card-number">{card.maskedNumber}</span>
                                  </div>

                                  <div className="card-details">
                                    <span className="card-expiry">Expires {card.expiryDate}</span>
                                    <span className="card-holder">{card.cardHolder}</span>
                                  </div>
                                </div>
                              </div>

                              <div className="card-actions">
                                <button
                                    type="button"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setShowCardNicknameEdit(card.id);
                                      setNewNickname(card.nickname);
                                    }}
                                    className="btn-edit-nickname"
                                    title="Edit nickname"
                                >
                                  ✏️
                                </button>
                                <button
                                    type="button"
                                    onClick={(e) => handleDeleteCard(card.id, e)}
                                    className="btn-delete-card"
                                    title="Delete this card"
                                >
                                  🗑️
                                </button>
                              </div>
                            </div>
                        ))}
                      </div>
                  )}
                </div>
            )}

            {/* New card option - FIXED: Always show this option */}
            <div className="payment-method-option">
              <label className="radio-option">
                <input
                    type="radio"
                    name="paymentMethod"
                    value="new"
                    checked={paymentMethod === 'new'}
                    onChange={(e) => handlePaymentMethodChange(e.target.value)}
                />
                <span>Add new payment method</span>
              </label>
            </div>
          </div>

          {/* FIXED: Show new card form when 'new' is selected */}
          {paymentMethod === 'new' && (
              <>
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

                {/* Save Card Options */}
                <div className="payment-section">
                  <h4 className="section-title">Save for Future Use</h4>

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

                  {formData.savePaymentMethod && (
                      <div className="form-group">
                        <label htmlFor="cardNickname">Card Nickname (Optional)</label>
                        <input
                            type="text"
                            id="cardNickname"
                            name="cardNickname"
                            value={formData.cardNickname}
                            onChange={handleChange}
                            placeholder="e.g., Personal Card, Work Card"
                            disabled={isSubmitting}
                        />
                        <span className="help-text">
                    Give this card a name to easily identify it later
                  </span>
                      </div>
                  )}
                </div>
              </>
          )}

          {/* Security Notice */}
          <div className="security-info">
            <span className="security-icon">🔒</span>
            <span className="security-text">
            Your payment information is encrypted and stored securely
          </span>
          </div>

          <div className="form-actions">
            <button
                type="submit"
                className="btn btn-primary"
                disabled={isSubmitting || cardsLoading}
            >
              {isSubmitting ? 'Processing...' : 'Save Payment Method'}
            </button>
          </div>
        </form>
      </div>
  );
}