import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const isActive = (path) => location.pathname === path;

  const navLinks = [
    { path: "/", label: "Accueil", icon: "🏠" },
    { path: "/map", label: "Carte", icon: "🗺️" },
    { path: "/missions", label: "Missions", icon: "🎯" },
    { path: "/team", label: "Équipe", icon: "👥" },
    { path: "/progression", label: "Progression", icon: "📈" },
    { path: "/strava", label: "Strava", icon: "🏃" },
    { path: "/zones", label: "Zones IDF", icon: "🗾" },
    { path: "/history", label: "Historique", icon: "📜" },
  ];

  return (
    <header className="navbar">
      {/* Logo / titre */}
      <Link to="/" className="navbar-left" style={{ textDecoration: "none" }}>
        <div className="nav-logo">⚡</div>
        <span className="nav-title">SportsIn</span>
      </Link>

      {/* Navigation centrale */}
      <nav className="nav-links">
        {navLinks.map((link) => (
          <Link
            key={link.path}
            to={link.path}
            className={`nav-link ${isActive(link.path) ? "active" : ""}`}
          >
            <span style={{ marginRight: "6px" }}>{link.icon}</span>
            {link.label}
          </Link>
        ))}
      </nav>

      {/* User section */}
      <div className="nav-user">
        {user && (
          <span className="nav-email">
            {user.username || user.email}
          </span>
        )}
        <button className="nav-logout" onClick={handleLogout}>
          Déconnexion
        </button>
      </div>
    </header>
  );
}

export default Header;
