import { useState, useCallback } from 'react';
import * as userService from '../api/user';
import { useAuthContext } from './useAuthContext';

export const useUserProfile = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { user, token } = useAuthContext();

  const getOrdersHistory = useCallback(async () => {
    if (!user || !token) return;
    
    setLoading(true);
    setError(null);
    try {
      const response = await userService.getOrdersHistory(user.username, token);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch order history');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [user, token]);

  const getUserInfo = useCallback(async () => {
    if (!user || !token) return;
    
    setLoading(true);
    setError(null);
    try {
      const response = await userService.returnInfo(user.username, token);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch user information');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [user, token]);

  const updateUserInfo = useCallback(async (userData) => {
    if (!user || !token) return;
    
    setLoading(true);
    setError(null);
    try {
      const response = await userService.changeUserInfo(user.username, token, userData);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update user information');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [user, token]);

  return {
    loading,
    error,
    getOrdersHistory,
    getUserInfo,
    updateUserInfo
  };
};