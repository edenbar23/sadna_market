import { useState, useCallback } from 'react';
import * as userAPI from '../api/user';
import { useAuthContext } from '../context/AuthContext';

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { setUser, setToken, user, token } = useAuthContext();

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

      if (response && response.data) {
        const tokenValue = response.data;

        // Set the token in context and localStorage
        setToken(tokenValue);

        // Fetch user details or create a basic user object
        const userObject = {
          username,
          token: tokenValue,
          // Add other user details if available from response
        };

        // Update auth context with user data
        setUser(userObject);

        return response;
      } else {
        throw new Error('Invalid response from server');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [setToken, setUser]);

  const logout = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Only call API if we have a logged in user
      if (user && token) {
        await userAPI.logoutUser(user.username, token);
      }

      // Always clear user data from context and localStorage
      setToken(null);
      setUser(null);

      return true;
    } catch (err) {
      console.error("Logout error:", err);

      // Even if API fails, still clear user data
      setToken(null);
      setUser(null);

      setError(err.response?.data?.message || 'Logout failed');
      return false;
    } finally {
      setLoading(false);
    }
  }, [user, token, setToken, setUser]);

  return {
    register,
    login,
    logout,
    loading,
    error
  };
};