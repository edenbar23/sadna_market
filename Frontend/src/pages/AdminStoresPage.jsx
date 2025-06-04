import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { adminGetAllStores, adminCloseStore } from '../api/admin';
import { Link } from 'react-router-dom';
import '../styles/admin.css';

export default function AdminStoresPage() {
    const { user } = useAuthContext();
    const [stores, setStores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [closingStore, setClosingStore] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState('all'); // all, active, inactive

    useEffect(() => {
        fetchStores();
    }, [user]);

    const fetchStores = async () => {
        if (!user?.isAdmin) {
            setError('Unauthorized: Admin access required');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const response = await adminGetAllStores();

            if (response.error) {
                setError(response.errorMessage || 'Failed to fetch stores');
            } else {
                setStores(response.data || []);
            }
        } catch (err) {
            console.error('Error fetching stores:', err);
            setError(err.errorMessage || 'Failed to fetch stores');
        } finally {
            setLoading(false);
        }
    };

    const handleCloseStore = async (storeId, storeName) => {
        if (!user?.isAdmin) {
            alert('Unauthorized action');
            return;
        }

        const confirmClosure = window.confirm(
            `Are you sure you want to close store "${storeName}"? This will make it inactive and inaccessible to customers.`
        );

        if (!confirmClosure) return;

        try {
            setClosingStore(storeId);
            const response = await adminCloseStore(user.username, user.token, storeId);

            if (response.error) {
                alert(`Failed to close store: ${response.errorMessage}`);
            } else {
                // Update store status in local state
                setStores(stores.map(store =>
                    store.storeId === storeId
                        ? { ...store, active: false }
                        : store
                ));
                alert(`Store "${storeName}" closed successfully`);
            }
        } catch (err) {
            console.error('Error closing store:', err);
            alert(`Failed to close store: ${err.errorMessage || 'Unknown error'}`);
        } finally {
            setClosingStore(null);
        }
    };

    // Filter stores based on search term and filter type
    const filteredStores = stores.filter(store => {
        const matchesSearch = !searchTerm ||
            store.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            store.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            store.founderUsername.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesFilter = filterType === 'all' ||
            (filterType === 'active' && store.active) ||
            (filterType === 'inactive' && !store.active);

        return matchesSearch && matchesFilter;
    });

    if (!user?.isAdmin) {
        return (
            <div className="admin-page">
                <div className="admin-error">
                    <h2>Unauthorized Access</h2>
                    <p>You need administrator privileges to access this page.</p>
                    <Link to="/admin" className="button">Back to Dashboard</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-header">
                <div className="header-content">
                    <div>
                        <h1>Store Management</h1>
                        <p>Manage all system stores</p>
                    </div>
                    <Link to="/admin" className="back-button">← Back to Dashboard</Link>
                </div>
            </div>

            {/* Search and Filter Controls */}
            <div className="controls-section">
                <div className="search-controls">
                    <input
                        type="text"
                        placeholder="Search stores by name, description, or founder..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                    <select
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">All Stores</option>
                        <option value="active">Active Stores</option>
                        <option value="inactive">Inactive Stores</option>
                    </select>
                </div>

                <div className="stats-summary">
                    <span>Total: {stores.length}</span>
                    <span>Showing: {filteredStores.length}</span>
                    <span>Active: {stores.filter(s => s.active).length}</span>
                    <span>Inactive: {stores.filter(s => !s.active).length}</span>
                </div>
            </div>

            {loading ? (
                <div className="admin-loading">
                    <div className="loading-spinner"></div>
                    <p>Loading stores...</p>
                </div>
            ) : error ? (
                <div className="admin-error">
                    <h3>Error Loading Stores</h3>
                    <p>{error}</p>
                    <button onClick={fetchStores} className="button">Retry</button>
                </div>
            ) : (
                <div className="stores-section">
                    {filteredStores.length === 0 ? (
                        <div className="no-stores">
                            <h3>No Stores Found</h3>
                            <p>No stores match your current search and filter criteria.</p>
                        </div>
                    ) : (
                        <div className="stores-grid">
                            {filteredStores.map((store) => (
                                <div key={store.storeId} className="store-card">
                                    <div className="store-header">
                                        <div className="store-icon">
                                            🏪
                                        </div>
                                        <div className="store-basic-info">
                                            <h3>{store.name}</h3>
                                            <p className="store-founder">Founded by @{store.founderUsername}</p>
                                        </div>
                                        <div className="store-badges">
                                            <span className={`badge status ${store.active ? 'active' : 'inactive'}`}>
                                                {store.active ? 'Active' : 'Inactive'}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="store-description">
                                        <p>{store.description || 'No description provided'}</p>
                                    </div>

                                    <div className="store-details">
                                        <div className="detail-item">
                                            <span className="detail-label">Store ID:</span>
                                            <span className="detail-value store-id">{store.storeId}</span>
                                        </div>
                                        <div className="detail-item">
                                            <span className="detail-label">Owners:</span>
                                            <span className="detail-value">{store.ownerUsernames?.length || 0}</span>
                                        </div>
                                        <div className="detail-item">
                                            <span className="detail-label">Managers:</span>
                                            <span className="detail-value">{store.managerUsernames?.length || 0}</span>
                                        </div>
                                    </div>

                                    {/* Store Personnel */}
                                    {(store.ownerUsernames?.length > 0 || store.managerUsernames?.length > 0) && (
                                        <div className="store-personnel">
                                            {store.ownerUsernames?.length > 0 && (
                                                <div className="personnel-section">
                                                    <h4>Owners:</h4>
                                                    <div className="personnel-list">
                                                        {store.ownerUsernames.map(owner => (
                                                            <span key={owner} className="personnel-tag owner">@{owner}</span>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                            {store.managerUsernames?.length > 0 && (
                                                <div className="personnel-section">
                                                    <h4>Managers:</h4>
                                                    <div className="personnel-list">
                                                        {store.managerUsernames.map(manager => (
                                                            <span key={manager} className="personnel-tag manager">@{manager}</span>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    <div className="store-actions">
                                        <Link
                                            to={`/store/${store.storeId}`}
                                            className="view-button"
                                            target="_blank"
                                        >
                                            View Store
                                        </Link>

                                        {store.active ? (
                                            <button
                                                onClick={() => handleCloseStore(store.storeId, store.name)}
                                                disabled={closingStore === store.storeId}
                                                className="close-button"
                                            >
                                                {closingStore === store.storeId ? 'Closing...' : 'Close Store'}
                                            </button>
                                        ) : (
                                            <span className="store-closed-indicator">Store Closed</span>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}