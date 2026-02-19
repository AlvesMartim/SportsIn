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

  // Score form
  const [showScoreForm, setShowScoreForm] = useState(false);
  const [scoreA, setScoreA] = useState(0);
  const [scoreB, setScoreB] = useState(0);

  // Chronomètre + polling
  const [elapsedTime, setElapsedTime] = useState(0);
  const timerRef = useRef(null);
  const pollRef = useRef(null);

  useEffect(() => {
    loadData();
    // Polling toutes les 3s pour détecter si l'autre joueur a terminé le match
    pollRef.current = setInterval(checkGameState, 3000);
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [gameId]);

  const checkGameState = async () => {
    if (!gameId) return;
    try {
      const gameData = await gameAPI.getById(gameId);
      if (gameData.state === "COMPLETED" && gameData.sessionId) {
        if (pollRef.current) clearInterval(pollRef.current);
        navigate(`/game/result/${gameData.sessionId}`);
      }
    } catch (_) {}
  };

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      if (gameId) {
        const gameData = await gameAPI.getById(gameId);
        setGame(gameData);

        if (gameData.state === "COMPLETED" && gameData.sessionId) {
          navigate(`/game/result/${gameData.sessionId}`);
          return;
        }

        if (gameData.sessionId) {
          const sessionData = await sessionAPI.getById(gameData.sessionId);
          setSession(sessionData);
          startTimer(sessionData.createdAt);
        }
      } else {
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
    if (timerRef.current) clearInterval(timerRef.current);
    const startTime = createdAt ? new Date(createdAt).getTime() : Date.now();
    timerRef.current = setInterval(() => {
      setElapsedTime(Math.floor((Date.now() - startTime) / 1000));
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

  const handleTerminateWithScores = async () => {
    if (!session) return;

    try {
      setTerminating(true);
      setError(null);

      const teamAId = game?.creatorTeam?.id?.toString();
      const teamBId = game?.opponentTeam?.id?.toString();

      // 1. Mettre à jour la session avec les métriques de score
      const updatedSession = {
        ...session,
        result: {
          metrics: [
            ...(teamAId ? [{ participantId: teamAId, metricType: "GOALS", value: Number(scoreA), context: "match" }] : []),
            ...(teamBId ? [{ participantId: teamBId, metricType: "GOALS", value: Number(scoreB), context: "match" }] : []),
          ],
        },
      };
      await sessionAPI.update(session.id, updatedSession);

      // 2. Terminer la session (backend détermine le gagnant via les métriques)
      await sessionAPI.terminate(session.id);

      // 3. Toujours marquer le jeu comme complété (même en cas d'égalité)
      // pour que l'autre joueur soit redirigé via le polling
      if (gameId && game) {
        let winnerId = null;
        if (Number(scoreA) > Number(scoreB)) winnerId = teamAId;
        else if (Number(scoreB) > Number(scoreA)) winnerId = teamBId;
        await gameAPI.complete(gameId, winnerId);
      }

      if (pollRef.current) clearInterval(pollRef.current);
      navigate(`/game/result/${session.id}`);
    } catch (err) {
      setError("Erreur lors de la terminaison de la session");
      console.error(err);
    } finally {
      setTerminating(false);
    }
  };

  // Styles
  const containerStyle = { width: "100vw", minHeight: "100vh", display: "flex", flexDirection: "column", backgroundColor: "#111", color: "white" };
  const mainStyle = { padding: "32px", maxWidth: "600px", margin: "0 auto", width: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", minHeight: "calc(100vh - 64px)" };
  const timerStyle = { fontSize: "72px", fontWeight: "bold", fontFamily: "monospace", marginBottom: "24px", color: "#4caf50", textShadow: "0 0 20px rgba(76, 175, 80, 0.5)" };
  const cardStyle = { background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)", borderRadius: "16px", padding: "24px", marginBottom: "20px", border: "1px solid #333", width: "100%", textAlign: "center" };
  const buttonStyle = { width: "100%", padding: "20px 24px", borderRadius: "12px", border: "none", cursor: "pointer", fontSize: "18px", fontWeight: "600", color: "white", transition: "all 0.2s", marginTop: "12px" };
  const inputStyle = { width: "80px", padding: "12px", fontSize: "32px", fontWeight: "bold", textAlign: "center", background: "#0d0d1a", border: "2px solid #444", borderRadius: "10px", color: "white", outline: "none" };
  const statusBadgeStyle = { display: "inline-block", padding: "8px 20px", borderRadius: "20px", fontSize: "14px", fontWeight: "600", background: "#4caf50", color: "white", marginBottom: "24px" };

  if (loading) {
    return <div style={containerStyle}><main style={mainStyle}><p style={{ opacity: 0.7 }}>Chargement...</p></main></div>;
  }

  if (!session) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <div style={cardStyle}>
            <h2>Aucune session active</h2>
            <p style={{ opacity: 0.7 }}>Créez un jeu pour démarrer une session.</p>
            <button style={{ ...buttonStyle, background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)" }} onClick={() => navigate("/game/create")}>
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

        {/* Équipes */}
        {game && (
          <div style={cardStyle}>
            <div style={{ display: "flex", justifyContent: "space-around", alignItems: "center" }}>
              <div>
                <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>ÉQUIPE 1</p>
                <p style={{ margin: "8px 0 0", fontWeight: "600", fontSize: "18px" }}>{game.creatorTeam?.nom || "Équipe A"}</p>
              </div>
              <div style={{ fontSize: "24px", fontWeight: "bold", color: "#ff9800" }}>VS</div>
              <div>
                <p style={{ margin: 0, fontSize: "12px", opacity: 0.7 }}>ÉQUIPE 2</p>
                <p style={{ margin: "8px 0 0", fontWeight: "600", fontSize: "18px" }}>{game.opponentTeam?.nom || "Équipe B"}</p>
              </div>
            </div>
          </div>
        )}

        {/* Formulaire de score */}
        {showScoreForm && (
          <div style={{ ...cardStyle, border: "1px solid #ff9800" }}>
            <h3 style={{ marginTop: 0, marginBottom: 20, color: "#ff9800" }}>Saisir le score final</h3>

            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "24px" }}>
              {/* Score équipe A */}
              <div style={{ textAlign: "center" }}>
                <p style={{ margin: "0 0 8px", fontSize: "13px", opacity: 0.7, fontWeight: 600 }}>{game?.creatorTeam?.nom || "Équipe A"}</p>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <button onClick={() => setScoreA(Math.max(0, Number(scoreA) - 1))} style={{ width: 36, height: 36, borderRadius: "50%", border: "1px solid #555", background: "#222", color: "white", cursor: "pointer", fontSize: "18px" }}>−</button>
                  <input type="number" min="0" value={scoreA} onChange={(e) => setScoreA(Math.max(0, Number(e.target.value)))} style={inputStyle} />
                  <button onClick={() => setScoreA(Number(scoreA) + 1)} style={{ width: 36, height: 36, borderRadius: "50%", border: "1px solid #555", background: "#222", color: "white", cursor: "pointer", fontSize: "18px" }}>+</button>
                </div>
              </div>

              <div style={{ fontSize: "28px", fontWeight: "bold", color: "#555", paddingTop: "24px" }}>:</div>

              {/* Score équipe B */}
              <div style={{ textAlign: "center" }}>
                <p style={{ margin: "0 0 8px", fontSize: "13px", opacity: 0.7, fontWeight: 600 }}>{game?.opponentTeam?.nom || "Équipe B"}</p>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <button onClick={() => setScoreB(Math.max(0, Number(scoreB) - 1))} style={{ width: 36, height: 36, borderRadius: "50%", border: "1px solid #555", background: "#222", color: "white", cursor: "pointer", fontSize: "18px" }}>−</button>
                  <input type="number" min="0" value={scoreB} onChange={(e) => setScoreB(Math.max(0, Number(e.target.value)))} style={inputStyle} />
                  <button onClick={() => setScoreB(Number(scoreB) + 1)} style={{ width: 36, height: 36, borderRadius: "50%", border: "1px solid #555", background: "#222", color: "white", cursor: "pointer", fontSize: "18px" }}>+</button>
                </div>
              </div>
            </div>

            {/* Résumé */}
            <p style={{ margin: "20px 0 0", fontSize: "14px", opacity: 0.7 }}>
              {Number(scoreA) > Number(scoreB)
                ? `🏆 Victoire : ${game?.creatorTeam?.nom || "Équipe A"}`
                : Number(scoreB) > Number(scoreA)
                ? `🏆 Victoire : ${game?.opponentTeam?.nom || "Équipe B"}`
                : "🤝 Match nul"}
            </p>

            <div style={{ display: "flex", gap: "12px", marginTop: "20px" }}>
              <button style={{ ...buttonStyle, flex: 1, background: "linear-gradient(135deg, #4caf50 0%, #388e3c 100%)" }} onClick={handleTerminateWithScores} disabled={terminating}>
                {terminating ? "Enregistrement..." : "✅ Confirmer le score"}
              </button>
              <button style={{ ...buttonStyle, flex: "none", width: "auto", padding: "20px 20px", background: "#333", fontSize: "14px" }} onClick={() => setShowScoreForm(false)} disabled={terminating}>
                Annuler
              </button>
            </div>
          </div>
        )}

        {error && (
          <div style={{ ...cardStyle, borderColor: "#e53935" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
        )}

        {/* Bouton terminer */}
        {!showScoreForm && (
          <button style={{ ...buttonStyle, background: "linear-gradient(135deg, #e53935 0%, #c62828 100%)" }} onClick={() => setShowScoreForm(true)}>
            🏁 Terminer la session
          </button>
        )}
      </main>
    </div>
  );
}

export default ActiveSessionPage;
