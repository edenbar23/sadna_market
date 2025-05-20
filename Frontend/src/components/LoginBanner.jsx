import React, { useState } from "react";
import "../index.css";
import {useAuth} from "../hooks/useAuth";

export default function LoginBanner({ onClose }) {
  const { login, loading, error } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    try {
      await login(username, password);
      onClose(); // Close banner on success
    } catch (err) {
      // Error is already set in `useAuth`, so no need to rethrow unless needed
      console.error("Login failed:", err);
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
        />
        <input
          type="password"
          placeholder="Password"
          value={password} 
          onChange={(e) => setPassword(e.target.value)} 
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
