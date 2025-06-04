import React from "react";
import { Navigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";

function ProtectedRoute({ children, requireAdmin = false }) {
    const { user, isAuthenticated, loading } = useAuthContext();

    // Show loading indicator while auth state is being determined
    if (loading) {
        return <div>Loading...</div>;
    }

    // Redirect to home if not authenticated
    if (!isAuthenticated) {
        return <Navigate to="/" />;
    }

    // Check admin requirement if specified
    if (requireAdmin && user.role !== "admin") {
        return <Navigate to="/" />;
    }

    return children;
}

export default ProtectedRoute;