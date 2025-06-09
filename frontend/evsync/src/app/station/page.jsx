// src/app/station/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { API_BASE_URL } from "@/lib/api";

export default function StationDetails() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const stationId = searchParams.get("stationId");
  const operatorId = searchParams.get("operatorId");
  if (!stationId || !operatorId) {
    router.push("/");
    return null;
  }

  const [station, setStation] = useState(null);
  const [attachedOutlets, setAttachedOutlets] = useState([]);
  const [allOutlets, setAllOutlets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // For changing station status
  const [newStatus, setNewStatus] = useState("");

  // For creating a brand-new outlet
  const [creatingOutlet, setCreatingOutlet] = useState(false);
  const [createdOutletStatus, setCreatedOutletStatus] = useState("AVAILABLE");
  const [createdOutletCost, setCreatedOutletCost] = useState("");
  const [createdOutletPower, setCreatedOutletPower] = useState("");

  // Fetch station, attached outlets, and all outlets
  useEffect(() => {
    async function fetchData() {
      try {
        // 1) Station
        const st = await fetch(`${API_BASE_URL.CHARGING_STATION}/${stationId}`);
        if (!st.ok) throw new Error("Failed to load station");
        const stData = await st.json();
        setStation(stData);
        setNewStatus(stData.status);

        // 2) Attached outlets
        const att = await fetch(
          API_BASE_URL.CHARGING_STATION_OUTLETS(stationId)
        );
        if (!att.ok) throw new Error("Failed to load outlets");
        setAttachedOutlets(await att.json());

        // 3) All outlets
        const all = await fetch(API_BASE_URL.OUTLET);
        if (!all.ok) throw new Error("Failed to load all outlets");
        setAllOutlets(await all.json());
      } catch (err) {
        console.error(err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [stationId]);

  // Helper: reload attached outlets
  async function reloadAttached() {
    try {
      const resp = await fetch(
        API_BASE_URL.CHARGING_STATION_OUTLETS(stationId)
      );
      if (!resp.ok) throw new Error("Reload failed");
      setAttachedOutlets(await resp.json());
    } catch (err) {
      console.error(err);
      alert("Could not reload outlets: " + err.message);
    }
  }

  // 1) Change status
  async function handleChangeStatus(e) {
    e.preventDefault();
    if (newStatus === station.status) return;
    try {
      const resp = await fetch(
        `${API_BASE_URL.CHARGING_STATION}/${stationId}/status?status=${encodeURIComponent(
          newStatus
        )}`,
        { method: "PUT" }
      );
      if (!resp.ok) throw new Error(await resp.text());
      const updated = await resp.json();
      setStation(updated);
      setNewStatus(updated.status);
    } catch (err) {
      console.error(err);
      alert("Status update failed: " + err.message);
    }
  }

  // 2) Remove outlet
  async function handleRemoveOutlet(id) {
    try {
      const resp = await fetch(
        `${API_BASE_URL.CHARGING_STATION}/${stationId}/remove-charging-outlet`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ id }),
        }
      );
      if (!resp.ok) throw new Error(await resp.text());
      await reloadAttached();
    } catch (err) {
      console.error(err);
      alert("Remove failed: " + err.message);
    }
  }

  // 3) Add existing outlet by choosing from list
  async function handleAddOutlet(id) {
    try {
      const resp = await fetch(
        `${API_BASE_URL.CHARGING_STATION}/${stationId}/add-charging-outlet`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ id }),
        }
      );
      if (!resp.ok) throw new Error(await resp.text());
      await reloadAttached();
    } catch (err) {
      console.error(err);
      alert("Add failed: " + err.message);
    }
  }

  // 4) Create new outlet
  async function handleCreateNewOutlet(e) {
    e.preventDefault();
    if (!createdOutletCost || !createdOutletPower) {
      alert("Provide cost & power");
      return;
    }
    try {
      const payload = {
        status: createdOutletStatus,
        costPerHour: parseFloat(createdOutletCost),
        maxPower: parseInt(createdOutletPower, 10),
        chargingStation: { id: Number(stationId) },
      };
      const resp = await fetch(
        `${API_BASE_URL.OUTLET}/${stationId}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        }
      );
      if (!resp.ok) throw new Error(await resp.text());
      setCreatingOutlet(false);
      setCreatedOutletCost("");
      setCreatedOutletPower("");
      await reloadAttached();
      // Refresh allOutlets too:
      const all = await fetch(API_BASE_URL.OUTLET);
      setAllOutlets(await all.json());
    } catch (err) {
      console.error(err);
      alert("Creation failed: " + err.message);
    }
  }

  // 5) Delete station
  async function handleDelete() {
    if (!confirm("Delete this station?")) return;
    try {
      const resp = await fetch(
        `${API_BASE_URL.CHARGING_STATION}/${stationId}`,
        { method: "DELETE" }
      );
      if (!resp.ok) throw new Error(await resp.text());
      router.push(`/map?operatorId=${operatorId}`);
    } catch (err) {
      console.error(err);
      alert("Delete failed: " + err.message);
    }
  }

  if (loading) {
    return <p className="p-6">Loading…</p>;
  }
  if (error) {
    return <p className="p-6 text-red-600">{error}</p>;
  }

  // Compute unattached outlets list
  const toAdd = allOutlets.filter(
    (o) => !attachedOutlets.some((att) => att.id === o.id)
  );

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="mx-auto max-w-2xl bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-2xl font-semibold mb-4">
          Configure Station #{station.id}
        </h2>

        {/* Status */}
        <form className="mb-6 flex items-center" onSubmit={handleChangeStatus}>
          <label className="mr-2">Status:</label>
          <select
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value)}
            className="border px-2 py-1"
          >
            {["AVAILABLE", "OCCUPIED", "OFFLINE", "MAINTENANCE"].map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <button type="submit" className="ml-4 bg-green-600 text-white px-3 py-1 rounded">
            Change
          </button>
        </form>

        {/* Attached Outlets */}
        <div className="mb-6">
          <h3 className="text-xl font-semibold mb-2">Attached Outlets</h3>
          {attachedOutlets.length > 0 ? (
            <ul className="space-y-2">
              {attachedOutlets.map((o) => (
                <li key={o.id} className="flex justify-between p-2 bg-gray-50 rounded">
                  <span>
                    #{o.id} — {o.status}, €{o.costPerHour}/h, {o.maxPower}kW
                  </span>
                  <button
                    onClick={() => handleRemoveOutlet(o.id)}
                    className="text-red-600"
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>No outlets attached.</p>
          )}
        </div>

        {/* Create New Outlet */}
        {!creatingOutlet ? (
          <button
            onClick={() => setCreatingOutlet(true)}
            className="mb-6 w-full bg-indigo-600 text-white py-2 rounded"
          >
            Create New Outlet
          </button>
        ) : (
          <form onSubmit={handleCreateNewOutlet} className="mb-6 space-y-2 bg-gray-50 p-4 rounded">
            <div>
              <label>Status:</label>
              <select
                value={createdOutletStatus}
                onChange={(e) => setCreatedOutletStatus(e.target.value)}
                className="ml-2 border px-2 py-1"
              >
                {["AVAILABLE", "OCCUPIED", "OFFLINE", "MAINTENANCE"].map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>Cost (€):</label>
              <input
                type="number"
                step="0.01"
                value={createdOutletCost}
                onChange={(e) => setCreatedOutletCost(e.target.value)}
                className="ml-2 border px-2 py-1"
              />
            </div>
            <div>
              <label>Power (kW):</label>
              <input
                type="number"
                value={createdOutletPower}
                onChange={(e) => setCreatedOutletPower(e.target.value)}
                className="ml-2 border px-2 py-1"
              />
            </div>
            <button type="submit" className="bg-green-600 text-white px-3 py-1 rounded">
              Create & Attach
            </button>
            <button
              type="button"
              onClick={() => setCreatingOutlet(false)}
              className="ml-2 bg-gray-400 text-white px-3 py-1 rounded"
            >
              Cancel
            </button>
          </form>
        )}

        {/* Available Outlets to Add */}
        <div className="mb-6">
          <h3 className="text-xl font-semibold mb-2">Available Outlets to Add</h3>
          {toAdd.length > 0 ? (
            <ul className="space-y-2">
              {toAdd.map((o) => (
                <li key={o.id} className="flex justify-between p-2 bg-gray-50 rounded">
                  <span>
                    #{o.id} — {o.status}, €{o.costPerHour}/h, {o.maxPower}kW
                  </span>
                  <button
                    onClick={() => handleAddOutlet(o.id)}
                    className="text-blue-600"
                  >
                    Add
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>No available outlets to add.</p>
          )}
        </div>

        {/* Delete Station */}
        <button
          onClick={handleDelete}
          className="w-full bg-red-600 text-white py-2 rounded"
        >
          Delete Station
        </button>
      </div>
    </div>
  );
}
