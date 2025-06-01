// Updated order.js API - simplified to use OrderDTO fields directly
import axios from 'axios';
import { getProductInfo } from './product';

const API_URL = 'http://localhost:8081/api/orders';

// Create axios instance
const orderApiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 10000
});

// Add request interceptor for token
orderApiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = token;
    }
    return config;
});

// Response interceptor to handle your Response<T> structure
orderApiClient.interceptors.response.use(
    response => {
        console.log('Raw Order API response:', response.data);

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
                throw error;
            }
        }

        // Fallback - return original response
        return response;
    },
    error => {
        console.error('Order API Error:', error.response?.data || error.message);

        // Handle your Response<T> error structure
        if (error.response?.data?.error === true) {
            const wrappedError = new Error(error.response.data.errorMessage || 'Unknown error');
            wrappedError.response = error.response;
            return Promise.reject(wrappedError);
        }

        return Promise.reject(error.response?.data || error);
    }
);

/**
 * Validate UUID format
 * @param {string} id - The UUID string to validate
 * @returns {boolean} - Whether the string is a valid UUID
 */
function isValidUUID(id) {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(id);
}

/**
 * Fetch order by ID (ensures UUID format)
 * @param {string} orderId - The order UUID as string
 * @returns {Promise<OrderDTO>}
 */
export const fetchOrderById = async (orderId) => {
    try {
        console.log('fetchOrderById called with:', orderId);
        console.log('Order ID type:', typeof orderId);

        if (!orderId) {
            throw new Error('Order ID is required');
        }

        // Validate UUID format
        if (!isValidUUID(orderId)) {
            console.error('Invalid UUID format:', orderId);
            throw new Error(`Invalid order ID format: ${orderId}. Expected UUID format.`);
        }

        console.log('✅ UUID format validated, calling API...');

        const response = await orderApiClient.get(`/${orderId}`);
        console.log('fetchOrderById response:', response.data);
        return response;
    } catch (error) {
        console.error('fetchOrderById error:', error);
        console.error('Error details:', {
            message: error.message,
            orderId: orderId,
            isValidUUID: isValidUUID(orderId || ''),
            responseStatus: error.response?.status,
            responseData: error.response?.data
        });
        throw error;
    }
};

/**
 * Fetch user order history - now simplified with OrderDTO fields
 * @param {string} username
 * @returns {Promise<Array<OrderDTO>>}
 */
export const fetchOrderHistory = async (username) => {
    try {
        const token = localStorage.getItem('token');

        if (!token) {
            throw new Error('Authentication token not found');
        }

        console.log('Fetching order history for:', username);

        const response = await orderApiClient.get(`/history/${username}`, {
            headers: {
                Authorization: token,
            },
        });

        console.log('Order history response:', response);

        // The response.data should now be the List<OrderDTO> (extracted by interceptor)
        let orders = response.data || [];

        // Ensure orders is an array
        if (!Array.isArray(orders)) {
            console.error('Expected array but got:', typeof orders, orders);
            return [];
        }

        console.log('Processing', orders.length, 'orders');

        // Transform the orders to fit what OrderCard expects
        const transformedOrders = await Promise.all(
            orders.map(async (order) => {
                try {
                    console.log('Processing order:', order.orderId);

                    // Validate order ID format
                    if (order.orderId && !isValidUUID(order.orderId)) {
                        console.warn('Order has invalid UUID format:', order.orderId);
                    }

                    // ✅ NEW: OrderDTO now includes storeName, paymentMethod, deliveryAddress
                    // So we don't need to fetch them separately anymore!

                    // Handle products - your OrderDTO should have products as a Map<String, Integer>
                    if (!order.products || typeof order.products !== 'object') {
                        console.warn('Order has no products or invalid products structure:', order.orderId);
                        return {
                            ...order,
                            products: [],
                            // These are now included in OrderDTO directly!
                            storeName: order.storeName || 'Unknown Store',
                            paymentMethod: order.paymentMethod || 'Unknown Payment Method',
                            deliveryAddress: order.deliveryAddress || 'Unknown Address'
                        };
                    }

                    const productEntries = Object.entries(order.products);
                    console.log('Product entries for order', order.orderId, ':', productEntries);

                    // Fetch product info for each product
                    const products = await Promise.all(
                        productEntries.map(async ([productId, quantity]) => {
                            try {
                                console.log('Fetching product info for:', productId);

                                // Validate product ID UUID format
                                if (!isValidUUID(productId)) {
                                    console.warn('Product ID has invalid UUID format:', productId);
                                }

                                const productRes = await getProductInfo(productId);
                                console.log('Product response for', productId, ':', productRes);

                                // Handle the product response structure
                                let productData;
                                if (productRes.data) {
                                    productData = productRes.data;
                                } else if (productRes.name) {
                                    productData = productRes;
                                } else {
                                    throw new Error('Invalid product response structure');
                                }

                                return {
                                    productId,
                                    name: productData.name || `Product ${productId}`,
                                    description: productData.description || '',
                                    price: productData.price || 0,
                                    category: productData.category || '',
                                    quantity: parseInt(quantity) || 0,
                                };
                            } catch (err) {
                                console.error("Failed to fetch product:", productId, err);
                                return {
                                    productId,
                                    name: `Product ${productId.substring(0, 8)}...`,
                                    description: 'Product details unavailable',
                                    price: 0,
                                    category: 'Unknown',
                                    quantity: parseInt(quantity) || 0,
                                };
                            }
                        })
                    );

                    const validProducts = products.filter(product => product !== null);

                    // ✅ IMPROVED: Transform to match what your frontend expects
                    // Now using data directly from OrderDTO
                    const transformedOrder = {
                        orderId: order.orderId,
                        orderDate: order.orderDate,
                        status: order.status || 'UNKNOWN',
                        totalPrice: order.totalPrice || 0,
                        finalPrice: order.finalPrice || order.totalPrice || 0,
                        products: validProducts,
                        storeId: order.storeId,

                        // ✅ These are now provided directly from OrderDTO!
                        storeName: order.storeName || 'Unknown Store',
                        paymentMethod: order.paymentMethod || 'Unknown Payment Method',
                        deliveryAddress: order.deliveryAddress || 'Unknown Address'
                    };

                    console.log('Transformed order:', transformedOrder);
                    return transformedOrder;
                } catch (err) {
                    console.error('Error processing order:', order.orderId, err);
                    return {
                        orderId: order.orderId || 'UNKNOWN',
                        orderDate: order.orderDate || new Date(),
                        status: 'ERROR',
                        totalPrice: 0,
                        finalPrice: 0,
                        products: [],
                        storeId: order.storeId || '',
                        storeName: 'Error Loading Store',
                        paymentMethod: 'Unknown',
                        deliveryAddress: 'Unknown'
                    };
                }
            })
        );

        console.log('Final transformed orders:', transformedOrders);
        return transformedOrders;

    } catch (error) {
        console.error('fetchOrderHistory error:', error);

        if (error.response?.status === 401) {
            throw new Error('Authentication failed. Please log in again.');
        } else if (error.response?.status === 404) {
            throw new Error('Order history not found.');
        } else if (error.message) {
            throw new Error(`Failed to fetch order history: ${error.message}`);
        } else {
            throw new Error('Failed to fetch order history. Please try again.');
        }
    }
};

/**
 * Fetch order status by order ID (with UUID validation)
 * @param {string} orderId - The order UUID as string
 * @returns {Promise<OrderStatus>}
 */
export const fetchOrderStatus = async (orderId) => {
    try {
        console.log('Fetching order status for:', orderId);

        if (!orderId) {
            throw new Error('Order ID is required');
        }

        // Validate UUID format
        if (!isValidUUID(orderId)) {
            throw new Error(`Invalid order ID format: ${orderId}. Expected UUID format.`);
        }

        const response = await orderApiClient.get(`/${orderId}/status`);
        console.log('fetchOrderStatus response:', response.data);
        return response;
    } catch (error) {
        console.error('fetchOrderStatus error:', error);
        throw error;
    }
};