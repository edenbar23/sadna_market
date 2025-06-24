import React, { useState } from "react";
import "../index.css";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import ErrorAlert from "./ErrorAlert";

export default function LoginBanner({ onClose, onLoginWithCart, onLogin }) {
  const { login, loading } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLoginClick = async () => {
    setError("");
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password");
      return;
    }
    let response;
    try {
      if (onLoginWithCart) {
        response = await onLoginWithCart(username, password, async (u, p) => {
          return await login(u, p);
        });
      } else if (onLogin) {
        response = await onLogin(username, password);
      } else {
        response = await login(username, password);
      }
    } catch (err) {
      console.log("[LoginBanner] Login error object:", err);
      let backendError =
        (typeof err === 'string' && err) ||
        err?.error ||
        err?.response?.data?.error ||
        err?.message;
      console.log("[LoginBanner] Extracted backendError from error:", backendError);
      if (!backendError) {
        backendError = "Invalid username or password. Please try again.";
      }
      setError(backendError);
      return;
    }
    // Check for error in response (even if no error was thrown)
    console.log("[LoginBanner] Login response object:", response);
    let backendError = response?.error || response?.data?.error;
    if (backendError) {
      console.log("[LoginBanner] Extracted backendError from response:", backendError);
      setError(backendError);
      return;
    }
    // Only close and navigate if login succeeded
    console.log("[LoginBanner] Login succeeded, closing modal and navigating home", response);
    onClose();
    navigate("/");
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
          <ErrorAlert message={error} onClose={() => setError("")} />
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
