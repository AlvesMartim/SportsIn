import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "../styles/auth.css";

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login(identifier, password);
      navigate("/");
    } catch (err) {
      setError("Identifiant ou mot de passe incorrect");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-container">
        {/* Main form */}
        <div className="auth-main">
          <div className="auth-header">
            <div className="auth-logo">⚡</div>
            <h1 className="auth-title">Bon retour !</h1>
            <p className="auth-subtitle">Connecte-toi pour rejoindre l'arène</p>
          </div>

          <div className="auth-card">
            <form className="auth-form" onSubmit={handleSubmit}>
              {error && (
                <div className="auth-error">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  {error}
                </div>
              )}

              <div className="auth-input-group">
                <label className="auth-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                  Email ou Pseudo
                </label>
                <input
                  type="text"
                  className="auth-input"
                  placeholder="ton@email.com ou monPseudo"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  required
                  autoComplete="username"
                  autoFocus
                />
              </div>

              <div className="auth-input-group">
                <label className="auth-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0110 0v4" />
                  </svg>
                  Mot de passe
                </label>
                <div className="auth-password-wrapper">
                  <input
                    type={showPassword ? "text" : "password"}
                    className="auth-input"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    className="auth-password-toggle"
                    onClick={() => setShowPassword(!showPassword)}
                    tabIndex={-1}
                  >
                    {showPassword ? (
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24" />
                        <line x1="1" y1="1" x2="23" y2="23" />
                      </svg>
                    ) : (
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                className={`auth-submit ${loading ? "auth-submit--loading" : ""}`}
                disabled={loading}
              >
                {loading ? (
                  <>
                    <div className="spinner" style={{ width: 20, height: 20 }} />
                    Connexion...
                  </>
                ) : (
                  <>
                    Se connecter
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="5" y1="12" x2="19" y2="12" />
                      <polyline points="12 5 19 12 12 19" />
                    </svg>
                  </>
                )}
              </button>
            </form>

            <p className="auth-footer">
              Pas encore de compte ? <Link to="/register">Créer un compte</Link>
            </p>
          </div>
        </div>

        {/* Features section (desktop only) */}
        <div className="auth-features">
          <h2>Rejoins la compétition sportive</h2>
          <p>Affronte d'autres équipes, conquiers des territoires et deviens le champion de ta ville !</p>

          <ul className="auth-feature-list">
            <li className="auth-feature-item">
              <div className="auth-feature-icon">⚔️</div>
              <div className="auth-feature-text">
                <h3>Défis en temps réel</h3>
                <p>Lance des matchs et affronte des équipes adverses sur le terrain</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🗺️</div>
              <div className="auth-feature-text">
                <h3>Carte interactive</h3>
                <p>Découvre les arènes autour de toi et conquiers des territoires</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🏆</div>
              <div className="auth-feature-text">
                <h3>Classements & récompenses</h3>
                <p>Grimpe dans les classements et débloque des succès</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">👥</div>
              <div className="auth-feature-text">
                <h3>Esprit d'équipe</h3>
                <p>Crée ou rejoins une équipe et jouez ensemble</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
