// src/api/order.js
const BASE_URL = "/orders"; // Adjust if your backend API prefix differs

// Helper to parse JSON response and handle errors
async function handleResponse(response) {
    const data = await response.json();
    if (!response.ok) {
        // Backend sends { success: false, error: "..."} or HTTP error code
        throw new Error(data.error || "API request failed");
    }
    return data.data; // Assuming your backend wraps data in { success, data, error }
}

// Get order details by orderId (UUID string)
export async function getOrder(orderId) {
    const response = await fetch(`${BASE_URL}/${orderId}`);
    return handleResponse(response);
}

// Cancel order by orderId
export async function cancelOrder(orderId) {
    const response = await fetch(`${BASE_URL}/${orderId}/cancel`, {
        method: "POST",
    });
    return handleResponse(response);
}

// Get order history for a user by username
export async function getUserOrderHistory(username) {
    const response = await fetch(`${BASE_URL}/history/${encodeURIComponent(username)}`);
    return handleResponse(response);
}

// Get order status by orderId
export async function getOrderStatus(orderId) {
    const response = await fetch(`${BASE_URL}/${orderId}/status`);
    return handleResponse(response);
}

export const fetchStoreOrders = async (storeId, token, username) => {
    console.log("API: Fetching store orders", { storeId, username });

    try {
        const response = await apiClient.get(`/stores/${storeId}/orders`, {
            headers: { Authorization: token },
            params: { username }
        });

        console.log("API: Store orders response:", response);

        // The response should have the structure: { error: false, data: [...], errorMessage: null }
        if (response.error) {
            throw new Error(response.errorMessage || 'Failed to fetch store orders');
        }

        return response.data;
    } catch (error) {
        console.error("API: Error fetching store orders:", error);
        throw error;
    }
};
