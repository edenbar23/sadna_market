import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Attach token from localStorage to every request
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = token;
  return config;
});

// Flatten responses if needed
apiClient.interceptors.response.use(
  resp => (resp.data && resp.data.data !== undefined ? resp.data.data : resp.data),
  err => Promise.reject(err.response?.data || err)
);

// --- Store Data ---
export const fetchAllStores = () => apiClient.get('/stores');
export const fetchStoreById = storeId => apiClient.get(`/stores/${storeId}`);
export const fetchStoreProducts = storeId => apiClient.get(`/products/store/${storeId}`);
export const fetchTopStores = () => apiClient.get('/stores/top-rated');

// --- Messaging ---
export const fetchStoreMessages = (storeId, token, username) =>
  apiClient.get(`/stores/${storeId}/messages`, {
    headers: { Authorization: token },
    params: { username }
  });

export const sendMessageToStore = (storeId, content, token, username) =>
  apiClient.post(`/messages`, { storeId, content }, {
    headers: { Authorization: token },
    params: { username }
  });

export const replyToMessage = (messageId, content, token, username) =>
  apiClient.post(`/messages/${messageId}/reply`, { content }, {
    headers: { Authorization: token },
    params: { username }
  });

// --- Orders ---
export const fetchStoreOrders = (storeId, token, username) =>
  apiClient.get(`/stores/${storeId}/orders`, {
    headers: { Authorization: token },
    params: { username }
  });

// --- Store CRUD ---
export const createStore = (data, token, username) =>
  apiClient.post('/stores', { ...data, founderUsername: username }, {
    headers: { Authorization: token }
  });

export const updateStore = (storeId, data, token, username) => {
  if (Object.keys(data).length === 1 && data.name) {
    return apiClient.put(`/stores/${storeId}/rename`, null, {
      headers: { Authorization: token },
      params: { username, newName: data.name }
    });
  }
  return apiClient.put(`/stores/${storeId}`, data, {
    headers: { Authorization: token },
    params: { username }
  });
};

export const closeStore = (storeId, token, username) =>
  apiClient.post(`/stores/${storeId}/close`, null, {
    headers: { Authorization: token },
    params: { founderUserName: username }
  });

export const reopenStore = (storeId, token, username) =>
  apiClient.post(`/stores/${storeId}/reopen`, null, {
    headers: { Authorization: token },
    params: { founderUserName: username }
  });

export const getStoreStatus = storeId =>
  apiClient.get(`/stores/${storeId}/status`);

// --- Personnel Management ---
export const appointStoreOwner = (storeId, newOwnerUserName, token, username) =>
  apiClient.post(`/stores/${storeId}/owners`, {
    founderUserName: username,
    newOwnerUserName,
    storeId
  }, {
    headers: { Authorization: token }
  });

export const removeStoreOwner = (storeId, removedOwnerUserName, token, username) =>
  apiClient.delete(`/stores/${storeId}/owners`, {
    data: {
      founderUserName: username,
      removedOwnerUserName,
      storeId
    },
    headers: { Authorization: token }
  });

export const appointStoreManager = (storeId, newManagerUserName, permissions, token, username) =>
  apiClient.post(`/stores/${storeId}/managers`, {
    appointingUserName: username,
    newManagerUserName,
    storeId,
    permissions
  }, {
    headers: { Authorization: token }
  });

export const removeStoreManager = (storeId, removedManagerUserName, token, username) =>
  apiClient.delete(`/stores/${storeId}/managers`, {
    data: {
      appointingUserName: username,
      removedManagerUserName,
      storeId
    },
    headers: { Authorization: token }
  });

// --- Product Management ---
export const addProductToStore = (storeId, productData, quantity, token, username) =>
  apiClient.post(`/products/store/${storeId}`, productData, {
    headers: { Authorization: token },
    params: { quantity, username }
  });

export const updateProduct = (storeId, productData, quantity, token, username) =>
  apiClient.put(`/products/store/${storeId}`, productData, {
    headers: { Authorization: token },
    params: { quantity, username }
  });

export const deleteProduct = (storeId, productId, token, username) =>
  apiClient.delete(`/products/store/${storeId}`, {
    data: { productId },
    headers: { Authorization: token },
    params: { username }
  });

export const searchStores = searchParams =>
  apiClient.post('/stores/search', searchParams);

export const updateStoreOwnerPermissions = async (storeId, ownerUsername, permissions, token, appointerUsername) => {
  console.log("Updating owner permissions with byUser =", appointerUsername);
  const response = await fetch(
    `http://localhost:8081/api/stores/${storeId}/owners/${ownerUsername}/permissions?byUser=${appointerUsername}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify({ permissions })
    }
  );

  const raw = await response.text();
  let data;
  try {
    data = JSON.parse(raw);
  } catch (err) {
    throw new Error("Server returned an invalid response: " + raw);
  }

  if (!response.ok) {
    throw new Error(data?.errorMessage || "Failed to update owner permissions");
  }

  return data;
};

export const updateStoreManagerPermissions = (storeId, managerUsername, permissions, token, appointerUsername) =>
  apiClient.put(
    `/stores/${storeId}/managers/${managerUsername}/permissions`,
    { permissions },
    {
      headers: { Authorization: `Bearer ${token}` },
      params: { byUser: appointerUsername } 
    }
  );

  export const rateStore = (storeId, rate, comment, token, username) =>
  apiClient.post(`/stores/rate`, {
    storeId,
    username,
    rate,
    comment
  }, {
    headers: { Authorization: token }
  });