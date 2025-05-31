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
    const token = localStorage.getItem('token') || localStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = token;
    }
    return config;
});

// Add response interceptor to handle your backend's Response<T> structure
apiClient.interceptors.response.use(
    response => {
        console.log('Address API response:', response);

        // Your backend returns Response<T> structure: { error: boolean, data: T, errorMessage: string }
        if (response.data && typeof response.data === 'object') {
            if (response.data.error === false && response.data.data !== undefined) {
                // Success case - extract the data
                return {
                    ...response,
                    data: response.data.data, // Extract the actual data
                    success: true,
                    originalResponse: response.data // Keep original for debugging
                };
            } else if (response.data.error === true) {
                // Error case - throw with error message
                const error = new Error(response.data.errorMessage || 'Unknown error');
                error.response = response;
                error.errorMessage = response.data.errorMessage;
                throw error;
            }
        }

        // Fallback - return original response
        return response;
    },
    error => {
        console.error('Address API Error:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message,
            url: error.config?.url
        });

        // Handle your Response<T> error structure
        if (error.response?.data?.error === true) {
            const wrappedError = new Error(error.response.data.errorMessage || 'Unknown error');
            wrappedError.response = error.response;
            wrappedError.errorMessage = error.response.data.errorMessage;
            return Promise.reject(wrappedError);
        }

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
        console.log('Adding address for user:', username);
        console.log('Address data being sent:', addressData);

        // Ensure all required fields are present and properly formatted
        const requestData = {
            fullName: addressData.fullName || '',
            addressLine1: addressData.addressLine1 || '',
            addressLine2: addressData.addressLine2 || '',
            city: addressData.city || '',
            state: addressData.state || 'N/A', // Backend requires state, use 'N/A' if not provided
            postalCode: addressData.postalCode || '',
            country: addressData.country || '',
            phoneNumber: addressData.phoneNumber || '',
            label: addressData.label || 'Home',
            isDefault: Boolean(addressData.isDefault)
        };

        console.log('Formatted request data:', requestData);

        const response = await apiClient.post(`/users/${username}/addresses`, requestData);
        console.log('Add address response:', response);

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
        console.log('Getting addresses for user:', username);

        const response = await apiClient.get(`/users/${username}/addresses`);
        console.log('Get addresses response:', response);

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
        console.log('Updating address:', { username, addressId, addressData });

        // Ensure all required fields are present and properly formatted
        const requestData = {
            addressId: addressId, // Include the addressId in the request body
            fullName: addressData.fullName || '',
            addressLine1: addressData.addressLine1 || '',
            addressLine2: addressData.addressLine2 || '',
            city: addressData.city || '',
            state: addressData.state || 'N/A', // Backend requires state
            postalCode: addressData.postalCode || '',
            country: addressData.country || '',
            phoneNumber: addressData.phoneNumber || '',
            label: addressData.label || 'Home',
            isDefault: Boolean(addressData.isDefault)
        };

        const response = await apiClient.put(`/users/${username}/addresses/${addressId}`, requestData);
        console.log('Update address response:', response);

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
        console.log('Deleting address:', { username, addressId });

        const response = await apiClient.delete(`/users/${username}/addresses/${addressId}`);
        console.log('Delete address response:', response);

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
        console.log('Setting default address:', { username, addressId });

        const response = await apiClient.patch(`/users/${username}/addresses/${addressId}/default`);
        console.log('Set default address response:', response);

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
        console.log('Getting default address for user:', username);

        const response = await apiClient.get(`/users/${username}/addresses/default`);
        console.log('Get default address response:', response);

        return response;
    } catch (error) {
        console.error('Error fetching default address:', error);
        // Don't throw error for default address - it might not exist
        return null;
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