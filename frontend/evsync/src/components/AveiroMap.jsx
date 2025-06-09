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
import { API_BASE_URL } from "@/lib/api";

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

export default function AveiroMap({ operatorId, consumerId }) {
  const router = useRouter();

  // ── Shared state ──
  const [stations, setStations]           = useState([]);
  const [loading, setLoading]             = useState(true);
  const [selectedStation, setSelectedStation] = useState(null);

  // ── Consumer-only state ──
  const [outlets, setOutlets]             = useState([]);
  const [loadingOutlets, setLoadingOutlets] = useState(false);
  const [reservationMsg, setReservationMsg] = useState("");
  const [activeSession, setActiveSession]   = useState(null);
  const [chosenOutletId, setChosenOutletId] = useState("");
  const [startTime, setStartTime]           = useState("");
  const [endTime, setEndTime]               = useState("");

  // 1) Load stations & session
  useEffect(() => {
    fetch(API_BASE_URL.CHARGING_STATION)
      .then((r) => r.ok ? r.json() : Promise.reject())
      .then((data) => {
        setStations(operatorId ? data : data.filter((s) => s.status === "AVAILABLE"));
      })
      .catch(console.error)
      .finally(() => setLoading(false));

    if (!operatorId && consumerId) {
      fetch(`${API_BASE_URL.SESSION}/active/${consumerId}`)
        .then((r) => (r.ok ? r.json() : Promise.reject()))
        .then(setActiveSession)
        .catch(() => setActiveSession(null));
    }
  }, [operatorId, consumerId]);

  // 2) Load outlets for consumer
  useEffect(() => {
    if (!selectedStation || operatorId) return;
    setLoadingOutlets(true);
    fetch(API_BASE_URL.CHARGING_STATION_OUTLETS(selectedStation.id))
      .then((r) => r.ok ? r.json() : Promise.reject())
      .then((data) => setOutlets(data.filter((o) => o.status === "AVAILABLE")))
      .catch(console.error)
      .finally(() => setLoadingOutlets(false));
  }, [selectedStation, operatorId]);

  // 3) Reservation via DB
  async function handleReserveApi(e) {
    e.preventDefault();
    setReservationMsg("");
    if (!consumerId) {
      alert("Please log in as consumer first.");
      return;
    }
    if (!chosenOutletId || !startTime || !endTime) {
      setReservationMsg("All fields are required.");
      return;
    }
    try {
      const url = `${API_BASE_URL.RESERVATION}?consumerId=${consumerId}`;
      const body = {
        stationId: selectedStation.id,
        outletId: Number(chosenOutletId),
        startTime,
        endTime,
      };
      const resp = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!resp.ok) throw new Error(await resp.text());
      setReservationMsg("Reservation saved!");
      setChosenOutletId("");
      setStartTime("");
      setEndTime("");
    } catch (err) {
      console.error("Reservation failed", err);
      setReservationMsg("Reservation failed");
    }
  }

  // 4) Session start
  async function handleStartSessionApi(oId) {
    if (!consumerId) {
      alert("Please log in as consumer first.");
      return;
    }
    if (activeSession && activeSession.outletId !== oId) {
      alert("You already have a session in progress.");
      return;
    }
    try {
      const body = { consumerId, stationId: selectedStation.id, outletId: Number(oId) };
      const resp = await fetch(API_BASE_URL.SESSION, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const session = await resp.json();
      setActiveSession(session);
    } catch (err) {
      console.error("Start session failed", err);
      alert("Could not start session");
    }
  }

  // 5) Session end
  async function handleEndSessionApi() {
    if (!activeSession) return;
    try {
      const resp = await fetch(`${API_BASE_URL.SESSION}/${activeSession.id}`, {
        method: "DELETE",
      });
      if (!resp.ok) throw new Error(await resp.text());
      setActiveSession(null);
    } catch (err) {
      console.error("End session failed", err);
      alert("Could not end session");
    }
  }

  // 6) Operator click
  function OperatorMapClick() {
    useMapEvents({
      click: (e) => {
        if (!operatorId) return;
        const { lat, lng } = e.latlng;
        router.push(
          `/station/new?operatorId=${operatorId}&lat=${lat}&lon=${lng}`
        );
      },
    });
    return null;
  }

  if (loading) {
    return <div className="flex h-full w-full items-center justify-center"><p>Loading…</p></div>;
  }

  return (
    <MapContainer center={[40.6405, -8.6538]} zoom={13} className="h-full w-full">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

      {stations.map((s) => (
        <Marker key={s.id} position={[s.latitude, s.longitude]}
          eventHandlers={{ click: () => setSelectedStation(s) }} />
      ))}

      {operatorId && <OperatorMapClick />}

      {selectedStation && (
        <Popup position={[selectedStation.latitude, selectedStation.longitude]}
          onClose={() => {
            setSelectedStation(null);
            setOutlets([]);
            setReservationMsg("");
          }}>
          <div className="w-64">
            <h3 className="text-lg font-semibold mb-2">Station #{selectedStation.id}</h3>

            {/* Operator UI */}
            {operatorId ? (
              <div className="space-y-4">
                <p>
                  <strong>Coords:</strong> {selectedStation.latitude.toFixed(5)}, {selectedStation.longitude.toFixed(5)}
                </p>
                <p><strong>Status:</strong> {selectedStation.status}</p>
                <p><strong>Operator ID:</strong> {selectedStation.operator?.id ?? operatorId}</p>
                <button
                  onClick={() =>
                    router.push(`/station?stationId=${selectedStation.id}&operatorId=${operatorId}`)
                  }
                  className="w-full bg-blue-600 text-white px-3 py-1 rounded"
                >
                  Configure Station
                </button>
              </div>
            ) : (
              /* Consumer UI */
              <>
                {loadingOutlets ? (
                  <p>Loading outlets…</p>
                ) : activeSession && activeSession.stationId === selectedStation.id ? (
                  <div className="space-y-2">
                    <p className="text-green-700 font-medium">
                      <strong>Session in progress</strong><br />
                      Outlet #{activeSession.outletId}<br />
                      Started: {new Date(activeSession.startTime).toLocaleString()}
                    </p>
                    <button
                      onClick={handleEndSessionApi}
                      className="w-full bg-red-600 text-white px-3 py-1 rounded"
                    >
                      End Session
                    </button>
                  </div>
                ) : outlets.length > 0 ? (
                  <div className="space-y-4">
                    {/* Start Session */}
                    <div>
                      <label className="block text-sm text-gray-700 mb-1">
                        Select an outlet to start session:
                      </label>
                      <select
                        className="w-full border px-2 py-1 rounded"
                        value={chosenOutletId}
                        onChange={(e) => setChosenOutletId(e.target.value)}
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
                            handleStartSessionApi(chosenOutletId);
                          }
                        }}
                        className="mt-2 w-full bg-orange-600 text-white px-3 py-1 rounded"
                      >
                        Start Session
                      </button>
                    </div>

                    {/* Reservation form */}
                    <form onSubmit={handleReserveApi} className="space-y-2">
                      <p className="font-medium">Make a reservation</p>

                      <div>
                        <label htmlFor="resOutlet" className="block text-sm text-gray-700">
                          Choose Outlet
                        </label>
                        <select
                          id="resOutlet"
                          className="w-full border px-2 py-1 rounded"
                          value={chosenOutletId}
                          onChange={(e) => setChosenOutletId(e.target.value)}
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
                        <label htmlFor="resStartTime" className="block text-sm text-gray-700">
                          Start Time
                        </label>
                        <input
                          id="resStartTime"
                          type="datetime-local"
                          className="w-full border px-2 py-1 rounded"
                          value={startTime}
                          onChange={(e) => setStartTime(e.target.value)}
                        />
                      </div>

                      <div>
                        <label htmlFor="resEndTime" className="block text-sm text-gray-700">
                          End Time
                        </label>
                        <input
                          id="resEndTime"
                          type="datetime-local"
                          className="w-full border px-2 py-1 rounded"
                          value={endTime}
                          onChange={(e) => setEndTime(e.target.value)}
                        />
                      </div>

                      {reservationMsg && (
                        <p className="text-green-600 text-sm">{reservationMsg}</p>
                      )}

                      <button
                        type="submit"
                        className="w-full bg-green-600 text-white px-3 py-1 rounded"
                      >
                        Reserve
                      </button>
                    </form>
                  </div>
                ) : (
                  <p className="text-gray-600 text-sm">No available outlets.</p>
                )}
              </>
            )}
          </div>
        </Popup>
      )}
    </MapContainer>
  );
}
