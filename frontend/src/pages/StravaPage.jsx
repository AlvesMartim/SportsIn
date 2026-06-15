import React, { useState, useEffect, useCallback } from "react";
import { useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { stravaAPI, zoneAPI } from "../api/api.js";
import "../styles/strava.css";

const SPORT_ICONS = {
  Run: "🏃",
  TrailRun: "⛰️",
  Ride: "🚴",
  MountainBikeRide: "🚵",
  Walk: "🚶",
  Hike: "🥾",
  WeightTraining: "🏋️",
  Swim: "🏊",
  Workout: "💪",
  Yoga: "🧘",
};

const SPORT_LABELS = {
  Run: "Course à pied",
  TrailRun: "Trail",
  Ride: "Vélo",
  MountainBikeRide: "VTT",
  Swim: "Natation",
  Walk: "Marche",
  Hike: "Randonnée",
  WeightTraining: "Musculation",
  Workout: "Entraînement",
  Yoga: "Yoga",
};

export default function StravaPage() {
  const { user } = useAuth();
  const location = useLocation();

  const [status, setStatus] = useState(null);
  const [stats, setStats] = useState(null);
  const [activities, setActivities] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);
  const [leaderboardPeriod, setLeaderboardPeriod] = useState("weekly");
  const [zones, setZones] = useState([]);
  const [selectedZoneId, setSelectedZoneId] = useState(null);
  const [syncing, setSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const joueurId = user?.id;

  // Notification OAuth de retour du callback
  const searchParams = new URLSearchParams(location.search);
  const oauthConnected = searchParams.get("connected") === "true";
  const oauthError = searchParams.get("error");

  const loadStatus = useCallback(async () => {
    if (!joueurId) return;
    try {
      const s = await stravaAPI.getStatus(joueurId);
      setStatus(s);
    } catch {
      setStatus({ connected: false });
    }
  }, [joueurId]);

  const loadStats = useCallback(async () => {
    if (!joueurId) return;
    try {
      const [s, acts] = await Promise.all([
        stravaAPI.getPlayerStats(joueurId),
        stravaAPI.getActivities(joueurId),
      ]);
      setStats(s);
      setActivities(acts.slice(0, 20));
    } catch {
      // stats non disponibles
    }
  }, [joueurId]);

  const loadLeaderboard = useCallback(async () => {
    try {
      const lb = await stravaAPI.getLeaderboard(leaderboardPeriod, selectedZoneId);
      setLeaderboard(lb);
    } catch {
      setLeaderboard([]);
    }
  }, [leaderboardPeriod, selectedZoneId]);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      loadStatus(),
      loadStats(),
      loadLeaderboard(),
      zoneAPI.getAll().then(setZones).catch(() => setZones([])),
    ]).finally(() => setLoading(false));
  }, [loadStatus, loadStats, loadLeaderboard]);

  const handleConnect = async () => {
    try {
      const { url } = await stravaAPI.getAuthUrl(joueurId);
      window.location.href = url;
    } catch (e) {
      setError("Impossible d'obtenir l'URL Strava : " + e.message);
    }
  };

  const handleDisconnect = async () => {
    if (!window.confirm("Déconnecter votre compte Strava ? Vos tokens seront supprimés.")) return;
    try {
      await stravaAPI.disconnect(joueurId);
      setStatus({ connected: false });
      setStats(null);
      setActivities([]);
    } catch (e) {
      setError("Erreur lors de la déconnexion : " + e.message);
    }
  };

  const handleSync = async () => {
    setSyncing(true);
    setSyncResult(null);
    setError(null);
    try {
      const result = await stravaAPI.syncNow(joueurId);
      setSyncResult(result);
      await loadStats();
    } catch (e) {
      setError("Erreur synchronisation : " + e.message);
    } finally {
      setSyncing(false);
    }
  };

  const formatDistance = (km) =>
    km >= 1 ? `${km.toFixed(1)} km` : `${Math.round(km * 1000)} m`;

  const formatDuration = (seconds) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return h > 0 ? `${h}h${m.toString().padStart(2, "0")}` : `${m} min`;
  };

  const formatDate = (iso) => {
    if (!iso) return "";
    return new Date(iso).toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  if (loading) return <div className="strava-page"><div className="strava-loading">Chargement...</div></div>;

  return (
    <div className="strava-page">
      <h1 className="strava-title">
        <span className="strava-logo-icon">🏃</span>
        Intégration Strava
      </h1>

      {/* Notifications OAuth */}
      {oauthConnected && (
        <div className="strava-alert strava-alert--success">
          Compte Strava connecté avec succès !
        </div>
      )}
      {oauthError && (
        <div className="strava-alert strava-alert--error">
          Erreur de connexion Strava : {oauthError}
        </div>
      )}
      {error && (
        <div className="strava-alert strava-alert--error">{error}</div>
      )}

      {/* Bloc connexion */}
      <section className="strava-card">
        <h2>Compte Strava</h2>
        {status?.connected ? (
          <div className="strava-connected">
            <div className="strava-status-badge strava-status-badge--on">
              Connecté
            </div>
            <p className="strava-meta">
              Athlète Strava #{status.stravaAthleteId} · Connecté le{" "}
              {formatDate(status.connectedAt)}
            </p>
            <div className="strava-actions">
              <button
                className="btn-strava-sync"
                onClick={handleSync}
                disabled={syncing}
              >
                {syncing ? "Synchronisation..." : "Synchroniser maintenant"}
              </button>
              <button className="btn-strava-disconnect" onClick={handleDisconnect}>
                Déconnecter
              </button>
            </div>
            {syncResult && (
              <div className="strava-sync-result">
                <strong>Synchronisation terminée</strong>
                <ul>
                  <li>{syncResult.activitiesFetched} activité(s) récupérée(s)</li>
                  <li>{syncResult.activitiesImported} importée(s)</li>
                  <li>{syncResult.activitiesSkipped} déjà existante(s)</li>
                  {syncResult.activitiesFlagged > 0 && (
                    <li className="strava-flag">
                      {syncResult.activitiesFlagged} flaggée(s) anti-cheat
                    </li>
                  )}
                  <li>
                    Influence accordée :{" "}
                    <strong>+{syncResult.totalInfluenceGranted?.toFixed(1)}</strong>
                  </li>
                </ul>
              </div>
            )}
          </div>
        ) : (
          <div className="strava-disconnected">
            <div className="strava-status-badge strava-status-badge--off">
              Non connecté
            </div>
            <p>
              Connectez votre compte Strava pour importer vos activités sportives
              réelles et booster votre influence sur les zones !
            </p>
            <button className="btn-strava-connect" onClick={handleConnect}>
              Connecter mon compte Strava
            </button>
          </div>
        )}
      </section>

      {/* Statistiques personnelles */}
      {stats && (
        <section className="strava-card">
          <h2>Mes statistiques</h2>
          <div className="strava-stats-grid">
            <div className="strava-stat">
              <span className="strava-stat-value">
                {formatDistance(stats.totalDistanceKm)}
              </span>
              <span className="strava-stat-label">Distance totale</span>
            </div>
            <div className="strava-stat">
              <span className="strava-stat-value">
                {Math.round(stats.totalElevationGainM)} m
              </span>
              <span className="strava-stat-label">Dénivelé cumulé</span>
            </div>
            <div className="strava-stat">
              <span className="strava-stat-value">{stats.totalActivities}</span>
              <span className="strava-stat-label">Activités</span>
            </div>
            <div className="strava-stat strava-stat--highlight">
              <span className="strava-stat-value">
                +{stats.totalInfluenceGranted?.toFixed(1)}
              </span>
              <span className="strava-stat-label">Influence accordée</span>
            </div>
          </div>
          {stats.activitiesBySport && Object.keys(stats.activitiesBySport).length > 0 && (
            <div className="strava-sports-breakdown">
              <h3>Par sport</h3>
              <div className="strava-sports-list">
                {Object.entries(stats.activitiesBySport).map(([sport, count]) => (
                  <span key={sport} className="strava-sport-chip">
                    {SPORT_LABELS[sport] || sport} ({count})
                  </span>
                ))}
              </div>
            </div>
          )}
        </section>
      )}

      {/* Classement équipes */}
      <section className="strava-card">
        <div className="strava-leaderboard-header">
          <h2>Classement équipes</h2>
          <div className="strava-period-toggle">
            <button
              className={leaderboardPeriod === "weekly" ? "active" : ""}
              onClick={() => setLeaderboardPeriod("weekly")}
            >
              Semaine
            </button>
            <button
              className={leaderboardPeriod === "monthly" ? "active" : ""}
              onClick={() => setLeaderboardPeriod("monthly")}
            >
              Mois
            </button>
          </div>
        </div>

        {/* Filtre par zone */}
        <div className="strava-zone-filter">
          <label className="strava-zone-label">Zone :</label>
          <select
            className="strava-zone-select"
            value={selectedZoneId ?? ""}
            onChange={(e) => setSelectedZoneId(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">Toutes les zones</option>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>
                {z.nom || `Zone ${z.id}`}
              </option>
            ))}
          </select>
          {selectedZoneId && (
            <button
              className="strava-zone-clear"
              onClick={() => setSelectedZoneId(null)}
            >
              ✕
            </button>
          )}
        </div>

        {leaderboard.length === 0 ? (
          <p className="strava-empty">
            Aucune activité enregistrée{selectedZoneId ? " dans cette zone" : ""} sur cette période.
          </p>
        ) : (
          <table className="strava-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Équipe</th>
                <th>Distance</th>
                <th>Activités</th>
                <th>Par sport</th>
              </tr>
            </thead>
            <tbody>
              {leaderboard.map((entry) => (
                <tr
                  key={entry.equipeId}
                  className={entry.rank === 1 ? "strava-rank-gold" : ""}
                >
                  <td>{entry.rank}</td>
                  <td>{entry.equipeName || `Équipe ${entry.equipeId}`}</td>
                  <td>{formatDistance(entry.distanceKm)}</td>
                  <td>{entry.activitiesCount}</td>
                  <td>
                    <div className="strava-sport-chips">
                      {(entry.sportBreakdown || []).map((s) => (
                        <span key={s.sport} className={`strava-sport-chip strava-sport-chip--${s.sport.toLowerCase()}`}>
                          {SPORT_ICONS[s.sport] || "🏃"} {s.label} {formatDistance(s.distanceKm)}
                        </span>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* Historique des activités importées */}
      {activities.length > 0 && (
        <section className="strava-card">
          <h2>Activités importées</h2>
          <div className="strava-activities-list">
            {activities.map((act) => (
              <div
                key={act.id}
                className={`strava-activity-item ${act.antiCheatFlagged ? "strava-activity-item--flagged" : ""}`}
              >
                <div className="strava-activity-info">
                  <span className="strava-activity-sport">
                    {SPORT_LABELS[act.sportType] || act.sportType}
                  </span>
                  <span className="strava-activity-name">{act.name}</span>
                  {act.antiCheatFlagged && (
                    <span className="strava-flag-badge" title={act.antiCheatReason}>
                      Flaggée
                    </span>
                  )}
                </div>
                <div className="strava-activity-metrics">
                  <span>{formatDistance(act.distanceMeters / 1000)}</span>
                  <span>{formatDuration(act.movingTimeSeconds)}</span>
                  {act.totalElevationGain > 0 && (
                    <span>+{Math.round(act.totalElevationGain)} m</span>
                  )}
                  {!act.antiCheatFlagged && (
                    <span className="strava-influence">
                      +{act.influenceGranted?.toFixed(1)} inf.
                    </span>
                  )}
                </div>
                <div className="strava-activity-date">{formatDate(act.startDate)}</div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
