import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

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
        console.error('Message API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

// Send a message to a store
export const sendMessage = async (senderUsername, receiverStoreId, content, token) => {
    console.log("API: Sending message to store", { senderUsername, receiverStoreId, content });

    const response = await apiClient.post('/messages/send', {
        senderUsername,
        receiverStoreId,
        content
    }, {
        headers: { Authorization: token }
    });

    console.log("API: Send message response:", response);
    return response;
};

// Reply to a message
export const replyToMessage = async (messageId, senderUsername, content, token) => {
    console.log("API: Replying to message", { messageId, senderUsername, content });

    const response = await apiClient.post(`/messages/${messageId}/reply`, {
        senderUsername,
        content
    }, {
        headers: { Authorization: token }
    });

    console.log("API: Reply message response:", response);
    return response;
};

// Get conversation between user and store
export const getUserStoreConversation = async (username, storeId, token) => {
    console.log("API: Getting user-store conversation", { username, storeId });

    const response = await apiClient.get('/messages/user-to-store', {
        params: { username, storeID: storeId },
        headers: { Authorization: token }
    });

    console.log("API: User-store conversation response:", response);
    return response;
};

// Get messages received by a store
export const getStoreMessages = async (storeId, username, token) => {
    console.log("API: Getting store messages", { storeId, username });

    const response = await apiClient.get(`/stores/${storeId}/messages`, {
        params: { username },
        headers: { Authorization: token }
    });

    console.log("API: Store messages response:", response);
    return response.data;
};

// Get messages sent by a user
export const getUserMessages = async (username, token) => {
    console.log("API: Getting user messages", { username });

    const response = await apiClient.get(`/messages/user/${username}/sent`, {
        headers: { Authorization: token }
    });

    console.log("API: User messages response:", response);
    return response;
};

// Mark message as read
export const markMessageAsRead = async (messageId, username, token) => {
    console.log("API: Marking message as read", { messageId, username });

    const response = await apiClient.patch(`/messages/${messageId}/read`, {
        username: username
    }, {
        headers: { Authorization: token }
    });

    console.log("API: Mark as read response:", response);
    return response;
};

// Report a message violation
export const reportViolation = async (messageId, reporterUsername, reason, token) => {
    console.log("API: Reporting violation", { messageId, reporterUsername, reason });

    const response = await apiClient.patch(`/messages/${messageId}/report-violation`, {
        reporterUsername,
        reason
    }, {
        headers: { Authorization: token }
    });

    console.log("API: Report violation response:", response);
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