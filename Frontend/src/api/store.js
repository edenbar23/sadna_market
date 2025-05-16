// storeApi.js

import mockStores from "../data/mockStores";
import mockProducts from "../data/mockProducts"; 

export const fetchStoreById = async (storeId) => {
  const store = mockStores.find((s) => s.id === storeId);
  if (!store) throw new Error("Store not found");
  return store;
};

export const fetchStoreProducts = async (storeId) => {
  // Assuming each product in mockProducts has a `storeId` field
  return mockProducts.filter((product) => product.storeId === storeId);
};

export const fetchTopStores = async () => {
  // Simulate an API call to fetch top stores
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockStores.slice(0, 10)); // Return the first 5 stores as top stores
    }, 1000);
  });
}
