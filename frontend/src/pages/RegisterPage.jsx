import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button.jsx";

function RegisterPage() {
  const navigate = useNavigate();
  const [pseudo, setPseudo] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");

  function handleSubmit(e) {
    e.preventDefault();
    if (password !== confirm) {
      alert("Les mots de passe ne correspondent pas");
      return;
    }
    // plus tard : appel API /auth/register
    console.log("Inscription fake :", { pseudo, email });

    navigate("/login");
  }

  return (
    <div style={{ padding: "40px" }}>
      <h1>Créer un compte</h1>
      <form
        onSubmit={handleSubmit}
        style={{ display: "flex", flexDirection: "column", gap: "10px", maxWidth: "300px" }}
      >
        <input
          type="text"
          placeholder="Pseudo"
          value={pseudo}
          onChange={(e) => setPseudo(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Mot de passe"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Confirmer le mot de passe"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          required
        />

        <button type="submit">S'inscrire</button>
      </form>

      <p style={{ marginTop: "10px" }}>
        Déjà un compte ? <Link to="/login">Connexion</Link>
      </p>
    </div>
  );
}

export default RegisterPage;