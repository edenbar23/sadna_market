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
    // Clear previous error
    setError("");

    // Validate inputs
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password");
      return;
    }

    try {
      let response;
      if (onLoginWithCart) {
        response = await onLoginWithCart(username, password, async (u, p) => {
          return await login(u, p);
        });
      } else if (onLogin) {
        response = await onLogin(username, password);
      } else {
        response = await login(username, password);
      }
      // If the response has an error, show it and do not close/navigate
      if (response && (response.error || (response.data && response.data.error))) {
        const backendError = response.error || (response.data && response.data.error);
        setError(backendError || "Invalid username or password. Please try again.");
        return;
      }
      // Only close and navigate if login succeeded
      console.log("[LoginBanner] Login succeeded, closing modal and navigating home", response);
      onClose();
      navigate("/");
    } catch (err) {
      console.log("Login error:", err);
      let backendError = "";
      if (err) {
        if (typeof err === "string") {
          backendError = err;
        } else if (typeof err.errorMessage === "string") {
          backendError = err.errorMessage;
        } else if (typeof err.error === "string") {
          backendError = err.error;
        } else if (err.response && err.response.data && typeof err.response.data.errorMessage === "string") {
          backendError = err.response.data.errorMessage;
        } else if (err.response && err.response.data && typeof err.response.data.error === "string") {
          backendError = err.response.data.error;
        } else if (err.message) {
          backendError = err.message;
        } else {
          backendError = JSON.stringify(err);
        }
      }
      setError(backendError || "Invalid username or password. Please try again.");
      // Do NOT close or navigate on error
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
