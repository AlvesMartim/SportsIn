import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { sessionAPI, metricValueAPI } from "../api/api.js";

function SessionDetailPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const [session, setSession] = useState(null);
  const [metrics, setMetrics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, [sessionId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [sessionData, metricsData] = await Promise.all([
        sessionAPI.getById(sessionId),
        metricValueAPI.getBySession(sessionId).catch(() => []),
      ]);

      setSession(sessionData);
      setMetrics(Array.isArray(metricsData) ? metricsData : []);
    } catch (err) {
      setError("Session non trouvée");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("fr-FR", {
      day: "numeric",
      month: "long",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatDuration = (start, end) => {
    if (!start) return "N/A";
    const startTime = new Date(start).getTime();
    const endTime = end ? new Date(end).getTime() : Date.now();
    const diff = Math.floor((endTime - startTime) / 1000);

    const hrs = Math.floor(diff / 3600);
    const mins = Math.floor((diff % 3600) / 60);
    const secs = diff % 60;

    if (hrs > 0) {
      return `${hrs}h ${mins}m ${secs}s`;
    }
    return `${mins}m ${secs}s`;
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
    maxWidth: "700px",
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

  const backButtonStyle = {
    padding: "8px 16px",
    borderRadius: "8px",
    border: "1px solid #333",
    background: "transparent",
    color: "white",
    cursor: "pointer",
    marginBottom: "20px",
  };

  const statRowStyle = {
    display: "flex",
    justifyContent: "space-between",
    padding: "12px 0",
    borderBottom: "1px solid #333",
  };

  const metricCardStyle = {
    background: "#1a1a2e",
    borderRadius: "12px",
    padding: "16px",
    marginBottom: "12px",
    border: "1px solid #333",
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

  if (error || !session) {
    return (
      <div style={containerStyle}>
        <main style={mainStyle}>
          <button style={backButtonStyle} onClick={() => navigate("/history")}>
            ← Retour
          </button>
          <div style={{ ...cardStyle, borderColor: "#e53935" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error || "Session non trouvée"}</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <button style={backButtonStyle} onClick={() => navigate("/history")}>
          ← Retour à l'historique
        </button>

        <h1 style={{ marginBottom: "8px" }}>Détails de la session</h1>
        <p style={{ opacity: 0.7, marginBottom: "24px", fontFamily: "monospace" }}>
          {session.id}
        </p>

        {/* Informations générales */}
        <div style={cardStyle}>
          <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Informations</h3>

          <div style={statRowStyle}>
            <span style={{ opacity: 0.7 }}>Statut</span>
            <span
              style={{
                padding: "4px 12px",
                borderRadius: "12px",
                fontSize: "12px",
                fontWeight: "600",
                background: session.state === "TERMINATED" ? "#4caf50" : "#ff9800",
              }}
            >
              {session.state === "TERMINATED" ? "Terminée" : session.state}
            </span>
          </div>

          {session.sport && (
            <div style={statRowStyle}>
              <span style={{ opacity: 0.7 }}>Sport</span>
              <span>{session.sport.nom || session.sport.code}</span>
            </div>
          )}

          {session.pointId && (
            <div style={statRowStyle}>
              <span style={{ opacity: 0.7 }}>Arène</span>
              <span>{session.pointId}</span>
            </div>
          )}

          <div style={statRowStyle}>
            <span style={{ opacity: 0.7 }}>Début</span>
            <span>{formatDate(session.createdAt)}</span>
          </div>

          {session.endedAt && (
            <div style={statRowStyle}>
              <span style={{ opacity: 0.7 }}>Fin</span>
              <span>{formatDate(session.endedAt)}</span>
            </div>
          )}

          <div style={{ ...statRowStyle, borderBottom: "none" }}>
            <span style={{ opacity: 0.7 }}>Durée</span>
            <span style={{ fontWeight: "600", color: "#1e88e5" }}>
              {formatDuration(session.createdAt, session.endedAt)}
            </span>
          </div>
        </div>

        {/* Participants */}
        {session.participants && session.participants.length > 0 && (
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Participants</h3>
            {session.participants.map((participant, index) => (
              <div
                key={participant.id || index}
                style={{
                  ...statRowStyle,
                  borderBottom: index < session.participants.length - 1 ? "1px solid #333" : "none",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                  <div
                    style={{
                      width: "36px",
                      height: "36px",
                      borderRadius: "8px",
                      background:
                        participant.id === session.winnerParticipantId
                          ? "linear-gradient(135deg, #4caf50 0%, #388e3c 100%)"
                          : "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontWeight: "bold",
                    }}
                  >
                    {participant.name?.charAt(0).toUpperCase() || "?"}
                  </div>
                  <div>
                    <p style={{ margin: 0, fontWeight: "600" }}>{participant.name}</p>
                    <p style={{ margin: "2px 0 0", fontSize: "11px", opacity: 0.5 }}>
                      {participant.type || "Participant"}
                    </p>
                  </div>
                </div>
                {participant.id === session.winnerParticipantId && (
                  <span style={{ color: "#4caf50", fontWeight: "600" }}>🏆 Gagnant</span>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Métriques */}
        {metrics.length > 0 && (
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0, marginBottom: "16px" }}>Métriques</h3>
            {metrics.map((metric, index) => (
              <div key={metric.id || index} style={metricCardStyle}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div>
                    <p style={{ margin: 0, fontSize: "12px", opacity: 0.5 }}>
                      {metric.metricType || "METRIC"}
                    </p>
                    <p style={{ margin: "4px 0 0", fontSize: "24px", fontWeight: "bold", color: "#1e88e5" }}>
                      {metric.value}
                    </p>
                  </div>
                  {metric.participantId && (
                    <span style={{ fontSize: "12px", opacity: 0.7 }}>
                      Participant {metric.participantId}
                    </span>
                  )}
                </div>
                {metric.context && (
                  <p style={{ margin: "8px 0 0", fontSize: "12px", opacity: 0.5 }}>
                    Contexte: {metric.context}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Résultat */}
        {session.winnerParticipantId && (
          <div style={{ ...cardStyle, background: "linear-gradient(135deg, #1b5e20 0%, #2e7d32 100%)", border: "2px solid #4caf50" }}>
            <div style={{ textAlign: "center" }}>
              <p style={{ margin: 0, fontSize: "14px", opacity: 0.8 }}>VAINQUEUR</p>
              <p style={{ margin: "8px 0 0", fontSize: "24px", fontWeight: "bold" }}>
                🏆 Équipe {session.winnerParticipantId}
              </p>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default SessionDetailPage;
