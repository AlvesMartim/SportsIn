import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { equipeAPI, sessionAPI } from "../api/api.js";
import Header from "../components/Header.jsx";
import Button from "../components/Button.jsx";
import "../styles/home.css";

export default function HomePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [team, setTeam] = useState(null);
  const [stats, setStats] = useState({ sessions: 0, victories: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      // Charger l'équipe de l'utilisateur
      const teamId = localStorage.getItem("insport_team_id");
      if (teamId) {
        const teamData = await equipeAPI.getById(teamId);
        setTeam(teamData);
      }

      // Charger les statistiques
      try {
        const sessions = await sessionAPI.getAll();
        const userSessions = Array.isArray(sessions) ? sessions : [];
        const victories = userSessions.filter(s => s.winnerParticipantId === teamId).length;
        setStats({
          sessions: userSessions.length,
          victories: victories,
        });
      } catch (e) {
        // API peut ne pas être disponible
      }
    } catch (err) {
      console.error("Erreur chargement données:", err);
    } finally {
      setLoading(false);
    }
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Bonjour";
    if (hour < 18) return "Bon après-midi";
    return "Bonsoir";
  };

  return (
    <div className="home-container">
      <Header />

      <main className="home-content">
        {/* Hero Section */}
        <div className="home-hero">
          <h1 className="home-title">
            {getGreeting()}{user ? `, ${user.username}` : ""} !
          </h1>
          <p className="home-subtitle">Prêt pour un nouveau défi sportif ?</p>
        </div>

        {/* Team Badge ou Warning */}
        {team ? (
          <div className="home-team-badge">
            <div
              className="home-team-badge__icon"
              style={{ backgroundColor: team.couleur || "#3b82f6" }}
            >
              👥
            </div>
            <div className="home-team-badge__info">
              <div className="home-team-badge__name">{team.nom}</div>
              <div className="home-team-badge__role">Membre de l'équipe</div>
            </div>
          </div>
        ) : !loading && (
          <div className="home-no-team" onClick={() => navigate("/team")}>
            <span className="home-no-team__icon">⚠️</span>
            <div className="home-no-team__text">
              <div className="home-no-team__title">Rejoins une équipe !</div>
              <div className="home-no-team__desc">Tu dois être dans une équipe pour jouer</div>
            </div>
            <span style={{ color: "var(--gray-500)" }}>→</span>
          </div>
        )}

        {/* Stats */}
        {(stats.sessions > 0 || stats.victories > 0) && (
          <div className="home-stats">
            <div className="home-stat">
              <span className="home-stat__value">{stats.sessions}</span>
              <span className="home-stat__label">Sessions</span>
            </div>
            <div className="home-stat">
              <span className="home-stat__value">{stats.victories}</span>
              <span className="home-stat__label">Victoires</span>
            </div>
            <div className="home-stat">
              <span className="home-stat__value">
                {stats.sessions > 0 ? Math.round((stats.victories / stats.sessions) * 100) : 0}%
              </span>
              <span className="home-stat__label">Win Rate</span>
            </div>
          </div>
        )}

        {/* Menu Principal */}
        <div className="home-menu stagger">
          <Button
            icon="⚔️"
            buttonTitle="Créer un jeu"
            description="Lance un défi et affronte une équipe adverse"
            goTo="/game/create"
            variant="success"
          />

          <Button
            icon="🗺️"
            buttonTitle="Explorer la carte"
            description="Découvre les arènes, zones et routes"
            goTo="/map"
          />

          <Button
            icon="👥"
            buttonTitle="Mon équipe"
            description="Gère ton équipe et tes coéquipiers"
            goTo="/team"
          />

          <Button
            icon="📜"
            buttonTitle="Historique"
            description="Consulte tes sessions passées"
            goTo="/history"
          />

          <Button
            icon="👤"
            buttonTitle="Mon profil"
            description="Modifie tes informations personnelles"
            goTo="/profile"
          />
        </div>

        {/* Quick Actions */}
        <div className="home-quick-actions">
          <button className="quick-action-btn" onClick={() => navigate("/map")}>
            📍 Arènes proches
          </button>
          <button className="quick-action-btn" onClick={() => navigate("/game/create")}>
            ⚡ Match rapide
          </button>
        </div>
      </main>
    </div>
  );
}
