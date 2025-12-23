import { useAuth } from "../context/AuthContext.jsx";
import Button from "../components/Button.jsx";
import "../styles/home.css";

export default function HomePage() {
  const { user } = useAuth();

  return (
    <div className="home-container">
      <main className="home-content">
        <h1 className="home-title">
          Bienvenue{user ? `, ${user.username}` : ""} 👋
        </h1>

        <p className="home-subtitle">Que veux-tu faire ?</p>

        <div className="home-menu">
          <Button
            icon="🗺️"
            buttonTitle="Voir la carte"
            description="Points, zones, routes…"
            goTo="/map"
          />

          <Button
            icon="👥"
            buttonTitle="Mon équipe"
            description="Nom, couleur, points contrôlés…"
            goTo="/team"
          />

          <Button
            icon="👤"
            buttonTitle="Mon profil"
            description="Email, pseudo, etc."
            goTo="/profile"
          />
        </div>
      </main>
    </div>
  );
}