// ==================== STORE MANAGEMENT ====================

/**
 * Admin: Close a store
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @param {string} storeId - Store ID to close
 * @returns {Promise<Object>} - Response from server
 */
export const adminCloseStore = async (adminUsername, token, storeId) => {
    try {
        const response = await apiClient.post(`${ADMIN_URL}/${adminUsername}/stores/${storeId}/close`, {}, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error(`Error closing store ${storeId}:`, error);
        throw error;
    }
};

/**
 * Get all stores (for admin store management)
 * @returns {Promise<Object>} - List of all stores
 */
export const adminGetAllStores = async () => {
    try {
        const response = await apiClient.get('/stores');
        return response;
    } catch (error) {
        console.error('Error fetching all stores:', error);
        throw error;
    }
};

// ==================== USER MANAGEMENT ====================

/**
 * Admin: Remove a user from the system
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @param {string} targetUsername - Username to remove
 * @returns {Promise<Object>} - Response from server
 */
export const adminRemoveUser = async (adminUsername, token, targetUsername) => {
    try {
        const response = await apiClient.delete(`${ADMIN_URL}/${adminUsername}/users/${targetUsername}`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error(`Error removing user ${targetUsername}:`, error);
        throw error;
    }
};

/**
 * Admin: Get all users in the system
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @returns {Promise<Object>} - List of all users
 */
export const adminGetAllUsers = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`${ADMIN_URL}/${adminUsername}/users`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching all users:', error);
        throw error;
    }
};

// ==================== REPORTS MANAGEMENT ====================

/**
 * Admin: Get all violation reports
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @returns {Promise<Object>} - List of all reports
 */
export const adminGetReports = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`${ADMIN_URL}/${adminUsername}/reports`, {
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
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @param {string} reportId - Report ID to respond to
 * @param {string} responseMessage - Response message
 * @returns {Promise<Object>} - Response from server
 */
export const adminRespondToReport = async (adminUsername, token, reportId, responseMessage) => {
    try {
        const response = await apiClient.post(`${ADMIN_URL}/${adminUsername}/reports/${reportId}/respond`,
            responseMessage, // Send as raw string as expected by backend
            {
                headers: {
                    Authorization: token,
                    'Content-Type': 'application/json'
                }
            }
        );
        return response;
    } catch (error) {
        console.error(`Error responding to report ${reportId}:`, error);
        throw error;
    }
};

// ==================== SYSTEM INSIGHTS ====================

/**
 * Admin: Get system insights and statistics
 * @param {string} adminUsername - Admin username
 * @param {string} token - Authentication token
 * @returns {Promise<Object>} - System insights data
 */
export const adminGetSystemInsights = async (adminUsername, token) => {
    try {
        const response = await apiClient.get(`${ADMIN_URL}/${adminUsername}/insights`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching system insights:', error);
        throw error;
    }
};

// ==================== ADMIN UTILITIES ====================

/**
 * Health check for admin service
 * @returns {Promise<string>} - Health status
 */
export const adminHealthCheck = async () => {
    try {
        const response = await apiClient.get(`${ADMIN_URL}/health`);
        return response;
    } catch (error) {
        console.error('Error checking admin service health:', error);
        throw error;
    }
};

// Default export with all admin functions
export default {
    // Store management
    adminCloseStore,
    adminGetAllStores,

    // User management
    adminRemoveUser,
    adminGetAllUsers,

    // Reports management
    adminGetReports,
    adminRespondToReport,

    // System insights
    adminGetSystemInsights,

    // Utilities
    adminHealthCheck
};