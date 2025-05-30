import axios from 'axios';

const API_URL = 'http://localhost:8081/api/users';

// Create axios instance with common config
const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 10000 // 10 second timeout
});

// Add request interceptor for authorization
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
        console.error('API Error:', error.response?.data || error.message);
        return Promise.reject(error.response?.data || error);
    }
);

// Function to fetch a user's stores from the API
export const fetchUserStores = async (username, token) => {
    try {
        const response = await apiClient.get(`/user/${username}/stores`, {
            headers: { Authorization: token }
        });
        return response;
    } catch (error) {
        console.error('Error fetching user stores:', error);
        throw error;
    }
};

// Authentication APIs
export const registerUser = async (userData) => {
    const response = await apiClient.post('/register', userData);
    return response;
};

export const loginUser = async (username, password) => {
    const response = await apiClient.post('/login', null, {
        params: { username, password }
    });
    return response;
};

export const logoutUser = async (username, token) => {
    const response = await apiClient.post(`/${username}/logout`, null, {
        headers: { Authorization: token }
    });
    return response;
};

// Guest cart management APIs
export const addToCartGuest = async (cart, storeId, productId, quantity) => {
    const response = await apiClient.post('/guest/cart', null, {
        params: { storeId, productId, quantity },
        data: cart
    });
    return response;
};

export const viewCartGuest = async (cart) => {
    const response = await apiClient.get('/guest/cart', {
        data: cart
    });
    return response;
};

export const updateCartGuest = async (cart, storeId, productId, newQuantity) => {
    const response = await apiClient.put('/guest/cart', null, {
        params: { storeId, productId, newQuantity },
        data: cart
    });
    return response;
};

export const removeFromCartGuest = async (cart, storeId, productId) => {
    const response = await apiClient.delete('/guest/cart', {
        params: { storeId, productId },
        data: cart
    });
    return response;
};

export const checkoutGuest = async (checkoutData) => {
    const response = await axios.post('http://localhost:8081/api/checkout/guest', checkoutData, {
        headers: {
            'Content-Type': 'application/json'
        },
        timeout: 10000
    });
    return response.data;
};

// User cart management APIs
export const addToCart = async (username, token, storeId, productId, quantity) => {
    const response = await apiClient.post(
        `/${username}/cart?storeId=${storeId}&productId=${productId}&quantity=${quantity}`,
        null,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const viewCart = async (username, token) => {
    const response = await apiClient.get(
        `/${username}/cart`,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const removeFromCart = async (username, token, storeId, productId) => {
    const response = await apiClient.delete(
        `/${username}/cart`,
        {
            params: { storeId, productId },
            headers: { Authorization: token }
        }
    );
    return response;
};

export const updateCart = async (username, token, storeId, productId, newQuantity) => {
    const response = await apiClient.put(
        `/${username}/cart`,
        null, // No request body needed
        {
            params: {
                storeId,
                productId,
                quantity: newQuantity
            },
            headers: { Authorization: token }
        }
    );
    return response;
};

export const checkout = async (username, token, checkoutData) => {
    const response = await axios.post(
        `http://localhost:8081/api/checkout/user/${username}`,
        checkoutData,
        {
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': token 
            },
            timeout: 10000
        }
    );
    return response.data;
};

// User profile management APIs
export const getOrdersHistory = async (username, token) => {
    const response = await apiClient.get(
        `/${username}/orders`,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const returnInfo = async (username, token) => {
    const response = await apiClient.get(
        `/${username}`,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const changeUserInfo = async (username, token, userData) => {
    const response = await apiClient.put(
        `/${username}`,
        userData,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

// Product review and rating APIs
export const saveReview = async (token, review) => {
    const response = await apiClient.post(
        `/reviews`,
        review,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const saveRate = async (token, rating) => {
    const response = await apiClient.post(
        `/ratings`,
        rating,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};

export const reportViolation = async (username, token, report) => {
    const response = await apiClient.post(
        `/${username}/violations`,
        report,
        {
            headers: { Authorization: token }
        }
    );
    return response;
};
