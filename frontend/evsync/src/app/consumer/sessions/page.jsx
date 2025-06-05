// src/app/consumer/sessions/page.jsx
"use client";

import { useEffect, useState } from "react";

export default function ConsumerSessions() {
  const [activeSession, setActiveSession] = useState(null);
  const [history, setHistory] = useState([]);
  const [consumerId, setConsumerId] = useState("");

  useEffect(() => {
    const cid = localStorage.getItem("consumerId") || "guest";
    setConsumerId(cid);

    const session = JSON.parse(localStorage.getItem("activeSession") || "null");
    if (session && session.consumerId === cid) {
      setActiveSession(session);
    }

    const allHistory = JSON.parse(localStorage.getItem("sessionHistory") || "[]");
    const mine = allHistory.filter((s) => s.consumerId === cid);
    setHistory(mine);
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-3xl mx-auto bg-white p-6 rounded-lg shadow-md space-y-6">
        <h1 className="text-2xl font-semibold">
          Sessions for {consumerId}
        </h1>

        <div className="bg-gray-50 p-4 rounded-lg border">
          <h2 className="text-xl font-medium mb-2">Active Session</h2>
          {activeSession ? (
            <div className="space-y-1">
              <p>
                <span className="font-medium">Station ID:</span>{" "}
                {activeSession.stationId}
              </p>
              <p>
                <span className="font-medium">Outlet ID:</span>{" "}
                {activeSession.outletId}
              </p>
              <p>
                <span className="font-medium">Started at:</span>{" "}
                {new Date(activeSession.startTime).toLocaleString()}
              </p>
            </div>
          ) : (
            <p className="text-gray-600">No active session.</p>
          )}
        </div>

        <div className="bg-gray-50 p-4 rounded-lg border">
          <h2 className="text-xl font-medium mb-2">Session History</h2>
          {history.length === 0 ? (
            <p className="text-gray-600">No past sessions.</p>
          ) : (
            <ul className="space-y-4">
              {history.map((s, idx) => (
                <li
                  key={`${s.startTime}-${idx}`}
                  className="bg-white p-3 rounded shadow-sm"
                >
                  <p>
                    <span className="font-medium">Station ID:</span> {s.stationId}
                  </p>
                  <p>
                    <span className="font-medium">Outlet ID:</span> {s.outletId}
                  </p>
                  <p>
                    <span className="font-medium">Start:</span>{" "}
                    {new Date(s.startTime).toLocaleString()}
                  </p>
                  <p>
                    <span className="font-medium">End:</span>{" "}
                    {new Date(s.endTime).toLocaleString()}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
