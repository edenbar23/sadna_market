// store.js - Real API implementation

import axios from 'axios';

// Base URL for your backend API
const API_BASE_URL = 'http://localhost:8081/api';

// Create an axios instance with common configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include authentication token when available
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

// Add response interceptor to handle common error scenarios
apiClient.interceptors.response.use(
    response => response.data,
    error => {
      // Handle API errors appropriately
      console.error('API Error:', error.response?.data || error.message);
      return Promise.reject(error.response?.data || error);
    }
);

// Store API functions
export const fetchAllStores = async () => {
  const response = await apiClient.get('/stores');
  return response.data;
};

export const fetchStoreById = async (storeId) => {
  const response = await apiClient.get(`/stores/${storeId}`);
  return response.data;
};

export const fetchStoreProducts = async (storeId) => {
  const response = await apiClient.get(`/products/store/${storeId}`);
  return response.data;
};

export const fetchTopStores = async () => {
  const response = await apiClient.get('/stores/top-rated');
  return response.data;
};

export const fetchStoreMessages = async (storeId, token, username) => {
  const response = await apiClient.get(`/stores/${storeId}/messages`, {
    headers: { Authorization: token },
    params: { username }
  });
  return response.data;
};

export const sendMessageToStore = async (storeId, content, token, username) => {
  const response = await apiClient.post(`/messages`, {
    storeId,
    content
  }, {
    headers: { Authorization: token },
    params: { username }
  });
  return response.data;
};

export const replyToMessage = async (messageId, content, token, username) => {
  const response = await apiClient.post(`/messages/${messageId}/reply`, {
    content
  }, {
    headers: { Authorization: token },
    params: { username }
  });
  return response.data;
};

export const fetchStoreOrders = async (storeId, token, username) => {
  const response = await apiClient.get(`/stores/${storeId}/orders`, {
    headers: { Authorization: token },
    params: { username }
  });
  return response.data;
};

// Store Management Functions
export const createStore = async (storeData, token, username) => {
  const response = await apiClient.post('/stores', {
    ...storeData,
    founderUsername: username
  }, {
    headers: { Authorization: token }
  });
  return response.data;
};

export const closeStore = async (storeId, token, username) => {
  const response = await apiClient.post(`/stores/${storeId}/close`, null, {
    headers: { Authorization: token },
    params: { founderUserName: username }
  });
  return response.data;
};

export const reopenStore = async (storeId, token, username) => {
  const response = await apiClient.post(`/stores/${storeId}/reopen`, null, {
    headers: { Authorization: token },
    params: { founderUserName: username }
  });
  return response.data;
};

export const getStoreStatus = async (storeId) => {
  const response = await apiClient.get(`/stores/${storeId}/status`);
  return response.data;
};

// Store Personnel Management
export const appointStoreOwner = async (storeId, newOwnerUsername, token, username) => {
  const response = await apiClient.post(`/stores/${storeId}/owners`, {
    founderUserName: username,
    newOwnerUserName: newOwnerUsername,
    storeId
  }, {
    headers: { Authorization: token }
  });
  return response.data;
};

export const removeStoreOwner = async (storeId, ownerToRemove, token, username) => {
  const response = await apiClient.delete(`/stores/${storeId}/owners`, {
    data: {
      founderUserName: username,
      removedOwnerUserName: ownerToRemove,
      storeId
    },
    headers: { Authorization: token }
  });
  return response.data;
};

export const appointStoreManager = async (storeId, newManagerUsername, permissions, token, username) => {
  const response = await apiClient.post(`/stores/${storeId}/managers`, {
    appointingUserName: username,
    newManagerUserName: newManagerUsername,
    storeId,
    permissions
  }, {
    headers: { Authorization: token }
  });
  return response.data;
};

export const removeStoreManager = async (storeId, managerToRemove, token, username) => {
  const response = await apiClient.delete(`/stores/${storeId}/managers`, {
    data: {
      appointingUserName: username,
      removedManagerUserName: managerToRemove,
      storeId
    },
    headers: { Authorization: token }
  });
  return response.data;
};

export const rateStore = async (storeId, rating, comment, token, username) => {
  const response = await apiClient.post(`/stores/rate`, {
    storeId,
    username,
    rate: rating,
    comment
  }, {
    headers: { Authorization: token }
  });
  return response.data;
};

// Product Management Functions
export const addProductToStore = async (storeId, productData, quantity, token, username) => {
  const response = await apiClient.post(`/products/store/${storeId}`,
      productData,
      {
        headers: { Authorization: token },
        params: { quantity, username }
      }
  );
  return response.data;
};

export const updateProduct = async (storeId, productData, quantity, token, username) => {
  const response = await apiClient.put(`/products/store/${storeId}`,
      productData,
      {
        headers: { Authorization: token },
        params: { quantity, username }
      }
  );
  return response.data;
};

export const deleteProduct = async (storeId, productId, token, username) => {
  const response = await apiClient.delete(`/products/store/${storeId}`, {
    data: { productId },
    headers: { Authorization: token },
    params: { username }
  });
  return response.data;
};

// For backwards compatibility with your mock data
export const getMockStores = () => {
  return [
    {
      id: 1,
      name: "TechHaven",
      description: "Your go-to store for tech gadgets and accessories",
      active: true,
      founderUsername: "techguru",
      ownerUsernames: ["techguru", "gadgetpro"],
      managerUsernames: ["techlead", "gadgetexpert"]
    },
    {
      id: 2,
      name: "FashionFusion",
      description: "Latest trends in fashion for all ages",
      active: true,
      founderUsername: "fashionista",
      ownerUsernames: ["fashionista", "styleicon"],
      managerUsernames: ["trendsetter", "fashionadvisor"]
    }
  ];
};

export const getMockMessages = () => {
  return [
    {
      id: 1,
      storeId: 1,
      sender: "john_doe",
      content: "Do you have this item in blue?",
      timestamp: "2025-05-14 10:23",
    },
    {
      id: 2,
      storeId: 1,
      sender: "admin",
      content: "Reminder to restock your best-selling items.",
      timestamp: "2025-05-14 12:45",
    },
  ];
};

export const getMockOrders = () => {
  return [
    {
      id: 101,
      storeId: 1,
      buyer: "alice123",
      items: [
        { product: "Gaming Mouse", quantity: 1, price: 49.99 },
        { product: "Keyboard", quantity: 2, price: 29.99 },
      ],
      total: 109.97,
      status: "Shipped",
      orderedAt: "2025-05-13 14:22",
    },
    {
      id: 102,
      storeId: 1,
      buyer: "bob456",
      items: [
        { product: "USB-C Cable", quantity: 3, price: 9.99 },
      ],
      total: 29.97,
      status: "Complete",
      orderedAt: "2025-05-14 09:10",
    }
  ];
};