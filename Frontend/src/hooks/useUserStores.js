import { useState } from "react";
import { fetchUserStores } from "@/api/user";
import { fetchStoreById } from "@/api/store";

export function useUserStores(user) {
    const [stores, setStores] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const loadUserStores = async () => {
        if (!user) return;

        setIsLoading(true);
        setError(null);

        try {
            const storeIds = await fetchUserStores(user.username, user.token);
            const storePromises = storeIds.map((storeId) => fetchStoreById(storeId));
            const storeData = await Promise.all(storePromises);

            setStores(storeData);
        } catch (err) {
            console.error("Error loading user stores:", err);
            setError(err.message || "Failed to load stores");
        } finally {
            setIsLoading(false);
        }
    };

    return { stores, isLoading, error, loadUserStores };
}