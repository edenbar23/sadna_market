import { useState, useCallback } from 'react';
import * as userService from '../api/user';
import { useAuthContext } from './useAuthContext';

export const useCart = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [cart, setCart] = useState(null);
  const { user, token } = useAuthContext();

  // Helper to determine if we're dealing with registered user or guest
  const isRegistered = !!user;

  // View cart
  const fetchCart = useCallback(async (guestCart = null) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (isRegistered) {
        response = await userService.viewCart(user.username, token);
      } else {
        response = await userService.viewCartGuest(guestCart);
      }
      setCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch cart');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isRegistered, user, token]);

  // Add to cart
  const addToCart = useCallback(async (storeId, productId, quantity, guestCart = null) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (isRegistered) {
        response = await userService.addToCart(user.username, token, storeId, productId, quantity);
      } else {
        response = await userService.addToCartGuest(guestCart, storeId, productId, quantity);
      }
      setCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add item to cart');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isRegistered, user, token]);

  // Update cart item
  const updateCartItem = useCallback(async (storeId, productId, newQuantity, guestCart = null) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (isRegistered) {
        response = await userService.updateCart(user.username, token, storeId, productId, newQuantity);
      } else {
        response = await userService.updateCartGuest(guestCart, storeId, productId, newQuantity);
      }
      setCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update cart item');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isRegistered, user, token]);

  // Remove from cart
  const removeFromCart = useCallback(async (storeId, productId, guestCart = null) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (isRegistered) {
        response = await userService.removeFromCart(user.username, token, storeId, productId);
      } else {
        response = await userService.removeFromCartGuest(guestCart, storeId, productId);
      }
      setCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item from cart');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isRegistered, user, token]);

  // Checkout
  const checkout = useCallback(async (paymentMethod, guestCart = null) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (isRegistered) {
        response = await userService.checkout(user.username, token, paymentMethod);
      } else {
        response = await userService.checkoutGuest(guestCart, paymentMethod);
      }
      setCart(null); // Clear cart after checkout
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isRegistered, user, token]);

  return {
    cart,
    loading,
    error,
    fetchCart,
    addToCart,
    updateCartItem,
    removeFromCart,
    checkout
  };
};