import { useState, useEffect, useRef, useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { sessionAPI, gameAPI } from "../api/api.js";

const SPORT_SCORING_PROFILES = {
  FOOTBALL: {
    label: "Football",
    mode: "single",
    metricType: "GOALS",
    metricLabel: "Buts",
    unit: "",
    step: 1,
    higherIsBetter: true,
    hint: "L'equipe avec le plus de buts gagne.",
  },
  BASKET: {
    label: "Basketball",
    mode: "single",
    metricType: "POINTS",
    metricLabel: "Points",
    unit: "",
    step: 1,
    higherIsBetter: true,
    hint: "L'equipe avec le plus de points gagne.",
  },
  TENNIS: {
    label: "Tennis",
    mode: "single",
    metricType: "POINTS",
    metricLabel: "Points",
    unit: "",
    step: 1,
    higherIsBetter: true,
    hint: "Saisissez les points de chaque equipe.",
  },
  MUSCULATION: {
    label: "Musculation",
    mode: "multi",
    metricType: "REPS",
    metricLabel: "Repetitions",
    unit: "reps",
    step: 1,
    higherIsBetter: true,
    hint: "Saisissez les repetitions par exercice. Le backend valide le resultat final.",
    fields: [
      { key: "exercise1", label: "Exercice 1", context: "exercise1" },
      { key: "exercise2", label: "Exercice 2", context: "exercise2" },
      { key: "exercise3", label: "Exercice 3", context: "exercise3" },
    ],
  },
  RUNNING: {
    label: "Course a pied",
    mode: "time",
    metricType: "TIME_SECONDS",
    metricLabel: "Temps",
    unit: "sec",
    step: 5,
    higherIsBetter: false,
    hint: "Saisissez le temps en secondes. Le temps le plus bas l'emporte.",
  },
};

function normalizeSportCode(sportCode) {
  return String(sportCode || "")
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, "_");
}

function resolveSportProfile(sportCode) {
  const aliases = {
    FOOT: "FOOTBALL",
    BASKETBALL: "BASKET",
    MUSCU: "MUSCULATION",
    COURSE: "RUNNING",
    COURSE_A_PIED: "RUNNING",
    COURSE_A_PIEDS: "RUNNING",
    RUN: "RUNNING",
  };

  const normalized = normalizeSportCode(sportCode);
  const canonical = aliases[normalized] || normalized;

  return SPORT_SCORING_PROFILES[canonical] || SPORT_SCORING_PROFILES.FOOTBALL;
}

function sanitizeNumber(value) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return Math.max(0, parsed);
}

function createInitialFormState(profile) {
  if (profile.mode === "multi") {
    const base = {};
    for (const field of profile.fields || []) {
      base[field.key] = 0;
    }

    return {
      teamA: { ...base },
      teamB: { ...base },
    };
  }

  return {
    teamA: 0,
    teamB: 0,
  };
}

function formatSeconds(totalSeconds) {
  const safe = Math.max(0, Math.floor(totalSeconds || 0));
  const mins = Math.floor(safe / 60);
  const secs = safe % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
}

function ActiveSessionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const gameId = searchParams.get("gameId");

  const [session, setSession] = useState(null);
  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [terminating, setTerminating] = useState(false);

  const [showScoreForm, setShowScoreForm] = useState(false);
  const [scoreFormData, setScoreFormData] = useState(() =>
    createInitialFormState(SPORT_SCORING_PROFILES.FOOTBALL)
  );

  const [elapsedTime, setElapsedTime] = useState(0);
  const timerRef = useRef(null);
  const pollRef = useRef(null);

  const resolvedSportCode = game?.sport?.code || session?.sport?.code || "FOOTBALL";
  const sportProfile = useMemo(
    () => resolveSportProfile(resolvedSportCode),
    [resolvedSportCode]
  );

  const teamAId =
    game?.creatorTeam?.id?.toString() ||
    session?.participants?.[0]?.id?.toString() ||
    null;
  const teamBId =
    game?.opponentTeam?.id?.toString() ||
    session?.participants?.[1]?.id?.toString() ||
    null;
  const teamAName = game?.creatorTeam?.nom || session?.participants?.[0]?.name || "Équipe A";
  const teamBName = game?.opponentTeam?.nom || session?.participants?.[1]?.name || "Équipe B";

  const startTimer = (createdAt) => {
    if (timerRef.current) clearInterval(timerRef.current);
    const startTime = createdAt ? new Date(createdAt).getTime() : Date.now();
    timerRef.current = setInterval(() => {
      setElapsedTime(Math.floor((Date.now() - startTime) / 1000));
    }, 1000);
  };

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

        return;
      }

      const activeSessions = await sessionAPI.getActive();
      if (Array.isArray(activeSessions) && activeSessions.length > 0) {
        setSession(activeSessions[0]);
        startTimer(activeSessions[0].createdAt);
      } else {
        setSession(null);
      }
    } catch (err) {
      setError("Erreur lors du chargement de la session");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    pollRef.current = setInterval(checkGameState, 3000);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [gameId]);

  useEffect(() => {
    if (showScoreForm) {
      setScoreFormData(createInitialFormState(sportProfile));
    }
  }, [showScoreForm, sportProfile]);

  const formatTime = (seconds) => {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    if (hrs > 0) {
      return `${hrs.toString().padStart(2, "0")}:${mins
        .toString()
        .padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
    }
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const getTeamAggregate = (teamKey) => {
    if (sportProfile.mode === "multi") {
      return (sportProfile.fields || []).reduce(
        (total, field) => total + sanitizeNumber(scoreFormData?.[teamKey]?.[field.key]),
        0
      );
    }

    return sanitizeNumber(scoreFormData?.[teamKey]);
  };

  const previewTeamA = getTeamAggregate("teamA");
  const previewTeamB = getTeamAggregate("teamB");

  const previewOutcome = useMemo(() => {
    if (previewTeamA === previewTeamB) {
      return "Égalité provisoire (validation finale côté backend).";
    }

    const leader = sportProfile.higherIsBetter
      ? previewTeamA > previewTeamB
        ? teamAName
        : teamBName
      : previewTeamA < previewTeamB
      ? teamAName
      : teamBName;

    return `Avantage: ${leader} (validation finale côté backend).`;
  }, [previewTeamA, previewTeamB, sportProfile.higherIsBetter, teamAName, teamBName]);

  const setDuelValue = (teamKey, value) => {
    const safeValue = sanitizeNumber(value);
    setScoreFormData((previous) => ({
      ...previous,
      [teamKey]: safeValue,
    }));
  };

  const setExerciseValue = (teamKey, exerciseKey, value) => {
    const safeValue = sanitizeNumber(value);
    setScoreFormData((previous) => ({
      ...previous,
      [teamKey]: {
        ...(previous[teamKey] || {}),
        [exerciseKey]: safeValue,
      },
    }));
  };

  const buildMetricsPayload = () => {
    const metrics = [];

    const pushMetric = (participantId, metricType, value, context) => {
      if (!participantId) return;
      metrics.push({
        participantId,
        metricType,
        value: sanitizeNumber(value),
        context,
      });
    };

    if (sportProfile.mode === "multi") {
      for (const field of sportProfile.fields || []) {
        pushMetric(
          teamAId,
          sportProfile.metricType,
          scoreFormData?.teamA?.[field.key],
          field.context || field.key
        );
        pushMetric(
          teamBId,
          sportProfile.metricType,
          scoreFormData?.teamB?.[field.key],
          field.context || field.key
        );
      }
      return metrics;
    }

    const context = sportProfile.mode === "time" ? "run" : "match";
    pushMetric(teamAId, sportProfile.metricType, scoreFormData?.teamA, context);
    pushMetric(teamBId, sportProfile.metricType, scoreFormData?.teamB, context);

    return metrics;
  };

  const handleTerminateWithScores = async () => {
    if (!session) return;

    try {
      setTerminating(true);
      setError(null);

      const metrics = buildMetricsPayload();
      if (metrics.length === 0) {
        setError("Impossible d'enregistrer les metriques: participants manquants.");
        return;
      }

      const updatedSession = {
        ...session,
        result: {
          ...(session.result || {}),
          metrics,
        },
      };

      await sessionAPI.update(session.id, updatedSession);

      const terminatedSession = await sessionAPI.terminate(session.id);
      setSession(terminatedSession || session);

      if (gameId && game) {
        const backendWinnerId = terminatedSession?.winnerParticipantId ?? null;
        await gameAPI.complete(gameId, backendWinnerId);
      }

      if (pollRef.current) clearInterval(pollRef.current);
      navigate(`/game/result/${terminatedSession?.id || session.id}`);
    } catch (err) {
      setError("Erreur lors de la terminaison de la session");
      console.error(err);
    } finally {
      setTerminating(false);
    }
  };

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
  const buttonStyle = {
    width: "100%",
    padding: "20px 24px",
    borderRadius: "12px",
    border: "none",
    cursor: "pointer",
    fontSize: "18px",
    fontWeight: "600",
    color: "white",
    transition: "all 0.2s",
    marginTop: "12px",
  };
  const inputStyle = {
    width: "80px",
    padding: "12px",
    fontSize: "32px",
    fontWeight: "bold",
    textAlign: "center",
    background: "#0d0d1a",
    border: "2px solid #444",
    borderRadius: "10px",
    color: "white",
    outline: "none",
  };
  const compactInputStyle = {
    width: "100%",
    padding: "10px",
    fontSize: "20px",
    fontWeight: "700",
    textAlign: "center",
    background: "#0d0d1a",
    border: "2px solid #444",
    borderRadius: "10px",
    color: "white",
    outline: "none",
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
  const stepValue = sportProfile.step || 1;

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
            <p style={{ opacity: 0.7 }}>Créez un jeu pour démarrer une session.</p>
            <button
              style={{
                ...buttonStyle,
                background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
              }}
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

        {showScoreForm && (
          <div style={{ ...cardStyle, border: "1px solid #ff9800" }}>
            <h3 style={{ marginTop: 0, marginBottom: 8, color: "#ff9800" }}>
              Saisir les resultats - {sportProfile.label}
            </h3>
            <p style={{ marginTop: 0, marginBottom: 16, fontSize: "13px", opacity: 0.75 }}>
              {sportProfile.hint}
            </p>
            <p
              style={{
                marginTop: 0,
                marginBottom: 20,
                fontSize: "12px",
                opacity: 0.55,
                fontFamily: "monospace",
              }}
            >
              Metric type backend: {sportProfile.metricType}
            </p>

            {(sportProfile.mode === "single" || sportProfile.mode === "time") && (
              <>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "24px" }}>
                  <div style={{ textAlign: "center" }}>
                    <p style={{ margin: "0 0 8px", fontSize: "13px", opacity: 0.7, fontWeight: 600 }}>
                      {teamAName}
                    </p>
                    <p style={{ margin: "0 0 6px", fontSize: "11px", opacity: 0.6 }}>
                      {sportProfile.metricLabel}
                    </p>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <button
                        onClick={() => setDuelValue("teamA", sanitizeNumber(scoreFormData.teamA) - stepValue)}
                        style={{
                          width: 36,
                          height: 36,
                          borderRadius: "50%",
                          border: "1px solid #555",
                          background: "#222",
                          color: "white",
                          cursor: "pointer",
                          fontSize: "18px",
                        }}
                      >
                        −
                      </button>
                      <input
                        type="number"
                        min="0"
                        step={stepValue}
                        value={sanitizeNumber(scoreFormData.teamA)}
                        onChange={(e) => setDuelValue("teamA", e.target.value)}
                        style={inputStyle}
                      />
                      <button
                        onClick={() => setDuelValue("teamA", sanitizeNumber(scoreFormData.teamA) + stepValue)}
                        style={{
                          width: 36,
                          height: 36,
                          borderRadius: "50%",
                          border: "1px solid #555",
                          background: "#222",
                          color: "white",
                          cursor: "pointer",
                          fontSize: "18px",
                        }}
                      >
                        +
                      </button>
                    </div>
                  </div>

                  <div style={{ fontSize: "28px", fontWeight: "bold", color: "#555", paddingTop: "24px" }}>
                    :
                  </div>

                  <div style={{ textAlign: "center" }}>
                    <p style={{ margin: "0 0 8px", fontSize: "13px", opacity: 0.7, fontWeight: 600 }}>
                      {teamBName}
                    </p>
                    <p style={{ margin: "0 0 6px", fontSize: "11px", opacity: 0.6 }}>
                      {sportProfile.metricLabel}
                    </p>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <button
                        onClick={() => setDuelValue("teamB", sanitizeNumber(scoreFormData.teamB) - stepValue)}
                        style={{
                          width: 36,
                          height: 36,
                          borderRadius: "50%",
                          border: "1px solid #555",
                          background: "#222",
                          color: "white",
                          cursor: "pointer",
                          fontSize: "18px",
                        }}
                      >
                        −
                      </button>
                      <input
                        type="number"
                        min="0"
                        step={stepValue}
                        value={sanitizeNumber(scoreFormData.teamB)}
                        onChange={(e) => setDuelValue("teamB", e.target.value)}
                        style={inputStyle}
                      />
                      <button
                        onClick={() => setDuelValue("teamB", sanitizeNumber(scoreFormData.teamB) + stepValue)}
                        style={{
                          width: 36,
                          height: 36,
                          borderRadius: "50%",
                          border: "1px solid #555",
                          background: "#222",
                          color: "white",
                          cursor: "pointer",
                          fontSize: "18px",
                        }}
                      >
                        +
                      </button>
                    </div>
                  </div>
                </div>

                <p style={{ margin: "16px 0 0", fontSize: "14px", opacity: 0.75 }}>
                  {sportProfile.mode === "time"
                    ? `${teamAName}: ${formatSeconds(previewTeamA)} | ${teamBName}: ${formatSeconds(previewTeamB)}`
                    : `${teamAName}: ${previewTeamA}${sportProfile.unit ? ` ${sportProfile.unit}` : ""} | ${teamBName}: ${previewTeamB}${sportProfile.unit ? ` ${sportProfile.unit}` : ""}`}
                </p>
              </>
            )}

            {sportProfile.mode === "multi" && (
              <div style={{ display: "grid", gap: "10px", marginTop: "10px" }}>
                {(sportProfile.fields || []).map((field) => (
                  <div
                    key={field.key}
                    style={{
                      background: "#0d0d1a",
                      border: "1px solid #2a2a3e",
                      borderRadius: "10px",
                      padding: "12px",
                    }}
                  >
                    <p style={{ margin: "0 0 10px", fontSize: "13px", fontWeight: 700, color: "#ffcc80" }}>
                      {field.label}
                    </p>
                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
                      <label style={{ textAlign: "left" }}>
                        <span style={{ display: "block", marginBottom: "6px", fontSize: "11px", opacity: 0.7 }}>
                          {teamAName}
                        </span>
                        <input
                          type="number"
                          min="0"
                          step={stepValue}
                          value={sanitizeNumber(scoreFormData?.teamA?.[field.key])}
                          onChange={(e) => setExerciseValue("teamA", field.key, e.target.value)}
                          style={compactInputStyle}
                        />
                      </label>
                      <label style={{ textAlign: "left" }}>
                        <span style={{ display: "block", marginBottom: "6px", fontSize: "11px", opacity: 0.7 }}>
                          {teamBName}
                        </span>
                        <input
                          type="number"
                          min="0"
                          step={stepValue}
                          value={sanitizeNumber(scoreFormData?.teamB?.[field.key])}
                          onChange={(e) => setExerciseValue("teamB", field.key, e.target.value)}
                          style={compactInputStyle}
                        />
                      </label>
                    </div>
                  </div>
                ))}

                <p style={{ margin: "8px 0 0", fontSize: "14px", opacity: 0.75 }}>
                  Totaux provisoires: {teamAName} {previewTeamA} reps | {teamBName} {previewTeamB} reps
                </p>
              </div>
            )}

            <p style={{ margin: "16px 0 0", fontSize: "14px", opacity: 0.75 }}>
              {previewOutcome}
            </p>

            <div style={{ display: "flex", gap: "12px", marginTop: "20px" }}>
              <button
                style={{
                  ...buttonStyle,
                  flex: 1,
                  background: "linear-gradient(135deg, #4caf50 0%, #388e3c 100%)",
                }}
                onClick={handleTerminateWithScores}
                disabled={terminating}
              >
                {terminating ? "Enregistrement..." : "✅ Confirmer les resultats"}
              </button>
              <button
                style={{
                  ...buttonStyle,
                  flex: "none",
                  width: "auto",
                  padding: "20px 20px",
                  background: "#333",
                  fontSize: "14px",
                }}
                onClick={() => setShowScoreForm(false)}
                disabled={terminating}
              >
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

        {!showScoreForm && (
          <button
            style={{ ...buttonStyle, background: "linear-gradient(135deg, #e53935 0%, #c62828 100%)" }}
            onClick={() => {
              setScoreFormData(createInitialFormState(sportProfile));
              setShowScoreForm(true);
            }}
          >
            🏁 Terminer la session
          </button>
        )}
      </main>
    </div>
  );
}

export default ActiveSessionPage;