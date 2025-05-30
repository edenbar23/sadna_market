import React, { useState } from 'react';

export default function AddressForm({
                                        initialData = {},
                                        onSubmit,
                                        onCancel,
                                        isLoading = false,
                                        title = "Add Address"
                                    }) {
    const [formData, setFormData] = useState({
        fullName: initialData.fullName || '',
        addressLine1: initialData.addressLine1 || '',
        addressLine2: initialData.addressLine2 || '',
        city: initialData.city || '',
        state: initialData.state || '',
        postalCode: initialData.postalCode || '',
        country: initialData.country || 'Israel', // Default to Israel
        phoneNumber: initialData.phoneNumber || '',
        label: initialData.label || 'Home',
        isDefault: initialData.isDefault || false
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        // Clear state field when changing to a country that doesn't use states
        if (name === 'country') {
            const countriesWithStates = ['United States', 'Canada', 'Australia'];
            if (!countriesWithStates.includes(value)) {
                setFormData(prev => ({
                    ...prev,
                    [name]: value,
                    state: '' // Clear state when switching to country without states
                }));
            }
        }

        // Clear error when user starts typing
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.fullName.trim()) {
            newErrors.fullName = 'Full name is required';
        }

        if (!formData.addressLine1.trim()) {
            newErrors.addressLine1 = 'Address line 1 is required';
        }

        if (!formData.city.trim()) {
            newErrors.city = 'City is required';
        }

        // Only validate state for countries that have states
        const countriesWithStates = ['United States', 'Canada', 'Australia'];
        if (countriesWithStates.includes(formData.country) && !formData.state.trim()) {
            const stateLabel = formData.country === 'Canada' ? 'Province' : 'State';
            newErrors.state = `${stateLabel} is required`;
        }

        if (!formData.postalCode.trim()) {
            newErrors.postalCode = 'Postal code is required';
        }

        if (!formData.country.trim()) {
            newErrors.country = 'Country is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (validateForm()) {
            onSubmit(formData);
        }
    };

    // Check if current country uses states/provinces
    const countriesWithStates = ['United States', 'Canada', 'Australia'];
    const showStateField = countriesWithStates.includes(formData.country);

    return (
        <div className="address-form-container">
            <form onSubmit={handleSubmit} className="address-form">
                <h3 className="address-form-title">{title}</h3>

                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="fullName">Full Name *</label>
                        <input
                            type="text"
                            id="fullName"
                            name="fullName"
                            value={formData.fullName}
                            onChange={handleChange}
                            className={errors.fullName ? 'error' : ''}
                            placeholder="Enter full name"
                        />
                        {errors.fullName && <span className="error-text">{errors.fullName}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="phoneNumber">Phone Number</label>
                        <input
                            type="tel"
                            id="phoneNumber"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleChange}
                            placeholder="Enter phone number"
                        />
                    </div>
                </div>

                <div className="form-group">
                    <label htmlFor="addressLine1">Address Line 1 *</label>
                    <input
                        type="text"
                        id="addressLine1"
                        name="addressLine1"
                        value={formData.addressLine1}
                        onChange={handleChange}
                        className={errors.addressLine1 ? 'error' : ''}
                        placeholder="Enter street address"
                    />
                    {errors.addressLine1 && <span className="error-text">{errors.addressLine1}</span>}
                </div>

                <div className="form-group">
                    <label htmlFor="addressLine2">Address Line 2</label>
                    <input
                        type="text"
                        id="addressLine2"
                        name="addressLine2"
                        value={formData.addressLine2}
                        onChange={handleChange}
                        placeholder="Apartment, suite, etc. (optional)"
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="country">Country *</label>
                    <select
                        id="country"
                        name="country"
                        value={formData.country}
                        onChange={handleChange}
                        className={errors.country ? 'error' : ''}
                    >
                        <option value="Israel">Israel</option>
                        <option value="United States">United States</option>
                        <option value="Canada">Canada</option>
                        <option value="United Kingdom">United Kingdom</option>
                        <option value="Germany">Germany</option>
                        <option value="France">France</option>
                        <option value="Australia">Australia</option>
                        <option value="Other">Other</option>
                    </select>
                    {errors.country && <span className="error-text">{errors.country}</span>}
                </div>

                <div className={showStateField ? "form-row" : "form-row two-col"}>
                    <div className="form-group">
                        <label htmlFor="city">City *</label>
                        <input
                            type="text"
                            id="city"
                            name="city"
                            value={formData.city}
                            onChange={handleChange}
                            className={errors.city ? 'error' : ''}
                            placeholder="Enter city"
                        />
                        {errors.city && <span className="error-text">{errors.city}</span>}
                    </div>

                    {showStateField && (
                        <div className="form-group">
                            <label htmlFor="state">
                                {formData.country === 'Canada' ? 'Province' : 'State'} *
                            </label>
                            <input
                                type="text"
                                id="state"
                                name="state"
                                value={formData.state}
                                onChange={handleChange}
                                className={errors.state ? 'error' : ''}
                                placeholder={`Enter ${formData.country === 'Canada' ? 'province' : 'state'}`}
                            />
                            {errors.state && <span className="error-text">{errors.state}</span>}
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="postalCode">
                            {formData.country === 'United Kingdom' ? 'Postcode' :
                                formData.country === 'Israel' ? 'Postal Code' : 'Postal Code'} *
                        </label>
                        <input
                            type="text"
                            id="postalCode"
                            name="postalCode"
                            value={formData.postalCode}
                            onChange={handleChange}
                            className={errors.postalCode ? 'error' : ''}
                            placeholder={`Enter ${formData.country === 'United Kingdom' ? 'postcode' : 'postal code'}`}
                        />
                        {errors.postalCode && <span className="error-text">{errors.postalCode}</span>}
                    </div>
                </div>

                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="label">Label</label>
                        <select
                            id="label"
                            name="label"
                            value={formData.label}
                            onChange={handleChange}
                        >
                            <option value="Home">Home</option>
                            <option value="Work">Work</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>
                </div>

                <div className="form-group checkbox-group">
                    <label className="checkbox-label">
                        <input
                            type="checkbox"
                            name="isDefault"
                            checked={formData.isDefault}
                            onChange={handleChange}
                        />
                        <span className="checkmark"></span>
                        Set as default address
                    </label>
                </div>

                <div className="form-actions">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="btn btn-secondary"
                        disabled={isLoading}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Saving...' : 'Save Address'}
                    </button>
                </div>
            </form>
        </div>
    );
}