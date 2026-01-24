import { useState, useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { sessionAPI, gameAPI } from "../api/api.js";

function ActiveSessionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const gameId = searchParams.get("gameId");

  const [session, setSession] = useState(null);
  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [terminating, setTerminating] = useState(false);

  // Chronomètre
  const [elapsedTime, setElapsedTime] = useState(0);
  const timerRef = useRef(null);

  useEffect(() => {
    loadData();
    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, [gameId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (gameId) {
        // Charger le jeu et sa session
        const gameData = await gameAPI.getById(gameId);
        setGame(gameData);

        if (gameData.sessionId) {
          const sessionData = await sessionAPI.getById(gameData.sessionId);
          setSession(sessionData);
          startTimer(sessionData.createdAt);
        }
      } else {
        // Charger les sessions actives
        const activeSessions = await sessionAPI.getActive();
        if (activeSessions.length > 0) {
          setSession(activeSessions[0]);
          startTimer(activeSessions[0].createdAt);
        }
      }
    } catch (err) {
      setError("Erreur lors du chargement de la session");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const startTimer = (createdAt) => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
    }

    const startTime = createdAt ? new Date(createdAt).getTime() : Date.now();

    timerRef.current = setInterval(() => {
      const now = Date.now();
      const elapsed = Math.floor((now - startTime) / 1000);
      setElapsedTime(elapsed);
    }, 1000);
  };

  const formatTime = (seconds) => {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hrs > 0) {
      return `${hrs.toString().padStart(2, "0")}:${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
    }
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const handleTerminate = async () => {
    if (!session) return;

    try {
      setTerminating(true);
      await sessionAPI.terminate(session.id);

      // Si on a un gameId, compléter le jeu aussi
      if (gameId && game) {
        // Pour simplifier, on considère l'équipe créatrice comme gagnante
        // En vrai, il faudrait un système de scores
        const winnerId = game.creatorTeam?.id?.toString();
        if (winnerId) {
          await gameAPI.complete(gameId, winnerId);
        }
      }

      // Rediriger vers les résultats
      navigate(`/game/result/${session.id}`);
    } catch (err) {
      setError("Erreur lors de la terminaison de la session");
      console.error(err);
    } finally {
      setTerminating(false);
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
    justifyContent: "center",
    minHeight: "calc(100vh - 64px)",
  };

  const timerStyle = {
    fontSize: "72px",
    fontWeight: "bold",
    fontFamily: "monospace",
    marginBottom: "24px",
    color: "#4caf50",
    textShadow: "0 0 20px rgba(76, 175, 80, 0.5)",
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

  const terminateButtonStyle = {
    width: "100%",
    padding: "20px 24px",
    borderRadius: "12px",
    border: "none",
    cursor: "pointer",
    fontSize: "18px",
    fontWeight: "600",
    background: "linear-gradient(135deg, #e53935 0%, #c62828 100%)",
    color: "white",
    transition: "all 0.2s",
    marginTop: "20px",
  };

  const statusBadgeStyle = {
    display: "inline-block",
    padding: "8px 20px",
    borderRadius: "20px",
    fontSize: "14px",
    fontWeight: "600",
    background: "#4caf50",
    color: "white",
    marginBottom: "24px",
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

  if (!session) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <div style={cardStyle}>
            <h2>Aucune session active</h2>
            <p style={{ opacity: 0.7 }}>
              Créez un jeu pour démarrer une session.
            </p>
            <button
              style={{ ...terminateButtonStyle, background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)" }}
              onClick={() => navigate("/game/create")}
            >
              Créer un jeu
            </button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <span style={statusBadgeStyle}>EN COURS</span>

        <div style={timerStyle}>{formatTime(elapsedTime)}</div>

        {game && (
          <div style={cardStyle}>
            <div style={{ display: "flex", justifyContent: "space-around", alignItems: "center" }}>
              <div>
                <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>ÉQUIPE 1</p>
                <p style={{ margin: "8px 0 0", fontWeight: "600", fontSize: "18px" }}>
                  {game.creatorTeam?.nom || "Équipe A"}
                </p>
              </div>
              <div style={{ fontSize: "24px", fontWeight: "bold", color: "#ff9800" }}>VS</div>
              <div>
                <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>ÉQUIPE 2</p>
                <p style={{ margin: "8px 0 0", fontWeight: "600", fontSize: "18px" }}>
                  {game.opponentTeam?.nom || "Équipe B"}
                </p>
              </div>
            </div>
          </div>
        )}

        {session && (
          <div style={cardStyle}>
            <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>SESSION ID</p>
            <p style={{ margin: "8px 0 0", fontFamily: "monospace" }}>{session.id}</p>

            {session.sport && (
              <>
                <p style={{ margin: "16px 0 0", fontSize: "12px", opacity: 0.7 }}>SPORT</p>
                <p style={{ margin: "8px 0 0" }}>{session.sport.nom || session.sport.code || "N/A"}</p>
              </>
            )}

            {session.pointId && (
              <>
                <p style={{ margin: "16px 0 0", fontSize: "12px", opacity: 0.7 }}>ARÈNE</p>
                <p style={{ margin: "8px 0 0" }}>{session.pointId}</p>
              </>
            )}
          </div>
        )}

        {error && (
          <div style={{ ...cardStyle, borderColor: "#e53935" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
        )}

        <button
          style={terminateButtonStyle}
          onClick={handleTerminate}
          disabled={terminating}
        >
          {terminating ? "Terminaison..." : "Terminer la session"}
        </button>
      </main>
    </div>
  );
}

export default ActiveSessionPage;
