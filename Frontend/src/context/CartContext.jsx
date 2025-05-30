import React, { createContext, useContext, useState, useEffect } from 'react';
import * as userService from '../api/user';

// Create the context
const CartContext = createContext();

// Create a provider component
export const CartProvider = ({ children }) => {
  const [guestCart, setGuestCart] = useState({ baskets: {} });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load cart from localStorage on initial render
  useEffect(() => {
    const savedCart = localStorage.getItem('guestCart');
    if (savedCart) {
      try {
        setGuestCart(JSON.parse(savedCart));
      } catch (err) {
        console.error('Error parsing cart from localStorage:', err);
        // If there's an error parsing, start with a fresh cart
        setGuestCart({ baskets: {} });
      }
    }
  }, []);

  // Save cart to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem('guestCart', JSON.stringify(guestCart));
  }, [guestCart]);

  // Add item to cart
  const addToCart = async (storeId, productId, quantity) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.addToCartGuest(guestCart, storeId, productId, quantity);
      setGuestCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add item to cart');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Update cart item
  const updateCartItem = async (storeId, productId, newQuantity) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.updateCartGuest(guestCart, storeId, productId, newQuantity);
      setGuestCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update cart item');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Remove from cart
  const removeFromCart = async (storeId, productId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.removeFromCartGuest(guestCart, storeId, productId);
      setGuestCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item from cart');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // View cart
  const fetchCart = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.viewCartGuest(guestCart);
      setGuestCart(response.data);
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch cart');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Checkout
  const checkout = async (paymentMethod) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.checkoutGuest(guestCart, paymentMethod);
      // Clear cart after successful checkout
      setGuestCart({ baskets: {} });
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Clear cart
  const clearCart = () => {
    setGuestCart({ baskets: {} });
  };

  return (
    <CartContext.Provider
      value={{
        guestCart,
        loading,
        error,
        addToCart,
        updateCartItem,
        removeFromCart,
        fetchCart,
        checkout,
        clearCart
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

// Custom hook to use the cart context
export const useCartContext = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCartContext must be used within a CartProvider');
  }
  return context;
};

export default CartContext;