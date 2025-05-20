import { useState, useCallback } from 'react';
import * as userAPI from '../api/user';
import { useAuthContext } from '../context/AuthContext'; 

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { setUser, setToken } = useAuthContext();

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
      const response = await userAPI.loginUser(username, password);
      if (response.data) {
        // Assuming response.data contains the token
        setToken(response.data);
        setUser({ username }); // You might want to fetch user details here
      }
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [setToken, setUser]);

  const logout = useCallback(async (username, token) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userAPI.logoutUser(username, token);
      setToken(null);
      setUser(null);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Logout failed');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [setToken, setUser]);

  return {
    register,
    login,
    logout,
    loading,
    error
  };
};