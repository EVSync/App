// src/app/reservations/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function Reservations() {
  const router = useRouter();
  const [consumerId, setConsumerId] = useState(null);
  const [reservations, setReservations] = useState([]);

  useEffect(() => {
    const savedConsumer = localStorage.getItem("consumerId");
    if (!savedConsumer) {
      router.push("/login");
    } else {
      setConsumerId(Number(savedConsumer));
      // In a real app you might fetch from backend. Here we’ll simulate:
      const storedRes = JSON.parse(localStorage.getItem("reservations") || "[]");
      const mine = storedRes.filter((r) => r.consumerId === Number(savedConsumer));
      setReservations(mine);
    }
  }, [router]);

  function handleReserve(e) {
    e.preventDefault();

    const stationId = Number(e.target.stationId.value);
    const outletId = Number(e.target.outletId.value);
    const startTime = e.target.startTime.value;
    const endTime = e.target.endTime.value;

    if (!stationId || !outletId || !startTime || !endTime) return;

    const newRes = {
      id: Date.now(),
      consumerId,
      stationId,
      outletId,
      startTime,
      endTime,
    };

    // Save to localStorage
    const stored = JSON.parse(localStorage.getItem("reservations") || "[]");
    stored.push(newRes);
    localStorage.setItem("reservations", JSON.stringify(stored));

    // Update local state
    setReservations((prev) => [...prev, newRes]);

    // Clear form
    e.target.reset();
  }

  function handleLogout() {
    localStorage.removeItem("consumerId");
    router.push("/login");
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-3xl mx-auto bg-white p-6 rounded-lg shadow-md">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-semibold">My Reservations</h2>
          <button
            onClick={handleLogout}
            className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition"
          >
            Log Out
          </button>
        </div>

        {/* List of existing reservations */}
        <div className="mb-8">
          {reservations.length > 0 ? (
            <ul className="space-y-4">
              {reservations.map((res) => (
                <li
                  key={res.id}
                  className="border p-4 rounded bg-gray-50 flex justify-between items-center"
                >
                  <div>
                    <p>
                      <strong>Station:</strong> {res.stationId} &nbsp;
                      <strong>Outlet:</strong> {res.outletId}
                    </p>
                    <p>
                      <strong>From:</strong>{" "}
                      {new Date(res.startTime).toLocaleString()} &nbsp;
                      <strong>To:</strong>{" "}
                      {new Date(res.endTime).toLocaleString()}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-600">You have no reservations yet.</p>
          )}
        </div>

        {/* New Reservation Form (simulated) */}
        <div className="mb-4">
          <h3 className="text-xl font-semibold mb-2">Create a New Reservation</h3>
          <form onSubmit={handleReserve} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label htmlFor="stationId" className="block text-gray-700 mb-1">
                  Station ID
                </label>
                <input
                  id="stationId"
                  name="stationId"
                  type="number"
                  min="1"
                  className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <div>
                <label htmlFor="outletId" className="block text-gray-700 mb-1">
                  Outlet ID
                </label>
                <input
                  id="outletId"
                  name="outletId"
                  type="number"
                  min="1"
                  className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <div className="sm:col-span-2">
                <label htmlFor="startTime" className="block text-gray-700 mb-1">
                  Start Time
                </label>
                <input
                  id="startTime"
                  name="startTime"
                  type="datetime-local"
                  className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
              <div className="sm:col-span-2">
                <label htmlFor="endTime" className="block text-gray-700 mb-1">
                  End Time
                </label>
                <input
                  id="endTime"
                  name="endTime"
                  type="datetime-local"
                  className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
              </div>
            </div>
            <button
              type="submit"
              className="w-full bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
            >
              Reserve
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
