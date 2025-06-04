// src/app/station/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";

export default function StationDetails() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [station, setStation] = useState(null);
  const [outlets, setOutlets] = useState([]);          // holds ChargingOutlet[]
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const stationId = searchParams.get("stationId");
  const operatorId = searchParams.get("operatorId");
  const API_BASE = "http://localhost:8080";

  // For changing station status
  const [newStatus, setNewStatus] = useState("");

  // For adding an existing outlet by ID
  const [newOutletId, setNewOutletId] = useState("");

  // For creating a brand-new outlet
  const [creatingOutlet, setCreatingOutlet] = useState(false);
  const [createdOutletStatus, setCreatedOutletStatus] = useState("AVAILABLE");
  const [createdOutletCost, setCreatedOutletCost] = useState("");
  const [createdOutletPower, setCreatedOutletPower] = useState("");

  // On mount: fetch station and its outlets
  useEffect(() => {
    if (!stationId || !operatorId) {
      router.push("/");
      return;
    }

    async function fetchStationAndOutlets() {
      try {
        // 1) Fetch station itself
        const stationResp = await fetch(`${API_BASE}/charging-station/${stationId}`);
        if (!stationResp.ok) {
          throw new Error(`HTTP ${stationResp.status}: ${stationResp.statusText}`);
        }
        const stationData = await stationResp.json();
        setStation(stationData);
        setNewStatus(stationData.status || "");

        // 2) Fetch outlets belonging to this station
        const outletsResp = await fetch(
          `${API_BASE}/charging-station/ChargingOutlets/${stationId}`
        );
        if (!outletsResp.ok) {
          throw new Error(`HTTP ${outletsResp.status}: ${outletsResp.statusText}`);
        }
        const outletsData = await outletsResp.json();
        setOutlets(outletsData);
      } catch (err) {
        console.error("Error fetching station/outlets:", err);
        setError("Could not load station or outlets.");
      } finally {
        setLoading(false);
      }
    }

    fetchStationAndOutlets();
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

  // Helper: reload only the outlets list
  async function reloadOutlets() {
    try {
      const resp = await fetch(
        `${API_BASE}/charging-station/ChargingOutlets/${stationId}`
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${resp.statusText}`);
      }
      const data = await resp.json();
      setOutlets(data);
    } catch (err) {
      console.error("Error reloading outlets:", err);
      alert("Could not reload outlets: " + (err.message || "Unknown"));
    }
  }

  // Change station’s status
  async function handleChangeStatus(e) {
    e.preventDefault();
    if (!newStatus || newStatus === station.status) return;

    try {
      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/status?status=${encodeURIComponent(
          newStatus
        )}`,
        { method: "PUT" }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      // Reload station
      const updatedResp = await fetch(`${API_BASE}/charging-station/${stationId}`);
      const updatedData = await updatedResp.json();
      setStation(updatedData);
      setNewStatus(updatedData.status);
    } catch (err) {
      console.error("Error updating status:", err);
      alert("Could not update status: " + (err.message || "Unknown"));
    }
  }

  // Remove an existing outlet (PUT /charging-station/{id}/remove-charging-outlet)
  async function handleRemoveOutlet(outletId) {
    try {
      // Build a minimal ChargingOutlet object with just its ID
      const payload = { id: outletId };

      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/remove-charging-outlet`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      // After removal, reload outlets list
      await reloadOutlets();
    } catch (err) {
      console.error("Error removing outlet:", err);
      alert("Could not remove outlet: " + (err.message || "Unknown"));
    }
  }

  // Add an existing outlet by ID (PUT /charging-station/{id}/add-charging-outlet)
  async function handleAddExistingOutlet(e) {
    e.preventDefault();
    const id = parseInt(newOutletId, 10);
    if (isNaN(id) || id <= 0) {
      alert("Enter a valid numeric Outlet ID");
      return;
    }

    try {
      // Build a minimal ChargingOutlet object with just its ID
      const payload = { id };

      const resp = await fetch(
        `${API_BASE}/charging-station/${stationId}/add-charging-outlet`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        }
      );
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      setNewOutletId("");
      await reloadOutlets();
    } catch (err) {
      console.error("Error adding existing outlet:", err);
      alert("Could not add outlet: " + (err.message || "Unknown"));
    }
  }

  // Create a brand-new outlet (POST /api/outlets/{stationId})
  async function handleCreateNewOutlet(e) {
    e.preventDefault();
    if (createdOutletCost === "" || createdOutletPower === "") {
      alert("Please provide cost and max power");
      return;
    }
    try {
      const payload = {
        status: createdOutletStatus,
        costPerHour: parseFloat(createdOutletCost),
        maxPower: parseInt(createdOutletPower, 10),
        chargingStation: { id: Number(stationId) },
      };
      const resp = await fetch(`${API_BASE}/api/outlets/${stationId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
      }
      setCreatingOutlet(false);
      setCreatedOutletStatus("AVAILABLE");
      setCreatedOutletCost("");
      setCreatedOutletPower("");
      await reloadOutlets();
    } catch (err) {
      console.error("Error creating new outlet:", err);
      alert("Could not create outlet: " + (err.message || "Unknown"));
    }
  }

  // Delete this station
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

        {/* Change Station Status */}
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
          </select>
          <button
            type="submit"
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
          >
            Change
          </button>
        </form>

        {/* List of Charging Outlets */}
        <div className="mb-6">
          <h3 className="text-xl font-semibold mb-2">Charging Outlets</h3>
          {outlets && outlets.length > 0 ? (
            <ul className="list-disc pl-6 space-y-2">
              {outlets.map((outlet) => (
                <li
                  key={outlet.id}
                  className="bg-gray-50 p-3 rounded flex justify-between items-center"
                >
                  <div>
                    <p className="font-medium">Outlet #{outlet.id}</p>
                    <p>
                      Status: <span className="font-semibold">{outlet.status}</span>
                    </p>
                    <p>
                      Cost/hr:{" "}
                      <span className="font-semibold">
                        {outlet.costPerHour.toFixed(2)}
                      </span>
                    </p>
                    <p>
                      Max Power:{" "}
                      <span className="font-semibold">{outlet.maxPower} kW</span>
                    </p>
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

        {/* Add Existing Outlet */}
        <form onSubmit={handleAddExistingOutlet} className="mb-6 flex space-x-2">
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

        {/* Create New Outlet */}
        {!creatingOutlet ? (
          <button
            onClick={() => setCreatingOutlet(true)}
            className="mb-6 w-full bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition"
          >
            Create New Charging Outlet
          </button>
        ) : (
          <form
            onSubmit={handleCreateNewOutlet}
            className="mb-8 bg-gray-50 p-4 rounded-lg space-y-4"
          >
            <h4 className="text-lg font-medium">New Outlet Details</h4>

            <div className="flex flex-col">
              <label
                className="block text-gray-700 mb-1"
                htmlFor="newOutletStatus"
              >
                Outlet Status:
              </label>
              <select
                id="newOutletStatus"
                className="border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                value={createdOutletStatus}
                onChange={(e) => setCreatedOutletStatus(e.target.value)}
              >
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="OCCUPIED">OCCUPIED</option>
                <option value="OFFLINE">OFFLINE</option>
                <option value="MAINTENANCE">MAINTENANCE</option>
              </select>
            </div>

            <div className="flex flex-col">
              <label className="block text-gray-700 mb-1" htmlFor="newOutletCost">
                Cost per Hour (€):
              </label>
              <input
                id="newOutletCost"
                type="number"
                step="0.01"
                placeholder="e.g. 2.50"
                className="border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                value={createdOutletCost}
                onChange={(e) => setCreatedOutletCost(e.target.value)}
              />
            </div>

            <div className="flex flex-col">
              <label className="block text-gray-700 mb-1" htmlFor="newOutletPower">
                Max Power (kW):
              </label>
              <input
                id="newOutletPower"
                type="number"
                placeholder="e.g. 22"
                className="border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                value={createdOutletPower}
                onChange={(e) => setCreatedOutletPower(e.target.value)}
              />
            </div>

            <div className="flex space-x-2">
              <button
                type="submit"
                className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
              >
                Create &amp; Attach
              </button>
              <button
                type="button"
                onClick={() => {
                  setCreatingOutlet(false);
                  setCreatedOutletStatus("AVAILABLE");
                  setCreatedOutletCost("");
                  setCreatedOutletPower("");
                }}
                className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500 transition"
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {/* Delete Station */}
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
