import Header from "../components/Header.jsx";
import { useAuth } from "../context/AuthContext.jsx";

function TeamPage() {
  const { user } = useAuth();

  return (
    <div
      style={{
        width: "100vw",
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        backgroundColor: "#111",
        color: "white",
      }}
    >
      <Header />

      <main style={{ padding: "32px" }}>
        <h1>Mon équipe</h1>
        <p style={{ opacity: 0.8, marginBottom: "16px" }}>
          Cette page pourra afficher plus tard :
        </p>
        <ul>
          <li>Nom de l’équipe du joueur</li>
          <li>Couleur de l’équipe</li>
          <li>Points contrôlés</li>
          <li>Membres, classement, etc.</li>
        </ul>

        {!user && (
          <p style={{ marginTop: "20px", color: "#f87171" }}>
            (Aucun utilisateur connecté pour l’instant.)
          </p>
        )}
      </main>
    </div>
  );
}

export default TeamPage;