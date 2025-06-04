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

  // Consumer-only state:
  const [outlets, setOutlets] = useState([]);
  const [loadingOutlets, setLoadingOutlets] = useState(false);
  const [reservationMsg, setReservationMsg] = useState("");

  // *** New: session state ***
  // activeSession: { consumerId, stationId, outletId, startTime }
  const [activeSession, setActiveSession] = useState(null);

  const [chosenOutletId, setChosenOutletId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  // 1) On mount, fetch all stations and filter AVAILABLE
  useEffect(() => {
    async function fetchStations() {
      try {
        const resp = await fetch("http://localhost:8080/charging-station");
        if (!resp.ok) throw new Error("Failed to load stations");
        const data = await resp.json();
        const available = data.filter((s) => s.status === "AVAILABLE");
        setStations(available);
      } catch (err) {
        console.error("Error fetching stations:", err);
      } finally {
        setLoading(false);
      }
    }
    fetchStations();

    // Load any existing activeSession from localStorage
    const savedSession = localStorage.getItem("activeSession");
    if (savedSession) {
      setActiveSession(JSON.parse(savedSession));
    }
  }, []);

  // 2) When a station is selected, fetch its outlets
  useEffect(() => {
    if (!selectedStation) return;
    setLoadingOutlets(true);
    async function fetchOutlets() {
      try {
        const resp = await fetch(
          `http://localhost:8080/charging-station/ChargingOutlets/${selectedStation.id}`
        );
        if (!resp.ok) throw new Error("Failed to load outlets");
        const data = await resp.json();
        // filter only AVAILABLE outlets
        const availableOutlets = data.filter((o) => o.status === "AVAILABLE");
        setOutlets(availableOutlets);
      } catch (err) {
        console.error("Error fetching outlets:", err);
        setOutlets([]);
      } finally {
        setLoadingOutlets(false);
      }
    }
    fetchOutlets();
  }, [selectedStation]);

  // 3) Handle new reservation (unchanged)
  function handleReserve(e) {
    e.preventDefault();
    setReservationMsg("");
    if (!chosenOutletId || !startTime || !endTime) {
      setReservationMsg("All fields are required.");
      return;
    }
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

  // === NEW: Handle starting a charging session ===
  function handleStartSession(outletId) {
    const consumerId = localStorage.getItem("consumerId") || "guest";

    // If another session already active for different outlet, block:
    if (activeSession && activeSession.outletId !== outletId) {
      alert("You already have a session in progress. End it first.");
      return;
    }

    const nowISOString = new Date().toISOString();
    const session = {
      consumerId,
      stationId: selectedStation.id,
      outletId: Number(outletId),
      startTime: nowISOString,
    };
    localStorage.setItem("activeSession", JSON.stringify(session));
    setActiveSession(session);
  }

  // === NEW: Handle ending a charging session ===
  function handleEndSession() {
    if (!activeSession) return;
    // Optionally, record session-end in “sessionHistory” in localStorage:
    const history = JSON.parse(localStorage.getItem("sessionHistory") || "[]");
    history.push({
      ...activeSession,
      endTime: new Date().toISOString(),
    });
    localStorage.setItem("sessionHistory", JSON.stringify(history));

    localStorage.removeItem("activeSession");
    setActiveSession(null);
  }

  if (loading) {
    return (
      <div className="flex h-full w-full items-center justify-center">
        <p className="text-lg">Loading map…</p>
      </div>
    );
  }

  return (
    <div className="h-screen w-full">
      <MapContainer
        center={[40.6405, -8.6538]}
        zoom={13}
        className="h-full w-full"
      >
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {stations.map((station) => (
          <Marker
            key={station.id}
            position={[station.latitude, station.longitude]}
            eventHandlers={{
              click: () => {
                setSelectedStation(station);
                setReservationMsg("");
              },
            }}
          />
        ))}

        {selectedStation && (
          <Popup
            position={[selectedStation.latitude, selectedStation.longitude]}
            onClose={() => {
              setSelectedStation(null);
              setOutlets([]);
              setReservationMsg("");
            }}
          >
            <div className="w-64">
              <h3 className="text-lg font-semibold mb-2">
                Station #{selectedStation.id}
              </h3>

              {loadingOutlets ? (
                <p>Loading outlets…</p>
              ) : (
                <>
                  {/* If there is an active session on this outlet, show session info */}
                  {activeSession &&
                  activeSession.stationId === selectedStation.id ? (
                    <div className="space-y-2">
                      <p className="text-green-700 font-medium">
                        <span className="font-semibold">Session in progress</span>
                        <br />
                        Outlet #{activeSession.outletId} <br />
                        Started:{ " " }
                        {new Date(activeSession.startTime).toLocaleString()}
                      </p>
                      <button
                        onClick={handleEndSession}
                        className="w-full bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition text-sm"
                      >
                        End Session
                      </button>
                    </div>
                  ) : (
                    // Otherwise, show reservation form + “Start Session” buttons
                    <>
                      {outlets.length > 0 ? (
                        <div className="space-y-4">
                          {/* New: “Start Session” UI */}
                          <div>
                            <label className="block text-sm text-gray-700 mb-1">
                              Select an outlet to start session:
                            </label>
                            <select
                              className="w-full border px-2 py-1 rounded focus:outline-none"
                              value={chosenOutletId}
                              onChange={(e) =>
                                setChosenOutletId(e.target.value)
                              }
                            >
                              <option value="">-- Select Outlet --</option>
                              {outlets.map((o) => (
                                <option key={o.id} value={o.id}>
                                  Outlet #{o.id} ({o.maxPower} kW)
                                </option>
                              ))}
                            </select>
                            <button
                              onClick={() => {
                                if (!chosenOutletId) {
                                  alert("Choose an outlet first.");
                                } else {
                                  handleStartSession(chosenOutletId);
                                }
                              }}
                              className="mt-2 w-full bg-orange-600 text-white px-3 py-1 rounded hover:bg-orange-700 transition text-sm"
                            >
                              Start Session
                            </button>
                          </div>

                          {/* Original reservation form */}
                          <form
                            onSubmit={handleReserve}
                            className="space-y-2"
                          >
                            <p className="font-medium">Make a reservation</p>
                            <div>
                              <label
                                htmlFor="resOutlet"
                                className="block text-sm text-gray-700"
                              >
                                Choose Outlet
                              </label>
                              <select
                                id="resOutlet"
                                className="w-full border px-2 py-1 rounded focus:outline-none"
                                value={chosenOutletId}
                                onChange={(e) =>
                                  setChosenOutletId(e.target.value)
                                }
                              >
                                <option value="">-- Select Outlet --</option>
                                {outlets.map((o) => (
                                  <option key={o.id} value={o.id}>
                                    Outlet #{o.id} ({o.maxPower} kW)
                                  </option>
                                ))}
                              </select>
                            </div>

                            <div>
                              <label
                                htmlFor="resStartTime"
                                className="block text-sm text-gray-700"
                              >
                                Start Time
                              </label>
                              <input
                                id="resStartTime"
                                type="datetime-local"
                                className="w-full border px-2 py-1 rounded focus:outline-none"
                                value={startTime}
                                onChange={(e) =>
                                  setStartTime(e.target.value)
                                }
                              />
                            </div>

                            <div>
                              <label
                                htmlFor="resEndTime"
                                className="block text-sm text-gray-700"
                              >
                                End Time
                              </label>
                              <input
                                id="resEndTime"
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
                              className="w-full bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 transition text-sm"
                            >
                              Reserve
                            </button>
                          </form>
                        </div>
                      ) : (
                        <p className="text-gray-600 text-sm">
                          No available outlets.
                        </p>
                      )}
                    </>
                  )}
                </>
              )}
            </div>
          </Popup>
        )}

        {/* Consumer mode never uses StationClickHandler */}
      </MapContainer>
    </div>
  );
}
