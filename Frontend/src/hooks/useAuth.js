import { useState, useCallback } from 'react';
import * as userAPI from '../api/user';
import { useAuthContext } from '../context/AuthContext';

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { setUser, setToken, user, token, login: contextLogin, logout: contextLogout } = useAuthContext();

  const register = useCallback(async (userData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userAPI.registerUser(userData);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await contextLogin(username, password);
      return response;
    } catch (err) {
      setError(
        err?.error ||
        (typeof err === 'string' && err) ||
        err?.response?.data?.error ||
        err?.message ||
        'Login failed'
      );
      throw err;
    } finally {
      setLoading(false);
    }
  }, [contextLogin]);

  const logout = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      await contextLogout();
      return true;
    } catch (err) {
      console.error("Logout error:", err);
      setError(err.response?.data?.message || 'Logout failed');
      return false;
    } finally {
      setLoading(false);
    }
  }, [contextLogout]);

  return {
    register,
    login,
    logout,
    loading,
    error
  };
};