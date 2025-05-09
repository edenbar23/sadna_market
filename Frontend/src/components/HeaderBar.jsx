import React from "react";
import '../index.css';
import logo from "../assets/logo.png"; // Adjust the path as necessary
import UserProfileBadge from "./UserProfileBadge";

function HeaderBar({ user, onLogout }) {
    return (
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
            <button className="button">Cart</button>
            <button className="button">Messages</button>
            <button className="button">Orders</button>
            <button className="button" onClick={onLogout}>Logout</button>
          </>
        ) : (
          <>
            <button className="button">Cart</button>
            <button className="button">Login</button>
            <button className="button">Register</button>
          </>
        )}
      </div>
    </header>
  );
}

export default HeaderBar;
