// src/pages/LoginPage.jsx
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

async function handleSubmit(e) {
  e.preventDefault();
  await login(email, password); 
  navigate("/home");         
}

  return (
    <div style={{ padding: "40px" }}>
      <h1>Connexion</h1>
      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "10px", maxWidth: "300px" }}>
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

        <button type="submit">Se connecter</button>
      </form>

      <p style={{ marginTop: "10px" }}>
        Pas de compte ? <Link to="/register">Créer un compte</Link>
      </p>
    </div>
  );
}

export default LoginPage;