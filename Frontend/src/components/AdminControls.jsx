import React, { useState, useRef, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import { adminGetSystemInsights } from "../api/admin";

export default function AdminControls() {
    const { user } = useAuthContext();
    const [isOpen, setIsOpen] = useState(false);
    const [pendingReports, setPendingReports] = useState(0);
    const dropdownRef = useRef(null);

    // Fetch notification data (pending reports count)
    useEffect(() => {
        const fetchNotifications = async () => {
            if (user?.isAdmin) {
                try {
                    const response = await adminGetSystemInsights(user.username, user.token);
                    if (response?.data?.pendingReports) {
                        setPendingReports(response.data.pendingReports);
                    }
                } catch (error) {
                    console.error('Failed to fetch admin notifications:', error);
                }
            }
        };

        fetchNotifications();
    }, [user]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    // Don't render if user is not admin
    if (!user?.isAdmin) {
        return null;
    }

    const menuItems = [
        {
            path: "/admin",
            icon: "📊",
            label: "Dashboard",
            description: "System overview"
        },
        {
            path: "/admin/users",
            icon: "👥",
            label: "Users",
            description: "Manage users"
        },
        {
            path: "/admin/stores",
            icon: "🏪",
            label: "Stores",
            description: "Manage stores"
        },
        {
            path: "/admin/reports",
            icon: "📋",
            label: "Reports",
            description: "Handle violations",
            badge: pendingReports > 0 ? pendingReports : null
        },
        {
            path: "/admin/insights",
            icon: "📈",
            label: "Insights",
            description: "System analytics"
        }
    ];

    return (
        <div className="admin-dropdown-container" ref={dropdownRef}>
            {/* Admin Trigger Button */}
            <button
                className={`admin-trigger-button ${isOpen ? 'active' : ''}`}
                onClick={() => setIsOpen(!isOpen)}
                aria-label="Admin Menu"
            >
                <span className="admin-trigger-icon">⚙️</span>
                <span className="admin-trigger-text">Admin</span>
                <span className={`admin-trigger-arrow ${isOpen ? 'open' : ''}`}>▼</span>
                {pendingReports > 0 && (
                    <span className="admin-notification-badge">{pendingReports}</span>
                )}
            </button>

            {/* Dropdown Menu */}
            <div className={`admin-dropdown-menu ${isOpen ? 'open' : ''}`}>
                <div className="admin-dropdown-header">
                    <span className="admin-dropdown-title">Admin Panel</span>
                    <span className="admin-dropdown-subtitle">System Management</span>
                </div>

                <div className="admin-menu-items">
                    {menuItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className="admin-menu-item"
                            onClick={() => setIsOpen(false)}
                        >
                            <div className="admin-menu-icon">{item.icon}</div>
                            <div className="admin-menu-content">
                                <div className="admin-menu-label">
                                    {item.label}
                                    {item.badge && (
                                        <span className="admin-menu-badge">{item.badge}</span>
                                    )}
                                </div>
                                <div className="admin-menu-description">{item.description}</div>
                            </div>
                        </Link>
                    ))}
                </div>

                <div className="admin-dropdown-footer">
                    <span className="admin-user-info">Logged in as {user.username}</span>
                </div>
            </div>

            {/* Backdrop */}
            {isOpen && <div className="admin-dropdown-backdrop" onClick={() => setIsOpen(false)} />}
        </div>
    );
}