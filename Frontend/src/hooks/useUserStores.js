import { useState } from 'react';
import { fetchUserStores } from '../api/user';

/**
 * Custom hook for fetching and managing a user's stores
 *
 * @param {object} user - The current user object with token and username
 * @returns {object} User stores data and management functions
 */
export function useUserStores(user) {
    const [stores, setStores] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Load the user's stores
     * @returns {Promise<Array>} The user's stores
     */
    const loadUserStores = async () => {
        if (!user || !user.username) {
            setStores([]);
            return [];
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const userStores = await fetchUserStores(user.username, token);
            setStores(userStores || []);
            return userStores;
        } catch (err) {
            console.error("Failed to load user stores:", err);
            setError(err.message || err.errorMessage || "Failed to load your stores");
            return [];
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Add a store to the user's stores list
     * @param {object} newStore - The store to add
     */
    const addStore = (newStore) => {
        setStores(currentStores => [...currentStores, newStore]);
    };

    /**
     * Update a store in the user's stores list
     * @param {object} updatedStore - The updated store
     */
    const updateStore = (updatedStore) => {
        setStores(currentStores =>
            currentStores.map(store =>
                store.id === updatedStore.id ? { ...store, ...updatedStore } : store
            )
        );
    };

    /**
     * Remove a store from the user's stores list
     * @param {string} storeId - ID of the store to remove
     */
    const removeStore = (storeId) => {
        setStores(currentStores =>
            currentStores.filter(store => store.id !== storeId)
        );
    };

    return {
        stores,
        isLoading,
        error,
        loadUserStores,
        addStore,
        updateStore,
        removeStore
    };
}