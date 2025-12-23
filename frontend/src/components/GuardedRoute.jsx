import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function GuardedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return null; // ou un spinner si tu veux
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default GuardedRoute;