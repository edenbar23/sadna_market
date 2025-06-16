import React, { useState } from "react";
import "../index.css";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function LoginBanner({ onClose, onLoginWithCart, onLogin }) {
  const { login, loading } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLoginClick = async () => {
    // Clear previous error
    setError("");

    // Validate inputs
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password");
      return;
    }

    try {
      if (onLoginWithCart) {
        await onLoginWithCart(username, password, async (u, p) => {
          await login(u, p);
        });
      } else if (onLogin) {
        await onLogin(username, password);
      } else {
        await login(username, password);
      }
      onClose();
      navigate("/");
    } catch (err) {
      console.log("Login error:", err);
      setError("Invalid username or password. Please try again.");
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
        <button className="close-btn" onClick={onClose}>✖</button>
        <h2>Welcome Back!</h2>
        {error && (
          <div className="login-error-box">
            <div className="error-icon">⚠️</div>
            <div className="error-message">{error}</div>
          </div>
        )}
        <div className="input-group">
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              setError(""); // Clear error when user types
            }}
            onKeyDown={handleKeyDown}
            className={error ? "input-error" : ""}
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setError(""); // Clear error when user types
            }}
            onKeyDown={handleKeyDown}
            className={error ? "input-error" : ""}
          />
        </div>
        <button
          className="login-btn"
          onClick={handleLoginClick}
          disabled={loading}
        >
          {loading ? "Logging in..." : "Sign In"}
        </button>
      </div>
    </div>
  );
}
