import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { adminGetSystemInsights } from '../api/admin';
import { Link } from 'react-router-dom';
import '../styles/admin.css';

export default function AdminDashboardPage() {
    const { user } = useAuthContext();
    const [insights, setInsights] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchInsights = async () => {
            if (!user?.isAdmin) {
                setError('Unauthorized: Admin access required');
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const response = await adminGetSystemInsights(user.username, user.token);

                if (response.error) {
                    setError(response.errorMessage || 'Failed to fetch system insights');
                } else {
                    setInsights(response.data);
                }
            } catch (err) {
                console.error('Error fetching system insights:', err);
                setError(err.errorMessage || 'Failed to fetch system insights');
            } finally {
                setLoading(false);
            }
        };

        fetchInsights();
    }, [user]);

    if (!user?.isAdmin) {
        return (
            <div className="admin-page">
                <div className="admin-error">
                    <h2>Unauthorized Access</h2>
                    <p>You need administrator privileges to access this page.</p>
                </div>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="admin-page">
                <div className="admin-loading">
                    <h2>Loading Dashboard...</h2>
                    <div className="loading-spinner"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="admin-page">
                <div className="admin-error">
                    <h2>Error Loading Dashboard</h2>
                    <p>{error}</p>
                    <button onClick={() => window.location.reload()} className="button">
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-header">
                <h1>Admin Dashboard</h1>
                <p>Welcome, Administrator {user.username}</p>
            </div>

            {insights && (
                <div className="admin-overview">
                    <div className="overview-cards">
                        <div className="overview-card">
                            <h3>Total Users</h3>
                            <div className="card-number">{insights.totalUsers}</div>
                            <div className="card-subtitle">
                                {insights.activeUsers} currently active
                            </div>
                        </div>

                        <div className="overview-card">
                            <h3>Total Stores</h3>
                            <div className="card-number">{insights.totalStores}</div>
                        </div>

                        <div className="overview-card">
                            <h3>Total Orders</h3>
                            <div className="card-number">{insights.totalOrders}</div>
                            <div className="card-subtitle">
                                ${insights.totalRevenue?.toFixed(2)} revenue
                            </div>
                        </div>

                        <div className="overview-card">
                            <h3>Pending Reports</h3>
                            <div className="card-number">{insights.pendingReports}</div>
                            <div className="card-subtitle">
                                {insights.pendingReports > 0 ? 'Requires attention' : 'All clear'}
                            </div>
                        </div>
                    </div>

                    <div className="activity-section">
                        <h3>System Activity (Last Hour)</h3>
                        <div className="activity-cards">
                            <div className="activity-card">
                                <h4>Transactions</h4>
                                <div className="activity-number">{insights.transactionRate}</div>
                                <div className="activity-label">per hour</div>
                            </div>
                            <div className="activity-card">
                                <h4>New Subscriptions</h4>
                                <div className="activity-number">{insights.subscriptionRate}</div>
                                <div className="activity-label">per hour</div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <div className="admin-actions">
                <h3>Quick Actions</h3>
                <div className="action-grid">
                    <Link to="/admin/users" className="action-card">
                        <div className="action-icon">👥</div>
                        <h4>Manage Users</h4>
                        <p>View and manage system users</p>
                    </Link>

                    <Link to="/admin/stores" className="action-card">
                        <div className="action-icon">🏪</div>
                        <h4>Manage Stores</h4>
                        <p>View and manage all stores</p>
                    </Link>

                    <Link to="/admin/reports" className="action-card">
                        <div className="action-icon">📋</div>
                        <h4>Handle Reports</h4>
                        <p>Review and respond to violation reports</p>
                        {insights?.pendingReports > 0 && (
                            <div className="action-badge">{insights.pendingReports}</div>
                        )}
                    </Link>

                    <Link to="/admin/insights" className="action-card">
                        <div className="action-icon">📊</div>
                        <h4>System Insights</h4>
                        <p>View detailed system analytics</p>
                    </Link>
                </div>
            </div>

            <div className="admin-summary">
                <h3>System Status</h3>
                <div className="status-indicators">
                    <div className="status-item">
                        <span className="status-label">System Health:</span>
                        <span className="status-value healthy">Operational</span>
                    </div>
                    <div className="status-item">
                        <span className="status-label">Last Updated:</span>
                        <span className="status-value">
                            {insights?.lastUpdated ?
                                new Date(insights.lastUpdated).toLocaleString() :
                                'Just now'
                            }
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
}