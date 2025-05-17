// storeApi.js

import mockStores from "../data/mockStores";
import mockProducts from "../data/mockProducts"; 

export const mockStoreMessages = [
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

export const mockStoreOrders = [
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
  },
  {
    id: 103,
    storeId: 1,
    buyer: "charlie789",
    items: [
      { product: "Wireless Charger", quantity: 1, price: 19.99 },
      { product: "Phone Case", quantity: 2, price: 14.99 },
    ],
    total: 49.97,
    status: "Pending",
    orderedAt: "2025-05-14 11:30",
  },
  {
    id: 104,
    storeId: 2,
    buyer: "dave101",
    items: [
      { product: "Laptop Stand", quantity: 1, price: 39.99 },
      { product: "Monitor", quantity: 1, price: 199.99 },
    ],
    total: 239.98,
    status: "Pending",
    orderedAt: "2025-05-14 13:00",
  }
];


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

export const fetchStoreMessages = async (storeId) => {
  return mockStoreMessages.filter(msg => msg.storeId === parseInt(storeId));
};

export const fetchStoreOrders = async (storeId) => {
  return mockStoreOrders.filter(order => order.storeId === parseInt(storeId));
};