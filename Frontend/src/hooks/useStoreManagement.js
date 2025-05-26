import { useState, useEffect } from 'react';
import { fetchStoreById, fetchStoreProducts, fetchStoreMessages, fetchStoreOrders } from '../api/store';

/**
 * Custom hook for managing a single store and its related data
 *
 * @param {string} storeId - The ID of the store to manage
 * @param {object} user - The current user object with token and username
 * @returns {object} Store data and management functions
 */
export function useStoreManagement(storeId, user) {
    const [store, setStore] = useState(null);
    const [products, setProducts] = useState([]);
    const [orders, setOrders] = useState([]);
    const [messages, setMessages] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Load store data
    const loadStoreData = async () => {
        if (!storeId) {
            setError("Store ID is required");
            setIsLoading(false);
            return;
        }

        console.log("Loading store data for store:", storeId);
        setIsLoading(true);
        setError(null);

        try {
            // Get token from user or localStorage
            const token = user?.token || localStorage.getItem("authToken");

            // Fetch store details
            console.log("Fetching store details...");
            const storeData = await fetchStoreById(storeId);
            console.log("Store data received:", storeData);
            setStore(storeData);

            // Fetch store products
            console.log("Fetching store products...");
            const storeProducts = await fetchStoreProducts(storeId);
            console.log("Products received:", storeProducts);
            setProducts(storeProducts || []);

            // Only fetch orders and messages if user is logged in
            if (user && user.username && token) {
                // Fetch store orders
                try {
                    console.log("Fetching store orders...");
                    const storeOrders = await fetchStoreOrders(storeId, token, user.username);
                    console.log("Orders received:", storeOrders);
                    setOrders(storeOrders || []);
                } catch (ordersError) {
                    console.warn("Could not fetch orders:", ordersError);
                    setOrders([]);
                    // Don't fail completely if just orders fail
                }

                // Fetch store messages
                try {
                    console.log("Fetching store messages...");
                    const storeMessages = await fetchStoreMessages(storeId, token, user.username);
                    console.log("Messages received:", storeMessages);
                    setMessages(storeMessages || []);
                } catch (messagesError) {
                    console.warn("Could not fetch messages:", messagesError);
                    setMessages([]);
                    // Don't fail completely if just messages fail
                }
            } else {
                console.log("User not logged in, skipping orders and messages");
                setOrders([]);
                setMessages([]);
            }
        } catch (err) {
            console.error("Failed to load store data:", err);
            setError(err.message || "Unknown error");
        } finally {
            setIsLoading(false);
        }
    };

    // Load store data when component mounts or dependencies change
    useEffect(() => {
        loadStoreData();
    }, [storeId, user]);

    // Update product in the list
    const updateProductInList = (updatedProduct) => {
        setProducts(currentProducts =>
            currentProducts.map(p =>
                p.productId === updatedProduct.productId ? { ...p, ...updatedProduct } : p
            )
        );
    };

    // Remove product from the list
    const removeProductFromList = (productId) => {
        setProducts(currentProducts =>
            currentProducts.filter(p => p.productId !== productId)
        );
    };

    // Add product to the list
    const addProductToList = (newProduct) => {
        setProducts(currentProducts => [...currentProducts, newProduct]);
    };

    // Update store data
    const updateStoreData = (updatedStoreData) => {
        setStore(currentStore => ({ ...currentStore, ...updatedStoreData }));
    };

    return {
        store,
        products,
        orders,
        messages,
        isLoading,
        error,
        loadStoreData,
        updateProductInList,
        removeProductFromList,
        addProductToList,
        updateStoreData
    };
}