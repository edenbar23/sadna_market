import mockProducts  from '../data/mockProducts'; 

export const fetchTopProducts = async () => {
    // Simulate an API call to fetch top stores
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockProducts.slice(0, 10)); // Return the first 5 stores as top stores
      }, 1000);
    });
  }

export const searchProducts = async (searchParams) => {
    // For development/testing, we can use mock data
    // Later you can replace this with actual API calls
    const mockProducts = await fetchTopProducts(); // Reuse your existing function

    // Filter products based on search query if provided
    if (searchParams.query) {
        const query = searchParams.query.toLowerCase();
        return mockProducts.filter(product =>
            product.name.toLowerCase().includes(query) ||
            (product.description && product.description.toLowerCase().includes(query)) ||
            (product.category && product.category.toLowerCase().includes(query))
        );
    }

    return mockProducts;
};

function ensureValidUUID(id) {
    if (!id || id === "undefined" || id === undefined) {
        throw new Error("Invalid UUID value");
    }
    return id;
}

// Update the addProductToStore function
export const addProductToStore = async (storeId, productData, quantity, token, username) => {
    try {
        // Validate the storeId
        const validStoreId = ensureValidUUID(storeId);

        // Create the request data object
        const requestData = {
            ...productData,
            productId: null // Make sure we don't send an undefined productId
        };

        const response = await apiClient.post(
            `/products/store/${validStoreId}`,
            requestData,
            {
                headers: { Authorization: token },
                params: { quantity, username }
            }
        );
        return response.data;
    } catch (error) {
        console.error("Error adding product to store:", error);
        throw error;
    }
};

// Update the updateProduct function
export const updateProduct = async (storeId, productData, quantity, token, username) => {
    try {
        // Validate the storeId and productId
        const validStoreId = ensureValidUUID(storeId);
        const validProductId = ensureValidUUID(productData.productId);

        // Create the request data object with valid productId
        const requestData = {
            ...productData,
            productId: validProductId
        };

        const response = await apiClient.put(
            `/products/store/${validStoreId}`,
            requestData,
            {
                headers: { Authorization: token },
                params: { quantity, username }
            }
        );
        return response.data;
    } catch (error) {
        console.error("Error updating product:", error);
        throw error;
    }
};

// Update the deleteProduct function
export const deleteProduct = async (storeId, productId, token, username) => {
    try {
        // Validate the storeId and productId
        const validStoreId = ensureValidUUID(storeId);
        const validProductId = ensureValidUUID(productId);

        const response = await apiClient.delete(
            `/products/store/${validStoreId}`,
            {
                data: { productId: validProductId },
                headers: { Authorization: token },
                params: { username }
            }
        );
        return response.data;
    } catch (error) {
        console.error("Error deleting product:", error);
        throw error;
    }
};