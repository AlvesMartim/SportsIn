import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { authAPI } from "../api/api.js";

function ProfilePage() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();

  // Formulaire pseudo
  const [newPseudo, setNewPseudo] = useState("");
  const [savingPseudo, setSavingPseudo] = useState(false);
  const [pseudoSuccess, setPseudoSuccess] = useState("");
  const [pseudoError, setPseudoError] = useState("");

  // Formulaire mot de passe
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [passwordError, setPasswordError] = useState("");

  const handleUpdatePseudo = async (e) => {
    e.preventDefault();
    if (!newPseudo.trim()) return;
    setSavingPseudo(true);
    setPseudoError("");
    setPseudoSuccess("");
    try {
      const updated = await authAPI.updateProfile(user.id, { pseudo: newPseudo.trim() });
      const updatedUser = { ...user, pseudo: updated.pseudo };
      sessionStorage.setItem("insport_user", JSON.stringify(updatedUser));
      if (setUser) setUser(updatedUser);
      setPseudoSuccess("Pseudo mis à jour avec succès !");
      setNewPseudo("");
    } catch (err) {
      setPseudoError(err.message || "Erreur lors de la mise à jour du pseudo");
    } finally {
      setSavingPseudo(false);
    }
  };

  const handleUpdatePassword = async (e) => {
    e.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");
    if (newPassword.length < 6) {
      setPasswordError("Le mot de passe doit contenir au moins 6 caractères");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError("Les mots de passe ne correspondent pas");
      return;
    }
    setSavingPassword(true);
    try {
      await authAPI.updateProfile(user.id, { password: newPassword });
      setPasswordSuccess("Mot de passe mis à jour avec succès !");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setPasswordError(err.message || "Erreur lors de la mise à jour du mot de passe");
    } finally {
      setSavingPassword(false);
    }
  };

  const containerStyle = {
    width: "100vw",
    minHeight: "calc(100vh - 64px)",
    backgroundColor: "#111",
    color: "white",
  };

  const mainStyle = {
    padding: "32px",
    maxWidth: "560px",
    margin: "0 auto",
  };

  const cardStyle = {
    background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "20px",
    border: "1px solid #333",
  };

  const inputStyle = {
    width: "100%",
    padding: "12px 14px",
    borderRadius: "10px",
    border: "1px solid #444",
    background: "#0d0d1a",
    color: "white",
    fontSize: "15px",
    outline: "none",
    boxSizing: "border-box",
  };

  const btnPrimary = {
    padding: "12px 24px",
    borderRadius: "10px",
    border: "none",
    background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
    color: "white",
    fontWeight: "600",
    fontSize: "15px",
    cursor: "pointer",
  };

  const labelStyle = {
    display: "block",
    marginBottom: "6px",
    fontSize: "13px",
    opacity: 0.7,
    fontWeight: 600,
    textTransform: "uppercase",
    letterSpacing: "0.05em",
  };

  const successStyle = { color: "#4caf50", fontSize: "14px", marginTop: "10px" };
  const errorStyle = { color: "#e53935", fontSize: "14px", marginTop: "10px" };

  if (!user) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <p style={{ opacity: 0.7 }}>Aucun utilisateur connecté.</p>
        </main>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <button
          onClick={() => navigate("/")}
          style={{ background: "transparent", border: "1px solid #333", color: "white", padding: "8px 16px", borderRadius: "8px", cursor: "pointer", marginBottom: "24px" }}
        >
          ← Retour
        </button>

        <h1 style={{ marginBottom: "24px" }}>Mon profil</h1>

        {/* Infos actuelles */}
        <div style={cardStyle}>
          <h3 style={{ marginTop: 0, marginBottom: "16px", color: "#90caf9" }}>Informations</h3>
          <p style={{ margin: "0 0 8px" }}><span style={{ opacity: 0.6 }}>Pseudo :</span> <strong>{user.pseudo}</strong></p>
          <p style={{ margin: 0 }}><span style={{ opacity: 0.6 }}>Email :</span> <strong>{user.email}</strong></p>
        </div>

        {/* Changer le pseudo */}
        <div style={cardStyle}>
          <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Changer le pseudo</h3>
          <form onSubmit={handleUpdatePseudo}>
            <label style={labelStyle}>Nouveau pseudo</label>
            <input
              style={inputStyle}
              type="text"
              placeholder={user.pseudo}
              value={newPseudo}
              onChange={(e) => setNewPseudo(e.target.value)}
              required
              minLength={2}
            />
            {pseudoError && <p style={errorStyle}>{pseudoError}</p>}
            {pseudoSuccess && <p style={successStyle}>{pseudoSuccess}</p>}
            <button type="submit" style={{ ...btnPrimary, marginTop: "14px" }} disabled={savingPseudo || !newPseudo.trim()}>
              {savingPseudo ? "Enregistrement..." : "Mettre à jour le pseudo"}
            </button>
          </form>
        </div>

        {/* Changer le mot de passe */}
        <div style={cardStyle}>
          <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Changer le mot de passe</h3>
          <form onSubmit={handleUpdatePassword}>
            <div style={{ marginBottom: "14px" }}>
              <label style={labelStyle}>Nouveau mot de passe</label>
              <div style={{ position: "relative" }}>
                <input
                  style={{ ...inputStyle, paddingRight: "44px" }}
                  type={showNewPassword ? "text" : "password"}
                  placeholder="••••••••"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  minLength={6}
                />
                <button
                  type="button"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  style={{ position: "absolute", right: "12px", top: "50%", transform: "translateY(-50%)", background: "none", border: "none", color: "#aaa", cursor: "pointer", fontSize: "18px", lineHeight: 1 }}
                >
                  {showNewPassword ? "🙈" : "👁"}
                </button>
              </div>
            </div>

            <div>
              <label style={labelStyle}>Confirmer le mot de passe</label>
              <input
                style={inputStyle}
                type={showNewPassword ? "text" : "password"}
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                minLength={6}
              />
            </div>

            {passwordError && <p style={errorStyle}>{passwordError}</p>}
            {passwordSuccess && <p style={successStyle}>{passwordSuccess}</p>}

            <button type="submit" style={{ ...btnPrimary, marginTop: "14px" }} disabled={savingPassword || !newPassword || !confirmPassword}>
              {savingPassword ? "Enregistrement..." : "Mettre à jour le mot de passe"}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default ProfilePage;
