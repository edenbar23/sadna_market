import React, { useState } from "react";
import "../index.css";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function LoginBanner({ onClose }) {
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
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleLogin();
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
              onClick={handleLogin}
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