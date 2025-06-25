import React, { useState } from "react";
import { useAuth } from "../hooks/useAuth"; // Use the same hook
import "../index.css";
import ErrorAlert from "./ErrorAlert";

export default function RegisterBanner({ onClose, onRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

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
      console.error("Registration failed:", err);
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
      setErrorMsg(backendError || "Registration failed. Please try again.");
    }
  };

  return (
    <div className="login-overlay">
      <div className="login-banner">
        <h2>Register</h2>
        {errorMsg && <ErrorAlert message={errorMsg} onClose={() => setErrorMsg("")} />}
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

        <button className="login-btn" onClick={handleSubmit} disabled={loading}>
          {loading ? "Registering..." : "Submit"}
        </button>
        <button className="close-btn" onClick={onClose}>✖</button>
      </div>
    </div>
  );
}
