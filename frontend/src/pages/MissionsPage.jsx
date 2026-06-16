import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { missionAPI, areneAPI } from "../api/api.js";
import Header from "../components/Header.jsx";
import WeatherForecastPanel from "../components/WeatherForecastPanel.jsx";
import "../styles/missions.css";
import "../styles/weather-forecast.css";

const MISSION_TYPE_LABELS = {
  RECAPTURE_RECENT_LOSS: "Reconquête",
  BREAK_ROUTE: "Rupture de route",
  DIVERSITY_SPORT: "Diversité sport",
  WEATHER_FLASH: "⚡ Alerte météo",
};

const WEATHER_EVENT_ICONS = {
  THUNDERSTORM: "⛈️",
  HEAVY_RAIN: "🌧️",
  EXTREME: "🌪️",
  HEAT: "🔥",
  WIND: "💨",
  SNOW: "❄️",
  default: "🌩️",
};

const PRIORITY_LABELS = { HIGH: "Haute", MEDIUM: "Moyenne", LOW: "Basse" };

const isWeatherFlash = (detail) =>
  detail?.payload?.missionCategory === "WEATHER_FLASH";

const getWeatherEventIcon = (eventType) =>
  WEATHER_EVENT_ICONS[String(eventType || "").toUpperCase()] || WEATHER_EVENT_ICONS.default;

const getMissionTypeLabel = (type, detail) => {
  if (isWeatherFlash(detail)) return MISSION_TYPE_LABELS.WEATHER_FLASH;
  return MISSION_TYPE_LABELS[type] || type;
};

const getMissionTypeKey = (type, detail) =>
  isWeatherFlash(detail) ? "WEATHER_FLASH" : type;

const formatPriority = (p) => PRIORITY_LABELS[p] || p;

const formatTimeRemaining = (endsAt) => {
  if (!endsAt) return "";
  const diff = new Date(endsAt) - new Date();
  if (diff <= 0) return "Expiré";
  const totalMinutes = Math.floor(diff / 60_000);
  const hours = Math.floor(totalMinutes / 60);
  const days = Math.floor(hours / 24);
  if (days > 0) return `${days}j ${hours % 24}h restant`;
  if (hours > 0) return `${hours}h ${totalMinutes % 60}min restant`;
  return `${totalMinutes}min restant`;
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString("fr-FR", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const FILTERS = [
  { key: null, label: "Toutes" },
  { key: "ACTIVE", label: "🎯 Actives" },
  { key: "WEATHER_FLASH", label: "⚡ Flash météo" },
  { key: "SUCCESS", label: "✅ Réussies" },
  { key: "EXPIRED", label: "⏰ Expirées" },
  { key: "FAILED", label: "❌ Échouées" },
];

export default function MissionsPage() {
  const navigate = useNavigate();
  const [missions, setMissions] = useState([]);
  const [details, setDetails] = useState({});
  const [filter, setFilter] = useState("ACTIVE");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [firstArenaId, setFirstArenaId] = useState(null);
  const [showForecast, setShowForecast] = useState(false);

  const teamId = sessionStorage.getItem("insport_team_id");

  useEffect(() => {
    areneAPI.getAll().then(arenas => {
      if (Array.isArray(arenas) && arenas.length > 0) {
        setFirstArenaId(arenas[0].id);
      }
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!teamId) {
      setLoading(false);
      return;
    }
    loadMissions();
  }, [filter]);

  const loadMissions = async () => {
    try {
      setLoading(true);
      setError(null);

      // WEATHER_FLASH is a client-side sub-filter of ACTIVE missions
      const serverFilter = filter === "WEATHER_FLASH" ? "ACTIVE" : filter;
      const data = await missionAPI.getByTeam(teamId, serverFilter);
      let list = Array.isArray(data) ? data : [];

      // Fetch details for each mission (to get description and payload)
      const detailResults = await Promise.all(
        list.map((m) => missionAPI.getById(m.id).catch(() => null))
      );
      const detailMap = {};
      for (const d of detailResults) {
        if (d) detailMap[d.id] = d;
      }
      setDetails(detailMap);

      // Apply client-side WEATHER_FLASH filter
      if (filter === "WEATHER_FLASH") {
        list = list.filter((m) => isWeatherFlash(detailMap[m.id]));
      }

      setMissions(list);
    } catch (err) {
      setError("Erreur lors du chargement des missions");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (!teamId) {
    return (
      <div className="missions-container">
        <Header />
        <main className="missions-content">
          <div className="missions-hero">
            <h1 className="missions-title">🎯 Missions</h1>
          </div>
          <div className="missions-no-team">
            <span className="missions-no-team__icon">⚠️</span>
            <p className="missions-no-team__text">
              Rejoins une équipe pour accéder aux missions dynamiques !
            </p>
            <button
              className="missions-no-team__link"
              onClick={() => navigate("/team")}
            >
              Rejoindre une équipe
            </button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="missions-container">
      <Header />

      <main className="missions-content">
        <div className="missions-hero">
          <h1 className="missions-title">🎯 Missions</h1>
          <p className="missions-subtitle">
            Complète des missions pour gagner des points et de l'XP
          </p>
        </div>

        {/* Feature 3 — Prévisions météo 24h */}
        {firstArenaId && (
          <div style={{ marginBottom: "16px" }}>
            <button
              onClick={() => setShowForecast(v => !v)}
              style={{
                background: "rgba(99,102,241,0.1)",
                border: "1px solid rgba(99,102,241,0.3)",
                color: "#a5b4fc",
                borderRadius: "10px",
                padding: "8px 16px",
                fontSize: "0.82rem",
                fontWeight: "600",
                cursor: "pointer",
                width: "100%",
                textAlign: "left",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <span>📅 Prévisions météo 24h — fenêtres de bonus</span>
              <span>{showForecast ? "▲" : "▼"}</span>
            </button>
            {showForecast && (
              <WeatherForecastPanel arenaId={firstArenaId} />
            )}
          </div>
        )}

        {/* Filter tabs */}
        <div className="missions-filters">
          {FILTERS.map((f) => (
            <button
              key={f.key ?? "all"}
              className={`missions-filter-btn${filter === f.key ? " missions-filter-btn--active" : ""}${f.key === "WEATHER_FLASH" ? " missions-filter-btn--weather" : ""}`}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {error && <div className="missions-error">{error}</div>}

        {loading && (
          <div className="missions-loading">
            <div className="missions-loading__spinner" />
            <p>Chargement des missions…</p>
          </div>
        )}

        {!loading && !error && (
          <>
            {missions.length === 0 ? (
              <div className="missions-empty">
                <div className="missions-empty__icon">
                  {filter === "WEATHER_FLASH" ? "⚡" : filter === "ACTIVE" ? "🔍" : "📋"}
                </div>
                <p className="missions-empty__text">
                  {filter === "WEATHER_FLASH"
                    ? "Aucune alerte météo active"
                    : filter === "ACTIVE"
                    ? "Aucune mission active pour le moment"
                    : `Aucune mission ${(FILTERS.find((f) => f.key === filter)?.label || "").replace(/[^\w\sÀ-ÿ]/g, "").trim().toLowerCase()}`}
                </p>
                <p className="missions-empty__hint">
                  {filter === "WEATHER_FLASH"
                    ? "Les alertes météo sont générées lors d'événements extrêmes"
                    : "Les missions sont générées automatiquement chaque jour à 6h"}
                </p>
              </div>
            ) : (
              <div className="missions-list">
                {missions.map((mission) => {
                  const detail = details[mission.id];
                  const weatherFlash = isWeatherFlash(detail);
                  const progressPct = Math.min(
                    100,
                    (mission.progressCurrent / mission.progressTarget) * 100
                  );
                  const isUrgent =
                    mission.status === "ACTIVE" &&
                    mission.endsAt &&
                    new Date(mission.endsAt) - new Date() < 86_400_000;

                  const eventType = detail?.payload?.eventType;
                  const eventStartsAt = detail?.payload?.eventStartsAt;
                  const arenaName = detail?.payload?.arenaName;

                  return (
                    <div
                      key={mission.id}
                      className={`mission-card mission-card--${mission.status}${weatherFlash ? " mission-card--weather-flash" : ""}`}
                    >
                      {/* Weather flash alert banner */}
                      {weatherFlash && (
                        <div className="weather-flash-banner">
                          <span className="weather-flash-banner__icon">
                            {getWeatherEventIcon(eventType)}
                          </span>
                          <span className="weather-flash-banner__label">
                            ALERTE {eventType || "MÉTÉO EXTRÊME"}
                          </span>
                          {eventStartsAt && (
                            <span className="weather-flash-banner__countdown">
                              Événement dans {formatTimeRemaining(eventStartsAt)}
                            </span>
                          )}
                        </div>
                      )}

                      {/* Header */}
                      <div className="mission-card__header">
                        <div className="mission-card__left">
                          <span className={`mission-card__type mission-card__type--${getMissionTypeKey(mission.type, detail)}`}>
                            {getMissionTypeLabel(mission.type, detail)}
                          </span>
                          <span className={`mission-card__priority mission-card__priority--${mission.priority}`}>
                            {formatPriority(mission.priority)}
                          </span>
                          <h3 className="mission-card__title">{mission.title}</h3>
                        </div>
                        <span className={`mission-card__status mission-card__status--${mission.status}`}>
                          {mission.status === "ACTIVE" && "En cours"}
                          {mission.status === "SUCCESS" && "Réussie ✓"}
                          {mission.status === "EXPIRED" && "Expirée"}
                          {mission.status === "FAILED" && "Échouée"}
                        </span>
                      </div>

                      {/* Description */}
                      {detail?.description && (
                        <p className="mission-card__description">{detail.description}</p>
                      )}

                      {/* Weather flash arena info */}
                      {weatherFlash && arenaName && (
                        <div className="weather-flash-arena">
                          📍 Arène cible : <strong>{arenaName}</strong>
                        </div>
                      )}

                      {/* Progress bar */}
                      <div className="mission-card__progress">
                        <div className="mission-card__progress-bar">
                          <div
                            className={`mission-card__progress-fill mission-card__progress-fill--${mission.status === "SUCCESS" ? "SUCCESS" : "ACTIVE"}`}
                            style={{ width: `${progressPct}%` }}
                          />
                        </div>
                        <span className="mission-card__progress-text">
                          {mission.progressCurrent}/{mission.progressTarget}
                        </span>
                      </div>

                      {/* Footer */}
                      <div className="mission-card__footer">
                        <div className="mission-card__rewards">
                          <span className="mission-card__reward mission-card__reward--points">
                            🏆 +{mission.rewardTeamPoints} pts
                          </span>
                          {detail?.rewardTeamXp > 0 && (
                            <span className="mission-card__reward mission-card__reward--xp">
                              ⭐ +{detail.rewardTeamXp} XP
                            </span>
                          )}
                        </div>
                        {mission.status === "ACTIVE" && mission.endsAt && (
                          <span
                            className={`mission-card__timer${isUrgent ? " mission-card__timer--urgent" : ""}`}
                          >
                            ⏳ {formatTimeRemaining(mission.endsAt)}
                          </span>
                        )}
                        {mission.status === "SUCCESS" && detail?.completedAt && (
                          <span className="mission-card__completed">
                            Terminée le {formatDate(detail.completedAt)}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
