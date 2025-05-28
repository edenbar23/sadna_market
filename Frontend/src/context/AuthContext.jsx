import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import * as userAPI from '../api/user';

// Create auth context
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load user and token from localStorage on initial render
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');

    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser));
        setToken(storedToken);
      } catch (e) {
        console.error("Error parsing user or token from localStorage:", e);
        // If there's any error parsing stored data, clear it
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }

    setLoading(false);
  }, []);

  // Update localStorage when user or token changes
  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }

    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [user, token]);

  // Logout function at the context level
  const logout = useCallback(async () => {
    try {
      if (user && token) {
        await userAPI.logoutUser(user.username, token);
      }
    } catch (err) {
      console.error("Error during logout:", err);
    } finally {
      // Always clear state regardless of API success/failure
      setUser(null);
      setToken(null);
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  }, [user, token]);

  const value = {
    user,
    setUser,
    token,
    setToken,
    logout,
    isAuthenticated: !!user && !!token,
    loading
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