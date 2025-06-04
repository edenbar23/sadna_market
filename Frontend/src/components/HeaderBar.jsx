import React, { useState, useEffect } from "react";
import "../index.css";
import logo from "../assets/logo.png";
import UserProfileBadge from "./UserProfileBadge";
import { Link } from "react-router-dom";
import LoginBanner from "./LoginBanner";
import RegisterBanner from "./RegisterBanner";
import { fetchUserStores } from "../api/user";
import AdminControls from "./AdminControls";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from '../context/AuthContext';
import { useCartContext } from '../context/CartContext';
import { useCart } from '../hooks/useCart';

function HeaderBar() {
  const { user, isAuthenticated, logout } = useAuthContext();
  const { guestCart } = useCartContext();
  const { cart, fetchCart } = useCart();
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [userStores, setUserStores] = useState([]);
  const [storesLoading, setStoresLoading] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const navigate = useNavigate();

  // Debug: Log all cart-related data
  console.log('=== CART DEBUG INFO ===');
  console.log('isAuthenticated:', isAuthenticated);
  console.log('user:', user);
  console.log('guestCart:', guestCart);
  console.log('cart (from useCart):', cart);
  console.log('refreshTrigger:', refreshTrigger);
  console.log('========================');

  // Fetch cart data when component mounts or refreshTrigger changes
  useEffect(() => {
    if (isAuthenticated && user) {
      console.log('Fetching cart for authenticated user...');
      fetchCart().then(cartData => {
        console.log('Fetched cart data:', cartData);
      }).catch(err => {
        console.error('Error fetching cart:', err);
      });
    }
  }, [isAuthenticated, user, fetchCart, refreshTrigger]);

  // Add event listener for cart updates
  useEffect(() => {
    const handleCartUpdate = () => {
      console.log("Cart update event received - forcing re-render");
      setRefreshTrigger(prev => prev + 1);
    };

    window.addEventListener('cartUpdated', handleCartUpdate);
    return () => {
      window.removeEventListener('cartUpdated', handleCartUpdate);
    };
  }, []);

  useEffect(() => {
    const loadUserStores = async () => {
      if (user && user.username && user.token) {
        setStoresLoading(true);
        try {
          const storesResponse = await fetchUserStores(user.username, user.token);
          if (storesResponse && storesResponse.data) {
            setUserStores(storesResponse.data);
            console.log("User stores loaded:", storesResponse.data);
          } else {
            setUserStores([]);
          }
        } catch (err) {
          console.error("Failed to fetch user stores:", err);
          setUserStores([]);
        } finally {
          setStoresLoading(false);
        }
      } else {
        setUserStores([]);
      }
    };

    loadUserStores();
  }, [user]);

  // Calculate total cart items with detailed logging
  const getCartItemCount = () => {
    console.log('Calculating cart item count...');

    if (isAuthenticated && cart) {
      console.log('Using authenticated user cart:', cart);
      const count = cart.totalItems || 0;
      console.log('Authenticated cart count:', count);
      return count;
    } else if (!isAuthenticated && guestCart) {
      console.log('Using guest cart:', guestCart);
      let totalItems = 0;
      if (guestCart && guestCart.baskets) {
        Object.entries(guestCart.baskets).forEach(([storeId, storeBasket]) => {
          console.log(`Store ${storeId}:`, storeBasket);
          Object.entries(storeBasket).forEach(([productId, quantity]) => {
            console.log(`  Product ${productId}: ${quantity}`);
            totalItems += quantity;
          });
        });
      }
      console.log('Guest cart count:', totalItems);
      return totalItems;
    } else {
      console.log('No cart data available');
      return 0;
    }
  };

  const cartItemCount = getCartItemCount();
  console.log('Final cart item count:', cartItemCount);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
      <>
        <header className="header">
          {/* Left Side */}
          <div className="header-left">
            <UserProfileBadge user={user} />

            {isAuthenticated && (
                !storesLoading ? (
                    userStores && userStores.length > 0 ? (
                        <Link to="/my-stores">
                          <button className="button">My Stores</button>
                        </Link>
                    ) : (
                        <Link to="/create-store">
                          <button className="button">Create Store</button>
                        </Link>
                    )
                ) : (
                    <span>Loading stores...</span>
                )
            )}
          </div>

          {/* Center: Logo */}
          <div className="logo-container">
            <Link to="/">
              <img src={logo} alt="Market Logo" className="logo" />
            </Link>
          </div>

          {/* Admin Controls - Between Logo and Right Side */}
          {user?.isAdmin && (
              <div className="header-admin">
                <AdminControls />
              </div>
          )}

          {/* Right Side Buttons */}
          <div className="header-right">
            {isAuthenticated ? (
                <>
                  <Link to="/cart" className="cart-link">
                    <button className="button cart-button">
                      Cart
                      {cartItemCount > 0 && (
                          <span className="cart-badge" style={{
                            position: 'absolute',
                            top: '-8px',
                            right: '-8px',
                            backgroundColor: '#dc3545',
                            color: 'white',
                            fontSize: '0.75rem',
                            fontWeight: 'bold',
                            minWidth: '20px',
                            height: '20px',
                            borderRadius: '50%',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            padding: '0 4px',
                            boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
                            zIndex: 1
                          }}>
                          {cartItemCount}
                        </span>
                      )}
                    </button>
                  </Link>
                  <Link to="/messages">
                    <button className="button">Messages</button>
                  </Link>
                  <Link to="/orders">
                    <button className="button">Orders</button>
                  </Link>
                  <button className="button" onClick={handleLogout}>Logout</button>
                </>
            ) : (
                <>
                  <Link to="/cart" className="cart-link">
                    <button className="button cart-button">
                      Cart
                      {cartItemCount > 0 && (
                          <span className="cart-badge" style={{
                            position: 'absolute',
                            top: '-8px',
                            right: '-8px',
                            backgroundColor: '#dc3545',
                            color: 'white',
                            fontSize: '0.75rem',
                            fontWeight: 'bold',
                            minWidth: '20px',
                            height: '20px',
                            borderRadius: '50%',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            padding: '0 4px',
                            boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
                            zIndex: 1
                          }}>
                          {cartItemCount}
                        </span>
                      )}
                    </button>
                  </Link>
                  <button className="button" onClick={() => setShowLogin(true)}>Login</button>
                  <button className="button" onClick={() => setShowRegister(true)}>Register</button>
                </>
            )}
          </div>
        </header>

        {showLogin && <LoginBanner onClose={() => setShowLogin(false)} />}
        {showRegister && (
            <RegisterBanner
                onClose={() => setShowRegister(false)}
                onRegister={() => {
                  setShowRegister(false);
                }}
                onLogin={() => {
                  setShowRegister(false);
                  setShowLogin(true);
                }}
            />
        )}
      </>
  );
}

export default HeaderBar;