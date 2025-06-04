import axios from 'axios';

// Base URL configuration
const API_BASE_URL = 'http://localhost:8081/api/admin';

// Create a configured axios instance
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // 10 second timeout
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = token;
    }
    return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
    response => response.data,
    error => {
        console.error('Admin API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

// ==================== ADMIN STORE MANAGEMENT ====================

/**
 * Admin: Close a store
 */
export const adminCloseStore = async (adminUsername, token, storeId) => {
    try {
        const response = await apiClient.post(`/${adminUsername}/stores/${storeId}/close`, null, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error closing store:', error);
        throw error;
    }
};

/**
 * Get all stores (for admin management)
 */
export const adminGetAllStores = async () => {
    try {
        // Since this endpoint doesn't exist in AdminController, we'll use the regular stores endpoint
        const response = await axios.get('http://localhost:8081/api/stores');
        return response.data;
    } catch (error) {
        console.error('Error fetching all stores:', error);
        throw error;
    }
};

// ==================== ADMIN USER MANAGEMENT ====================

/**
 * Admin: Get all users
 */
export const adminGetAllUsers = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`/${adminUsername}/users`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching all users:', error);
        throw error;
    }
};

/**
 * Admin: Remove a user
 */
export const adminRemoveUser = async (adminUsername, token, targetUsername) => {
    try {
        const response = await apiClient.delete(`/${adminUsername}/users/${targetUsername}`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error removing user:', error);
        throw error;
    }
};

// ==================== ADMIN REPORTS MANAGEMENT ====================

/**
 * Admin: Get all violation reports
 */
export const adminGetReports = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`/${adminUsername}/reports`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching reports:', error);
        throw error;
    }
};

/**
 * Admin: Respond to a violation report
 */
export const adminRespondToReport = async (adminUsername, token, reportId, responseMessage) => {
    try {
        const response = await apiClient.post(`/${adminUsername}/reports/${reportId}/respond`,
            responseMessage, // Send as plain text body
            {
                headers: {
                    Authorization: token,
                    'Content-Type': 'text/plain'
                }
            }
        );
        return response;
    } catch (error) {
        console.error('Error responding to report:', error);
        throw error;
    }
};

// ==================== ADMIN SYSTEM INSIGHTS ====================

/**
 * Admin: Get system insights and statistics
 */
export const adminGetSystemInsights = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`/${adminUsername}/insights`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching system insights:', error);
        throw error;
    }
};

// ==================== HEALTH CHECK ====================

/**
 * Admin service health check
 */
export const adminHealthCheck = async () => {
    try {
        const response = await apiClient.get('/health');
        return response;
    } catch (error) {
        console.error('Error checking admin service health:', error);
        throw error;
    }
};

export default {
    adminCloseStore,
    adminGetAllStores,
    adminGetAllUsers,
    adminRemoveUser,
    adminGetReports,
    adminRespondToReport,
    adminGetSystemInsights,
    adminHealthCheck
};