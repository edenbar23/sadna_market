
import { Navigate } from "react-router-dom";

function ProtectedRoute({ user, children, requireAdmin = false }) {
  if (!user) {
    return <Navigate to="/" />;
  }

  if (requireAdmin && user.type !== "admin") {
    return <Navigate to="/" />;
  }

  return children;
}

export default ProtectedRoute;
