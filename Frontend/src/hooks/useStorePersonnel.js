import { useState } from 'react';
import {
  appointStoreOwner,
  appointStoreManager,
  removeStoreOwner,
  removeStoreManager,
  updateStoreOwnerPermissions,
  updateStoreManagerPermissions
} from '../api/store';

/**
 * Custom hook for managing store personnel (owners and managers) and their permissions.
 */
export function useStorePersonnel(storeId, user) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // -- Owner and Manager appointment/removal --
  const handleAppointOwner = async (targetUsername) => {
    if (!user || !user.username) throw new Error("You must be logged in to appoint an owner");
    if (!storeId) throw new Error("Store ID is required");
    if (!targetUsername || !targetUsername.trim()) throw new Error("Target username is required");
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await appointStoreOwner(storeId, targetUsername.trim(), token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to appoint owner");
      throw err;
    } finally { setIsLoading(false); }
  };

  const handleRemoveOwner = async (targetUsername) => {
    if (!user || !user.username) throw new Error("You must be logged in to remove an owner");
    if (!storeId) throw new Error("Store ID is required");
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await removeStoreOwner(storeId, targetUsername, token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to remove owner");
      throw err;
    } finally { setIsLoading(false); }
  };

  const handleAppointManager = async (targetUsername, permissions = []) => {
    if (!user || !user.username) throw new Error("You must be logged in to appoint a manager");
    if (!storeId) throw new Error("Store ID is required");
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await appointStoreManager(storeId, targetUsername, permissions, token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to appoint manager");
      throw err;
    } finally { setIsLoading(false); }
  };

  const handleRemoveManager = async (targetUsername) => {
    if (!user || !user.username) throw new Error("You must be logged in to remove a manager");
    if (!storeId) throw new Error("Store ID is required");
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await removeStoreManager(storeId, targetUsername, token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to remove manager");
      throw err;
    } finally { setIsLoading(false); }
  };

  // --- Permissions: update for both owners and managers ---
  const updateOwnerPermissions = async (ownerUsername, permissions) => {
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await updateStoreOwnerPermissions(storeId, ownerUsername, permissions, token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to update owner permissions");
      throw err;
    } finally { setIsLoading(false); }
  };

  const updateManagerPermissions = async (managerUsername, permissions) => {
    setIsLoading(true); setError(null);
    try {
      const token = user.token || localStorage.getItem("token");
      return await updateStoreManagerPermissions(storeId, managerUsername, permissions, token, user.username);
    } catch (err) {
      setError(err?.errorMessage || err?.message || "Failed to update manager permissions");
      throw err;
    } finally { setIsLoading(false); }
  };

  const usePermissions = (storeId, personnel) => {
    const [userPermissions, setUserPermissions] = useState({});
  
    useEffect(() => {
      const fetchPermissions = async () => {
        const updatedPermissions = {};
        for (const user of personnel) {
          try {
            const response = await getUserPermissions(storeId, user.username);
            updatedPermissions[user.username] = response.data || [];
          } catch (err) {
            updatedPermissions[user.username] = []; // fallback
          }
        }
        setUserPermissions(updatedPermissions);
      };
  
      if (storeId && personnel?.length) fetchPermissions();
    }, [storeId, personnel]);
  
    return userPermissions;
  };

  return {
    handleAppointOwner,
    handleRemoveOwner,
    handleAppointManager,
    handleRemoveManager,
    updateOwnerPermissions,
    updateManagerPermissions,
    isLoading,
    error
  };
}
