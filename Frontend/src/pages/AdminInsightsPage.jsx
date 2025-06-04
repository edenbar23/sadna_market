import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { adminGetSystemInsights } from '../api/admin';
import { Link } from 'react-router-dom';
import '../styles/admin.css';

export default function AdminInsightsPage() {
    const { user } = useAuthContext();
    const [insights, setInsights] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [refreshing, setRefreshing] = useState(false);

    useEffect(() => {
        fetchInsights();
    }, [user]);

    const fetchInsights = async () => {
        if (!user?.isAdmin) {
            setError('Unauthorized: Admin access required');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);
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
            setRefreshing(false);
        }
    };

    const handleRefresh = async () => {
        setRefreshing(true);
        await fetchInsights();
    };

    const formatNumber = (num) => {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    };

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
                        <h1>System Insights</h1>
                        <p>Detailed analytics and system performance metrics</p>
                    </div>
                    <div className="header-actions">
                        <button
                            onClick={handleRefresh}
                            disabled={refreshing}
                            className="refresh-button"
                        >
                            {refreshing ? '🔄 Refreshing...' : '🔄 Refresh Data'}
                        </button>
                        <Link to="/admin" className="back-button">← Back to Dashboard</Link>
                    </div>
                </div>
            </div>

            {loading ? (
                <div className="admin-loading">
                    <div className="loading-spinner"></div>
                    <p>Loading system insights...</p>
                </div>
            ) : error ? (
                <div className="admin-error">
                    <h3>Error Loading Insights</h3>
                    <p>{error}</p>
                    <button onClick={fetchInsights} className="button">Retry</button>
                </div>
            ) : insights ? (
                <div className="insights-content">
                    {/* Key Metrics Section */}
                    <div className="metrics-section">
                        <h2>Key System Metrics</h2>
                        <div className="metrics-grid">
                            <div className="metric-card primary">
                                <div className="metric-icon">👥</div>
                                <div className="metric-content">
                                    <h3>Total Users</h3>
                                    <div className="metric-number">{formatNumber(insights.totalUsers)}</div>
                                    <div className="metric-subtitle">
                                        {insights.activeUsers} currently active ({((insights.activeUsers / insights.totalUsers) * 100).toFixed(1)}%)
                                    </div>
                                </div>
                            </div>

                            <div className="metric-card success">
                                <div className="metric-icon">🏪</div>
                                <div className="metric-content">
                                    <h3>Total Stores</h3>
                                    <div className="metric-number">{formatNumber(insights.totalStores)}</div>
                                    <div className="metric-subtitle">Active marketplace presence</div>
                                </div>
                            </div>

                            <div className="metric-card info">
                                <div className="metric-icon">📦</div>
                                <div className="metric-content">
                                    <h3>Total Orders</h3>
                                    <div className="metric-number">{formatNumber(insights.totalOrders)}</div>
                                    <div className="metric-subtitle">Lifetime order volume</div>
                                </div>
                            </div>

                            <div className="metric-card warning">
                                <div className="metric-icon">💰</div>
                                <div className="metric-content">
                                    <h3>Total Revenue</h3>
                                    <div className="metric-number">{formatCurrency(insights.totalRevenue)}</div>
                                    <div className="metric-subtitle">Platform lifetime revenue</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Activity Monitoring Section */}
                    <div className="activity-section">
                        <h2>Real-Time Activity</h2>
                        <div className="activity-grid">
                            <div className="activity-card">
                                <div className="activity-header">
                                    <h3>Transaction Rate</h3>
                                    <span className="activity-period">Last Hour</span>
                                </div>
                                <div className="activity-chart">
                                    <div className="activity-number">{insights.transactionRate}</div>
                                    <div className="activity-label">transactions/hour</div>
                                </div>
                                <div className="activity-indicator">
                                    <div className="indicator-bar">
                                        <div
                                            className="indicator-fill transaction"
                                            style={{width: `${Math.min((insights.transactionRate / 100) * 100, 100)}%`}}
                                        ></div>
                                    </div>
                                    <span className="indicator-text">
                                        {insights.transactionRate > 50 ? 'High Activity' : insights.transactionRate > 20 ? 'Moderate Activity' : 'Low Activity'}
                                    </span>
                                </div>
                            </div>

                            <div className="activity-card">
                                <div className="activity-header">
                                    <h3>New Subscriptions</h3>
                                    <span className="activity-period">Last Hour</span>
                                </div>
                                <div className="activity-chart">
                                    <div className="activity-number">{insights.subscriptionRate}</div>
                                    <div className="activity-label">signups/hour</div>
                                </div>
                                <div className="activity-indicator">
                                    <div className="indicator-bar">
                                        <div
                                            className="indicator-fill subscription"
                                            style={{width: `${Math.min((insights.subscriptionRate / 20) * 100, 100)}%`}}
                                        ></div>
                                    </div>
                                    <span className="indicator-text">
                                        {insights.subscriptionRate > 10 ? 'Growing Fast' : insights.subscriptionRate > 5 ? 'Steady Growth' : 'Slow Growth'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* System Health Section */}
                    <div className="health-section">
                        <h2>System Health & Status</h2>
                        <div className="health-grid">
                            <div className="health-card">
                                <div className="health-header">
                                    <h3>Pending Reports</h3>
                                    <div className={`health-status ${insights.pendingReports > 0 ? 'warning' : 'healthy'}`}>
                                        {insights.pendingReports > 0 ? '⚠️ Attention Needed' : '✅ All Clear'}
                                    </div>
                                </div>
                                <div className="health-content">
                                    <div className="health-number">{insights.pendingReports}</div>
                                    <div className="health-description">
                                        {insights.pendingReports > 0
                                            ? `${insights.pendingReports} violation reports awaiting review`
                                            : 'No pending violation reports'
                                        }
                                    </div>
                                    {insights.pendingReports > 0 && (
                                        <Link to="/admin/reports" className="health-action">
                                            Review Reports →
                                        </Link>
                                    )}
                                </div>
                            </div>

                            <div className="health-card">
                                <div className="health-header">
                                    <h3>User Engagement</h3>
                                    <div className="health-status healthy">✅ Excellent</div>
                                </div>
                                <div className="health-content">
                                    <div className="health-number">
                                        {((insights.activeUsers / insights.totalUsers) * 100).toFixed(1)}%
                                    </div>
                                    <div className="health-description">
                                        Active user ratio indicates strong platform engagement
                                    </div>
                                </div>
                            </div>

                            <div className="health-card">
                                <div className="health-header">
                                    <h3>Platform Growth</h3>
                                    <div className="health-status healthy">📈 Growing</div>
                                </div>
                                <div className="health-content">
                                    <div className="health-number">
                                        {insights.subscriptionRate > 0 ? `+${insights.subscriptionRate}` : '0'}
                                    </div>
                                    <div className="health-description">
                                        New users per hour indicates platform growth rate
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Summary Statistics */}
                    <div className="summary-section">
                        <h2>Summary Statistics</h2>
                        <div className="summary-grid">
                            <div className="summary-stat">
                                <span className="stat-label">Average Revenue per Order</span>
                                <span className="stat-value">
                                    {insights.totalOrders > 0 ? formatCurrency(insights.totalRevenue / insights.totalOrders) : '$0.00'}
                                </span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Orders per Store</span>
                                <span className="stat-value">
                                    {insights.totalStores > 0 ? (insights.totalOrders / insights.totalStores).toFixed(1) : '0'}
                                </span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Active User Ratio</span>
                                <span className="stat-value">
                                    {((insights.activeUsers / insights.totalUsers) * 100).toFixed(1)}%
                                </span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Last Updated</span>
                                <span className="stat-value">
                                    {insights.lastUpdated
                                        ? new Date(insights.lastUpdated).toLocaleString()
                                        : new Date().toLocaleString()
                                    }
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            ) : null}
        </div>
    );
}