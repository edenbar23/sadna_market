import { useState, useCallback } from 'react';
import * as userService from '../api/user';
import { useAuthContext } from '../context/AuthContext';
import { useCartContext } from '../context/CartContext.jsx';

export const useCart = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [cart, setCart] = useState(null);
  const { user, token } = useAuthContext();
  const { 
    guestCart, 
    addToCart: addToGuestCart, 
    updateCartItem: updateGuestCartItem,
    removeFromCart: removeGuestCartItem,
    fetchCart: fetchGuestCart,
    checkout: checkoutGuest,
    loading: guestLoading,
    error: guestError
  } = useCartContext();

  // Helper to determine if we're dealing with registered user or guest
  const isRegistered = !!user;

  // View cart
  const fetchCart = useCallback(async () => {
    if (isRegistered) {
      setLoading(true);
      setError(null);
      try {
        const response = await userService.viewCart(user.username, token);
        setCart(response.data);
        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to fetch cart');
        throw err;
      } finally {
        setLoading(false);
      }
    } else {
      try {
        const data = await fetchGuestCart();
        setCart(data);
        return data;
      } catch (err) {
        throw err;
      }
    }
  }, [isRegistered, user, token, fetchGuestCart]);

  // Add to cart
  const addToCart = useCallback(async (storeId, productId, quantity) => {
    if (isRegistered) {
      setLoading(true);
      setError(null);
      try {
        const response = await userService.addToCart(user.username, token, storeId, productId, quantity);
        setCart(response.data);
        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to add item to cart');
        throw err;
      } finally {
        setLoading(false);
      }
    } else {
      try {
        const data = await addToGuestCart(storeId, productId, quantity);
        setCart(data);
        return data;
      } catch (err) {
        throw err;
      }
    }
  }, [isRegistered, user, token, addToGuestCart]);

  // Update cart item
  const updateCartItem = useCallback(async (storeId, productId, newQuantity) => {
    if (isRegistered) {
      setLoading(true);
      setError(null);
      try {
        const response = await userService.updateCart(user.username, token, storeId, productId, newQuantity);
        setCart(response.data);
        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to update cart item');
        throw err;
      } finally {
        setLoading(false);
      }
    } else {
      try {
        const data = await updateGuestCartItem(storeId, productId, newQuantity);
        setCart(data);
        return data;
      } catch (err) {
        throw err;
      }
    }
  }, [isRegistered, user, token, updateGuestCartItem]);

  // Remove from cart
  const removeFromCart = useCallback(async (storeId, productId) => {
    if (isRegistered) {
      setLoading(true);
      setError(null);
      try {
        const response = await userService.removeFromCart(user.username, token, storeId, productId);
        setCart(response.data);
        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to remove item from cart');
        throw err;
      } finally {
        setLoading(false);
      }
    } else {
      try {
        const data = await removeGuestCartItem(storeId, productId);
        setCart(data);
        return data;
      } catch (err) {
        throw err;
      }
    }
  }, [isRegistered, user, token, removeGuestCartItem]);

  // Checkout
  const checkout = useCallback(async (paymentMethod) => {
    if (isRegistered) {
      setLoading(true);
      setError(null);
      try {
        const response = await userService.checkout(user.username, token, paymentMethod);
        setCart(null); // Clear cart after checkout
        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || 'Checkout failed');
        throw err;
      } finally {
        setLoading(false);
      }
    } else {
      try {
        const data = await checkoutGuest(paymentMethod);
        setCart(null); // Clear cart after checkout
        return data;
      } catch (err) {
        throw err;
      }
    }
  }, [isRegistered, user, token, checkoutGuest]);

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
