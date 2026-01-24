import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { sessionAPI } from "../api/api.js";

function ActivityHistoryPage() {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await sessionAPI.getAll();
      // Trier par date décroissante
      const sorted = (Array.isArray(data) ? data : []).sort((a, b) => {
        const dateA = new Date(a.createdAt || 0);
        const dateB = new Date(b.createdAt || 0);
        return dateB - dateA;
      });
      setSessions(sorted);
    } catch (err) {
      setError("Erreur lors du chargement de l'historique");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("fr-FR", {
      day: "numeric",
      month: "long",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadge = (state) => {
    const styles = {
      ACTIVE: { background: "#4caf50", text: "En cours" },
      TERMINATED: { background: "#1e88e5", text: "Terminée" },
    };
    const style = styles[state] || { background: "#757575", text: state || "N/A" };

    return (
      <span
        style={{
          padding: "4px 10px",
          borderRadius: "12px",
          fontSize: "11px",
          fontWeight: "600",
          background: style.background,
          color: "white",
        }}
      >
        {style.text}
      </span>
    );
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
    maxWidth: "800px",
    margin: "0 auto",
    width: "100%",
  };

  const cardStyle = {
    background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
    borderRadius: "12px",
    padding: "16px 20px",
    marginBottom: "12px",
    border: "1px solid #333",
    cursor: "pointer",
    transition: "all 0.2s",
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

  return (
    <div style={containerStyle}>
      <main style={mainStyle}>
        <button style={backButtonStyle} onClick={() => navigate("/")}>
          ← Retour
        </button>

        <h1 style={{ marginBottom: "8px" }}>Historique des sessions</h1>
        <p style={{ opacity: 0.7, marginBottom: "24px" }}>
          {sessions.length} session{sessions.length > 1 ? "s" : ""} enregistrée{sessions.length > 1 ? "s" : ""}
        </p>

        {error && (
          <div style={{ ...cardStyle, borderColor: "#e53935", cursor: "default" }}>
            <p style={{ color: "#e53935", margin: 0 }}>{error}</p>
          </div>
        )}

        {sessions.length === 0 ? (
          <div style={{ ...cardStyle, cursor: "default", textAlign: "center" }}>
            <p style={{ margin: 0, opacity: 0.7 }}>Aucune session pour l'instant</p>
            <p style={{ margin: "8px 0 0", fontSize: "14px", opacity: 0.5 }}>
              Créez un jeu pour commencer !
            </p>
          </div>
        ) : (
          sessions.map((session) => (
            <div
              key={session.id}
              style={cardStyle}
              onClick={() => navigate(`/session/${session.id}`)}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = "#1e88e5";
                e.currentTarget.style.transform = "translateX(4px)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = "#333";
                e.currentTarget.style.transform = "translateX(0)";
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "12px" }}>
                <div>
                  <p style={{ margin: 0, fontWeight: "600", fontSize: "16px" }}>
                    {session.sport?.nom || session.sport?.code || "Session sportive"}
                  </p>
                  <p style={{ margin: "4px 0 0", fontSize: "12px", opacity: 0.7 }}>
                    {formatDate(session.createdAt)}
                  </p>
                </div>
                {getStatusBadge(session.state)}
              </div>

              <div style={{ display: "flex", gap: "24px", flexWrap: "wrap" }}>
                {session.pointId && (
                  <div>
                    <p style={{ margin: 0, fontSize: "11px", opacity: 0.5 }}>ARÈNE</p>
                    <p style={{ margin: "2px 0 0", fontSize: "13px" }}>{session.pointId}</p>
                  </div>
                )}

                {session.participants && session.participants.length > 0 && (
                  <div>
                    <p style={{ margin: 0, fontSize: "11px", opacity: 0.5 }}>PARTICIPANTS</p>
                    <p style={{ margin: "2px 0 0", fontSize: "13px" }}>
                      {session.participants.map((p) => p.name).join(" vs ")}
                    </p>
                  </div>
                )}

                {session.winnerParticipantId && (
                  <div>
                    <p style={{ margin: 0, fontSize: "11px", opacity: 0.5 }}>GAGNANT</p>
                    <p style={{ margin: "2px 0 0", fontSize: "13px", color: "#4caf50" }}>
                      Équipe {session.winnerParticipantId}
                    </p>
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </main>
    </div>
  );
}

export default ActivityHistoryPage;
