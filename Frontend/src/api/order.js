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
  console.log("Raw response data:", orders);

  // Transform the orders to fit what OrderCard expects
  return await Promise.all(
    orders.map(async (order) => {
      const productEntries = Object.entries(order.products); // [[productId, quantity], ...]

      // Fetch product info for each product
      const products = await Promise.all(
        productEntries.map(async ([productId, quantity]) => {
          try {
            const productRes = await getProductInfo(productId);
            console.log("Product response:", productRes);
            console.log("Product ID:", productId, "Quantity:", quantity);

            // Make sure to access the nested data property
            return {
              productId,
              name: productRes.data.name, // Fix: Access name from productRes.data.name
              description: productRes.data.description,
              price: productRes.data.price,
              category: productRes.data.category,
              quantity,
            };
          } catch (err) {
            console.error("Failed to fetch product:", productId, err);
            return null;
          }
        })
      );

      // Filter out any null products (failed fetches)
      const validProducts = products.filter(product => product !== null);

      // Return the transformed order
      return {
        orderId: order.orderId,
        orderDate: order.orderDate,
        status: order.status,
        totalPrice: order.totalPrice,
        finalPrice: order.finalPrice,
        products: validProducts,
        storeId: order.storeId,
        paymentId: order.paymentId,
        deliveryId: order.deliveryId,
        userName: order.userName,
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