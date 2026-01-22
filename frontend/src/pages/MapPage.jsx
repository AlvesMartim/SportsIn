import React, { useState, useEffect, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import { areneAPI } from "../api/api.js";
import { useAuth } from "../context/AuthContext.jsx";

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

const CENTER_FRANCE = [46.2276, 2.2137]; // Centre de la France

// Composant pour contrôler la carte avec le hook useMap
function MapController({ playerLocation, onRecenter }) {
  const map = useMap();

  // Recenter la carte sur la position du joueur
  const handleRecenter = () => {
    if (playerLocation) {
      map.setView(playerLocation, 15, {
        animate: true,
        duration: 0.5
      });
    }
  };

  // Stocker la fonction de recentrage pour l'utiliser depuis le bouton externe
  if (onRecenter) {
    onRecenter.current = handleRecenter;
  }

  return null;
}

function MapPage() {
  const { user } = useAuth();
  const [arenes, setArenes] = useState([]);
  const [playerLocation, setPlayerLocation] = useState(null);
  const [mapCenter, setMapCenter] = useState(CENTER_FRANCE);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [geoError, setGeoError] = useState(null);
  const recenterRef = useRef(null);

  useEffect(() => {
    // Récupérer la géolocalisation de l'utilisateur avec haute précision
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude, accuracy } = position.coords;
          setPlayerLocation([latitude, longitude]);
          setMapCenter([latitude, longitude]);
          console.log(`📍 Localisation du joueur: ${latitude.toFixed(4)}, ${longitude.toFixed(4)}`);
          console.log(`📏 Précision: ±${Math.round(accuracy)} mètres`);
        },
        (err) => {
          console.warn("Géolocalisation non disponible:", err);
          setGeoError("Impossible d'accéder à votre localisation. Géolocalisation non activée ?");
          // Garder le centre par défaut
          setMapCenter(CENTER_FRANCE);
        },
        {
          enableHighAccuracy: true,  // Utiliser le GPS si disponible
          timeout: 10000,             // 10 secondes max
          maximumAge: 0               // Pas de cache
        }
      );
    }
  }, []);

  useEffect(() => {
    async function fetchArenes() {
      try {
        setLoading(true);
        const data = await areneAPI.getAll();
        console.log("Arènes chargées:", data);
        setArenes(Array.isArray(data) ? data : []);
        setError(null);
      } catch (err) {
        console.error("Erreur chargement arènes", err);
        setError(`Erreur: ${err.message}`);
        setArenes([]);
      } finally {
        setLoading(false);
      }
    }

    fetchArenes();
  }, []);

  return (
    <div style={{ width: "100vw", height: "100vh", position: "relative" }}>
      <MapContainer
        center={mapCenter}
        zoom={playerLocation ? 15 : 6}
        style={{ width: "100%", height: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Contrôleur de la carte pour le recentrage */}
        <MapController playerLocation={playerLocation} onRecenter={recenterRef} />

        {/* Marqueur du joueur connecté */}
        {playerLocation && (
          <Marker position={playerLocation} icon={playerIcon} title="Ma position">
            <Popup>
              <div style={{ width: "200px" }}>
                <h3 style={{ margin: "0 0 8px 0" }}>📍 Votre position</h3>
                <p style={{ margin: "4px 0" }}>
                  <strong>Joueur:</strong> {user?.username || "Anonyme"}
                </p>
                <p style={{ margin: "4px 0" }}>
                  <strong>Latitude:</strong> {playerLocation[0].toFixed(4)}
                </p>
                <p style={{ margin: "4px 0" }}>
                  <strong>Longitude:</strong> {playerLocation[1].toFixed(4)}
                </p>
              </div>
            </Popup>
          </Marker>
        )}

        {/* Affichage du statut de chargement */}
        {(loading || error || geoError) && (
          <div
            style={{
              position: "absolute",
              top: 70,
              left: 10,
              background: "white",
              padding: "10px 15px",
              borderRadius: "4px",
              fontSize: "13px",
              zIndex: 1000,
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            {loading && <p>📍 Chargement des arènes...</p>}
            {error && <p style={{ color: "red" }}>⚠️ {error}</p>}
            {geoError && <p style={{ color: "orange" }}>⚠️ {geoError}</p>}
            {!loading && !error && <p>✅ {arenes.length} arènes chargées</p>}
          </div>
        )}

        {/* Affichage des marqueurs pour chaque arène */}
        {arenes.map((arene) => (
          <Marker
            key={arene.id}
            position={[arene.latitude, arene.longitude]}
            icon={areneIcon}
            title={arene.nom}
          >
            <Popup>
              <div style={{ width: "250px" }}>
                <h3 style={{ margin: "0 0 8px 0" }}>🏟️ {arene.nom}</h3>
                <p style={{ margin: "4px 0" }}>
                  <strong>ID:</strong> {arene.id}
                </p>
                <p style={{ margin: "4px 0" }}>
                  <strong>Latitude:</strong> {arene.latitude.toFixed(4)}
                </p>
                <p style={{ margin: "4px 0" }}>
                  <strong>Longitude:</strong> {arene.longitude.toFixed(4)}
                </p>
                {arene.sportsDisponibles && arene.sportsDisponibles.length > 0 && (
                  <p style={{ margin: "4px 0" }}>
                    <strong>Sports:</strong> {arene.sportsDisponibles.join(", ")}
                  </p>
                )}
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {/* Bouton de recentrage */}
      {playerLocation && (
        <button
          onClick={() => recenterRef.current?.()}
          style={{
            position: "absolute",
            top: 80,
            right: 20,
            padding: "10px 16px",
            backgroundColor: "#007bff",
            color: "white",
            border: "none",
            borderRadius: "4px",
            fontSize: "14px",
            fontWeight: "bold",
            cursor: "pointer",
            zIndex: 1001,
            boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
            transition: "all 0.3s ease",
            display: "flex",
            alignItems: "center",
            gap: "6px"
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = "#0056b3";
            e.target.style.boxShadow = "0 4px 8px rgba(0,0,0,0.3)";
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = "#007bff";
            e.target.style.boxShadow = "0 2px 4px rgba(0,0,0,0.2)";
          }}
        >
          📍 Recentrer
        </button>
      )}
    </div>
  );
}

export default MapPage;