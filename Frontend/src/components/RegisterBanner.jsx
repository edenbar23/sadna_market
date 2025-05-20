import React, { useState } from "react";
import { useAuth } from "../hooks/useAuth"; // Use the same hook
import "../index.css";

export default function RegisterBanner({ onClose, onRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");

  const { register, loading, error } = useAuth();

  const handleSubmit = async () => {
    const userData = {
      username,
      password,
      email,
      firstName,
      lastName
    };

    try {
      const response = await register(userData);
      if (response) {
        onRegister?.(response); // Optional callback from parent
        onClose(); // Close the banner
      }
    } catch (err) {
      // error is handled by useAuth, so no need to rethrow
      console.error("Registration failed:", err);
    }
  };

  return (
    <div className="login-overlay">
      <div className="login-banner">
        <h2>Register</h2>
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
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          type="text"
          placeholder="First Name"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Last Name"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
        />

        {error && <p className="error-msg">{error}</p>}
        <button className="login-btn" onClick={handleSubmit} disabled={loading}>
          {loading ? "Registering..." : "Submit"}
        </button>
        <button className="close-btn" onClick={onClose}>✖</button>
      </div>
    </div>
  );
}
