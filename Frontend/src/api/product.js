import mockProducts  from '../data/mockProducts';
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/products'; // adjust if deployed

// Attach the auth token to headers
const authHeaders = (token) => ({
    headers: { Authorization: token },
});

// 1. Search for products
export const searchProducts = async (searchRequest) => {
    const response = await axios.post(`${BASE_URL}/search`, searchRequest);
    return response.data;
};


export const fetchTopProducts = async () => {
    const response = await axios.get(`${BASE_URL}/top-rated`);
    return response.data;
};

// export const searchProducts = async (searchParams) => {
//     // For development/testing, we can use mock data
//     // Later you can replace this with actual API calls
//     const mockProducts = await fetchTopProducts(); // Reuse your existing function
//
//     // Filter products based on search query if provided
//     if (searchParams.query) {
//         const query = searchParams.query.toLowerCase();
//         return mockProducts.filter(product =>
//             product.name.toLowerCase().includes(query) ||
//             (product.description && product.description.toLowerCase().includes(query)) ||
//             (product.category && product.category.toLowerCase().includes(query))
//         );
//     }
//
//     return mockProducts;
// };

function ensureValidUUID(id) {
    if (!id || id === "undefined" || id === undefined) {
        throw new Error("Invalid UUID value");
    }
    return id;
}
//
// // Update the addProductToStore function
// export const addProductToStore = async (storeId, productData, quantity, token, username) => {
//     try {
//         // Validate the storeId
//         const validStoreId = ensureValidUUID(storeId);
//
//         // Create the request data object
//         const requestData = {
//             ...productData,
//             productId: null // Make sure we don't send an undefined productId
//         };
//
//         const response = await apiClient.post(
//             `/products/store/${validStoreId}`,
//             requestData,
//             {
//                 headers: { Authorization: token },
//                 params: { quantity, username }
//             }
//         );
//         return response.data;
//     } catch (error) {
//         console.error("Error adding product to store:", error);
//         throw error;
//     }
// };
//
// // Update the updateProduct function
// export const updateProduct = async (storeId, productData, quantity, token, username) => {
//     try {
//         // Validate the storeId and productId
//         const validStoreId = ensureValidUUID(storeId);
//         const validProductId = ensureValidUUID(productData.productId);
//
//         // Create the request data object with valid productId
//         const requestData = {
//             ...productData,
//             productId: validProductId
//         };
//
//         const response = await apiClient.put(
//             `/products/store/${validStoreId}`,
//             requestData,
//             {
//                 headers: { Authorization: token },
//                 params: { quantity, username }
//             }
//         );
//         return response.data;
//     } catch (error) {
//         console.error("Error updating product:", error);
//         throw error;
//     }
// };
//
// // Update the deleteProduct function
// export const deleteProduct = async (storeId, productId, token, username) => {
//     try {
//         // Validate the storeId and productId
//         const validStoreId = ensureValidUUID(storeId);
//         const validProductId = ensureValidUUID(productId);
//
//         const response = await apiClient.delete(
//             `/products/store/${validStoreId}`,
//             {
//                 data: { productId: validProductId },
//                 headers: { Authorization: token },
//                 params: { username }
//             }
//         );
//         return response.data;
//     } catch (error) {
//         console.error("Error deleting product:", error);
//         throw error;
//     }
// };


// 2. Get product info by ID
export const getProductInfo = async (productId) => {
    const response = await axios.get(`${BASE_URL}/${productId}`);
    return response.data;
};

// 3. Rate a product
export const rateProduct = async (token, rateRequest) => {
    const response = await axios.post(`${BASE_URL}/rate`, rateRequest, authHeaders(token));
    return response.data;
};

// 4. Add a new product to a store
export const addProductToStore = async (token, storeId, productRequest, quantity, username) => {
    const response = await axios.post(
        `${BASE_URL}/store/${storeId}`,
        productRequest,
        {
            ...authHeaders(token),
            params: { quantity, username },
        }
    );
    return response.data;
};

// 5. Update an existing product in a store
export const updateProduct = async (token, storeId, productRequest, quantity, username) => {
    const response = await axios.put(
        `${BASE_URL}/store/${storeId}`,
        productRequest,
        {
            ...authHeaders(token),
            params: { quantity, username },
        }
    );
    return response.data;
};

// 6. Delete a product from a store
export const deleteProduct = async (token, storeId, productRequest, username) => {
    const response = await axios.delete(
        `${BASE_URL}/store/${storeId}`,
        {
            ...authHeaders(token),
            data: productRequest, // must use "data" key for DELETE body
            params: { username },
        }
    );
    return response.data;
};

// 7. Get all products for a store
export const getStoreProducts = async (storeId) => {
    const response = await axios.get(`${BASE_URL}/store/${storeId}`);
    return response.data;
};