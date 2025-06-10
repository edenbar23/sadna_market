import React, { useState } from "react";
import "../index.css";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function LoginBanner({ onClose, onLoginWithCart, onLogin }) {
  const { login, loading, error } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!username.trim() || !password.trim()) {
      return; // Don't proceed if fields are empty
    }

    try {
      await login(username, password);
      onClose(); // Close banner on success
      navigate("/"); // Refresh the current page to show logged-in state
    } catch (err) {
      console.error("Login failed:", err);
    }
  }

  const handleLoginClick = async () => {
    if (!username.trim() || !password.trim()) return;

    try {
      if (onLoginWithCart) {
        // use fallback to local login if needed
        await onLoginWithCart(username, password, async (u, p) => {
          await login(u, p);
        });
      } else if (onLogin) {
        await onLogin(username, password);
      } else {
        await login(username, password);
      }

      onClose(); // Close the banner
      navigate("/"); // Go home or refresh
    } catch (err) {
      console.error("Login failed:", err);
      // Error is already handled by `useAuth`, no need to set local error state
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleLoginClick();
    }
  };

  return (
      <div className="login-overlay">
        <div className="login-banner">
          <h2>Login</h2>
          <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyDown={handleKeyDown}
          />
          <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={handleKeyDown}
          />
          <button
              className="login-btn"
              onClick={handleLoginClick}
              disabled={loading}
          >
            {loading ? "Logging in..." : "Submit"}
          </button>
          {error && <div className="error">{error}</div>}
          <button className="close-btn" onClick={onClose}>✖</button>
        </div>
      </div>
  );
}
