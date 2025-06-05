// src/app/consumer/reservations/page.jsx
"use client";

import { useEffect, useState } from "react";

export default function ConsumerReservations() {
  const [reservations, setReservations] = useState([]);
  const [consumerId, setConsumerId] = useState("");

  useEffect(() => {
    const cid = localStorage.getItem("consumerId") || "guest";
    setConsumerId(cid);

    const all = JSON.parse(localStorage.getItem("reservations") || "[]");
    const mine = all.filter((r) => r.consumerId === cid);
    setReservations(mine);
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-3xl mx-auto bg-white p-6 rounded-lg shadow-md">
        <h1 className="text-2xl font-semibold mb-4">
          Reservations for {consumerId}
        </h1>

        {reservations.length === 0 ? (
          <p className="text-gray-600">No reservations found.</p>
        ) : (
          <ul className="space-y-4">
            {reservations.map((res) => (
              <li
                key={res.id}
                className="border rounded p-4 bg-gray-50 hover:bg-gray-100 transition"
              >
                <p>
                  <span className="font-medium">Reservation ID:</span> {res.id}
                </p>
                <p>
                  <span className="font-medium">Station ID:</span> {res.stationId}
                </p>
                <p>
                  <span className="font-medium">Outlet ID:</span> {res.outletId}
                </p>
                <p>
                  <span className="font-medium">Start:</span>{" "}
                  {new Date(res.startTime).toLocaleString()}
                </p>
                <p>
                  <span className="font-medium">End:</span>{" "}
                  {new Date(res.endTime).toLocaleString()}
                </p>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
