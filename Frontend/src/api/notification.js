// Frontend/src/api/notification.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

// Get all notifications for user
export const getUserNotifications = async (username, token) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/notifications/${username}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching user notifications:', error);
        throw error;
    }
};

// Get unread notifications for user
export const getUnreadNotifications = async (username, token) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/notifications/${username}/unread`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching unread notifications:', error);
        throw error;
    }
};

// Get unread notification count
export const getUnreadCount = async (username, token) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/notifications/${username}/unread/count`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching unread count:', error);
        throw error;
    }
};

// Mark notification as read
export const markNotificationAsRead = async (notificationId, token) => {
    try {
        const response = await axios.put(`${API_BASE_URL}/notifications/${notificationId}/read`, {}, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error marking notification as read:', error);
        throw error;
    }
};

// Mark all notifications as read
export const markAllNotificationsAsRead = async (username, token) => {
    try {
        const response = await axios.put(`${API_BASE_URL}/notifications/${username}/read-all`, {}, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error marking all notifications as read:', error);
        throw error;
    }
};

// Send test notification (for debugging)
export const sendTestNotification = async (username, token) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/notifications/test/${username}`, {}, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error sending test notification:', error);
        throw error;
    }
};