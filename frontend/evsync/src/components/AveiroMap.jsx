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

// Fix Leaflet’s default icon paths
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

export default function AveiroMap({ operatorId }) {
  const router = useRouter();

  // Shared state:
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedStation, setSelectedStation] = useState(null);

  // Operator‐only state:
  const [isAdding, setIsAdding] = useState(false);

  // Consumer‐only state:
  const [outlets, setOutlets] = useState([]);
  const [loadingOutlets, setLoadingOutlets] = useState(false);
  const [reservationMsg, setReservationMsg] = useState("");
  const [chosenOutletId, setChosenOutletId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  // 1) On mount, decide which fetch to do—operator vs consumer:
  useEffect(() => {
    async function fetchStations() {
      try {
        if (operatorId) {
          // Operator mode: fetch only stations for that operator
          const resp = await fetch(
            `http://localhost:8080/charging-station/operator/${operatorId}`
          );
          if (!resp.ok) throw new Error("Failed to load operator’s stations");
          const data = await resp.json();
          setStations(data);
        } else {
          // Consumer mode: fetch all stations, then filter AVAILABLE
          const resp = await fetch("http://localhost:8080/charging-station");
          if (!resp.ok) throw new Error("Failed to load stations");
          const data = await resp.json();
          const available = data.filter((s) => s.status === "AVAILABLE");
          setStations(available);
        }
      } catch (err) {
        console.error(err);
        setStations([]);
      } finally {
        setLoading(false);
      }
    }
    fetchStations();
  }, [operatorId]);

  // 2) When a station is clicked (selectedStation changed), act accordingly:
  useEffect(() => {
    if (!selectedStation || operatorId) return;

    // Consumer clicked a station: fetch its AVAILABLE outlets
    setLoadingOutlets(true);
    (async () => {
      try {
        const resp = await fetch(
          `http://localhost:8080/charging-station/ChargingOutlets/${selectedStation.id}`
        );
        if (!resp.ok) throw new Error("Failed to load outlets");
        const data = await resp.json();
        const availableOutlets = data.filter((o) => o.status === "AVAILABLE");
        setOutlets(availableOutlets);
      } catch (err) {
        console.error("Error fetching outlets:", err);
        setOutlets([]);
      } finally {
        setLoadingOutlets(false);
      }
    })();
  }, [selectedStation, operatorId]);

  // 3) Operator: clicking on the map background to add a new station
  function StationClickHandler() {
    useMapEvents({
      click(e) {
        if (!isAdding) return;
        const { lat, lng } = e.latlng;
        router.push(
          `/station/new?operatorId=${operatorId}&lat=${lat}&lon=${lng}`
        );
      },
    });
    return null;
  }

  // 4) Consumer: handle reservation submission
  function handleReserve(e) {
    e.preventDefault();
    setReservationMsg("");

    if (!chosenOutletId || !startTime || !endTime) {
      setReservationMsg("All fields are required.");
      return;
    }
    // Store in localStorage
    const consumerId = localStorage.getItem("consumerId") || "guest";
    const newRes = {
      id: Date.now(),
      consumerId,
      stationId: selectedStation.id,
      outletId: Number(chosenOutletId),
      startTime,
      endTime,
    };
    const stored = JSON.parse(localStorage.getItem("reservations") || "[]");
    stored.push(newRes);
    localStorage.setItem("reservations", JSON.stringify(stored));
    setReservationMsg("Reservation saved!");
    setChosenOutletId("");
    setStartTime("");
    setEndTime("");
  }

  if (loading) {
    return (
      <div className="flex h-full w-full items-center justify-center">
        <p className="text-lg">Loading map…</p>
      </div>
    );
  }

  return (
    <div className="h-full w-full">
      <div className="flex items-center justify-between px-4 py-2 bg-gray-100 border-b">
        <h2 className="text-xl font-semibold">
          {operatorId
            ? `Operator #${operatorId} – Manage Stations`
            : "Consumer View – Reserve a Slot"}
        </h2>

        {operatorId && (
          <button
            onClick={() => setIsAdding((prev) => !prev)}
            className={`px-3 py-1 rounded text-white ${
              isAdding ? "bg-red-600 hover:bg-red-700" : "bg-green-600 hover:bg-green-700"
            } transition`}
          >
            {isAdding ? "Cancel Add" : "Add Station"}
          </button>
        )}
      </div>

      <MapContainer
        center={[40.6405, -8.6538]}
        zoom={13}
        className="h-[calc(100vh-3rem)] w-full"
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

        {stations.map((st) => (
          <Marker
            key={st.id}
            position={[st.latitude, st.longitude]}
            eventHandlers={{
              click: () => {
                setSelectedStation(st);
                setReservationMsg("");
              },
            }}
          >
            <Popup>
              <div className="w-64">
                <p className="font-bold">
                  Station #{st.id} ({st.latitude.toFixed(5)},{" "}
                  {st.longitude.toFixed(5)})
                </p>
                <p>Status: {st.status}</p>

                {operatorId ? (
                  // Operator popup content:
                  <>
                    <p className="text-sm text-gray-600 mt-2">
                      (Click marker to configure…)
                    </p>
                    <button
                      onClick={() =>
                        router.push(
                          `/station?stationId=${st.id}&operatorId=${operatorId}`
                        )
                      }
                      className="mt-2 w-full bg-blue-600 text-white py-1 rounded hover:bg-blue-700 transition text-sm"
                    >
                      Configure Station
                    </button>
                  </>
                ) : (
                  // Consumer popup content:
                  <>
                    {loadingOutlets ? (
                      <p className="mt-2 text-sm">Loading outlets…</p>
                    ) : outlets.length > 0 ? (
                      <form onSubmit={handleReserve} className="space-y-2 mt-2">
                        <div>
                          <label
                            htmlFor="outletSelect"
                            className="block text-sm text-gray-700"
                          >
                            Choose Outlet
                          </label>
                          <select
                            id="outletSelect"
                            className="w-full border px-2 py-1 rounded focus:outline-none"
                            value={chosenOutletId}
                            onChange={(e) => setChosenOutletId(e.target.value)}
                          >
                            <option value="">-- Select --</option>
                            {outlets.map((o) => (
                              <option key={o.id} value={o.id}>
                                Outlet #{o.id} ({o.maxPower} kW)
                              </option>
                            ))}
                          </select>
                        </div>
                        <div>
                          <label
                            htmlFor="startTime"
                            className="block text-sm text-gray-700"
                          >
                            Start Time
                          </label>
                          <input
                            id="startTime"
                            type="datetime-local"
                            className="w-full border px-2 py-1 rounded focus:outline-none"
                            value={startTime}
                            onChange={(e) => setStartTime(e.target.value)}
                          />
                        </div>
                        <div>
                          <label
                            htmlFor="endTime"
                            className="block text-sm text-gray-700"
                          >
                            End Time
                          </label>
                          <input
                            id="endTime"
                            type="datetime-local"
                            className="w-full border px-2 py-1 rounded focus:outline-none"
                            value={endTime}
                            onChange={(e) => setEndTime(e.target.value)}
                          />
                        </div>
                        {reservationMsg && (
                          <p className="text-green-600 text-sm">
                            {reservationMsg}
                          </p>
                        )}
                        <button
                          type="submit"
                          className="w-full bg-green-600 text-white py-1 rounded hover:bg-green-700 transition text-sm"
                        >
                          Reserve
                        </button>
                      </form>
                    ) : (
                      <p className="mt-2 text-sm text-gray-600">
                        No available outlets.
                      </p>
                    )}
                  </>
                )}
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Operator’s “click‐to‐add‐station” handler */}
        {operatorId && isAdding && <StationClickHandler />}
      </MapContainer>
    </div>
  );
}
