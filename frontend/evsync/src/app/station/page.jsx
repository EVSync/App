// src/app/station/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";

export default function StationDetails() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [station, setStation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const stationId = searchParams.get("stationId");
  const operatorId = searchParams.get("operatorId");
  const API_BASE = "http://localhost:8080"; // adjust if needed

  // For the dropdown to change status
  const [newStatus, setNewStatus] = useState("");

  // For the form that adds a new outlet by ID
  const [newOutletId, setNewOutletId] = useState("");

  // 1) On mount, fetch station/sh revealing all fields including chargingOutlets
  useEffect(() => {
    if (!stationId || !operatorId) {
      router.push("/");
      return;
    }
    async function fetchStation() {
      try {
        const resp = await fetch(`${API_BASE}/charging-station/${stationId}`);
        if (!resp.ok) {
          throw new Error(`HTTP ${resp.status}: ${resp.statusText}`);
        }
        const data = await resp.json();
        setStation(data);
        setNewStatus(data.status || "");
      } catch (err) {
        console.error("Error fetching station:", err);
        setError("Could not load station details.");
      } finally {
        setLoading(false);
      }
    }
    fetchStation();
  }, [stationId, operatorId, router]);

  if (loading) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <p className="text-lg">Loading station details…</p>
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
  if (!station) {
    return null;
  }

  // 2) Change the station’s status
  async function handleChangeStatus(e) {
    e.preventDefault();
    if (!newStatus || newStatus === station.status) return;
    try {
      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/status?status=${encodeURIComponent(
          newStatus
        )}`,
        {
          method: "PUT",
        }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      // Re‐load station details
      const updated = await fetch(`${API_BASE}/charging-station/${stationId}`);
      const updatedData = await updated.json();
      setStation(updatedData);
      setNewStatus(updatedData.status);
    } catch (err) {
      console.error("Error updating status:", err);
      alert("Could not update status: " + (err.message || "Unknown"));
    }
  }

  // 3) Remove an existing outlet
  async function handleRemoveOutlet(outletId) {
    try {
      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/remove-charging-outlet?chargingOutlet.id=${outletId}`,
        { method: "PUT" }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      // Re‐load station
      const updated = await fetch(`${API_BASE}/charging-station/${stationId}`);
      setStation(await updated.json());
    } catch (err) {
      console.error("Error removing outlet:", err);
      alert("Could not remove outlet: " + (err.message || "Unknown"));
    }
  }

  // 4) Add a new outlet by specifying an existing Outlet ID
  async function handleAddOutlet(e) {
    e.preventDefault();
    const id = parseInt(newOutletId, 10);
    if (isNaN(id) || id <= 0) {
      alert("Enter a valid numeric Outlet ID");
      return;
    }
    try {
      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/add-charging-outlet?chargingOutlet.id=${id}`,
        { method: "PUT" }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      setNewOutletId("");
      const updated = await fetch(`${API_BASE}/charging-station/${stationId}`);
      setStation(await updated.json());
    } catch (err) {
      console.error("Error adding outlet:", err);
      alert("Could not add outlet: " + (err.message || "Unknown"));
    }
  }

  // 5) Delete this station entirely
  async function handleDeleteStation() {
    if (!confirm("Are you sure you want to DELETE this station?")) return;
    try {
      const resp = await fetch(`${API_BASE}/charging-station/${stationId}`, {
        method: "DELETE",
      });
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      router.push(`/map?operatorId=${operatorId}`);
    } catch (err) {
      console.error("Error deleting station:", err);
      alert("Could not delete station: " + (err.message || "Unknown"));
    }
  }

  return (
    <div className="min-h-screen w-full p-6 bg-gray-100">
      <div className="max-w-2xl mx-auto bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-2xl font-semibold mb-4">
          Configure Station #{station.id}
        </h2>
        <p className="mb-2">
          <strong>Coordinates:</strong>{" "}
          {station.latitude.toFixed(5)}, {station.longitude.toFixed(5)}
        </p>
        <p className="mb-4">
          <strong>Operator ID:</strong> {station.operator?.id || "N/A"}
        </p>

        {/* ===== Change Status ===== */}
        <form
          className="mb-6 flex items-center space-x-4"
          onSubmit={handleChangeStatus}
        >
          <label className="block text-gray-700 font-medium" htmlFor="status">
            Status:
          </label>
          <select
            id="status"
            className="border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value)}
          >
            <option value="AVAILABLE">AVAILABLE</option>
            <option value="OCCUPIED">OCCUPIED</option>
            <option value="OFFLINE">OFFLINE</option>
            <option value="MAINTENANCE">MAINTENANCE</option>
            {/* Ensure these match your ChargingStationStatus enum in Spring */}
          </select>
          <button
            type="submit"
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
          >
            Change
          </button>
        </form>

        {/* ===== List of Charging Outlets ===== */}
        <div className="mb-6">
          <h3 className="text-xl font-semibold mb-2">Charging Outlets</h3>
          {station.chargingOutlets && station.chargingOutlets.length > 0 ? (
            <ul className="list-disc pl-6 space-y-1">
              {station.chargingOutlets.map((outlet) => (
                <li
                  key={outlet.id}
                  className="flex justify-between items-center bg-gray-50 p-2 rounded"
                >
                  <div>
                    <span className="font-medium">Outlet #{outlet.id}</span>
                    &nbsp;– Status: {outlet.status}
                  </div>
                  <button
                    onClick={() => handleRemoveOutlet(outlet.id)}
                    className="text-red-600 hover:text-red-800 text-sm"
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-600">No outlets attached to this station.</p>
          )}
        </div>

        {/* ===== Add a New Outlet ===== */}
        <form onSubmit={handleAddOutlet} className="mb-8 flex space-x-2">
          <input
            type="text"
            placeholder="Existing Outlet ID to Add"
            className="border px-3 py-2 rounded w-2/3 focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={newOutletId}
            onChange={(e) => setNewOutletId(e.target.value)}
          />
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
          >
            Add Outlet
          </button>
        </form>

        {/* ===== Delete Station ===== */}
        <button
          onClick={handleDeleteStation}
          className="w-full bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
        >
          Delete This Station
        </button>
      </div>
    </div>
  );
}
