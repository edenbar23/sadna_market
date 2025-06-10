// Frontend/src/components/NotificationBell.jsx
import React, { useState, useEffect } from 'react';
import { useAuthContext } from '../context/AuthContext';
import * as notificationAPI from '../api/notification';
import notificationService from '../services/notificationService';
import '../styles/notification-bell.css';

const NotificationBell = () => {
    const { user, token } = useAuthContext();
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [loading, setLoading] = useState(false);
    const [systemReady, setSystemReady] = useState(false);

    useEffect(() => {
        if (user && token) {
            console.log('User logged in, initializing notifications for:', user.username);
            setSystemReady(true);
            initializeNotifications();
            connectToNotificationService();
        } else {
            console.log('No user or token, skipping notification initialization');
            setSystemReady(false);
            disconnectFromNotificationService();
        }

        return () => {
            disconnectFromNotificationService();
        };
    }, [user, token]);

    const initializeNotifications = async () => {
        try {
            console.log('Requesting notification permission...');
            await notificationService.requestNotificationPermission();

            console.log('Loading unread count...');
            await loadUnreadCount();

            console.log('Loading notifications...');
            await loadNotifications();
        } catch (error) {
            console.error('Failed to initialize notifications:', error);
        }
    };

    const connectToNotificationService = async () => {
        try {
            console.log('Connecting to notification service...');
            await notificationService.connect(user.username, token);

            console.log('Adding notification handler...');
            notificationService.addNotificationHandler(handleNewNotification);

            console.log('Connected to notification service successfully');
        } catch (error) {
            console.error('Failed to connect to notification service:', error);
        }
    };

    const disconnectFromNotificationService = () => {
        console.log('Disconnecting from notification service...');
        notificationService.removeNotificationHandler(handleNewNotification);
        notificationService.disconnect();
    };

    const handleNewNotification = (notification) => {
        console.log('Handling new notification:', notification);
        setUnreadCount(prev => prev + 1);
        setNotifications(prev => [notification, ...prev.slice(0, 9)]);
        showToastNotification(notification);
    };

    const loadUnreadCount = async () => {
        try {
            console.log('Loading unread count for user:', user.username);
            const response = await notificationAPI.getUnreadCount(user.username, token);
            console.log('Unread count response:', response);
            if (!response.error) {
                setUnreadCount(response.data || 0);
                console.log('Set unread count to:', response.data || 0);
            } else {
                console.error('Error in unread count response:', response.errorMessage);
            }
        } catch (error) {
            console.error('Failed to load unread count:', error);
        }
    };

    const loadNotifications = async () => {
        setLoading(true);
        try {
            console.log('Loading unread notifications...');
            const response = await notificationAPI.getUnreadNotifications(user.username, token);
            console.log('Notifications response:', response);
            if (!response.error) {
                setNotifications(response.data || []);
                console.log('Set notifications to:', response.data || []);
            } else {
                console.error('Error in notifications response:', response.errorMessage);
            }
        } catch (error) {
            console.error('Failed to load notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async (notificationId) => {
        try {
            console.log('Marking notification as read:', notificationId);
            await notificationAPI.markNotificationAsRead(notificationId, token);

            setNotifications(prev =>
                prev.map(n =>
                    n.notificationId === notificationId
                        ? { ...n, isRead: true }
                        : n
                )
            );

            setUnreadCount(prev => Math.max(0, prev - 1));
            console.log('Notification marked as read successfully');
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    };

    const markAllAsRead = async () => {
        try {
            console.log('Marking all notifications as read...');
            await notificationAPI.markAllNotificationsAsRead(user.username, token);

            setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
            setUnreadCount(0);
            console.log('All notifications marked as read successfully');
        } catch (error) {
            console.error('Failed to mark all notifications as read:', error);
        }
    };

    const sendTestNotification = async () => {
        try {
            console.log('Sending test notification...');
            const response = await notificationAPI.sendTestNotification(user.username, token);
            console.log('Test notification response:', response);
            if (!response.error) {
                // Reload notifications after sending test
                await loadUnreadCount();
                await loadNotifications();
            }
        } catch (error) {
            console.error('Failed to send test notification:', error);
        }
    };

    const showToastNotification = (notification) => {
        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.innerHTML = `
            <div class="toast-title">${notification.title}</div>
            <div class="toast-message">${notification.message}</div>
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 4000);
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'ORDER_RECEIVED':
                return '🛒';
            case 'STORE_CLOSED':
                return '🔒';
            case 'STORE_REOPENED':
                return '🔓';
            case 'MESSAGE_RECEIVED':
                return '💬';
            case 'ROLE_ASSIGNED':
                return '👤';
            case 'ROLE_REMOVED':
                return '❌';
            case 'VIOLATION_REPLY':
                return '⚠️';
            case 'SYSTEM_ANNOUNCEMENT':
                return '📢';
            default:
                return '🔔';
        }
    };

    const formatTimeAgo = (timestamp) => {
        const now = new Date();
        const notificationTime = new Date(timestamp);
        const diffInSeconds = Math.floor((now - notificationTime) / 1000);

        if (diffInSeconds < 60) return 'Just now';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
        return `${Math.floor(diffInSeconds / 86400)}d ago`;
    };

    // Don't render anything for non-authenticated users
    if (!user) {
        return null;
    }

    return (
        <div className="notification-bell-container">
            <button
                className="notification-bell-button"
                onClick={() => setShowDropdown(!showDropdown)}
                aria-label={`Notifications ${unreadCount > 0 ? `(${unreadCount} unread)` : ''}`}
                title="Notifications"
            >
                🔔
                {unreadCount > 0 && (
                    <span className="notification-badge">
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {showDropdown && (
                <div className="notification-dropdown">
                    <div className="notification-header">
                        <h3>Notifications</h3>
                        <div>
                            {unreadCount > 0 && (
                                <button
                                    className="mark-all-read-btn"
                                    onClick={markAllAsRead}
                                    style={{ marginRight: '8px' }}
                                >
                                    Mark all read
                                </button>
                            )}
                            <button
                                className="mark-all-read-btn"
                                onClick={sendTestNotification}
                                style={{ fontSize: '10px' }}
                            >
                                Test
                            </button>
                        </div>
                    </div>

                    <div className="notification-list">
                        {loading ? (
                            <div className="notification-loading">Loading...</div>
                        ) : !systemReady ? (
                            <div className="no-notifications">
                                System not ready...
                            </div>
                        ) : notifications.length === 0 ? (
                            <div className="no-notifications">
                                No new notifications
                                <br />
                                <small style={{ color: '#999' }}>
                                    System ready - Try the Test button above
                                </small>
                            </div>
                        ) : (
                            notifications.slice(0, 10).map(notification => (
                                <div
                                    key={notification.notificationId}
                                    className={`notification-item ${!notification.read ? 'unread' : ''}`}
                                    onClick={() => {
                                        if (!notification.read) {
                                            markAsRead(notification.notificationId);
                                        }
                                    }}
                                >
                                    <div className="notification-icon">
                                        {getNotificationIcon(notification.type)}
                                    </div>
                                    <div className="notification-content">
                                        <div className="notification-title">
                                            {notification.title}
                                        </div>
                                        <div className="notification-message">
                                            {notification.message}
                                        </div>
                                        <div className="notification-time">
                                            {formatTimeAgo(notification.createdAt)}
                                        </div>
                                    </div>
                                    {!notification.read && (
                                        <div className="unread-indicator"></div>
                                    )}
                                </div>
                            ))
                        )}
                    </div>

                    <div className="notification-footer">
                        <button
                            className="view-all-btn"
                            onClick={() => {
                                setShowDropdown(false);
                                loadNotifications();
                            }}
                        >
                            Refresh notifications
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default NotificationBell;