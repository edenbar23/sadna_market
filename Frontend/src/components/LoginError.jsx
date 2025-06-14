import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../index.css';

export default function LoginError() {
  const navigate = useNavigate();

  return (
    <div className="login-error-container">
      <div className="login-error-content">
        <h1>Login Failed</h1>
        <p>Invalid username or password. Please try again.</p>
        <button 
          className="return-btn"
          onClick={() => navigate('/')}
        >
          Return to Login
        </button>
      </div>
    </div>
  );
} 