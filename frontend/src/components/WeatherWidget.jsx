import { useState, useEffect } from "react";
import { weatherAPI } from "../api/api.js";

const TAG_ICONS = {
  RAIN: "🌧️", HEAVY_RAIN: "🌧️", WIND: "💨", HEAT: "🔥",
  SNOW: "❄️", STORM: "⛈️", EXTREME: "🌪️", COLD: "🥶",
};

const TAG_LABELS = {
  RAIN: "Pluie", HEAVY_RAIN: "Fortes pluies", WIND: "Vent fort",
  HEAT: "Chaleur", SNOW: "Neige", STORM: "Orage", EXTREME: "Conditions extrêmes", COLD: "Grand froid",
};

function getWeatherEmoji(weatherMain, tags = []) {
  if (tags.includes("STORM")) return "⛈️";
  if (tags.includes("SNOW")) return "❄️";
  if (tags.includes("RAIN")) return "🌧️";
  if (tags.includes("WIND")) return "💨";
  if (tags.includes("HEAT")) return "🔥";
  if (tags.includes("COLD")) return "🥶";
  const main = (weatherMain || "").toLowerCase();
  if (main.includes("clear")) return "☀️";
  if (main.includes("cloud")) return "☁️";
  if (main.includes("rain")) return "🌧️";
  if (main.includes("snow")) return "❄️";
  return "🌤️";
}

function getHardshipLabel(hardshipIndex) {
  if (hardshipIndex >= 1.4) return { label: "Très difficile", color: "#ef4444" };
  if (hardshipIndex >= 1.2) return { label: "Difficile", color: "#f97316" };
  if (hardshipIndex >= 1.05) return { label: "Challengeant", color: "#eab308" };
  return { label: "Favorables", color: "#22c55e" };
}

export default function WeatherWidget({ lat, lng, arenaId, sport, compact = false, className = "" }) {
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(true);
  const [unavailable, setUnavailable] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setWeather(null);
    setUnavailable(false);
    setLoading(true);

    const load = async () => {
      try {
        let data;
        if (arenaId) {
          data = await weatherAPI.getForArena(arenaId, sport);
        } else if (lat != null && lng != null) {
          data = await weatherAPI.getCurrent(lat, lng, sport);
        }
        if (cancelled) return;
        if (data && data.available) {
          setWeather(data);
        } else {
          setUnavailable(true);
        }
      } catch (_) {
        if (!cancelled) setUnavailable(true);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, [lat, lng, arenaId, sport]);

  if (loading) {
    return (
      <div className={`weather-widget weather-widget--loading ${className}`}>
        <span className="weather-widget__spinner" /> Chargement météo…
      </div>
    );
  }

  if (unavailable || !weather) {
    if (compact) return null;
    return (
      <div className={`weather-widget weather-widget--unavailable ${className}`}>
        <span style={{ opacity: 0.5 }}>🌫️</span>
        <span style={{ color: "#64748b", fontSize: "0.82rem" }}>
          Météo indisponible — le backend météo n'est peut-être pas démarré
        </span>
      </div>
    );
  }

  const emoji = getWeatherEmoji(weather.weatherMain, weather.tags || []);
  const windKmh = weather.windSpeedMps != null ? (weather.windSpeedMps * 3.6).toFixed(0) : null;
  const hardshipInfo = sport ? getHardshipLabel(weather.hardshipIndex || 1.0) : null;
  const isExtreme = weather.isExtreme;
  const influencePct = sport ? ((weather.influenceBonus || 0) * 100).toFixed(0) : null;

  if (compact) {
    return (
      <div className={`weather-widget weather-widget--compact ${isExtreme ? "weather-widget--extreme" : ""} ${className}`}>
        <span className="weather-widget__emoji">{emoji}</span>
        <span className="weather-widget__temp">{(weather.temperatureC || 0).toFixed(1)}°C</span>
        {windKmh && <span className="weather-widget__wind">💨 {windKmh} km/h</span>}
        {sport && weather.hardshipIndex > 1.01 && (
          <span className="weather-widget__bonus" style={{ color: hardshipInfo.color }}>
            +{influencePct}% influence
          </span>
        )}
      </div>
    );
  }

  return (
    <div className={`weather-widget ${isExtreme ? "weather-widget--extreme" : ""} ${className}`}>
      {isExtreme && (
        <div className="weather-widget__alert">
          ⚠️ Conditions extrêmes — Bonus d'influence amplifié !
        </div>
      )}

      <div className="weather-widget__header">
        <span className="weather-widget__emoji-lg">{emoji}</span>
        <div className="weather-widget__main">
          <span className="weather-widget__temp-lg">{(weather.temperatureC || 0).toFixed(1)} °C</span>
          <span className="weather-widget__desc">{weather.description || weather.weatherMain || ""}</span>
        </div>
        {sport && hardshipInfo && (
          <div className="weather-widget__conditions" style={{ color: hardshipInfo.color }}>
            <span className="weather-widget__conditions-label">Conditions</span>
            <span className="weather-widget__conditions-value">{hardshipInfo.label}</span>
          </div>
        )}
      </div>

      <div className="weather-widget__stats">
        {windKmh && (
          <div className="weather-widget__stat">
            <span>💨</span>
            <span>{windKmh} km/h</span>
          </div>
        )}
        {weather.precipitationMm > 0 && (
          <div className="weather-widget__stat">
            <span>🌧️</span>
            <span>{weather.precipitationMm.toFixed(1)} mm</span>
          </div>
        )}
        {sport && weather.hardshipIndex > 1.0 && (
          <div className="weather-widget__stat weather-widget__stat--bonus">
            <span>⚡</span>
            <span>+{influencePct}% influence si victoire</span>
          </div>
        )}
      </div>

      {(weather.tags || []).length > 0 && (
        <div className="weather-widget__tags">
          {weather.tags.filter(t => t !== "EXTREME").map(tag => (
            <span key={tag} className="weather-widget__tag">
              {TAG_ICONS[tag] || "🏷️"} {TAG_LABELS[tag] || tag}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
