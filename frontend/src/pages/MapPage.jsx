import React, { useState, useEffect, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap, Polyline, Polygon } from "react-leaflet";
import { useNavigate } from "react-router-dom";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import { areneAPI, routeAPI, zoneAPI, gameAPI, equipeAPI } from "../api/api.js";
import { useAuth } from "../context/AuthContext.jsx";
import Header from "../components/Header.jsx";
import "../styles/map.css";

// Fix des icônes Leaflet pour Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

// Icônes personnalisées
const playerIcon = L.icon({
  iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png",
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const areneIcon = L.icon({
  iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png",
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const CENTER_FRANCE = [46.2276, 2.2137];

function MapController({ playerLocation, onRecenter }) {
  const map = useMap();

  const handleRecenter = () => {
    if (playerLocation) {
      map.setView(playerLocation, 15, { animate: true, duration: 0.5 });
    }
  };

  if (onRecenter) {
    onRecenter.current = handleRecenter;
  }

  return null;
}

const TEAM_COLORS = {
  1: "#3b82f6",
  2: "#ef4444",
  3: "#22c55e",
  4: "#f59e0b",
  5: "#8b5cf6",
  default: "#6b7280",
};

const getTeamColor = (teamId) => TEAM_COLORS[teamId] || TEAM_COLORS.default;

function MapPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [arenes, setArenes] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [zones, setZones] = useState([]);
  const [playerLocation, setPlayerLocation] = useState(null);
  const [mapCenter, setMapCenter] = useState(CENTER_FRANCE);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [geoError, setGeoError] = useState(null);
  const recenterRef = useRef(null);
  const [showRoutes, setShowRoutes] = useState(true);
  const [showZones, setShowZones] = useState(true);
  const [showArenas, setShowArenas] = useState(true);
  const [launchingGame, setLaunchingGame] = useState(null);
  const [showLegend, setShowLegend] = useState(false);

  const handleLaunchGame = async (arene) => {
    const teamId = localStorage.getItem("insport_team_id");

    if (!teamId) {
      navigate("/team");
      return;
    }

    try {
      setLaunchingGame(arene.id);
      const team = await equipeAPI.getById(teamId);

      const gameData = {
        pointId: arene.id,
        sport: arene.sportsDisponibles?.[0] ? { code: arene.sportsDisponibles[0] } : null,
        creatorTeam: team,
      };

      const game = await gameAPI.create(gameData);
      navigate(`/game/lobby/${game.id}`);
    } catch (err) {
      console.error("Erreur lors de la création du jeu:", err);
    } finally {
      setLaunchingGame(null);
    }
  };

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setPlayerLocation([latitude, longitude]);
          setMapCenter([latitude, longitude]);
        },
        (err) => {
          setGeoError("Localisation non disponible");
          setMapCenter(CENTER_FRANCE);
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    }
  }, []);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const [arenesData, routesData, zonesData] = await Promise.all([
          areneAPI.getAll().catch(() => []),
          routeAPI.getAll().catch(() => []),
          zoneAPI.getAll().catch(() => []),
        ]);

        setArenes(Array.isArray(arenesData) ? arenesData : []);
        setRoutes(Array.isArray(routesData) ? routesData : []);
        setZones(Array.isArray(zonesData) ? zonesData : []);
        setError(null);
      } catch (err) {
        setError(`Erreur: ${err.message}`);
        setArenes([]);
        setRoutes([]);
        setZones([]);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  return (
    <div className="map-page">
      <Header />

      <div className="map-container">
        <MapContainer center={mapCenter} zoom={playerLocation ? 15 : 6} className="map-leaflet">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <MapController playerLocation={playerLocation} onRecenter={recenterRef} />

          {playerLocation && (
            <Marker position={playerLocation} icon={playerIcon}>
              <Popup className="map-popup">
                <div className="map-popup-content">
                  <h4>Ma position</h4>
                  <p>{user?.username || "Joueur"}</p>
                </div>
              </Popup>
            </Marker>
          )}

          {showZones && zones.map((zone) => {
            const points = zone.points || [];
            if (points.length < 3) return null;
            const positions = points.map((p) => [p.latitude, p.longitude]);
            const color = getTeamColor(zone.controllingTeamId);

            return (
              <Polygon
                key={zone.id}
                positions={positions}
                pathOptions={{ color, fillColor: color, fillOpacity: 0.2, weight: 2 }}
              >
                <Popup className="map-popup">
                  <div className="map-popup-content">
                    <h4>{zone.nom || `Zone ${zone.id}`}</h4>
                    <p className="map-popup-meta">
                      {zone.controllingTeamId ? `Contrôlée par Équipe ${zone.controllingTeamId}` : "Zone libre"}
                    </p>
                  </div>
                </Popup>
              </Polygon>
            );
          })}

          {showRoutes && routes.map((route) => {
            const points = route.points || [];
            if (points.length < 2) return null;
            const positions = points.map((p) => [p.latitude, p.longitude]);

            return (
              <Polyline
                key={route.id}
                positions={positions}
                pathOptions={{ color: "#f59e0b", weight: 4, opacity: 0.8, dashArray: "8, 8" }}
              >
                <Popup className="map-popup">
                  <div className="map-popup-content">
                    <h4>{route.nom || `Route ${route.id}`}</h4>
                    {route.bonusActif && <span className="map-popup-badge">Bonus actif</span>}
                  </div>
                </Popup>
              </Polyline>
            );
          })}

          {showArenas && arenes.map((arene) => (
            <Marker key={arene.id} position={[arene.latitude, arene.longitude]} icon={areneIcon}>
              <Popup className="map-popup map-popup--arena">
                <div className="map-popup-content">
                  <h4>{arene.nom || `Arène ${arene.id}`}</h4>
                  {arene.sportsDisponibles?.length > 0 && (
                    <div className="map-popup-sports">
                      {arene.sportsDisponibles.map((sport) => (
                        <span key={sport} className="map-popup-sport">{sport}</span>
                      ))}
                    </div>
                  )}
                  {arene.controllingTeamId && (
                    <p className="map-popup-meta">Contrôlée par Équipe {arene.controllingTeamId}</p>
                  )}
                  <button
                    className="map-popup-action"
                    onClick={() => handleLaunchGame(arene)}
                    disabled={launchingGame === arene.id}
                  >
                    {launchingGame === arene.id ? "Création..." : "⚔️ Lancer un jeu"}
                  </button>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>

        {/* Status bar */}
        <div className="map-status">
          {loading && <span className="map-status-item map-status-item--loading">Chargement...</span>}
          {error && <span className="map-status-item map-status-item--error">{error}</span>}
          {geoError && <span className="map-status-item map-status-item--warning">{geoError}</span>}
          {!loading && !error && (
            <span className="map-status-item map-status-item--success">{arenes.length} arènes</span>
          )}
        </div>

        {/* Controls */}
        <div className="map-controls">
          {playerLocation && (
            <button className="map-control-btn map-control-btn--primary" onClick={() => recenterRef.current?.()}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              Ma position
            </button>
          )}
          <button className="map-control-btn" onClick={() => navigate("/")}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
            </svg>
            Accueil
          </button>
        </div>

        {/* Layer controls */}
        <div className="map-layers">
          <div className="map-layers-header">
            <span>Couches</span>
          </div>
          <label className="map-layer-item">
            <input type="checkbox" checked={showArenas} onChange={(e) => setShowArenas(e.target.checked)} />
            <span className="map-layer-icon map-layer-icon--arena">📍</span>
            <span>Arènes ({arenes.length})</span>
          </label>
          <label className="map-layer-item">
            <input type="checkbox" checked={showZones} onChange={(e) => setShowZones(e.target.checked)} />
            <span className="map-layer-icon map-layer-icon--zone"></span>
            <span>Zones ({zones.length})</span>
          </label>
          <label className="map-layer-item">
            <input type="checkbox" checked={showRoutes} onChange={(e) => setShowRoutes(e.target.checked)} />
            <span className="map-layer-icon map-layer-icon--route"></span>
            <span>Routes ({routes.length})</span>
          </label>
        </div>

        {/* Legend toggle */}
        <button className="map-legend-toggle" onClick={() => setShowLegend(!showLegend)}>
          {showLegend ? "✕" : "🎨"}
        </button>

        {/* Legend */}
        {showLegend && (
          <div className="map-legend">
            <div className="map-legend-header">Légende</div>
            <div className="map-legend-content">
              {Object.entries(TEAM_COLORS)
                .filter(([key]) => key !== "default")
                .map(([teamId, color]) => (
                  <div key={teamId} className="map-legend-item">
                    <span className="map-legend-color" style={{ background: color }}></span>
                    <span>Équipe {teamId}</span>
                  </div>
                ))}
              <div className="map-legend-item">
                <span className="map-legend-color" style={{ background: TEAM_COLORS.default }}></span>
                <span>Non contrôlée</span>
              </div>
              <div className="map-legend-divider"></div>
              <div className="map-legend-item">
                <span className="map-legend-route"></span>
                <span>Route</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default MapPage;
