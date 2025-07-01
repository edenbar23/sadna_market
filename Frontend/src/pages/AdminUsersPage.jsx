import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { adminGetAllUsers, adminRemoveUser } from '../api/admin';
import { Link } from 'react-router-dom';
import '../styles/admin.css';

export default function AdminUsersPage() {
    const { user } = useAuthContext();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [removingUser, setRemovingUser] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState('all'); // all, admin, regular, active, inactive

//     useEffect(() => {
//         fetchUsers();
//     }, [user]);

    // Fetch all users from the server
    useEffect(() => {
        fetchUsers();

        // Auto-refresh every 30 seconds
        const interval = setInterval(() => {
            fetchUsers(false); // false = don't show loading spinner
        }, 30000);

        return () => clearInterval(interval);
    }, [user]);
    const fetchUsers = async () => {
        if (!user?.isAdmin) {
            setError('Unauthorized: Admin access required');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const response = await adminGetAllUsers(user.username, user.token);

            if (response.error) {
                setError(response.errorMessage || 'Failed to fetch users');
            } else {
                setUsers(response.data || []);
            }
        } catch (err) {
            console.error('Error fetching users:', err);
            setError(err.errorMessage || 'Failed to fetch users');
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveUser = async (targetUsername) => {
        if (!user?.isAdmin) {
            alert('Unauthorized action');
            return;
        }

        if (targetUsername === user.username) {
            alert('You cannot remove yourself!');
            return;
        }

        const confirmRemoval = window.confirm(
            `Are you sure you want to remove user "${targetUsername}"? This action cannot be undone.`
        );

        if (!confirmRemoval) return;

        try {
            setRemovingUser(targetUsername);
            const response = await adminRemoveUser(user.username, user.token, targetUsername);

            if (response.error) {
                alert(`Failed to remove user: ${response.errorMessage}`);
            } else {
                // Remove user from local state
                setUsers(users.filter(u => u.userName !== targetUsername));
                alert(`User "${targetUsername}" removed successfully`);
            }
        } catch (err) {
            console.error('Error removing user:', err);
            alert(`Failed to remove user: ${err.errorMessage || 'Unknown error'}`);
        } finally {
            setRemovingUser(null);
        }
    };

    // Filter users based on search term and filter type
    const filteredUsers = users.filter(u => {
        const matchesSearch = !searchTerm ||
            u.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
            u.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            u.lastName.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesFilter = filterType === 'all' ||
            (filterType === 'admin' && u.isAdmin) ||
            (filterType === 'regular' && !u.isAdmin) ||
            (filterType === 'active' && u.isLoggedIn) ||
            (filterType === 'inactive' && !u.isLoggedIn);

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
                        <h1>User Management</h1>
                        <p>Manage all system users</p>
                    </div>
                    <Link to="/admin" className="back-button">← Back to Dashboard</Link>
                </div>
            </div>

            {/* Search and Filter Controls */}
            <div className="controls-section">
                <div className="search-controls">
                    <input
                        type="text"
                        placeholder="Search users by name, username, or email..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                    <select
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">All Users</option>
                        <option value="admin">Administrators</option>
                        <option value="regular">Regular Users</option>
                        <option value="active">Currently Active</option>
                        <option value="inactive">Inactive</option>
                    </select>
                </div>

                <div className="stats-summary">
                    <span>Total: {users.length}</span>
                    <span>Showing: {filteredUsers.length}</span>
                    <span>Admins: {users.filter(u => u.isAdmin).length}</span>
                    <span>Active: {users.filter(u => u.isLoggedIn).length}</span>
                </div>
            </div>

            {loading ? (
                <div className="admin-loading">
                    <div className="loading-spinner"></div>
                    <p>Loading users...</p>
                </div>
            ) : error ? (
                <div className="admin-error">
                    <h3>Error Loading Users</h3>
                    <p>{error}</p>
                    <button onClick={fetchUsers} className="button">Retry</button>
                </div>
            ) : (
                <div className="users-section">
                    {filteredUsers.length === 0 ? (
                        <div className="no-users">
                            <h3>No Users Found</h3>
                            <p>No users match your current search and filter criteria.</p>
                        </div>
                    ) : (
                        <div className="users-grid">
                            {filteredUsers.map((u) => (
                                <div key={u.userName} className="user-card">
                                    <div className="user-header">
                                        <div className="user-avatar">
                                            {u.firstName ? u.firstName[0].toUpperCase() : u.userName[0].toUpperCase()}
                                        </div>
                                        <div className="user-basic-info">
                                            <h3>{u.firstName} {u.lastName}</h3>
                                            <p className="username">@{u.userName}</p>
                                        </div>
                                        <div className="user-badges">
                                            {u.isAdmin && <span className="badge admin">Admin</span>}
                                            <span className={`badge status ${u.isLoggedIn ? 'active' : 'inactive'}`}>
                                                {u.isLoggedIn ? 'Active' : 'Offline'}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="user-details">
                                        <div className="detail-item">
                                            <span className="detail-label">Email:</span>
                                            <span className="detail-value">{u.email}</span>
                                        </div>
                                        <div className="detail-item">
                                            <span className="detail-label">Cart Items:</span>
                                            <span className="detail-value">{u.cartItemsCount}</span>
                                        </div>
                                    </div>

                                    <div className="user-actions">
                                        {u.userName === user.username ? (
                                            <span className="current-user-indicator">This is you</span>
                                        ) : u.isAdmin ? (
                                            <span className="admin-protected">Protected Admin</span>
                                        ) : (
                                            <button
                                                onClick={() => handleRemoveUser(u.userName)}
                                                disabled={removingUser === u.userName}
                                                className="remove-button"
                                            >
                                                {removingUser === u.userName ? 'Removing...' : 'Remove User'}
                                            </button>
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