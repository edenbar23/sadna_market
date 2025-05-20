import { mockUsers } from '../data/mockUsers.js';
import mockStores from "../data/mockStores";
import axios from 'axios';

export const fetchUserStores = async (username) => {
    const user = mockUsers.find((u) => u.username === username);
    if (!user) {
      throw new Error(`User ${username} not found`);
    }
    // Return the full store objects based on the IDs the user owns
    const userStores = mockStores.filter((store) => user.stores.includes(store.id));
    return userStores;
  };
  

const API_URL = 'http://localhost:8081/api/users';

// Create axios instance with common config
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add authorization header to requests when we have a token
const setAuthHeader = (token) => {
  if (token) {
    return { headers: { 'Authorization': token } };
  }
  return {};
};

// Guest user methods
export const registerUser = async (userData) => {
  const response = await apiClient.post(API_URL, userData);
  return response.data;
};

export const loginUser = async (username, password) => {
  const response = await apiClient.post(`${API_URL}/login`, null, {
    params: { username, password }
  });
  return response.data;
};

export const addToCartGuest = async (cart, storeId, productId, quantity) => {
  const response = await apiClient.post(`${API_URL}/cart/add`, null, {
    params: { cart, storeId, productId, quantity }
  });
  return response.data;
};

export const viewCartGuest = async (cart) => {
  const response = await apiClient.get(`${API_URL}/cart`, {
    params: { cart }
  });
  return response.data;
};

export const updateCartGuest = async (cart, storeId, productId, newQuantity) => {
  const response = await apiClient.put(`${API_URL}/cart/update`, null, {
    params: { cart, storeId, productId, newQuantity }
  });
  return response.data;
};

export const removeFromCartGuest = async (cart, storeId, productId) => {
  const response = await apiClient.delete(`${API_URL}/cart/remove`, {
    params: { cart, storeId, productId }
  });
  return response.data;
};

export const checkoutGuest = async (cart, paymentMethod) => {
  const response = await apiClient.post(`${API_URL}/checkout`, null, {
    params: { cart, pm: paymentMethod }
  });
  return response.data;
};

// Registered user methods
export const logoutUser = async (username, token) => {
  const response = await apiClient.put(`${API_URL}/${username}/logout`, null, setAuthHeader(token));
  return response.data;
};

export const addToCart = async (username, token, storeId, productId, quantity) => {
  const response = await apiClient.post(
    `${API_URL}/${username}/cart/add`, 
    null,
    {
      ...setAuthHeader(token),
      params: { storeId, productId, quantity }
    }
  );
  return response.data;
};

export const viewCart = async (username, token) => {
  const response = await apiClient.get(
    `${API_URL}/${username}/cart`, 
    setAuthHeader(token)
  );
  return response.data;
};

export const removeFromCart = async (username, token, storeId, productId) => {
  const response = await apiClient.delete(
    `${API_URL}/${username}/cart/remove`, 
    {
      ...setAuthHeader(token),
      params: { storeId, productId }
    }
  );
  return response.data;
};

export const updateCart = async (username, token, storeId, productId, newQuantity) => {
  const response = await apiClient.put(
    `${API_URL}/cart/update`, 
    null,
    {
      ...setAuthHeader(token),
      params: { storeId, productId, newQuantity }
    }
  );
  return response.data;
};

export const checkout = async (username, token, paymentMethod) => {
  const response = await apiClient.post(
    `${API_URL}/checkout`, 
    null,
    {
      ...setAuthHeader(token),
      params: { pm: paymentMethod }
    }
  );
  return response.data;
};

export const saveReview = async (token, review) => {
  const response = await apiClient.post(
    `${API_URL}/review`, 
    null,
    {
      ...setAuthHeader(token),
      params: { review }
    }
  );
  return response.data;
};

export const saveRate = async (token, rating) => {
  const response = await apiClient.post(
    `${API_URL}/rate`, 
    null,
    {
      ...setAuthHeader(token),
      params: { rating }
    }
  );
  return response.data;
};

export const reportViolation = async (username, token, report) => {
  const response = await apiClient.post(
    `${API_URL}/${username}/violation`, 
    null,
    {
      ...setAuthHeader(token),
      params: { report }
    }
  );
  return response.data;
};

export const getOrdersHistory = async (username, token) => {
  const response = await apiClient.get(
    `${API_URL}/${username}/orders`, 
    setAuthHeader(token)
  );
  return response.data;
};

export const returnInfo = async (username, token) => {
  const response = await apiClient.post(
    `${API_URL}/${username}/return`, 
    null,
    setAuthHeader(token)
  );
  return response.data;
};

export const changeUserInfo = async (username, token, userData) => {
  const response = await apiClient.put(
    `${API_URL}/${username}/change-info`, 
    null,
    {
      ...setAuthHeader(token),
      params: { user: userData }
    }
  );
  return response.data;
};

// Admin methods
export const deleteUser = async (adminUsername, token, username) => {
  const response = await apiClient.delete(
    `${API_URL}/ban/${adminUsername}`, 
    {
      ...setAuthHeader(token),
      params: { username }
    }
  );
  return response.data;
};

export const getViolationReports = async (admin, token) => {
  const response = await apiClient.get(
    `${API_URL}/${admin}/violations`, 
    setAuthHeader(token)
  );
  return response.data;
};

export const replyViolationReport = async (admin, token, reportId, user, message) => {
  const response = await apiClient.post(
    `${API_URL}/${admin}/violations/reply`, 
    null,
    {
      ...setAuthHeader(token),
      params: { reportId, user, message }
    }
  );
  return response.data;
};

export const sendMessageToUser = async (admin, token, addressee, message) => {
  const response = await apiClient.post(
    `${API_URL}/${admin}/message`, 
    null,
    {
      ...setAuthHeader(token),
      params: { addressee, message }
    }
  );
  return response.data;
};

export const getUserPurchasedHistory = async (admin, token, username) => {
  const response = await apiClient.get(
    `${API_URL}/${admin}/purchase-history`, 
    {
      ...setAuthHeader(token),
      params: { username }
    }
  );
  return response.data;
};

export const getTransactionsRate = async (admin, token) => {
  const response = await apiClient.get(
    `${API_URL}/${admin}/transaction-rate`, 
    setAuthHeader(token)
  );
  return response.data;
};

export const getSubscriptionsRate = async (admin, token) => {
  const response = await apiClient.get(
    `${API_URL}/${admin}/subscription-rate`, 
    setAuthHeader(token)
  );
  return response.data;
};