import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import * as userAPI from '../api/user';

// Create auth context
export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isValidating, setIsValidating] = useState(false);

  // Clear user and token from both state and localStorage
  const clearAuth = useCallback(() => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }, []);

  // Validate token with backend
  const validateToken = useCallback(async (username, tokenToValidate) => {
    try {
      // Try to fetch user info to validate token
      const response = await userAPI.returnInfo(username, tokenToValidate);
      return response && !response.error;
    } catch (error) {
      console.error('Token validation failed:', error);
      return false;
    }
  }, []);

  // Initialize auth state and validate token on app startup
  useEffect(() => {
    const initializeAuth = async () => {
      setLoading(true);
      setIsValidating(true);

      try {
        // Load user and token from localStorage
        const storedUser = localStorage.getItem('user');
        const storedToken = localStorage.getItem('token');

        if (storedUser && storedToken) {
          try {
            const userData = JSON.parse(storedUser);

            // Validate the token with the backend
            console.log('Validating stored token for user:', userData.username);
            const isValid = await validateToken(userData.username, storedToken);

            if (isValid) {
              console.log('Token is valid, user remains logged in');
              setUser(userData);
              setToken(storedToken);
            } else {
              console.log('Token is invalid, clearing auth data');
              clearAuth();
            }
          } catch (parseError) {
            console.error('Error parsing stored user data:', parseError);
            clearAuth();
          }
        } else {
          console.log('No stored auth data found');
        }
      } catch (error) {
        console.error('Error during auth initialization:', error);
        clearAuth();
      } finally {
        setLoading(false);
        setIsValidating(false);
      }
    };

    initializeAuth();
  }, [validateToken, clearAuth]);

  // Update localStorage when user or token changes
  useEffect(() => {
    if (user && token) {
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  }, [user, token]);

  // Logout function at the context level
  const logout = useCallback(async () => {
    setIsValidating(true);
    try {
      if (user && token) {
        // Try to logout from backend, but don't fail if it doesn't work
        try {
          await userAPI.logoutUser(user.username, token);
          console.log('Successfully logged out from backend');
        } catch (err) {
          console.warn("Backend logout failed (server might be down):", err);
          // Continue with local logout even if backend fails
        }
      }
    } catch (err) {
      console.error("Error during logout:", err);
    } finally {
      // Always clear local state regardless of API success/failure
      clearAuth();
      setIsValidating(false);
    }
  }, [user, token, clearAuth]);

  // Enhanced login that also validates token immediately
  const login = useCallback(async (username, password) => {
    try {
      const response = await userAPI.loginUser(username, password);

      if (response && response.data) {
        const tokenValue = response.data;

        // Immediately validate the new token by fetching user info
        const userInfoResponse = await userAPI.returnInfo(username, tokenValue);

        if (userInfoResponse && userInfoResponse.data) {
          const userObject = {
            username,
            token: tokenValue,
            role: userInfoResponse.data.role || 'user',
            stores: userInfoResponse.data.stores || [],
          };

          setToken(tokenValue);
          setUser(userObject);
          return response;
        } else {
          throw new Error('Failed to validate login');
        }
      } else {
        throw new Error('Invalid response from server');
      }
    } catch (err) {
      console.error('Login failed:', err);
      clearAuth();
      throw err;
    }
  }, [clearAuth]);

  const value = {
    user,
    setUser,
    token,
    setToken,
    logout,
    login,
    isAuthenticated: !!user && !!token,
    loading,
    isValidating,
    validateToken
  };

  return (
      <AuthContext.Provider value={value}>
        {children}
      </AuthContext.Provider>
  );
};

// Custom hook to use the auth context
export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  return context;
};