// src/components/AveiroMap.jsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  useMapEvents,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

export default function AveiroMap({ operatorId }) {
  const API_BASE = "http://localhost:8080"; // adjust if your Spring runs elsewhere
  const router = useRouter();

  const [stations, setStations] = useState([]);     // existing stations
  const [loading, setLoading] = useState(true);     // while GET /charging-station
  const [error, setError] = useState(null);

  // new state: are we in “add mode”?
  const [isAdding, setIsAdding] = useState(false);

  // 1) On mount, fetch all existing stations
  useEffect(() => {
    async function fetchStations() {
      try {
        const resp = await fetch(`${API_BASE}/charging-station`);
        if (!resp.ok) {
          throw new Error(`HTTP ${resp.status}: ${resp.statusText}`);
        }
        const data = await resp.json(); 
        setStations(data);
      } catch (err) {
        console.error("Error loading stations:", err);
        setError("Could not load charging stations from server.");
      } finally {
        setLoading(false);
      }
    }
    fetchStations();
  }, []);

  // 2) Fix Leaflet’s default icon paths so the marker images load from /public/leaflet/images/
  useEffect(() => {
    delete L.Icon.Default.prototype._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: "/leaflet/images/marker-icon-2x.png",
      iconUrl: "/leaflet/images/marker-icon.png",
      shadowUrl: "/leaflet/images/marker-shadow.png",
    });
  }, []);

  // 3) A tiny helper component that, if isAdding===true, traps the next click on the map
  function StationClickHandler() {
    useMapEvents({
      click: async (e) => {
        if (!isAdding) return; 
        const { lat, lng } = e.latlng;

        // Payload for POST /charging-station
        const payload = {
          latitude: lat,
          longitude: lng,
          status: "AVAILABLE",
          operator: { id: Number(operatorId) },
        };

        try {
          const resp = await fetch(`${API_BASE}/charging-station`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });
          if (!resp.ok) {
            throw new Error(`HTTP ${resp.status} ${await resp.text()}`);
          }
          const createdStation = await resp.json();
          // append to state so marker shows up immediately
          setStations((prev) => [...prev, createdStation]);
        } catch (err) {
          console.error("Error creating station:", err);
          alert("Error creating station: " + (err.message || "Unknown"));
        } finally {
          setIsAdding(false); // leave “add mode” after one click
        }
      },
    });
    return null;
  }

  // 4) When you click an existing Marker, navigate to the Station Details page
  function onMarkerClick(station) {
    router.push(`/station?stationId=${station.id}&operatorId=${operatorId}`);
  }

  const aveiroCoords = [40.6405, -8.6538];

  if (loading) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <p className="text-lg">Loading map data…</p>
      </div>
    );
  }
  if (error) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  return (
    <div className="h-screen w-full flex flex-col">
      {/* ======== ADD STATION BUTTON ======== */}
      <div className="p-4 bg-gray-100 border-b border-gray-300 flex items-center space-x-4">
        <button
          onClick={() => setIsAdding(true)}
          className={`px-4 py-2 rounded ${
            isAdding
              ? "bg-yellow-500 text-white hover:bg-yellow-600"
              : "bg-blue-600 text-white hover:bg-blue-700"
          }`}
        >
          {isAdding ? "Click on map to place station..." : "Add Charging Station"}
        </button>
        {isAdding && (
          <span className="text-sm italic text-gray-700">
            (Click anywhere on the map pinpoint a new station)
          </span>
        )}
      </div>

      {/* ======== THE MAP ======== */}
      <div className="flex-1">
        <MapContainer
          center={aveiroCoords}
          zoom={13}
          className="h-full w-full"
        >
          <TileLayer
            attribution={`&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors`}
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {/* Render all existing stations */}
          {stations.map((st) => (
            <Marker
              key={st.id}
              position={[st.latitude, st.longitude]}
              eventHandlers={{
                click: () => onMarkerClick(st),
              }}
            >
              <Popup>
                <div className="text-center">
                  <strong>Station ID: {st.id}</strong>
                  <br />
                  Status: {st.status}
                  <br />
                  (Click marker to configure…)
                </div>
              </Popup>
            </Marker>
          ))}

          {/* If isAdding, this will catch the next click */}
          <StationClickHandler />
        </MapContainer>
      </div>
    </div>
  );
}
