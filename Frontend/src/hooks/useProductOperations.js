import { useState } from 'react';
import { addProductToStore, updateProduct, deleteProduct } from '../api/store';

/**
 * Custom hook for product operations in a store
 *
 * @param {string} storeId - The ID of the store
 * @param {object} user - The current user object with token and username
 * @returns {object} Product operations and state
 */
export function useProductOperations(storeId, user) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Add a new product to the store
     *
     * @param {object} productData - Product data to add
     * @returns {Promise<object>} The added product
     */
    const handleAddProduct = async (productData) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to add a product");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await addProductToStore(
                storeId,
                productData,
                productData.quantity || 10,
                token,
                user.username
            );
            return result;
        } catch (err) {
            console.error("Failed to add product:", err);
            setError(err.message || err.errorMessage || "Failed to add product");
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Update an existing product
     *
     * @param {object} productData - Updated product data
     * @returns {Promise<object>} Result of the update operation
     */
    const handleUpdateProduct = async (productData) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to update a product");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await updateProduct(
                storeId,
                productData,
                productData.quantity,
                token,
                user.username
            );
            return result;
        } catch (err) {
            console.error("Failed to update product:", err);
            setError(err.message || err.errorMessage || "Failed to update product");
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Delete a product from the store
     *
     * @param {string} productId - ID of the product to delete
     * @returns {Promise<object>} Result of the delete operation
     */
    const handleDeleteProduct = async (productId) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to delete a product");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            const result = await deleteProduct(storeId, productId, token, user.username);
            return result;
        } catch (err) {
            console.error("Failed to delete product:", err);
            setError(err.message || err.errorMessage || "Failed to delete product");
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    return {
        handleAddProduct,
        handleUpdateProduct,
        handleDeleteProduct,
        isLoading,
        error
    };
}