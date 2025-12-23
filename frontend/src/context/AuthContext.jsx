import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem("insport_token");
    const savedUser = localStorage.getItem("insport_user");

    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  async function login(email, password) {
    const fakeToken = "FAKE_TOKEN";
    const fakeUser = { id: 1, email, username: "Player1" };

    setToken(fakeToken);
    setUser(fakeUser);

    localStorage.setItem("insport_token", fakeToken);
    localStorage.setItem("insport_user", JSON.stringify(fakeUser));
  }

  function logout() {
    setUser(null);
    setToken(null);
    localStorage.removeItem("insport_token");
    localStorage.removeItem("insport_user");
  }

  const value = {
    user,
    token,
    isAuthenticated: !!token,
    loading,
    login,
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