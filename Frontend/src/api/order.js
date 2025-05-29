import axios from 'axios';
import { fetchStoreById } from './store'; // Assuming you have a store API module
import { getProductInfo } from './product'; // Assuming you have a product API module

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

// Add response interceptor for error handling
orderApiClient.interceptors.response.use(
    response => response.data,
    error => {
        console.error('Order API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

/**
 * Fetch order by ID
 * @param {string} orderId
 * @returns {Promise<Response<OrderDTO>>}
 */
export const fetchOrderById = async (orderId) => {
    const response = await orderApiClient.get(`/${orderId}`);
    return response;
};

/**
 * Fetch user order history
 * @param {string} username
 * @param {string} token
 * @returns {Promise<Response<List<OrderDTO>>>}
 */
export const fetchOrderHistory = async (username) => {
    const token = localStorage.getItem('token');

    const res = await axios.get(`${API_URL}/history/${username}`, {
        headers: {
            Authorization: token,
        },
    });

    const orders = res.data.data;
    console.log("Raw response data:", res.data);

    // You need to transform the orders to fit what OrderCard expects
    return await Promise.all(
        orders.map(async (order) => {
            const productEntries = Object.entries(order.products); // [[productId, quantity], ...]

            // Assuming you have a getProductById function that fetches product info
            const products = await Promise.all(
                productEntries.map(async ([productId, quantity]) => {
                    const productRes = getProductInfo(productId); // Replace with your actual function to fetch product info
                    return {
                        name: productRes.data.name,
                        quantity,
                    };
                })
            );

            // Assuming you also have APIs for storeName, paymentMethod, and deliveryAddress:
            const storeRes = fetchStoreById(order.storeId);
            console.log("Store response:", storeRes.data);
            //   const paymentRes = await axios.get(`http://localhost:8081/api/payments/${order.paymentId}`);
            //   const deliveryRes = await axios.get(`http://localhost:8081/api/deliveries/${order.deliveryId}`);

            return {
                storeName: storeRes.data.name,
                products,
                // paymentMethod: paymentRes.data.method,
                // deliveryAddress: deliveryRes.data.address,
                totalPrice: order.finalPrice, // or totalPrice depending on what you want to show
                status: order.status,
            };
        })
    );
};

/**
 * Fetch order status by order ID
 * @param {string} orderId
 * @returns {Promise<Response<OrderStatus>>}
 */
export const fetchOrderStatus = async (orderId) => {
    const response = await orderApiClient.get(`/${orderId}/status`);
    return response;
};