import React, { useState, useEffect } from 'react';
import { getUserAddresses, deleteAddress, setDefaultAddress } from '../api/address';
import { useAuthContext } from '../context/AuthContext';
import AddressForm from './AddressForm';

export default function AddressList({ onSelectAddress, showSelection = false }) {
    const [addresses, setAddresses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [editingAddress, setEditingAddress] = useState(null);
    const [selectedAddress, setSelectedAddress] = useState(null);
    const { user } = useAuthContext();

    useEffect(() => {
        if (user?.username) {
            fetchAddresses();
        }
    }, [user]);

    const fetchAddresses = async () => {
        try {
            setLoading(true);
            const response = await getUserAddresses(user.username);
            setAddresses(response.data || []);
        } catch (err) {
            setError('Failed to load addresses');
            console.error('Error fetching addresses:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAddress = async (addressId) => {
        if (!window.confirm('Are you sure you want to delete this address?')) {
            return;
        }

        try {
            await deleteAddress(user.username, addressId);
            setAddresses(prev => prev.filter(addr => addr.addressId !== addressId));
        } catch (err) {
            setError('Failed to delete address');
            console.error('Error deleting address:', err);
        }
    };

    const handleSetDefault = async (addressId) => {
        try {
            await setDefaultAddress(user.username, addressId);
            setAddresses(prev => prev.map(addr => ({
                ...addr,
                isDefault: addr.addressId === addressId
            })));
        } catch (err) {
            setError('Failed to set default address');
            console.error('Error setting default address:', err);
        }
    };

    const handleAddressSelect = (address) => {
        setSelectedAddress(address);
        if (onSelectAddress) {
            onSelectAddress(address);
        }
    };

    if (loading) {
        return <div className="address-list-loading">Loading addresses...</div>;
    }

    if (error) {
        return <div className="address-list-error">Error: {error}</div>;
    }

    return (
        <div className="address-list-container">
            <div className="address-list-header">
                <h3>Shipping Addresses</h3>
                <button
                    className="btn btn-primary"
                    onClick={() => setShowAddForm(true)}
                >
                    Add New Address
                </button>
            </div>

            {showAddForm && (
                <div className="modal-backdrop">
                    <div className="modal-content">
                        <AddressForm
                            title="Add New Address"
                            onSubmit={async (formData) => {
                                try {
                                    await addAddress(user.username, formData);
                                    setShowAddForm(false);
                                    fetchAddresses();
                                } catch (err) {
                                    setError('Failed to add address');
                                }
                            }}
                            onCancel={() => setShowAddForm(false)}
                        />
                    </div>
                </div>
            )}

            {editingAddress && (
                <div className="modal-backdrop">
                    <div className="modal-content">
                        <AddressForm
                            title="Edit Address"
                            initialData={editingAddress}
                            onSubmit={async (formData) => {
                                try {
                                    await updateAddress(user.username, editingAddress.addressId, formData);
                                    setEditingAddress(null);
                                    fetchAddresses();
                                } catch (err) {
                                    setError('Failed to update address');
                                }
                            }}
                            onCancel={() => setEditingAddress(null)}
                        />
                    </div>
                </div>
            )}

            <div className="address-list">
                {addresses.length === 0 ? (
                    <div className="no-addresses">
                        <p>No addresses saved yet.</p>
                        <button
                            className="btn btn-primary"
                            onClick={() => setShowAddForm(true)}
                        >
                            Add Your First Address
                        </button>
                    </div>
                ) : (
                    addresses.map(address => (
                        <div
                            key={address.addressId}
                            className={`address-card ${
                                showSelection && selectedAddress?.addressId === address.addressId ? 'selected' : ''
                            } ${address.isDefault ? 'default' : ''}`}
                            onClick={() => showSelection && handleAddressSelect(address)}
                        >
                            {address.isDefault && (
                                <div className="default-badge">Default</div>
                            )}

                            <div className="address-header">
                                <span className="address-label">{address.label}</span>
                                {showSelection && (
                                    <input
                                        type="radio"
                                        name="selectedAddress"
                                        checked={selectedAddress?.addressId === address.addressId}
                                        onChange={() => handleAddressSelect(address)}
                                    />
                                )}
                            </div>

                            <div className="address-content">
                                <div className="address-text">
                                    <div className="name">{address.fullName}</div>
                                    <div className="street">{address.addressLine1}</div>
                                    {address.addressLine2 && (
                                        <div className="street">{address.addressLine2}</div>
                                    )}
                                    <div className="city-state">
                                        {address.city}, {address.state} {address.postalCode}
                                    </div>
                                    <div className="country">{address.country}</div>
                                    {address.phoneNumber && (
                                        <div className="phone">Phone: {address.phoneNumber}</div>
                                    )}
                                </div>

                                {!showSelection && (
                                    <div className="address-actions">
                                        <button
                                            className="btn btn-small btn-secondary"
                                            onClick={() => setEditingAddress(address)}
                                        >
                                            Edit
                                        </button>

                                        {!address.isDefault && (
                                            <button
                                                className="btn btn-small btn-primary"
                                                onClick={() => handleSetDefault(address.addressId)}
                                            >
                                                Set Default
                                            </button>
                                        )}

                                        <button
                                            className="btn btn-small btn-danger"
                                            onClick={() => handleDeleteAddress(address.addressId)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}