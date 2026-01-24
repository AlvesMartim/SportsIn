import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "../styles/auth.css";

function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [pseudo, setPseudo] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const getPasswordStrength = () => {
    if (!password) return 0;
    let strength = 0;
    if (password.length >= 6) strength++;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    return Math.min(strength, 4);
  };

  const passwordStrength = getPasswordStrength();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (password !== confirm) {
      setError("Les mots de passe ne correspondent pas");
      return;
    }

    if (password.length < 6) {
      setError("Le mot de passe doit contenir au moins 6 caractères");
      return;
    }

    setLoading(true);

    try {
      await register(pseudo, email, password);
      navigate("/");
    } catch (err) {
      setError(err.message || "Une erreur est survenue lors de l'inscription");
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
            <h1 className="auth-title">Créer un compte</h1>
            <p className="auth-subtitle">Rejoins des milliers de joueurs</p>
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
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                  Pseudo
                </label>
                <input
                  type="text"
                  className="auth-input"
                  placeholder="Ton pseudo de champion"
                  value={pseudo}
                  onChange={(e) => setPseudo(e.target.value)}
                  required
                  autoComplete="username"
                  autoFocus
                />
              </div>

              <div className="auth-input-group">
                <label className="auth-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                    <polyline points="22,6 12,13 2,6" />
                  </svg>
                  Email
                </label>
                <input
                  type="email"
                  className="auth-input"
                  placeholder="ton@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="email"
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
                    autoComplete="new-password"
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
                {password && (
                  <>
                    <div className="auth-password-strength">
                      {[1, 2, 3, 4].map((level) => (
                        <div
                          key={level}
                          className={`auth-password-strength-bar ${
                            passwordStrength >= level ? "active" : ""
                          } ${passwordStrength >= 3 ? "strong" : passwordStrength >= 2 ? "medium" : ""}`}
                        />
                      ))}
                    </div>
                    <p className="auth-password-hint">
                      {passwordStrength < 2
                        ? "Mot de passe faible"
                        : passwordStrength < 3
                        ? "Mot de passe moyen"
                        : "Mot de passe fort"}
                    </p>
                  </>
                )}
              </div>

              <div className="auth-input-group">
                <label className="auth-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                  Confirmer le mot de passe
                </label>
                <input
                  type={showPassword ? "text" : "password"}
                  className="auth-input"
                  placeholder="••••••••"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  required
                  autoComplete="new-password"
                  style={{
                    borderColor:
                      confirm && password !== confirm
                        ? "var(--danger-400)"
                        : confirm && password === confirm
                        ? "var(--success-400)"
                        : undefined,
                  }}
                />
              </div>

              <button
                type="submit"
                className={`auth-submit ${loading ? "auth-submit--loading" : ""}`}
                disabled={loading}
                style={{
                  background: "linear-gradient(135deg, var(--success-500) 0%, var(--success-600) 100%)",
                  boxShadow: "0 4px 20px rgba(34, 197, 94, 0.3)",
                }}
              >
                {loading ? (
                  <>
                    <div className="spinner" style={{ width: 20, height: 20 }} />
                    Création du compte...
                  </>
                ) : (
                  <>
                    Créer mon compte
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
                      <circle cx="8.5" cy="7" r="4" />
                      <line x1="20" y1="8" x2="20" y2="14" />
                      <line x1="23" y1="11" x2="17" y2="11" />
                    </svg>
                  </>
                )}
              </button>
            </form>

            <p className="auth-footer">
              Déjà un compte ? <Link to="/login">Se connecter</Link>
            </p>
          </div>
        </div>

        {/* Features section (desktop only) */}
        <div className="auth-features">
          <h2>Prêt à dominer le terrain ?</h2>
          <p>Crée ton compte en quelques secondes et commence à jouer immédiatement !</p>

          <ul className="auth-feature-list">
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🎯</div>
              <div className="auth-feature-text">
                <h3>Inscription rapide</h3>
                <p>Quelques clics et tu es prêt à jouer</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🔒</div>
              <div className="auth-feature-text">
                <h3>Données sécurisées</h3>
                <p>Tes informations sont protégées</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🎮</div>
              <div className="auth-feature-text">
                <h3>100% gratuit</h3>
                <p>Accès complet à toutes les fonctionnalités</p>
              </div>
            </li>
            <li className="auth-feature-item">
              <div className="auth-feature-icon">🌟</div>
              <div className="auth-feature-text">
                <h3>Communauté active</h3>
                <p>Des milliers de joueurs t'attendent</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
