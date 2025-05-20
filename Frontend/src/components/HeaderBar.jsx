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




function HeaderBar({ user, onLogout, onLogin }) {
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [userStores, setUserStores] = useState([]);
  const navigate = useNavigate();
  useEffect(() => {
    if (user) {
      fetchUserStores(user.username).then(setUserStores).catch(console.error);
    }
  }, [user]);

  return (
    <>
      <header className="header">
        {/* Left Side */}
        <UserProfileBadge user={user} />
        {/* Admin Controls - only for admin users */}
        {user?.role === "admin" && <AdminControls />}
        
        {user && (
            userStores.length > 0 ? (
              <Link to="/my-stores">
                <button className="button">Stores</button>
              </Link>
            ) : (
              <Link to="/create-store">
                <button className="button">Create Store</button>
              </Link>
            )
          )}

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
              <button className="button" onClick={() => {
    onLogout();        // Clear user session
    navigate("/");     // Redirect to homepage
  }}>Logout</button>
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
    onRegister={(userData) => {
      // Make sure onLogin is passed from the parent of HeaderBar
      setShowRegister(false);
    }
    onLogin={() => setShowLogin(true);}

  }
  />
)}

    </>
  );
}

export default HeaderBar;
