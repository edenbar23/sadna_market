// src/api/store.js - Backend integration
import axios from 'axios';

// Base URL for your backend API
const API_BASE_URL = 'http://localhost:8081/api';

// Create an axios instance with common configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Add request interceptor to include authentication token when available
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

// Add response interceptor to handle common error scenarios
apiClient.interceptors.response.use(
    response => response.data,
    error => {
      // Log the error for debugging
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
  console.log(response);
  console.log(response.data);
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
  console.log('API: Creating store with data:', storeData, 'and username:', username);
  storeData.founderUsername =username;
  const response = await apiClient.post('/stores', {
    ...storeData
  }, {
    headers: { Authorization: token }
  });
  return response.data;
};

export const updateStore = async (storeId, storeData, token, username) => {
  // If only the name is being updated, use the rename endpoint
  if (Object.keys(storeData).length === 1 && storeData.name) {
    const response = await apiClient.put(`/stores/${storeId}/rename`, null, {
      headers: { Authorization: token },
      params: { 
        username: username,
        newName: storeData.name 
      }
    });
    return response;
  }

  // For other updates, use the general update endpoint
  const response = await apiClient.put(`/stores/${storeId}`, storeData, {
    headers: { Authorization: token },
    params: { username }
  });
  return response;
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

// Product Management Functions for Stores
export const addProductToStore = async (storeId, productData, quantity, token, username) => {
  console.log('API: Adding product to store with params:', {
    storeId,
    productData,
    quantity,
    username
  });

  // Validate inputs
  if (!storeId) {
    throw new Error('Store ID is required');
  }
  if (!productData) {
    throw new Error('Product data is required');
  }
  if (!productData.name || !productData.name.trim()) {
    throw new Error('Product name is required');
  }
  if (!productData.price || isNaN(parseFloat(productData.price))) {
    throw new Error('Valid product price is required');
  }
  if (!username || !username.trim()) {
    throw new Error('Username is required');
  }
  if (!token) {
    throw new Error('Authentication token is required');
  }

  try {
    // Clean the product data
    const cleanProductData = {
      name: productData.name.trim(),
      description: productData.description?.trim() || "",
      category: productData.category?.trim() || "",
      price: parseFloat(productData.price)
      // Don't include productId for new products
    };

    const productQuantity = quantity && !isNaN(parseInt(quantity)) ? parseInt(quantity) : 10;

    console.log('API: Sending clean data:', { cleanProductData, productQuantity });

    const response = await apiClient.post(`/products/store/${storeId}`,
        cleanProductData,
        {
          headers: { Authorization: token },
          params: { quantity: productQuantity, username: username.trim() }
        }
    );

    console.log('API: Add product response:', response);
    return response;
  } catch (error) {
    console.error('API: Add product failed:', error);

    // Extract meaningful error message
    let errorMessage = 'Failed to add product';

    if (error.response?.data?.errorMessage) {
      errorMessage = error.response.data.errorMessage;
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data) {
      // If there's response data but no specific error message
      errorMessage = JSON.stringify(error.response.data);
    } else if (error.errorMessage) {
      errorMessage = error.errorMessage;
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('API: Extracted error message:', errorMessage);
    throw new Error(errorMessage);
  }
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

export const searchStores = async (searchParams) => {
  try {
    const response = await apiClient.post('/stores/search', searchParams);
    return response.data;
  } catch (error) {
    console.error('Error searching stores:', error);
    throw error;
  }
};