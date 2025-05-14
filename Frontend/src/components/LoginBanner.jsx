import React from "react";
import "../index.css";

export default function LoginBanner({ onClose }) {
  return (
    <div className="login-overlay">
      <div className="login-banner">
        <h2>Login</h2>
        <input type="text" placeholder="Username" />
        <input type="password" placeholder="Password" />
        <button className="login-btn">Submit</button>
        <button className="close-btn" onClick={onClose}>✖</button>
      </div>
    </div>
  );
}
