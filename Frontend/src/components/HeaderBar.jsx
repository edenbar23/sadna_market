import React, { useState, useEffect } from "react";
import "../index.css";
import logo from "../assets/logo.png";
import UserProfileBadge from "./UserProfileBadge";
import { Link, useNavigate } from "react-router-dom";
import LoginBanner from "./LoginBanner";
import RegisterBanner from "./RegisterBanner";
import { fetchUserStores } from "../api/user";
import AdminControls from "./AdminControls";
import { useAuthContext } from "../context/AuthContext";
import { useCartContext } from "../context/CartContext";
import { useCart } from "../hooks/useCart";
import CreateStoreModal from "@/components/CreateStoreModal.jsx";
import {useStoreOperations} from "@/hooks/index.js";

function HeaderBar() {
  const { user, isAuthenticated, logout } = useAuthContext();
  const { guestCart } = useCartContext();
  const { cart, fetchCart } = useCart();
  const { handleCreateStore } = useStoreOperations(user);
  const [storeCreated, setStoreCreated] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [userStores, setUserStores] = useState([]);
  const [storesLoading, setStoresLoading] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const navigate = useNavigate();

  const handleCreateStoreClick = () => setShowCreateModal(true);
  const handleCloseModal = () => setShowCreateModal(false);

  useEffect(() => {
    if (isAuthenticated && user) {
      fetchCart().catch(console.error);
    }
  }, [isAuthenticated, user, fetchCart, refreshTrigger]);

  useEffect(() => {
    const handleCartUpdate = () => setRefreshTrigger((prev) => prev + 1);
    window.addEventListener("cartUpdated", handleCartUpdate);
    return () => window.removeEventListener("cartUpdated", handleCartUpdate);
  }, []);

  useEffect(() => {
    const loadUserStores = async () => {
      if (user?.username && user?.token) {
        setStoresLoading(true);
        try {
          const res = await fetchUserStores(user.username, user.token);
          setUserStores(res?.data || []);
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

  const getCartItemCount = () => {
    if (isAuthenticated && cart) {
      return cart.totalItems || 0;
    } else if (!isAuthenticated && guestCart?.baskets) {
      return Object.values(guestCart.baskets).reduce((acc, storeBasket) => {
        return (
            acc +
            Object.values(storeBasket).reduce(
                (sum, quantity) => sum + quantity,
                0
            )
        );
      }, 0);
    }
    return 0;
  };

  const cartItemCount = getCartItemCount();
  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
      <>
        <header className="header">
          {/* Left: Profile */}
          <div className="header-left">
            <UserProfileBadge user={user} />
          </div>

          {/* Admin Controls */}
          {user?.isAdmin && (
              <div className="admin-section">
                <AdminControls />
              </div>
          )}

          {/* Store Management */}
          <div className="store-section">
            {isAuthenticated &&
                (!storesLoading ? (
                    userStores.length > 0 ? (
                        <Link to="/my-stores">
                          <button className="button store-button">🏪 My Stores</button>
                        </Link>
                    ) : (
                        <button
                            className="button store-button"
                            onClick={handleCreateStoreClick}
                        >
                          ➕ Create Store
                        </button>
                    )
                ) : (
                    <span>Loading stores...</span>
                ))}
            {showCreateModal && (
                <CreateStoreModal
                    onSuccess={() => {
                      setStoreCreated(true);
                      // Reload stores after creation
                      if (user?.username && user?.token) {
                      setStoresLoading(true);
                      fetchUserStores(user.username, user.token)
                      .then((res) => setUserStores(res?.data || []))
                      .catch(() => setUserStores([]))
                      .finally(() => setStoresLoading(false));
                      }
                    }
                }
                    handleCreateStore={handleCreateStore} onClose={handleCloseModal}
                    isLoading={false} />
            )}
          </div>

          {/* Center: Logo */}
          <div className="logo-container">
            <Link to="/">
              <img src={logo} alt="Market Logo" className="logo" />
            </Link>
          </div>

          {/* Right: Actions */}
          <div className="header-right">
            <Link to="/cart" className="cart-link">
              <button className="button cart-button stylish-button">
                🛒 Cart
                {cartItemCount > 0 && (
                    <span className="cart-badge" style={badgeStyle}>
                  {cartItemCount}
                </span>
                )}
              </button>
            </Link>

            {isAuthenticated ? (
                <>
                  <Link to="/messages">
                    <button className="button stylish-button">💬 Messages</button>
                  </Link>
                  <Link to="/orders">
                    <button className="button stylish-button">📦 Orders</button>
                  </Link>
                  <button
                      className="button stylish-button logout-button"
                      onClick={handleLogout}
                  >
                    🚪 Logout
                  </button>
                </>
            ) : (
                <>
                  <button
                      className="button stylish-button"
                      onClick={() => setShowLogin(true)}
                  >
                    🔑 Login
                  </button>
                  <button
                      className="button stylish-button"
                      onClick={() => setShowRegister(true)}
                  >
                    📝 Register
                  </button>
                </>
            )}
          </div>
        </header>

        {/* Banners */}
        {showLogin && <LoginBanner onClose={() => setShowLogin(false)} />}
        {showRegister && (
            <RegisterBanner
                onClose={() => setShowRegister(false)}
                onRegister={() => setShowRegister(false)}
                onLogin={() => {
                  setShowRegister(false);
                  setShowLogin(true);
                }}
            />
        )}
      </>
  );
}

const badgeStyle = {
  position: "absolute",
  top: "-8px",
  right: "-8px",
  backgroundColor: "#dc3545",
  color: "white",
  fontSize: "0.75rem",
  fontWeight: "bold",
  minWidth: "20px",
  height: "20px",
  borderRadius: "50%",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "0 4px",
  boxShadow: "0 2px 4px rgba(0, 0, 0, 0.2)",
  zIndex: 1,
};

export default HeaderBar;
