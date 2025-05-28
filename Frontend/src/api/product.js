import axios from 'axios';

// Base URL configuration
const API_BASE_URL = 'http://localhost:8081/api';
const PRODUCTS_URL = `${API_BASE_URL}/products`;

// Create a configured axios instance with common settings
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // 10 second timeout
});

// Request interceptor to automatically add auth token when available
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = token;
    }
    return config;
});

// Response interceptor for consistent error handling
apiClient.interceptors.response.use(
    response => response.data,
    error => {
        console.error('API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

/**
 * Validates UUID values for API calls
 * @param {string} id - The UUID to validate
 * @returns {string} - The validated UUID
 * @throws {Error} - If UUID is invalid
 */
function ensureValidUUID(id) {
    if (!id || id === "undefined" || id === undefined) {
        throw new Error("Invalid UUID value");
    }
    return id;
}

/**
 * Search for products using criteria
 * @param {Object} searchRequest - Search parameters
 * @returns {Promise<Object>} - Search results
 */
export const searchProducts = async (searchRequest) => {
    try {
        return await apiClient.post(`/products/search`, searchRequest);
    } catch (error) {
        console.error("Error searching products:", error);
        throw error;
    }
};

/**
 * Get top rated products
 * @returns {Promise<Object>} - Top rated products
 */
export const fetchTopProducts = async () => {
    try {
        return await apiClient.get(`/products/top-rated`);
    } catch (error) {
        console.error("Error fetching top products:", error);
        throw error;
    }
};

/**
 * Get product details by ID
 * @param {string} productId - The product UUID
 * @returns {Promise<Object>} - Product details
 */
export const getProductInfo = async (productId) => {
    try {
        ensureValidUUID(productId);
        return await apiClient.get(`/products/${productId}`);
    } catch (error) {
        console.error(`Error fetching product ${productId}:`, error);
        throw error;
    }
};

/**
 * Rate a product
 * @param {Object} rateRequest - includes productId, storeId, username, and rating
 * @param {string} token - JWT token
 */
export const rateProduct = async (rateRequest, token) => {
    try {
        ensureValidUUID(rateRequest.productId);
        ensureValidUUID(rateRequest.storeId);

        return await apiClient.post(
            `/products/rate`,
            rateRequest,
            {
                headers: {
                    Authorization: token
                }
            }
        );
    } catch (error) {
        console.error(`Error rating product:`, error);
        throw error;
    }
};

/**
 * Add product to a store
 * @param {string} storeId - Store UUID
 * @param {Object} productRequest - Product data
 * @param {number} quantity - Initial quantity
 * @param {string} username - Username performing the action
 * @returns {Promise<Object>} - Added product result
 */
export const addProductToStore = async (storeId, productRequest, quantity, username) => {
    try {
        ensureValidUUID(storeId);
        // Ensure we don't send invalid productId
        const cleanProductRequest = {
            ...productRequest,
            productId: null
        };

        return await apiClient.post(
            `/products/store/${storeId}`,
            cleanProductRequest,
            {
                params: { quantity, username }
            }
        );
    } catch (error) {
        console.error(`Error adding product to store ${storeId}:`, error);
        throw error;
    }
};

/**
 * Update a product
 * @param {string} storeId - Store UUID
 * @param {Object} productRequest - Updated product data with productId
 * @param {number} quantity - Updated quantity
 * @param {string} username - Username performing the action
 * @returns {Promise<Object>} - Update result
 */
export const updateProduct = async (storeId, productRequest, quantity, username) => {
    try {
        ensureValidUUID(storeId);
        ensureValidUUID(productRequest.productId);

        return await apiClient.put(
            `/products/store/${storeId}`,
            productRequest,
            {
                params: { quantity, username }
            }
        );
    } catch (error) {
        console.error(`Error updating product in store ${storeId}:`, error);
        throw error;
    }
};

/**
 * Delete a product
 * @param {string} storeId - Store UUID
 * @param {string} productId - Product UUID to delete
 * @param {string} username - Username performing the action
 * @returns {Promise<Object>} - Deletion result
 */
export const deleteProduct = async (storeId, productId, username) => {
    try {
        ensureValidUUID(storeId);
        ensureValidUUID(productId);

        return await apiClient.delete(
            `/products/store/${storeId}`,
            {
                data: { productId },
                params: { username }
            }
        );
    } catch (error) {
        console.error(`Error deleting product ${productId} from store ${storeId}:`, error);
        throw error;
    }
};

/**
 * Get all products for a store
 * @param {string} storeId - Store UUID
 * @returns {Promise<Object>} - Store products
 */
export const getStoreProducts = async (storeId) => {
    try {
        ensureValidUUID(storeId);
        return await apiClient.get(`/products/store/${storeId}`);
    } catch (error) {
        console.error(`Error fetching products for store ${storeId}:`, error);
        throw error;
    }
};

/**
 * Add a review to a product
 * @param {Object} reviewRequest - Review data
 * @returns {Promise<Object>} - Review result
 */
export const addProductReview = async (reviewRequest) => {
    try {
        ensureValidUUID(reviewRequest.productId);
        return await apiClient.post(`/products/review`, reviewRequest);
    } catch (error) {
        console.error(`Error adding product review:`, error);
        throw error;
    }
};

export default {
    searchProducts,
    fetchTopProducts,
    getProductInfo,
    rateProduct,
    addProductToStore,
    updateProduct,
    deleteProduct,
    getStoreProducts,
    addProductReview
};