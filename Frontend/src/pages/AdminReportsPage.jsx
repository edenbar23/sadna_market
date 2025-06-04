import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { adminGetReports, adminRespondToReport } from '../api/admin';
import { Link } from 'react-router-dom';
import '../styles/admin.css';

export default function AdminReportsPage() {
    const { user } = useAuthContext();
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [respondingReport, setRespondingReport] = useState(null);
    const [responseText, setResponseText] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchReports();
    }, [user]);

    const fetchReports = async () => {
        if (!user?.isAdmin) {
            setError('Unauthorized: Admin access required');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const response = await adminGetReports(user.username, user.token);

            if (response.error) {
                setError(response.errorMessage || 'Failed to fetch reports');
            } else {
                setReports(response.data || []);
            }
        } catch (err) {
            console.error('Error fetching reports:', err);
            setError(err.errorMessage || 'Failed to fetch reports');
        } finally {
            setLoading(false);
        }
    };

    const handleRespondToReport = async (reportId) => {
        if (!user?.isAdmin || !responseText.trim()) {
            return;
        }

        try {
            setRespondingReport(reportId);
            const response = await adminRespondToReport(user.username, user.token, reportId, responseText);

            if (response.error) {
                alert(`Failed to respond to report: ${response.errorMessage}`);
            } else {
                alert('Response sent successfully');
                setResponseText('');
                setRespondingReport(null);
                // Optionally refresh reports or mark as responded
                fetchReports();
            }
        } catch (err) {
            console.error('Error responding to report:', err);
            alert(`Failed to respond to report: ${err.errorMessage || 'Unknown error'}`);
        } finally {
            setRespondingReport(null);
        }
    };

    // Filter reports based on search term
    const filteredReports = reports.filter(report => {
        if (!searchTerm) return true;

        const searchLower = searchTerm.toLowerCase();
        return (
            report.reporterUsername.toLowerCase().includes(searchLower) ||
            report.comment.toLowerCase().includes(searchLower) ||
            report.storeName.toLowerCase().includes(searchLower) ||
            report.productName.toLowerCase().includes(searchLower)
        );
    });

    // Sort reports by creation date (newest first)
    const sortedReports = filteredReports.sort((a, b) =>
        new Date(b.createdAt) - new Date(a.createdAt)
    );

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
                        <h1>Reports Management</h1>
                        <p>Review and respond to violation reports</p>
                    </div>
                    <Link to="/admin" className="back-button">← Back to Dashboard</Link>
                </div>
            </div>

            {/* Search Controls */}
            <div className="controls-section">
                <div className="search-controls">
                    <input
                        type="text"
                        placeholder="Search reports by reporter, store, product, or comment..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                </div>

                <div className="stats-summary">
                    <span>Total Reports: {reports.length}</span>
                    <span>Showing: {sortedReports.length}</span>
                    {reports.length > 0 && (
                        <span>Latest: {new Date(Math.max(...reports.map(r => new Date(r.createdAt)))).toLocaleDateString()}</span>
                    )}
                </div>
            </div>

            {loading ? (
                <div className="admin-loading">
                    <div className="loading-spinner"></div>
                    <p>Loading reports...</p>
                </div>
            ) : error ? (
                <div className="admin-error">
                    <h3>Error Loading Reports</h3>
                    <p>{error}</p>
                    <button onClick={fetchReports} className="button">Retry</button>
                </div>
            ) : (
                <div className="reports-section">
                    {sortedReports.length === 0 ? (
                        <div className="no-reports">
                            <h3>No Reports Found</h3>
                            <p>
                                {reports.length === 0
                                    ? 'No violation reports have been submitted yet.'
                                    : 'No reports match your search criteria.'
                                }
                            </p>
                        </div>
                    ) : (
                        <div className="reports-list">
                            {sortedReports.map((report) => (
                                <div key={report.reportId} className="report-card">
                                    <div className="report-header">
                                        <div className="report-info">
                                            <h3>Report #{report.reportId.slice(0, 8)}</h3>
                                            <div className="report-meta">
                                                <span className="reporter">Reporter: @{report.reporterUsername}</span>
                                                <span className="report-date">
                                                    {new Date(report.createdAt).toLocaleString()}
                                                </span>
                                            </div>
                                        </div>
                                        <div className="report-status">
                                            <span className="status-badge">Pending Review</span>
                                        </div>
                                    </div>

                                    <div className="report-details">
                                        <div className="detail-section">
                                            <h4>Store Information</h4>
                                            <div className="detail-grid">
                                                <div className="detail-item">
                                                    <span className="detail-label">Store:</span>
                                                    <span className="detail-value">{report.storeName}</span>
                                                </div>
                                                <div className="detail-item">
                                                    <span className="detail-label">Store ID:</span>
                                                    <span className="detail-value store-id">{report.storeId}</span>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="detail-section">
                                            <h4>Product Information</h4>
                                            <div className="detail-grid">
                                                <div className="detail-item">
                                                    <span className="detail-label">Product:</span>
                                                    <span className="detail-value">{report.productName}</span>
                                                </div>
                                                <div className="detail-item">
                                                    <span className="detail-label">Product ID:</span>
                                                    <span className="detail-value store-id">{report.productId}</span>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="detail-section">
                                            <h4>Violation Report</h4>
                                            <div className="report-comment">
                                                <p>{report.comment}</p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="response-section">
                                        <h4>Admin Response</h4>
                                        <div className="response-form">
                                            <textarea
                                                value={respondingReport === report.reportId ? responseText : ''}
                                                onChange={(e) => {
                                                    setResponseText(e.target.value);
                                                    if (respondingReport !== report.reportId) {
                                                        setRespondingReport(report.reportId);
                                                    }
                                                }}
                                                placeholder="Write your response to this violation report..."
                                                className="response-textarea"
                                                rows="4"
                                            />
                                            <div className="response-actions">
                                                <button
                                                    onClick={() => handleRespondToReport(report.reportId)}
                                                    disabled={
                                                        !responseText.trim() ||
                                                        (respondingReport === report.reportId && respondingReport !== null)
                                                    }
                                                    className="respond-button"
                                                >
                                                    {respondingReport === report.reportId ? 'Sending Response...' : 'Send Response'}
                                                </button>
                                                {respondingReport === report.reportId && (
                                                    <button
                                                        onClick={() => {
                                                            setRespondingReport(null);
                                                            setResponseText('');
                                                        }}
                                                        className="cancel-button"
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </div>
                                        </div>
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