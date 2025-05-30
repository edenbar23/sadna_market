import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../../context/AuthContext';
import { getUserAddresses, getDefaultAddress, addAddress } from '../../api/address';
import AddressList from '../AddressList';
import AddressForm from '../AddressForm';

export default function ShippingForm({ onComplete }) {
    const [selectedOption, setSelectedOption] = useState('saved'); // Start with 'saved' if addresses exist
    const [selectedAddress, setSelectedAddress] = useState(null);
    const [showNewAddressForm, setShowNewAddressForm] = useState(false);
    const [addresses, setAddresses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const { user } = useAuthContext();
    const isGuest = !user?.username;

    // For guest users - simple form data
    const [guestFormData, setGuestFormData] = useState({
        fullName: "",
        address: "",
        city: "",
        postalCode: ""
    });

    useEffect(() => {
        if (!isGuest) {
            loadUserAddresses();
        } else {
            setLoading(false);
        }
    }, [isGuest, user]);

    const loadUserAddresses = async () => {
        try {
            setLoading(true);
            setError(null);

            console.log('Loading addresses for user:', user.username); // Debug log

            // Load all addresses
            const addressResponse = await getUserAddresses(user.username);
            console.log('Address response:', addressResponse); // Debug log

            const userAddresses = addressResponse.data || [];
            console.log('User addresses loaded:', userAddresses); // Debug log

            setAddresses(userAddresses);

            // Set default selection based on whether addresses exist
            if (userAddresses.length > 0) {
                setSelectedOption('saved');

                // Try to get default address
                try {
                    const defaultResponse = await getDefaultAddress(user.username);
                    if (defaultResponse.data) {
                        setSelectedAddress(defaultResponse.data);
                        console.log('Default address selected:', defaultResponse.data);
                    } else {
                        // If no default, select the first address
                        setSelectedAddress(userAddresses[0]);
                        console.log('First address selected:', userAddresses[0]);
                    }
                } catch (err) {
                    console.log('No default address found, selecting first:', err);
                    // No default address, select first if available
                    if (userAddresses.length > 0) {
                        setSelectedAddress(userAddresses[0]);
                    }
                }
            } else {
                // No addresses, default to 'new'
                setSelectedOption('new');
                console.log('No addresses found, defaulting to new');
            }
        } catch (error) {
            console.error('Error loading addresses:', error);
            setError('Failed to load addresses: ' + error.message);
            // If there's an error, default to new address
            setSelectedOption('new');
        } finally {
            setLoading(false);
        }
    };

    const handleGuestChange = (e) => {
        const { name, value } = e.target;
        setGuestFormData({ ...guestFormData, [name]: value });
    };

    const handleGuestSubmit = (e) => {
        e.preventDefault();
        const allFilled = Object.values(guestFormData).every((val) => val.trim() !== "");
        if (allFilled) {
            onComplete({
                type: 'guest',
                data: guestFormData
            });
        } else {
            alert("Please fill in all fields.");
        }
    };

    const handleRegisteredUserSubmit = (e) => {
        e.preventDefault();

        if (selectedOption === 'saved' && selectedAddress) {
            onComplete({
                type: 'saved',
                data: selectedAddress
            });
        } else if (selectedOption === 'new') {
            setShowNewAddressForm(true);
        } else {
            alert("Please select a shipping address.");
        }
    };

    const handleNewAddressSubmit = async (formData) => {
        try {
            setSaving(true);

            console.log('Saving new address:', formData); // Debug log

            // Save the address to the user's account
            const response = await addAddress(user.username, formData);

            console.log('Address save response:', response); // Debug log

            if (response && response.data) {
                // Address was saved successfully
                const savedAddress = response.data;

                // Refresh the address list
                await loadUserAddresses();

                // Use the newly saved address for shipping
                onComplete({
                    type: 'saved',
                    data: savedAddress
                });

                setShowNewAddressForm(false);

                // Show success message
                alert('Address saved successfully and will be used for shipping!');
            } else {
                throw new Error('Failed to save address');
            }

        } catch (error) {
            console.error('Error saving new address:', error);
            alert('Error saving address: ' + (error.message || 'Unknown error'));
        } finally {
            setSaving(false);
        }
    };

    const handleAddressSelect = (address) => {
        setSelectedAddress(address);
        console.log('Address selected:', address); // Debug log
    };

    if (loading) {
        return (
            <div className="shipping-form">
                <h3>Loading shipping information...</h3>
            </div>
        );
    }

    if (error) {
        return (
            <div className="shipping-form">
                <h3>Shipping Address</h3>
                <div className="error-message" style={{ color: 'red', marginBottom: '1rem' }}>
                    {error}
                </div>
                <button
                    onClick={() => setSelectedOption('new')}
                    className="btn btn-primary"
                >
                    Add New Address
                </button>
            </div>
        );
    }

    // Guest user form - simple version
    if (isGuest) {
        return (
            <form className="shipping-form" onSubmit={handleGuestSubmit}>
                <h3>Shipping Address</h3>
                <input
                    type="text"
                    name="fullName"
                    placeholder="Full Name"
                    value={guestFormData.fullName}
                    onChange={handleGuestChange}
                />
                <input
                    type="text"
                    name="address"
                    placeholder="Address"
                    value={guestFormData.address}
                    onChange={handleGuestChange}
                />
                <input
                    type="text"
                    name="city"
                    placeholder="City"
                    value={guestFormData.city}
                    onChange={handleGuestChange}
                />
                <input
                    type="text"
                    name="postalCode"
                    placeholder="Postal Code"
                    value={guestFormData.postalCode}
                    onChange={handleGuestChange}
                />
                <button type="submit">Save Shipping</button>
            </form>
        );
    }

    // Debug info - remove this after testing
    console.log('Rendering with:', {
        addresses: addresses.length,
        selectedOption,
        selectedAddress: selectedAddress?.addressId
    });

    // Registered user form - with saved addresses
    return (
        <div className="shipping-form-container">
            {showNewAddressForm ? (
                <div className="new-address-overlay">
                    <div className="new-address-modal">
                        <AddressForm
                            title="Add Shipping Address"
                            onSubmit={handleNewAddressSubmit}
                            onCancel={() => setShowNewAddressForm(false)}
                            isLoading={saving}
                        />
                    </div>
                </div>
            ) : (
                <form className="shipping-form" onSubmit={handleRegisteredUserSubmit}>
                    <h3>Shipping Address</h3>

                    {/* Debug info - remove after testing */}
                    <div style={{ fontSize: '0.8rem', color: '#666', marginBottom: '1rem' }}>
                        Debug: Found {addresses.length} addresses
                    </div>

                    {/* Show saved addresses if they exist */}
                    {addresses.length > 0 && (
                        <div className="address-option">
                            <label className="radio-option">
                                <input
                                    type="radio"
                                    name="addressOption"
                                    value="saved"
                                    checked={selectedOption === 'saved'}
                                    onChange={(e) => setSelectedOption(e.target.value)}
                                />
                                <span>Use saved address ({addresses.length} available)</span>
                            </label>

                            {selectedOption === 'saved' && (
                                <div className="saved-addresses">
                                    <div className="address-selection">
                                        {addresses.map(address => (
                                            <div
                                                key={address.addressId}
                                                className={`address-option-card ${
                                                    selectedAddress?.addressId === address.addressId ? 'selected' : ''
                                                }`}
                                                onClick={() => handleAddressSelect(address)}
                                            >
                                                <input
                                                    type="radio"
                                                    name="selectedAddress"
                                                    checked={selectedAddress?.addressId === address.addressId}
                                                    onChange={() => handleAddressSelect(address)}
                                                />
                                                <div className="address-summary">
                                                    <div className="address-label">
                                                        {address.label} {address.isDefault && '(Default)'}
                                                    </div>
                                                    <div className="address-preview">
                                                        {address.fullName}<br />
                                                        {address.addressLine1}<br />
                                                        {address.city}, {address.state} {address.postalCode}
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Always show option to add new address */}
                    <div className="address-option">
                        <label className="radio-option">
                            <input
                                type="radio"
                                name="addressOption"
                                value="new"
                                checked={selectedOption === 'new'}
                                onChange={(e) => setSelectedOption(e.target.value)}
                            />
                            <span>Add a new address</span>
                        </label>
                    </div>

                    <div className="form-actions">
                        <button type="submit" className="btn btn-primary">
                            {selectedOption === 'saved' ? 'Use Selected Address' : 'Add New Address'}
                        </button>
                    </div>
                </form>
            )}
        </div>
    );
}