import React, { useState } from "react";
import "../index.css";
import logo from "../assets/logo.png";
import UserProfileBadge from "./UserProfileBadge";
import { Link } from "react-router-dom";
import LoginBanner from "./LoginBanner"; // Make sure the path is correct

function HeaderBar({ user, onLogout }) {
  const [showLogin, setShowLogin] = useState(false);

  return (
    <>
      <header className="header">
        {/* Left Side */}
        <UserProfileBadge user={user} />

        {/* Center: Logo */}
        <div className="logo-container">
          <img src={logo} alt="Market Logo" className="logo" />
        </div>

        {/* Right Side Buttons */}
        <div className="space-x-3">
          {user ? (
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
              <button className="button" onClick={onLogout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/cart">
                <button className="button">Cart</button>
              </Link>
              <button className="button" onClick={() => setShowLogin(true)}>Login</button>
              <button className="button">Register</button>
            </>
          )}
        </div>
      </header>

      {showLogin && <LoginBanner onClose={() => setShowLogin(false)} />}
    </>
  );
}

export default HeaderBar;
