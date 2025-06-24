import React from 'react';
import '../index.css';

export default function ErrorAlert({ message, onClose }) {
  if (!message) return null;
  return (
    <div className="login-error-box" style={{ margin: '16px 0' }}>
      <div className="error-icon">⚠️</div>
      <div className="error-message" style={{ flex: 1 }}>{message}</div>
      {onClose && (
        <button
          className="notification-close"
          onClick={onClose}
          aria-label="Close error message"
          style={{ background: 'none', border: 'none', color: 'white', fontSize: 20, marginLeft: 8, cursor: 'pointer' }}
        >
          ×
        </button>
      )}
    </div>
  );
} 