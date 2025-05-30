import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

// Create axios instance
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 10000
});

// Add request interceptor for token
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = token;
    }
    return config;
});

// Add response interceptor for error handling
apiClient.interceptors.response.use(
    response => response.data,
    error => {
        console.error('Address API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

/**
 * Add a new address for a user
 * @param {string} username
 * @param {Object} addressData
 * @returns {Promise<Object>}
 */
export const addAddress = async (username, addressData) => {
    try {
        const response = await apiClient.post(`/users/${username}/addresses`, addressData);
        return response;
    } catch (error) {
        console.error('Error adding address:', error);
        throw error;
    }
};

/**
 * Get all addresses for a user
 * @param {string} username
 * @returns {Promise<Array>}
 */
export const getUserAddresses = async (username) => {
    try {
        const response = await apiClient.get(`/users/${username}/addresses`);
        return response;
    } catch (error) {
        console.error('Error fetching addresses:', error);
        throw error;
    }
};

/**
 * Update an existing address
 * @param {string} username
 * @param {string} addressId
 * @param {Object} addressData
 * @returns {Promise<Object>}
 */
export const updateAddress = async (username, addressId, addressData) => {
    try {
        const response = await apiClient.put(`/users/${username}/addresses/${addressId}`, addressData);
        return response;
    } catch (error) {
        console.error('Error updating address:', error);
        throw error;
    }
};

/**
 * Delete an address
 * @param {string} username
 * @param {string} addressId
 * @returns {Promise<Object>}
 */
export const deleteAddress = async (username, addressId) => {
    try {
        const response = await apiClient.delete(`/users/${username}/addresses/${addressId}`);
        return response;
    } catch (error) {
        console.error('Error deleting address:', error);
        throw error;
    }
};

/**
 * Set an address as default
 * @param {string} username
 * @param {string} addressId
 * @returns {Promise<Object>}
 */
export const setDefaultAddress = async (username, addressId) => {
    try {
        const response = await apiClient.patch(`/users/${username}/addresses/${addressId}/default`);
        return response;
    } catch (error) {
        console.error('Error setting default address:', error);
        throw error;
    }
};

/**
 * Get user's default address
 * @param {string} username
 * @returns {Promise<Object>}
 */
export const getDefaultAddress = async (username) => {
    try {
        const response = await apiClient.get(`/users/${username}/addresses/default`);
        return response;
    } catch (error) {
        console.error('Error fetching default address:', error);
        throw error;
    }
};

export default {
    addAddress,
    getUserAddresses,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
    getDefaultAddress
};