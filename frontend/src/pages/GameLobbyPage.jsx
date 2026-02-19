import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { gameAPI } from "../api/api.js";

function GameLobbyPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();

  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [starting, setStarting] = useState(false);

  // Pour rejoindre en tant qu'adversaire
  const [joining, setJoining] = useState(false);

  // Timer et timeout pour l'attente
  const [waitTime, setWaitTime] = useState(0);
  const [cancelling, setCancelling] = useState(false);
  const TIMEOUT_SECONDS = 120; // 2 minutes

  const loadGame = useCallback(async () => {
    try {
      const gameData = await gameAPI.getById(gameId);
      setGame(gameData);

      // Si le jeu est en cours, rediriger vers la session active
      if (gameData.state === "IN_PROGRESS" && gameData.sessionId) {
        navigate(`/session/active?gameId=${gameId}`);
      }

      // Si le jeu est terminé, rediriger vers les résultats
      if (gameData.state === "COMPLETED" && gameData.sessionId) {
        navigate(`/game/result/${gameData.sessionId}`);
      }
    } catch (err) {
      setError("Jeu non trouvé");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [gameId, navigate]);

  useEffect(() => {
    loadGame();

    // Polling toutes les 3 secondes pour vérifier si un adversaire a rejoint
    const interval = setInterval(loadGame, 3000);
    return () => clearInterval(interval);
  }, [loadGame]);

  // Timer pour afficher le temps d'attente
  useEffect(() => {
    if (game?.state !== "WAITING") return;

    const timer = setInterval(() => {
      setWaitTime((prev) => prev + 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [game?.state]);

  // Annuler la recherche et supprimer le game
  const handleCancelSearch = async () => {
    try {
      setCancelling(true);
      await gameAPI.delete(gameId);
      navigate("/");
    } catch (err) {
      setError("Erreur lors de l'annulation");
      console.error(err);
    } finally {
      setCancelling(false);
    }
  };


  const handleJoinGame = async (teamId) => {
    try {
      setJoining(true);
      setError(null);
      await gameAPI.join(gameId, teamId);
      await loadGame();
    } catch (err) {
      setError("Erreur lors de la jonction au jeu");
      console.error(err);
    } finally {
      setJoining(false);
    }
  };

  const handleStartGame = async () => {
    try {
      setStarting(true);
      setError(null);
      const updatedGame = await gameAPI.start(gameId);

      if (updatedGame.sessionId) {
        navigate(`/session/active?gameId=${gameId}`);
      }
    } catch (err) {
      setError("Erreur lors du démarrage du jeu");
      console.error(err);
    } finally {
      setStarting(false);
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
  };

  const cardStyle = {
    background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "20px",
    border: "1px solid #333",
  };

  const teamCardStyle = {
    background: "#1a1a2e",
    borderRadius: "12px",
    padding: "20px",
    display: "flex",
    alignItems: "center",
    gap: "16px",
    border: "1px solid #333",
  };

  const vsStyle = {
    fontSize: "24px",
    fontWeight: "bold",
    color: "#ff9800",
    textAlign: "center",
    margin: "16px 0",
  };

  const buttonStyle = {
    width: "100%",
    padding: "16px 24px",
    borderRadius: "12px",
    border: "none",
    cursor: "pointer",
    fontSize: "16px",
    fontWeight: "600",
    background: "linear-gradient(135deg, #4caf50 0%, #388e3c 100%)",
    color: "white",
    transition: "all 0.2s",
  };

  const waitingStyle = {
    textAlign: "center",
    padding: "40px 20px",
  };

  const pulseStyle = {
    width: "60px",
    height: "60px",
    borderRadius: "50%",
    background: "#1e88e5",
    margin: "0 auto 20px",
    animation: "pulse 2s infinite",
  };

  const backButtonStyle = {
    padding: "8px 16px",
    borderRadius: "8px",
    border: "1px solid #333",
    background: "transparent",
    color: "white",
    cursor: "pointer",
    marginBottom: "20px",
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <p style={{ textAlign: "center", opacity: 0.7 }}>Chargement...</p>
        </main>
      </div>
    );
  }

  if (error && !game) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <button style={backButtonStyle} onClick={() => navigate("/")}>
            ← Retour
          </button>
          <div style={{ ...cardStyle, borderColor: "#e53935" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
        </main>
      </div>
    );
  }

  const isWaiting = game?.state === "WAITING";
  const isMatched = game?.state === "MATCHED";
  const playerTeamId = sessionStorage.getItem("insport_team_id");
  const isCreator = game?.creatorTeam?.id?.toString() === playerTeamId;

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <button style={backButtonStyle} onClick={() => navigate("/")}>
          ← Retour
        </button>

        <h1 style={{ marginBottom: "8px" }}>Lobby du jeu</h1>
        <p style={{ opacity: 0.7, marginBottom: "24px" }}>
          {isWaiting ? "En attente d'un adversaire..." : "Adversaire trouvé !"}
        </p>

        {error && (
          <div style={{ ...cardStyle, borderColor: "#e53935", marginBottom: "20px" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
        )}

        {/* Statut du jeu */}
        <div style={cardStyle}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
            <span style={{ fontSize: "12px", opacity: 0.7 }}>STATUT</span>
            <span style={{
              padding: "4px 12px",
              borderRadius: "20px",
              fontSize: "12px",
              fontWeight: "600",
              background: isMatched ? "#4caf50" : "#ff9800",
              color: "white",
            }}>
              {isWaiting ? "EN ATTENTE" : isMatched ? "PRÊT" : game?.state}
            </span>
          </div>

          {/* Équipe créatrice */}
          <div style={teamCardStyle}>
            <div style={{
              width: "50px",
              height: "50px",
              borderRadius: "10px",
              background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: "20px",
              fontWeight: "bold",
            }}>
              {game?.creatorTeam?.nom?.charAt(0).toUpperCase() || "?"}
            </div>
            <div>
              <p style={{ margin: 0, fontWeight: "600", fontSize: "18px" }}>
                {game?.creatorTeam?.nom || "Équipe créatrice"}
              </p>
              <p style={{ margin: "4px 0 0", fontSize: "12px", opacity: 0.7 }}>Créateur</p>
            </div>
          </div>

          <div style={vsStyle}>VS</div>

          {/* Équipe adverse ou attente */}
          {game?.opponentTeam ? (
            <div style={teamCardStyle}>
              <div style={{
                width: "50px",
                height: "50px",
                borderRadius: "10px",
                background: "linear-gradient(135deg, #e53935 0%, #c62828 100%)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "20px",
                fontWeight: "bold",
              }}>
                {game.opponentTeam.nom?.charAt(0).toUpperCase() || "?"}
              </div>
              <div>
                <p style={{ margin: 0, fontWeight: "600", fontSize: "18px" }}>
                  {game.opponentTeam.nom}
                </p>
                <p style={{ margin: "4px 0 0", fontSize: "12px", opacity: 0.7 }}>Adversaire</p>
              </div>
            </div>
          ) : (
            <div style={waitingStyle}>
              <div style={pulseStyle}></div>
              <p style={{ opacity: 0.7, marginBottom: "8px" }}>
                Recherche d'une équipe adverse...
              </p>
              <p style={{
                fontSize: "14px",
                opacity: 0.5,
                fontFamily: "monospace"
              }}>
                ⏱️ {Math.floor(waitTime / 60)}:{(waitTime % 60).toString().padStart(2, "0")}
              </p>
              {waitTime >= TIMEOUT_SECONDS && (
                <p style={{
                  color: "#ff9800",
                  fontSize: "14px",
                  marginTop: "12px"
                }}>
                  La recherche prend plus de temps que prévu...
                </p>
              )}
            </div>
          )}
        </div>

        {/* Actions */}
        {isWaiting && isCreator && (
          <button
            style={{
              ...buttonStyle,
              background: "linear-gradient(135deg, #757575 0%, #616161 100%)",
              marginBottom: "12px"
            }}
            onClick={handleCancelSearch}
            disabled={cancelling}
          >
            {cancelling ? "Annulation..." : "❌ Annuler la recherche"}
          </button>
        )}

        {isMatched && isCreator && (
          <button
            style={buttonStyle}
            onClick={handleStartGame}
            disabled={starting}
          >
            {starting ? "Démarrage..." : "🎮 Démarrer le match !"}
          </button>
        )}

        {isMatched && !isCreator && (
          <div style={{ ...cardStyle, textAlign: "center" }}>
            <p style={{ margin: 0, opacity: 0.7 }}>
              En attente que le créateur démarre le match...
            </p>
          </div>
        )}

        {/* Option pour rejoindre si on n'est pas le créateur et qu'il n'y a pas d'adversaire */}
        {isWaiting && !isCreator && playerTeamId && (
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Rejoindre ce jeu</h3>
            <button
              style={{ ...buttonStyle, background: "linear-gradient(135deg, #e53935 0%, #c62828 100%)" }}
              onClick={() => handleJoinGame(parseInt(playerTeamId))}
              disabled={joining}
            >
              {joining ? "En cours..." : "Rejoindre en tant qu'adversaire"}
            </button>
          </div>
        )}

        {/* Info sur la partie */}
        <div style={{ ...cardStyle, marginTop: "20px" }}>
          {game?.sport?.code && (
            <div style={{ marginBottom: "16px" }}>
              <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>SPORT</p>
              <p style={{ margin: "8px 0 0", fontWeight: "600", fontSize: "18px" }}>
                🏆 {game.sport.code}
              </p>
            </div>
          )}
          {game?.pointId && (
            <div>
              <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>ARÈNE</p>
              <p style={{ margin: "8px 0 0", fontWeight: "600" }}>
                📍 {game.pointId}
              </p>
            </div>
          )}
        </div>
      </main>

      <style>{`
        @keyframes pulse {
          0% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.5; transform: scale(1.1); }
          100% { opacity: 1; transform: scale(1); }
        }
      `}</style>
    </div>
  );
}

export default GameLobbyPage;
