import React from 'react';
import { Navigate } from 'react-router-dom';

/**
 * ProtectedRoute component that handles authentication and authorization
 * @param {Object} props
 * @param {Object} props.user - Current user object
 * @param {boolean} props.requireAdmin - Whether admin privileges are required
 * @param {React.ReactNode} props.children - Child components to render if authorized
 */
export default function ProtectedRoute({ user, requireAdmin = false, children }) {
    // Check if user is authenticated
    if (!user) {
        console.log('ProtectedRoute: User not authenticated, redirecting to home');
        return <Navigate to="/" replace />;
    }

    // Check if admin privileges are required
    if (requireAdmin && !user.isAdmin) {
        console.log('ProtectedRoute: Admin access required but user is not admin');
        return (
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100vh',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                textAlign: 'center',
                padding: '2rem'
            }}>
                <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>🚫 Access Denied</h1>
                <p style={{ fontSize: '1.2rem', marginBottom: '2rem', opacity: 0.9 }}>
                    You need administrator privileges to access this page.
                </p>
                <button
                    onClick={() => window.history.back()}
                    style={{
                        background: 'rgba(255, 255, 255, 0.2)',
                        color: 'white',
                        padding: '0.75rem 2rem',
                        borderRadius: '10px',
                        border: '1px solid rgba(255, 255, 255, 0.3)',
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
                    Go Back
                </button>
            </div>
        );
    }

    console.log('ProtectedRoute: User authorized, rendering children');
    return children;
}