// src/app/station/new/page.jsx
"use client";

import { useState, useEffect } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { API_BASE_URL } from "@/lib/api";

export default function CreateStationPage() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const operatorId = searchParams.get("operatorId");
  const latParam = searchParams.get("lat");
  const lonParam = searchParams.get("lon");

  const [lat, setLat] = useState("");
  const [lon, setLon] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // On mount, parse the query strings for lat/lon
    if (latParam && lonParam) {
      setLat(latParam);
      setLon(lonParam);
    } else {
      setErrorMsg("Missing latitude or longitude in URL.");
    }
  }, [latParam, lonParam]);

  async function handleCreate(e) {
    e.preventDefault();
    setErrorMsg("");
    if (!operatorId) {
      setErrorMsg("Missing operatorId.");
      return;
    }
    if (!lat || !lon) {
      setErrorMsg("Invalid coordinates.");
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        latitude: parseFloat(lat),
        longitude: parseFloat(lon),
        status: "AVAILABLE",
        operator: { id: Number(operatorId) },
      };
      const resp = await fetch(`${API_BASE_URL.CHARGING_STATION}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || `Status ${resp.status}`);
      }
      // On success, go back to the operator map
      router.push(`/map?operatorId=${operatorId}`);
    } catch (err) {
      console.error("Create station failed:", err);
      setErrorMsg("Could not create station: " + (err.message || "Unknown"));
      setIsSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-md bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-2xl font-semibold mb-4 text-center">
          Create New Charging Station
        </h2>

        {errorMsg && (
          <p className="text-red-600 text-sm mb-4 text-center">{errorMsg}</p>
        )}

        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-gray-700 mb-1">Operator ID</label>
            <input
              type="text"
              readOnly
              className="w-full border px-3 py-2 rounded bg-gray-100"
              value={operatorId || ""}
            />
          </div>

          <div>
            <label className="block text-gray-700 mb-1">Latitude</label>
            <input
              type="text"
              readOnly
              className="w-full border px-3 py-2 rounded bg-gray-100"
              value={lat || ""}
            />
          </div>

          <div>
            <label className="block text-gray-700 mb-1">Longitude</label>
            <input
              type="text"
              readOnly
              className="w-full border px-3 py-2 rounded bg-gray-100"
              value={lon || ""}
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className={`w-full ${
              isSubmitting ? "bg-gray-400" : "bg-blue-600 hover:bg-blue-700"
            } text-white px-4 py-2 rounded transition`}
          >
            {isSubmitting ? "Creating…" : "Confirm & Create"}
          </button>

          <button
            type="button"
            onClick={() => router.push(`/map?operatorId=${operatorId}`)}
            className="w-full mt-2 bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded transition"
          >
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
}
