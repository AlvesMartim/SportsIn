import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header
      style={{
        height: "56px",
        padding: "0 20px",
        backgroundColor: "#111",
        color: "white",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        boxShadow: "0 2px 4px rgba(0,0,0,0.3)",
        zIndex: 2000,
      }}
    >
      {/* Logo / titre */}
      <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
        <span
          style={{
            width: 24,
            height: 24,
            borderRadius: "50%",
            background:
              "linear-gradient(135deg, #e53935 0%, #1e88e5 100%)",
          }}
        />
        <span style={{ fontWeight: 700, letterSpacing: "0.03em" }}>
          InSport
        </span>
      </div>

      {/* Liens + user */}
      <nav
        style={{
          display: "flex",
          alignItems: "center",
          gap: "16px",
          fontSize: "0.9rem",
        }}
      >
        <Link to="/map" style={{ color: "white" }}>
          Carte
        </Link>
        <Link to="/profile" style={{ color: "white" }}>
          Profil
        </Link>

        <span style={{ opacity: 0.8 }}>
          {user ? user.email : "Invité"}
        </span>

        <button
          onClick={handleLogout}
          style={{
            padding: "6px 12px",
            borderRadius: "999px",
            border: "1px solid #444",
            backgroundColor: "#222",
            color: "white",
            cursor: "pointer",
          }}
        >
          Déconnexion
        </button>
      </nav>
    </header>
  );
}

export default Header;