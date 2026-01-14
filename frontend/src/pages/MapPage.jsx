import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import { areneAPI } from "../api/api.js";

// Fix des icônes Leaflet pour Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const CENTER_FRANCE = [46.2276, 2.2137]; // Centre de la France

function MapPage() {
  const [arenes, setArenes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
    <div style={{ width: "100vw", height: "100vh" }}>
      <MapContainer
        center={CENTER_FRANCE}
        zoom={6}
        style={{ width: "100%", height: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Affichage du statut de chargement */}
        {(loading || error) && (
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
            {!loading && !error && <p>✅ {arenes.length} arènes chargées</p>}
          </div>
        )}

        {/* Affichage des marqueurs pour chaque arène */}
        {arenes.map((arene) => (
          <Marker
            key={arene.id}
            position={[arene.latitude, arene.longitude]}
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
    </div>
  );
}

export default MapPage;