import { useState } from 'react';
import { appointStoreOwner, appointStoreManager, removeStoreOwner, removeStoreManager } from '../api/store';

/**
 * Custom hook for managing store personnel (owners and managers)
 *
 * @param {string} storeId - The ID of the store
 * @param {object} user - The current user object with token and username
 * @returns {object} Store personnel management functions and state
 */
export function useStorePersonnel(storeId, user) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Appoint a user as store owner
     *
     * @param {string} targetUsername - Username to appoint as owner
     * @returns {Promise<object>} Result of the operation
     */
    const handleAppointOwner = async (targetUsername) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to appoint an owner");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        if (!targetUsername || !targetUsername.trim()) {
            throw new Error("Target username is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            console.log('Appointing owner:', { storeId, targetUsername, appointer: user.username });

            const result = await appointStoreOwner(
                storeId,
                targetUsername.trim(),
                token,
                user.username
            );

            console.log('Appoint owner result:', result);
            return result;
        } catch (err) {
            console.error("Failed to appoint owner:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to appoint owner";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Remove a user from store owners
     *
     * @param {string} targetUsername - Username to remove from owners
     * @returns {Promise<object>} Result of the operation
     */
    const handleRemoveOwner = async (targetUsername) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to remove an owner");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await removeStoreOwner(
                storeId,
                targetUsername,
                token,
                user.username
            );
            return result;
        } catch (err) {
            console.error("Failed to remove owner:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to remove owner";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Appoint a user as store manager with specific permissions
     *
     * @param {string} targetUsername - Username to appoint as manager
     * @param {Array<string>} permissions - List of permission keys
     * @returns {Promise<object>} Result of the operation
     */
    const handleAppointManager = async (targetUsername, permissions = []) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to appoint a manager");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await appointStoreManager(
                storeId,
                targetUsername,
                permissions,
                token,
                user.username
            );
            return result;
        } catch (err) {
            console.error("Failed to appoint manager:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to appoint manager";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Remove a user from store managers
     *
     * @param {string} targetUsername - Username to remove from managers
     * @returns {Promise<object>} Result of the operation
     */
    const handleRemoveManager = async (targetUsername) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to remove a manager");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await removeStoreManager(
                storeId,
                targetUsername,
                token,
                user.username
            );
            return result;
        } catch (err) {
            console.error("Failed to remove manager:", err);
            const errorMsg = err?.errorMessage || err?.message || "Failed to remove manager";
            setError(errorMsg);
            throw new Error(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    return {
        handleAppointOwner,
        handleRemoveOwner,
        handleAppointManager,
        handleRemoveManager,
        isLoading,
        error
    };
}