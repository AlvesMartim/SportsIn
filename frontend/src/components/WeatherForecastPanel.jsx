import { useState, useEffect } from "react";
import { weatherAPI } from "../api/api.js";

const TAG_ICONS = {
  RAIN: "🌧️", WIND: "💨", HEAT: "🔥", SNOW: "❄️",
  STORM: "⛈️", EXTREME: "🌪️", COLD: "🥶",
};

function formatHour(isoString) {
  const d = new Date(isoString);
  return d.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
}

function formatDay(isoString) {
  const d = new Date(isoString);
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  if (d.toDateString() === today.toDateString()) return "Aujourd'hui";
  if (d.toDateString() === tomorrow.toDateString()) return "Demain";
  return d.toLocaleDateString("fr-FR", { weekday: "short", day: "numeric" });
}

function getWindowBg(window) {
  if (window.isExtreme) return "linear-gradient(135deg, #2d0a0a, #3d1515)";
  if (window.influenceMultiplier > 1.15) return "linear-gradient(135deg, #0f2027, #1a3a2a)";
  return "rgba(255,255,255,0.03)";
}

function getWindowBorder(window) {
  if (window.isExtreme) return "1px solid rgba(239,68,68,0.5)";
  if (window.influenceMultiplier > 1.15) return "1px solid rgba(34,197,94,0.3)";
  return "1px solid rgba(255,255,255,0.07)";
}

export default function WeatherForecastPanel({ arenaId, arenaName }) {
  const [forecast, setForecast] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!arenaId) return;
    let cancelled = false;
    setLoading(true);

    weatherAPI.getForecastForArena(arenaId)
      .then(data => { if (!cancelled) setForecast(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [arenaId]);

  if (loading) {
    return (
      <div style={{ color: "#64748b", fontSize: "0.85rem", padding: "12px 0" }}>
        ⏳ Chargement des prévisions…
      </div>
    );
  }

  if (!forecast || !forecast.windows || forecast.windows.length === 0) {
    return (
      <div style={{ color: "#64748b", fontSize: "0.85rem", padding: "12px 0" }}>
        🌫️ Prévisions indisponibles
      </div>
    );
  }

  const windows = forecast.windows;

  return (
    <div className="forecast-panel">
      <div className="forecast-panel__header">
        <span className="forecast-panel__title">📅 Prévisions 24h</span>
        {forecast.arenaName && (
          <span className="forecast-panel__arena">{forecast.arenaName}</span>
        )}
        {forecast.hasExtremeEvent && (
          <span className="forecast-panel__extreme-badge">
            ⚡ Événement extrême prévu
          </span>
        )}
      </div>

      <div className="forecast-panel__timeline">
        {windows.map((w, i) => {
          const tags = (w.tags || []).filter(t => t !== "EXTREME");
          const multiplierPct = ((w.influenceMultiplier - 1) * 100).toFixed(0);

          return (
            <div
              key={i}
              className={`forecast-window${w.isExtreme ? " forecast-window--extreme" : w.influenceMultiplier > 1.1 ? " forecast-window--bonus" : ""}`}
              style={{
                background: getWindowBg(w),
                border: getWindowBorder(w),
              }}
            >
              <div className="forecast-window__time">
                <span className="forecast-window__day">{formatDay(w.at)}</span>
                <span className="forecast-window__hour">{formatHour(w.at)}</span>
              </div>

              <div className="forecast-window__info">
                <span className="forecast-window__temp">
                  {w.temperatureC != null ? `${w.temperatureC.toFixed(0)}°C` : "—"}
                </span>
                <div className="forecast-window__tags">
                  {tags.length > 0
                    ? tags.map(t => <span key={t}>{TAG_ICONS[t] || "🏷️"}</span>)
                    : <span style={{ color: "#475569", fontSize: "0.8rem" }}>☀️ Clément</span>
                  }
                  {w.isExtreme && <span title="Extrême">🌪️</span>}
                </div>
              </div>

              <div className="forecast-window__bonus">
                {w.influenceMultiplier > 1.01 ? (
                  <span
                    className="forecast-window__mult"
                    style={{ color: w.isExtreme ? "#f87171" : "#4ade80" }}
                  >
                    ×{w.influenceMultiplier.toFixed(1)}
                    {Number(multiplierPct) > 0 && (
                      <span style={{ fontSize: "0.65rem" }}> (+{multiplierPct}%)</span>
                    )}
                  </span>
                ) : (
                  <span style={{ color: "#475569", fontSize: "0.75rem" }}>×1.0</span>
                )}
                <span className="forecast-window__label">influence</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
