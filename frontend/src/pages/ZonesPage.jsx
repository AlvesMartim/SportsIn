import { useEffect, useState } from "react";

const DEPT_COLORS = {
  "75": "#e63946",
  "77": "#457b9d",
  "78": "#2a9d8f",
  "91": "#e9c46a",
  "92": "#f4a261",
  "93": "#e76f51",
  "94": "#8338ec",
  "95": "#06d6a0",
};

function ZonesPage() {
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/zones/classement")
      .then((r) => {
        if (!r.ok) throw new Error("Erreur " + r.status);
        return r.json();
      })
      .then((data) => {
        setZones(data);
        setLoading(false);
      })
      .catch((e) => {
        setError(e.message);
        setLoading(false);
      });
  }, []);

  const maxInfluence = zones.length > 0 ? zones[0].totalInfluence : 1;

  return (
    <div style={{ padding: "2rem", maxWidth: "800px", margin: "0 auto" }}>
      <h1 style={{ fontSize: "1.8rem", fontWeight: 700, marginBottom: "0.4rem" }}>
        Classement des Zones IDF
      </h1>
      <p style={{ color: "#aaa", marginBottom: "2rem" }}>
        Influence accumulée par département via les activités Strava
      </p>

      {loading && <p style={{ color: "#aaa" }}>Chargement...</p>}
      {error && (
        <div style={{ background: "#3d1515", color: "#f87171", padding: "1rem", borderRadius: "8px" }}>
          Erreur : {error}
        </div>
      )}

      {!loading && !error && (
        <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
          {zones.map((zone, index) => {
            const pct = maxInfluence > 0 ? (zone.totalInfluence / maxInfluence) * 100 : 0;
            const color = DEPT_COLORS[zone.code] || "#6b7280";
            return (
              <div
                key={zone.code}
                style={{
                  background: "#1e1e2e",
                  border: `1px solid ${color}44`,
                  borderRadius: "12px",
                  padding: "1.2rem 1.5rem",
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "0.6rem" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.8rem" }}>
                    <span
                      style={{
                        background: index < 3 ? color : "#2d2d3e",
                        color: index < 3 ? "#fff" : "#aaa",
                        fontWeight: 700,
                        fontSize: "0.85rem",
                        width: "28px",
                        height: "28px",
                        borderRadius: "50%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      {index + 1}
                    </span>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: "1.05rem" }}>
                        {zone.nom}
                        <span style={{ color: "#666", fontSize: "0.85rem", marginLeft: "0.5rem" }}>
                          ({zone.code})
                        </span>
                      </div>
                      <div style={{ fontSize: "0.8rem", color: "#888" }}>
                        {zone.nbArenes} arène{zone.nbArenes > 1 ? "s" : ""}
                        {zone.controllingTeamNom && (
                          <span style={{ color, marginLeft: "0.5rem" }}>
                            · Contrôlé par {zone.controllingTeamNom}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <div style={{ fontWeight: 700, fontSize: "1.1rem", color }}>
                      {zone.totalInfluence.toFixed(1)}
                    </div>
                    <div style={{ fontSize: "0.75rem", color: "#666" }}>influence</div>
                  </div>
                </div>

                {/* Barre de progression */}
                <div style={{ background: "#2d2d3e", borderRadius: "4px", height: "6px", overflow: "hidden" }}>
                  <div
                    style={{
                      width: `${pct}%`,
                      height: "100%",
                      background: color,
                      borderRadius: "4px",
                      transition: "width 0.6s ease",
                    }}
                  />
                </div>
              </div>
            );
          })}

          {zones.every((z) => z.totalInfluence === 0) && (
            <p style={{ color: "#888", textAlign: "center", marginTop: "1rem" }}>
              Aucune activité Strava enregistrée dans une zone IDF pour l'instant.
              Synchronise une activité pour voir le classement évoluer !
            </p>
          )}
        </div>
      )}
    </div>
  );
}

export default ZonesPage;
