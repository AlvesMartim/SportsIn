import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { stravaAPI } from "../api/api.js";

/**
 * Section Strava affichée sur la page Profil.
 * Gère la connexion OAuth, l'affichage du statut et l'import d'activités.
 */
function StravaSection({ userId }) {
  const [searchParams, setSearchParams] = useSearchParams();

  const [status, setStatus] = useState(null); // { connected, athleteId }
  const [loadingStatus, setLoadingStatus] = useState(true);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState(null); // { success, data, error }
  const [activities, setActivities] = useState([]);
  const [showActivities, setShowActivities] = useState(false);
  const [loadingActivities, setLoadingActivities] = useState(false);
  const [disconnecting, setDisconnecting] = useState(false);

  // Capturer le résultat OAuth dans un state au montage pour ne pas le perdre
  // quand on nettoie le paramètre URL (sinon le banner disparaît immédiatement)
  const [oauthResult] = useState(() => searchParams.get("strava"));

  useEffect(() => {
    // Nettoyer le paramètre strava de l'URL (au montage seulement)
    if (searchParams.get("strava")) {
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("strava");
      setSearchParams(newParams, { replace: true });
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!userId) return;
    loadStatus();
  }, [userId]);

  const loadStatus = async () => {
    setLoadingStatus(true);
    try {
      const data = await stravaAPI.getStatus(userId);
      setStatus(data);
    } catch {
      setStatus({ connected: false });
    } finally {
      setLoadingStatus(false);
    }
  };

  const handleConnect = () => {
    window.location.href = stravaAPI.getConnectUrl(userId);
  };

  const handleDisconnect = async () => {
    setDisconnecting(true);
    try {
      await stravaAPI.disconnect(userId);
      setStatus({ connected: false });
      setActivities([]);
      setShowActivities(false);
    } catch {
      // ignore
    } finally {
      setDisconnecting(false);
    }
  };

  const handleImportLatest = async () => {
    setImporting(true);
    setImportResult(null);
    try {
      const activity = await stravaAPI.importLatest(userId);
      setImportResult({ success: true, data: activity });
      // Rafraîchir la liste si elle est affichée
      if (showActivities) loadActivities();
    } catch (err) {
      setImportResult({ success: false, error: err.message });
    } finally {
      setImporting(false);
    }
  };

  const loadActivities = async () => {
    setLoadingActivities(true);
    try {
      const list = await stravaAPI.getActivities(userId);
      setActivities(list);
      setShowActivities(true);
    } catch {
      setActivities([]);
    } finally {
      setLoadingActivities(false);
    }
  };

  // --- Styles (cohérents avec ProfilePage) ---
  const cardStyle = {
    background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "20px",
    border: "1px solid #333",
  };

  const btnBase = {
    padding: "11px 20px",
    borderRadius: "10px",
    border: "none",
    fontWeight: "600",
    fontSize: "14px",
    cursor: "pointer",
    marginRight: "10px",
    marginTop: "8px",
  };

  const btnOrange = {
    ...btnBase,
    background: "linear-gradient(135deg, #fc4c02 0%, #d43b00 100%)",
    color: "white",
  };

  const btnGray = {
    ...btnBase,
    background: "#333",
    color: "#ccc",
  };

  const btnBlue = {
    ...btnBase,
    background: "linear-gradient(135deg, #1e88e5 0%, #1565c0 100%)",
    color: "white",
  };

  const badgeConnected = {
    display: "inline-block",
    padding: "4px 12px",
    borderRadius: "20px",
    background: "#1b5e20",
    color: "#81c784",
    fontSize: "13px",
    fontWeight: 600,
    marginBottom: "14px",
  };

  const badgeDisconnected = {
    display: "inline-block",
    padding: "4px 12px",
    borderRadius: "20px",
    background: "#3e2723",
    color: "#ef9a9a",
    fontSize: "13px",
    fontWeight: 600,
    marginBottom: "14px",
  };

  const oauthBanner = oauthResult ? {
    padding: "10px 16px",
    borderRadius: "8px",
    marginBottom: "14px",
    fontSize: "14px",
    background: oauthResult === "success" ? "#1b5e20" : "#4e2020",
    color: oauthResult === "success" ? "#81c784" : "#ef9a9a",
    border: `1px solid ${oauthResult === "success" ? "#388e3c" : "#c62828"}`,
  } : null;

  const formatDistance = (meters) => {
    if (!meters) return "—";
    return meters >= 1000
      ? (meters / 1000).toFixed(1) + " km"
      : Math.round(meters) + " m";
  };

  const formatDuration = (seconds) => {
    if (!seconds) return "—";
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return h > 0 ? `${h}h${m.toString().padStart(2, "0")}` : `${m} min`;
  };

  return (
    <div style={cardStyle}>
      <h3 style={{ marginTop: 0, marginBottom: "16px", color: "#ff6b35" }}>
        Strava
      </h3>

      {/* Bannière résultat OAuth */}
      {oauthResult && oauthBanner && (
        <div style={oauthBanner}>
          {oauthResult === "success" && "Compte Strava connecté avec succès !"}
          {oauthResult === "error" && "Erreur lors de la connexion à Strava. Réessayez."}
          {oauthResult === "denied" && "Connexion Strava annulée."}
          {oauthResult === "not_configured" && "Strava n'est pas encore configuré sur ce serveur."}
        </div>
      )}

      {loadingStatus ? (
        <p style={{ opacity: 0.6, fontSize: "14px" }}>Chargement du statut Strava...</p>
      ) : (
        <>
          {/* Statut connexion */}
          <div style={status?.connected ? badgeConnected : badgeDisconnected}>
            {status?.connected ? "Strava connecté" : "Strava non connecté"}
          </div>

          {status?.connected && status.athleteId && (
            <p style={{ fontSize: "13px", opacity: 0.6, margin: "0 0 14px" }}>
              Athlète #{status.athleteId}
            </p>
          )}

          {/* Actions */}
          <div>
            {!status?.connected ? (
              <button style={btnOrange} onClick={handleConnect}>
                Connecter Strava
              </button>
            ) : (
              <>
                <button
                  style={importing ? { ...btnBlue, opacity: 0.6 } : btnBlue}
                  onClick={handleImportLatest}
                  disabled={importing}
                >
                  {importing ? "Import en cours..." : "Importer ma dernière activité"}
                </button>

                <button
                  style={loadingActivities ? { ...btnGray, opacity: 0.6 } : btnGray}
                  onClick={loadActivities}
                  disabled={loadingActivities}
                >
                  {loadingActivities ? "Chargement..." : "Voir mes activités"}
                </button>

                <button
                  style={disconnecting ? { ...btnGray, opacity: 0.5, marginTop: "8px" } : { ...btnGray, marginTop: "8px" }}
                  onClick={handleDisconnect}
                  disabled={disconnecting}
                >
                  {disconnecting ? "Déconnexion..." : "Déconnecter Strava"}
                </button>
              </>
            )}
          </div>

          {/* Résultat import */}
          {importResult && (
            <div style={{
              marginTop: "14px",
              padding: "12px 14px",
              borderRadius: "10px",
              background: importResult.success ? "#1b5e20" : "#4e2020",
              border: `1px solid ${importResult.success ? "#388e3c" : "#c62828"}`,
              fontSize: "14px",
              color: importResult.success ? "#81c784" : "#ef9a9a",
            }}>
              {importResult.success ? (
                <>
                  Activité importée : <strong>{importResult.data.name}</strong>
                  {" — "}{formatDistance(importResult.data.distanceMeters)}
                  {" — "}{formatDuration(importResult.data.movingTimeSeconds)}
                  {" — "}+<strong>{importResult.data.pointsAttribues} pt{importResult.data.pointsAttribues > 1 ? "s" : ""}</strong> pour l'équipe
                </>
              ) : (
                importResult.error
              )}
            </div>
          )}

          {/* Liste des activités */}
          {showActivities && activities.length > 0 && (
            <div style={{ marginTop: "18px" }}>
              <h4 style={{ margin: "0 0 10px", fontSize: "14px", opacity: 0.8 }}>
                Activités Strava ({activities.length})
              </h4>
              {activities.map((a) => (
                <div key={a.id} style={{
                  background: "#0d0d1a",
                  border: "1px solid #2a2a3e",
                  borderRadius: "10px",
                  padding: "12px 14px",
                  marginBottom: "8px",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  gap: "10px",
                }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: "14px" }}>{a.name}</div>
                    <div style={{ fontSize: "12px", opacity: 0.6, marginTop: "2px" }}>
                      {a.sportCode} · {formatDistance(a.distanceMeters)} · {formatDuration(a.movingTimeSeconds)}
                      {a.startDate ? ` · ${new Date(a.startDate).toLocaleDateString("fr-FR")}` : ""}
                    </div>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "8px", flexShrink: 0 }}>
                    {a.importedAsSession ? (
                      <span style={{ fontSize: "12px", color: "#4caf50" }}>Importé</span>
                    ) : (
                      <button
                        style={{ ...btnBlue, padding: "6px 12px", fontSize: "12px", marginTop: 0, marginRight: 0 }}
                        onClick={async () => {
                          try {
                            await stravaAPI.importById(userId, a.stravaActivityId);
                            loadActivities();
                          } catch (err) {
                            alert(err.message);
                          }
                        }}
                      >
                        Importer
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {showActivities && activities.length === 0 && (
            <p style={{ fontSize: "13px", opacity: 0.5, marginTop: "14px" }}>
              Aucune activité Strava trouvée.
            </p>
          )}
        </>
      )}
    </div>
  );
}

export default StravaSection;
