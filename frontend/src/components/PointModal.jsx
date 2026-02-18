import React from "react";
import { useNavigate } from "react-router-dom";

function PointModal({ point, onClose }) {
  const navigate = useNavigate();

  if (!point) return null;

  const handleCreateSession = () => {
    navigate(`/game/create?pointId=${point.id}`);
  };

  const handleViewSessions = () => {
    navigate(`/sessions?pointId=${point.id}`);
  };

  return (
    <div
      style={{
        position: "absolute",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        background: "rgba(0,0,0,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 2000,
      }}
    >
      <div
        style={{
          background: "white",
          padding: "30px",
          borderRadius: "12px",
          width: "480px",
          maxHeight: "80vh",
          overflowY: "auto",
          textAlign: "center",
        }}
      >
        <h2 style={{ marginBottom: "16px", color: "#1e293b" }}>
          {point.name}
        </h2>

        <p>
          <strong>Statut :</strong> {point.status}
        </p>

        <p style={{ marginTop: "20px", marginBottom: "6px" }}>
          <strong>Influence :</strong>
        </p>

        {/* Barre d'influence */}
        <div
          style={{
            width: "100%",
            height: "10px",
            background: "#eee",
            borderRadius: "8px",
            overflow: "hidden",
            marginBottom: "20px",
          }}
        >
          <div
            style={{
              width: `${point.influence}%`,
              height: "100%",
              background:
                point.status === "TEAM_RED"
                  ? "#e53935"
                  : point.status === "TEAM_BLUE"
                  ? "#1e88e5"
                  : "#757575",
              transition: "width 0.3s",
            }}
          />
        </div>

        {/* Sports */}
        <p style={{ marginTop: "10px", fontWeight: "bold" }}>Sports :</p>
        <ul style={{ textAlign: "left", marginBottom: "30px" }}>
          {point.sports.map((s, i) => (
            <li key={i}>{s}</li>
          ))}
        </ul>

        {/* BOUTONS */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginTop: "20px",
          }}
        >
          <button
            onClick={handleCreateSession}
            style={{
              padding: "12px 20px",
              background: "#111",
              color: "white",
              borderRadius: "8px",
              cursor: "pointer",
              border: "none",
              width: "140px",
            }}
          >
            Créer une session
          </button>

          <button
            onClick={handleViewSessions}
            style={{
              padding: "12px 20px",
              background: "#111",
              color: "white",
              borderRadius: "8px",
              cursor: "pointer",
              border: "none",
              width: "140px",
            }}
          >
            Voir les sessions
          </button>

          <button
            onClick={onClose}
            style={{
              padding: "12px 20px",
              background: "#111",
              color: "white",
              borderRadius: "8px",
              cursor: "pointer",
              border: "none",
              width: "100px",
            }}
          >
            Fermer
          </button>
        </div>
      </div>
    </div>
  );
}

export default PointModal;