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
     * @param {number} quantity - Initial quantity (optional, defaults to 10)
     * @returns {Promise<object>} The added product result
     */
    const handleAddProduct = async (productData, quantity = 10) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to add a product");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        if (!productData || !productData.name || !productData.price) {
            throw new Error("Product name and price are required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");

            console.log('Adding product with data:', {
                storeId,
                productData,
                quantity,
                username: user.username
            });

            // Ensure productData has the required fields
            const cleanProductData = {
                name: productData.name,
                description: productData.description || "",
                category: productData.category || "",
                price: parseFloat(productData.price),
                productId: null // Ensure we don't send productId for new products
            };

            const result = await addProductToStore(
                storeId,
                cleanProductData,
                parseInt(quantity),
                token,
                user.username
            );

            console.log('Add product result:', result);
            return result;
        } catch (err) {
            console.error("Failed to add product:", err);

            // Extract meaningful error message
            let errorMessage = 'Failed to add product';

            if (err.response?.data?.errorMessage) {
                errorMessage = err.response.data.errorMessage;
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            } else if (err.errorMessage) {
                errorMessage = err.errorMessage;
            } else if (err.message) {
                errorMessage = err.message;
            } else if (typeof err === 'string') {
                errorMessage = err;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Update an existing product
     *
     * @param {object} productData - Updated product data
     * @param {number} quantity - Updated quantity (optional)
     * @returns {Promise<object>} Result of the update operation
     */
    const handleUpdateProduct = async (productData, quantity) => {
        if (!user || !user.username) {
            throw new Error("You must be logged in to update a product");
        }

        if (!storeId) {
            throw new Error("Store ID is required");
        }

        if (!productData || !productData.productId) {
            throw new Error("Product ID is required for updates");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");
            if (!token) {
                throw new Error("Authentication token is required");
            }

            console.log('Updating product with data:', {
                storeId,
                productData,
                quantity,
                username: user.username
            });

            const result = await updateProduct(
                storeId,
                productData,
                quantity || 1,
                token,
                user.username
            );

            if (!result) {
                throw new Error("No response from server");
            }

            console.log('Update product result:', result);
            return result;
        } catch (err) {
            console.error("Failed to update product:", err);

            // Extract meaningful error message
            let errorMessage = 'Failed to update product';

            if (err.response?.data?.errorMessage) {
                errorMessage = err.response.data.errorMessage;
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            } else if (err.errorMessage) {
                errorMessage = err.errorMessage;
            } else if (err.message) {
                errorMessage = err.message;
            } else if (typeof err === 'string') {
                errorMessage = err;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
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

        if (!productId) {
            throw new Error("Product ID is required");
        }

        setIsLoading(true);
        setError(null);

        try {
            const token = user.token || localStorage.getItem("authToken");

            console.log('Deleting product:', {
                storeId,
                productId,
                username: user.username
            });

            const result = await deleteProduct(storeId, productId, token, user.username);

            console.log('Delete product result:', result);
            return result;
        } catch (err) {
            console.error("Failed to delete product:", err);

            // Extract meaningful error message
            let errorMessage = 'Failed to delete product';

            if (err.response?.data?.errorMessage) {
                errorMessage = err.response.data.errorMessage;
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            } else if (err.errorMessage) {
                errorMessage = err.errorMessage;
            } else if (err.message) {
                errorMessage = err.message;
            } else if (typeof err === 'string') {
                errorMessage = err;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
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