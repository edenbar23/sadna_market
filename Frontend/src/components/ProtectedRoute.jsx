import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, user, requireAdmin = false }) => {
  // Check if user is authenticated
  if (!user) {
    return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '50vh',
          textAlign: 'center',
          padding: '2rem'
        }}>
          <h2>Authentication Required</h2>
          <p>Please log in to access this page.</p>
        </div>
    );
  }

  // Check if admin access is required
  if (requireAdmin && !user.isAdmin) {
    return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '50vh',
          textAlign: 'center',
          padding: '2rem',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white',
          borderRadius: '15px',
          margin: '2rem',
          border: '1px solid rgba(255, 255, 255, 0.2)'
        }}>
          <div style={{
            fontSize: '4rem',
            marginBottom: '1rem'
          }}>
            🚫
          </div>
          <h2 style={{ marginBottom: '1rem', fontSize: '1.8rem' }}>
            Unauthorized Access
          </h2>
          <p style={{
            marginBottom: '2rem',
            opacity: 0.9,
            maxWidth: '400px',
            lineHeight: '1.5'
          }}>
            You need administrator privileges to access this page. Please contact your system administrator if you believe this is an error.
          </p>
          <button
              onClick={() => window.history.back()}
              style={{
                background: 'rgba(255, 255, 255, 0.2)',
                color: 'white',
                border: '1px solid rgba(255, 255, 255, 0.3)',
                padding: '0.75rem 2rem',
                borderRadius: '10px',
                cursor: 'pointer',
                fontSize: '1rem',
                transition: 'all 0.3s ease'
              }}
              onMouseOver={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.3)';
                e.target.style.transform = 'translateY(-2px)';
              }}
              onMouseOut={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                e.target.style.transform = 'translateY(0)';
              }}
          >
            ← Go Back
          </button>
        </div>
    );
  }

  return children;
};

export default ProtectedRoute;