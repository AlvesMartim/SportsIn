import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

import PointModal from "../components/PointModal.jsx";
import { getPoints } from "../api/points.js";

// Fix des icônes Leaflet pour Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

function getIconForStatus(status) {
  let className = "marker-base";

  if (status === "TEAM_RED") className += " marker-red";
  else if (status === "TEAM_BLUE") className += " marker-blue";
  else className += " marker-neutral";

  return L.divIcon({
    className,
    iconSize: [24, 24],
    iconAnchor: [12, 24],
  });
}

const CENTER_IDF = [48.8566, 2.3522];

function MapPage() {
  const [points, setPoints] = useState([]);
  const [selectedPoint, setSelectedPoint] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getPoints();
        setPoints(data);
      } catch (err) {
        console.error("Erreur chargement points", err);
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  return (
    <div style={{ width: "100vw", height: "100vh" }}>
      <MapContainer
        center={CENTER_IDF}
        zoom={12}
        style={{ width: "100%", height: "100%" }}
      >
        <TileLayer
          attribution="&copy; OpenStreetMap contributors"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {loading && (
          <div
            style={{
              position: "absolute",
              top: 70,
              left: 10,
              background: "white",
              padding: "6px 10px",
              borderRadius: "4px",
              fontSize: "12px",
              zIndex: 1000,
            }}
          >
            Chargement des points...
          </div>
        )}

        {points.map((p) => (
          <Marker
            key={p.id}
            position={[p.lat, p.lng]}
            icon={getIconForStatus(p.status)}
            eventHandlers={{ click: () => setSelectedPoint(p) }}
          />
        ))}
      </MapContainer>

      <PointModal
        point={selectedPoint}
        onClose={() => setSelectedPoint(null)}
      />
    </div>
  );
}

export default MapPage;