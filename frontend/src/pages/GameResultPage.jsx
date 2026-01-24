import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { sessionAPI, equipeAPI } from "../api/api.js";

function GameResultPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const [session, setSession] = useState(null);
  const [winnerTeam, setWinnerTeam] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, [sessionId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const sessionData = await sessionAPI.getById(sessionId);
      setSession(sessionData);

      // Charger l'équipe gagnante si disponible
      if (sessionData.winnerParticipantId) {
        try {
          const team = await equipeAPI.getById(sessionData.winnerParticipantId);
          setWinnerTeam(team);
        } catch (e) {
          console.error("Équipe gagnante non trouvée");
        }
      }
    } catch (err) {
      setError("Session non trouvée");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Styles
  const containerStyle = {
    width: "100vw",
    minHeight: "100vh",
    display: "flex",
    flexDirection: "column",
    backgroundColor: "#111",
    color: "white",
  };

  const mainStyle = {
    padding: "32px",
    maxWidth: "600px",
    margin: "0 auto",
    width: "100%",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  };

  const cardStyle = {
    background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "20px",
    border: "1px solid #333",
    width: "100%",
    textAlign: "center",
  };

  const winnerCardStyle = {
    ...cardStyle,
    background: "linear-gradient(135deg, #1b5e20 0%, #2e7d32 100%)",
    border: "2px solid #4caf50",
  };

  const trophyStyle = {
    fontSize: "80px",
    marginBottom: "16px",
  };

  const buttonStyle = {
    padding: "16px 32px",
    borderRadius: "12px",
    border: "none",
    cursor: "pointer",
    fontSize: "16px",
    fontWeight: "600",
    background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
    color: "white",
    transition: "all 0.2s",
    marginTop: "20px",
  };

  const secondaryButtonStyle = {
    ...buttonStyle,
    background: "transparent",
    border: "1px solid #1e88e5",
    color: "#1e88e5",
    marginLeft: "12px",
  };

  const statsRowStyle = {
    display: "flex",
    justifyContent: "space-between",
    padding: "12px 0",
    borderBottom: "1px solid #333",
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <p style={{ opacity: 0.7 }}>Chargement...</p>
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <div style={{ ...cardStyle, borderColor: "#e53935" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
          <button style={buttonStyle} onClick={() => navigate("/")}>
            Retour à l'accueil
          </button>
        </main>
      </div>
    );
  }

  const participants = session?.participants || [];
  const hasWinner = session?.winnerParticipantId || winnerTeam;

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <h1 style={{ marginBottom: "8px" }}>Résultats du match</h1>
        <p style={{ opacity: 0.7, marginBottom: "32px" }}>
          Session terminée
        </p>

        {/* Gagnant */}
        {hasWinner ? (
          <div style={winnerCardStyle}>
            <div style={trophyStyle}>🏆</div>
            <p style={{ margin: 0, fontSize: "14px", opacity: 0.8 }}>VAINQUEUR</p>
            <h2 style={{ margin: "8px 0 0", fontSize: "28px" }}>
              {winnerTeam?.nom || `Équipe ${session.winnerParticipantId}`}
            </h2>
          </div>
        ) : (
          <div style={cardStyle}>
            <p style={{ margin: 0, fontSize: "18px" }}>Match nul ou pas de vainqueur</p>
          </div>
        )}

        {/* Participants */}
        {participants.length > 0 && (
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Participants</h3>
            {participants.map((participant, index) => (
              <div key={participant.id || index} style={{
                ...statsRowStyle,
                borderBottom: index < participants.length - 1 ? "1px solid #333" : "none",
              }}>
                <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                  <div style={{
                    width: "36px",
                    height: "36px",
                    borderRadius: "8px",
                    background: participant.id === session?.winnerParticipantId
                      ? "linear-gradient(135deg, #4caf50 0%, #388e3c 100%)"
                      : "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "bold",
                  }}>
                    {participant.name?.charAt(0).toUpperCase() || "?"}
                  </div>
                  <span style={{ fontWeight: "600" }}>{participant.name}</span>
                </div>
                {participant.id === session?.winnerParticipantId && (
                  <span style={{ color: "#4caf50", fontWeight: "600" }}>Gagnant</span>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Détails de la session */}
        <div style={cardStyle}>
          <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Détails</h3>

          <div style={statsRowStyle}>
            <span style={{ opacity: 0.7 }}>Session ID</span>
            <span style={{ fontFamily: "monospace" }}>{session?.id}</span>
          </div>

          {session?.sport && (
            <div style={statsRowStyle}>
              <span style={{ opacity: 0.7 }}>Sport</span>
              <span>{session.sport.nom || session.sport.code || "N/A"}</span>
            </div>
          )}

          {session?.pointId && (
            <div style={statsRowStyle}>
              <span style={{ opacity: 0.7 }}>Arène</span>
              <span>{session.pointId}</span>
            </div>
          )}

          <div style={statsRowStyle}>
            <span style={{ opacity: 0.7 }}>Statut</span>
            <span style={{
              padding: "4px 12px",
              borderRadius: "12px",
              fontSize: "12px",
              background: session?.state === "TERMINATED" ? "#4caf50" : "#ff9800",
            }}>
              {session?.state || "N/A"}
            </span>
          </div>

          {session?.createdAt && (
            <div style={statsRowStyle}>
              <span style={{ opacity: 0.7 }}>Début</span>
              <span>{new Date(session.createdAt).toLocaleString("fr-FR")}</span>
            </div>
          )}

          {session?.endedAt && (
            <div style={{ ...statsRowStyle, borderBottom: "none" }}>
              <span style={{ opacity: 0.7 }}>Fin</span>
              <span>{new Date(session.endedAt).toLocaleString("fr-FR")}</span>
            </div>
          )}
        </div>

        {/* Actions */}
        <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center" }}>
          <button style={buttonStyle} onClick={() => navigate("/")}>
            Retour à l'accueil
          </button>
          <button style={secondaryButtonStyle} onClick={() => navigate("/game/create")}>
            Nouveau jeu
          </button>
        </div>
      </main>
    </div>
  );
}

export default GameResultPage;
