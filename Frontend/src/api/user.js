import { mockUsers } from '../data/mockUsers.js';
import mockStores from "../data/mockStores";

export const fetchUserStores = async (username) => {
    const user = mockUsers.find((u) => u.username === username);
    if (!user) {
      throw new Error(`User ${username} not found`);
    }
    // Return the full store objects based on the IDs the user owns
    const userStores = mockStores.filter((store) => user.stores.includes(store.storeId));
    return userStores;
  };