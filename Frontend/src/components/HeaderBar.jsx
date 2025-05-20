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

function HeaderBar() {
  const { user, isAuthenticated, logout } = useAuthContext();
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [userStores, setUserStores] = useState([]);
  const [storesLoading, setStoresLoading] = useState(false);
  const navigate = useNavigate();

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
        // Clear stores when user logs out
        setUserStores([]);
      }
    };

    loadUserStores();
  }, [user]);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
      <>
        <header className="header">
          {/* Left Side */}
          <UserProfileBadge user={user} />

          {/* Admin Controls - only for admin users */}
          {user?.role === "admin" && <AdminControls />}

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

          {/* Center: Logo */}
          <div className="logo-container">
            <Link to="/">
              <img src={logo} alt="Market Logo" className="logo" />
            </Link>
          </div>

          {/* Right Side Buttons */}
          <div className="space-x-3">
            {isAuthenticated ? (
                <>
                  <Link to="/cart">
                    <button className="button">Cart</button>
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
                  <Link to="/cart">
                    <button className="button">Cart</button>
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