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

// ── Fix Leaflet’s default icon paths ──
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

  // ── Shared state: all charging stations ──
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedStation, setSelectedStation] = useState(null);

  // ── Consumer-only state: outlets, reservation/session UI ──
  const [outlets, setOutlets] = useState([]);
  const [loadingOutlets, setLoadingOutlets] = useState(false);
  const [reservationMsg, setReservationMsg] = useState("");
  const [activeSession, setActiveSession] = useState(null);
  const [chosenOutletId, setChosenOutletId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  // === 1) On mount: fetch all stations from backend ===
  useEffect(() => {
    async function fetchStations() {
      try {
        const resp = await fetch("http://localhost:8080/charging-station");
        if (!resp.ok) throw new Error("Failed to load stations");
        const data = await resp.json();

        if (operatorId) {
          // Operator: see all stations regardless of status
          setStations(data);
        } else {
          // Consumer: see only status === "AVAILABLE"
          setStations(data.filter((s) => s.status === "AVAILABLE"));
        }
      } catch (err) {
        console.error("Error fetching stations:", err);
      } finally {
        setLoading(false);
      }
    }
    fetchStations();

    // Load any existing activeSession (for consumer) from localStorage
    const saved = localStorage.getItem("activeSession");
    if (saved) {
      setActiveSession(JSON.parse(saved));
    }
  }, [operatorId]);

  // === 2) When a station is clicked (consumer mode only), fetch its outlets ===
  useEffect(() => {
    if (!selectedStation || operatorId) return; 
    // only consumers fetch outlets for reservations/sessions

    setLoadingOutlets(true);
    async function fetchOutlets() {
      try {
        const resp = await fetch(
          `http://localhost:8080/charging-station/ChargingOutlets/${selectedStation.id}`
        );
        if (!resp.ok) throw new Error("Failed to load outlets");
        const data = await resp.json();
        setOutlets(data.filter((o) => o.status === "AVAILABLE"));
      } catch (err) {
        console.error("Error fetching outlets:", err);
        setOutlets([]);
      } finally {
        setLoadingOutlets(false);
      }
    }
    fetchOutlets();
  }, [selectedStation, operatorId]);

  // === 3) Consumer: handle “Make a Reservation” ===
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

  // === 4) Consumer: handle “Start Session” ===
  function handleStartSession(outletId) {
    const consumerId = localStorage.getItem("consumerId") || "guest";
    if (activeSession && activeSession.outletId !== outletId) {
      alert("You already have a session in progress. End it first.");
      return;
    }
    const nowISOString = new Date().toISOString();
    const sessionObj = {
      consumerId,
      stationId: selectedStation.id,
      outletId: Number(outletId),
      startTime: nowISOString,
    };
    localStorage.setItem("activeSession", JSON.stringify(sessionObj));
    setActiveSession(sessionObj);
  }

  // === 5) Consumer: handle “End Session” ===
  function handleEndSession() {
    if (!activeSession) return;
    const history = JSON.parse(localStorage.getItem("sessionHistory") || "[]");
    history.push({ ...activeSession, endTime: new Date().toISOString() });
    localStorage.setItem("sessionHistory", JSON.stringify(history));
    localStorage.removeItem("activeSession");
    setActiveSession(null);
  }

  // === 6) Operator-only: clicking anywhere on blank map → create a new station ===
  function OperatorMapClick() {
    useMapEvents({
      click: (e) => {
        if (!operatorId) return; // only if we truly have a primitive operatorId
        const { lat, lng } = e.latlng;
        router.push(
          `/station/new?operatorId=${operatorId}&lat=${lat}&lon=${lng}`
        );
      },
    });
    return null;
  }

  if (loading) {
    return (
      <div className="flex h-full w-full items-center justify-center">
        <p className="text-lg">Loading map…</p>
      </div>
    );
  }

  return (
    <MapContainer
      center={[40.6405, -8.6538]}
      zoom={13}
      className="h-full w-full"
    >
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

      {/* 1) Render every station as a Leaflet <Marker> */}
      {stations.map((station) => (
        <Marker
          key={station.id}
          position={[station.latitude, station.longitude]}
          eventHandlers={{
            click: () => setSelectedStation(station),
          }}
        />
      ))}

      {/* 2) If operatorId is a string/number, mount the “click-to-create” handler */}
      {operatorId && <OperatorMapClick />}

      {/* 3) If a station is selected, open its Popup */}
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

            {operatorId ? (
              // ─── Operator’s version of the Popup ───
              <div className="space-y-4">
                <p>
                  <strong>Coordinates:</strong>{" "}
                  {selectedStation.latitude.toFixed(5)},{" "}
                  {selectedStation.longitude.toFixed(5)}
                </p>
                <p>
                  <strong>Status:</strong> {selectedStation.status}
                </p>
                <p>
                  <strong>Operator ID:</strong>{" "}
                  {/* The station’s `operator` field or else fallback to the primitive */}
                  {typeof selectedStation.operator === "object"
                    ? selectedStation.operator.id
                    : operatorId}
                </p>
                <button
                  onClick={() =>
                    router.push(
                      `/station?stationId=${selectedStation.id}&operatorId=${operatorId}`
                    )
                  }
                  className="
                    w-full bg-blue-600 text-white px-3 py-1 
                    rounded hover:bg-blue-700 transition text-sm
                  "
                >
                  Configure Station
                </button>
              </div>
            ) : (
              // ─── Consumer’s version of the Popup ───
              <>
                {loadingOutlets ? (
                  <p>Loading outlets…</p>
                ) : (
                  <>
                    {activeSession &&
                    activeSession.stationId === selectedStation.id ? (
                      <div className="space-y-2">
                        <p className="text-green-700 font-medium">
                          <strong>Session in progress</strong>
                          <br />
                          Outlet #{activeSession.outletId}
                          <br />
                          Started:{" "}
                          {new Date(activeSession.startTime).toLocaleString()}
                        </p>
                        <button
                          onClick={handleEndSession}
                          className="
                            w-full bg-red-600 text-white px-3 py-1 
                            rounded hover:bg-red-700 transition text-sm
                          "
                        >
                          End Session
                        </button>
                      </div>
                    ) : (
                      <>
                        {outlets.length > 0 ? (
                          <div className="space-y-4">
                            {/* Start Session UI */}
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
                                className="
                                  mt-2 w-full bg-orange-600 text-white 
                                  px-3 py-1 rounded hover:bg-orange-700 
                                  transition text-sm
                                "
                              >
                                Start Session
                              </button>
                            </div>

                            {/* Reservation form */}
                            <form onSubmit={handleReserve} className="space-y-2">
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
                                  onChange={(e) =>
                                    setEndTime(e.target.value)
                                  }
                                />
                              </div>

                              {reservationMsg && (
                                <p className="text-green-600 text-sm">
                                  {reservationMsg}
                                </p>
                              )}
                              <button
                                type="submit"
                                className="
                                  w-full bg-green-600 text-white 
                                  px-3 py-1 rounded hover:bg-green-700 
                                  transition text-sm
                                "
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
              </>
            )}
          </div>
        </Popup>
      )}
    </MapContainer>
  );
}
