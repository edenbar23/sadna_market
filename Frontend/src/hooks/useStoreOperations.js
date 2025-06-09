import { useState } from 'react';
import { createStore, updateStore, closeStore, reopenStore } from '../api/store';

/**
 * Custom hook for store operations like creating, updating, closing
 *
 * @param {object} user - The current user object with token and username
 * @returns {object} Store operations functions and state
 */
export function useStoreOperations(user) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Create a new store
     *
     * @param {object} storeData - Store data to create
     * @returns {Promise<object>} The created store
     */
    const handleCreateStore = async (storeData) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to create a store");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await createStore(storeData, token, user.username);
            console.log("creating store", storeData, token, user.username);
            return result;
        } catch (err) {
            console.error("Failed to create store:", err);
            setError(err.message || err.errorMessage || "Failed to create store");
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Update an existing store
     *
     * @param {string} storeId - ID of the store to update
     * @param {object} storeData - Updated store data
     * @returns {Promise<object>} The updated store
     */
    const handleUpdateStore = async (storeId, storeData) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to update a store");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await updateStore(storeId, storeData, token, user.username);
            return result;
        } catch (err) {
            console.error("Failed to update store:", err);
            setError(err.message || err.errorMessage || "Failed to update store");
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Toggle store status (close/reopen)
     *
     * @param {string} storeId - ID of the store
     * @param {boolean} isCurrentlyActive - Current active status of the store
     * @returns {Promise<object>} Result of the operation
     */
    const handleToggleStoreStatus = async (storeId, isCurrentlyActive) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to change store status");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");

            console.log('Toggling store status:', {
                storeId,
                isCurrentlyActive,
                action: isCurrentlyActive ? 'close' : 'reopen',
                username: user.username
            });

            let result;
            if (isCurrentlyActive) {
                // Close the store
                console.log('Closing store...');
                result = await closeStore(storeId, token, user.username);
            } else {
                // Reopen the store
                console.log('Reopening store...');
                result = await reopenStore(storeId, token, user.username);
            }

            console.log('Store status toggle result:', result);
            return result;
        } catch (err) {
            console.error("Failed to change store status:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to change store status";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Close a store
     *
     * @param {string} storeId - ID of the store to close
     * @returns {Promise<object>} Result of the operation
     */
    const handleCloseStore = async (storeId) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to close a store");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            console.log('Closing store:', { storeId, username: user.username });

            const result = await closeStore(storeId, token, user.username);
            console.log('Close store result:', result);
            return result;
        } catch (err) {
            console.error("Failed to close store:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to close store";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Reopen a store
     *
     * @param {string} storeId - ID of the store to reopen
     * @returns {Promise<object>} Result of the operation
     */
    const handleReopenStore = async (storeId) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to reopen a store");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            console.log('Reopening store:', { storeId, username: user.username });

            const result = await reopenStore(storeId, token, user.username);
            console.log('Reopen store result:', result);
            return result;
        } catch (err) {
            console.error("Failed to reopen store:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to reopen store";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    return {
        handleCreateStore,
        handleUpdateStore,
        handleToggleStoreStatus,
        handleCloseStore,
        handleReopenStore,
        isLoading,
        error
    };
}