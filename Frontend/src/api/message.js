import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api'; // Changed from API_URL to match other files

// Create axios instance with common config
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 10000 // 10 second timeout
});

// Add request interceptor for authorization
apiClient.interceptors.request.use(config => {
    // Only add token from localStorage if not already in headers
    if (!config.headers.Authorization) {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = token;
        }
    }
    return config;
});

// Add response interceptor for error handling
apiClient.interceptors.response.use(
    response => response.data,
    error => {
        console.error('API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

// Send a message to a store
export const sendMessage = async (senderUsername, receiverStoreId, content, token) => {
    const response = await apiClient.post('/messages/send', {
        senderUsername,
        receiverStoreId,
        content
    }, {
        headers: { Authorization: token }
    });
    return response;
};

// Reply to a message
export const replyToMessage = async (messageId, senderUsername, content, token) => {
    const response = await apiClient.post(`/messages/${messageId}/reply`, {
        senderUsername,
        content
    }, {
        headers: { Authorization: token }
    });
    return response;
};

// Get conversation between user and store
export const getUserStoreConversation = async (username, storeId, token) => {
    const response = await apiClient.get('/messages/user-to-store', {
        params: { username, storeID: storeId },
        headers: { Authorization: token }
    });
    return response;
};

// Get messages received by a store - FIXED ENDPOINT
export const getStoreMessages = async (storeId, username, token) => {
    const response = await apiClient.get(`/stores/${storeId}/messages`, {
        params: { username },
        headers: { Authorization: token }
    });
    return response.data;
};

// Get messages sent by a user
export const getUserMessages = async (username, token) => {
    const response = await apiClient.get(`/messages/user/${username}/sent`, {
        headers: { Authorization: token }
    });
    return response;
};

// Mark message as read
export const markMessageAsRead = async (messageId, username, token) => {
    const response = await apiClient.patch(`/messages/${messageId}/read`, null, {
        params: { username },
        headers: { Authorization: token }
    });
    return response;
};

// Report a message violation
export const reportViolation = async (messageId, reporterUsername, reason, token) => {
    const response = await apiClient.patch(`/messages/${messageId}/report-violation`, {
        reporterUsername,
        reason
    }, {
        headers: { Authorization: token }
    });
    return response;
};

export default {
    sendMessage,
    replyToMessage,
    getUserStoreConversation,
    getStoreMessages,
    getUserMessages,
    markMessageAsRead,
    reportViolation
};