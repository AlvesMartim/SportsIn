import { createContext, useContext, useEffect, useState } from "react";
import { authAPI } from "../api/api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = sessionStorage.getItem("insport_token");
    const savedUser = sessionStorage.getItem("insport_user");

    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  async function login(identifier, password) {
    const response = await authAPI.login(identifier, password);

    if (!response.success) {
      throw new Error(response.error || "Erreur de connexion");
    }

    const { token: newToken, user: userData } = response;

    setToken(newToken);
    setUser(userData);

    sessionStorage.setItem("insport_token", newToken);
    sessionStorage.setItem("insport_user", JSON.stringify(userData));

    return userData;
  }

  async function register(pseudo, email, password) {
    const response = await authAPI.register(pseudo, email, password);

    if (!response.success) {
      throw new Error(response.error || "Erreur lors de l'inscription");
    }

    const { token: newToken, user: userData } = response;

    setToken(newToken);
    setUser(userData);

    sessionStorage.setItem("insport_token", newToken);
    sessionStorage.setItem("insport_user", JSON.stringify(userData));

    return userData;
  }

  function logout() {
    setUser(null);
    setToken(null);
    sessionStorage.removeItem("insport_token");
    sessionStorage.removeItem("insport_user");
    sessionStorage.removeItem("insport_team_id");
  }

  const value = {
    user,
    setUser,
    token,
    isAuthenticated: !!token,
    loading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {loading ? (
        <div style={{ color: "white", padding: 20 }}>Loading...</div>
      ) : (
        children
      )}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}