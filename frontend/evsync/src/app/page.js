// src/app/page.jsx
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function Home() {
  const router = useRouter();
  const [mode, setMode] = useState("create"); // "create" or "enter"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [operatorId, setOperatorId] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const API_BASE = "http://localhost:8080"; 


  async function handleCreateOperator(e) {
    e.preventDefault();
    setErrorMsg("");
    try {
      const response = await fetch(`${API_BASE}/api/operators`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          password: password.trim(),
          operatorType: "OPERATOR",
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || response.statusText);
      }

      const data = await response.json(); // e.g. { id: 5, email: "...", ... }
      const newId = data.id;
      if (!newId) throw new Error("No operator ID returned");
      // Always push a primitive string or number in the URL—never the full object
      router.push(`/map?operatorId=${newId}`);
    } catch (err) {
      console.error("Create operator error:", err);
      setErrorMsg("Failed to create operator: " + (err.message || "Unknown"));
    }
  }

  // 2) Enter an existing operator ID manually
  function handleEnterOperator(e) {
    e.preventDefault();
    setErrorMsg("");
    const parsed = parseInt(operatorId, 10);
    if (isNaN(parsed) || parsed <= 0) {
      setErrorMsg("Enter a valid numeric operator ID");
      return;
    }
    // We push only the primitive number/string
    router.push(`/map?operatorId=${parsed}`);
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-gray-50 px-4">
      <h1 className="text-4xl font-bold mb-6">Aveiro EV Map</h1>

      {/* 
        Buttons: Create/Enter Operator, Consumer Login, Contributor Map, 
        plus Consumer shortcuts (Reservations / Sessions)
      */}
      <div className="flex flex-col sm:flex-row sm:space-x-4 space-y-4 sm:space-y-0 mb-8">
        <button
          className={
            mode === "create"
              ? "px-4 py-2 rounded bg-blue-600 text-white"
              : "px-4 py-2 rounded bg-white text-blue-600 border border-blue-600"
          }
          onClick={() => {
            setErrorMsg("");
            setMode("create");
          }}
        >
          Create Operator
        </button>
        <button
          className={
            mode === "enter"
              ? "px-4 py-2 rounded bg-blue-600 text-white"
              : "px-4 py-2 rounded bg-white text-blue-600 border border-blue-600"
          }
          onClick={() => {
            setErrorMsg("");
            setMode("enter");
          }}
        >
          Enter Operator ID
        </button>
        <Link
          href="/login"
          className="px-4 py-2 rounded bg-green-600 text-white hover:bg-green-700 transition"
        >
          Consumer Login
        </Link>
        <Link
          href="/map"
          className="px-4 py-2 rounded bg-yellow-500 text-white hover:bg-yellow-600 transition"
        >
          Contributor Map
        </Link>

        {/* Consumer shortcuts always shown (they’ll navigate to their pages) */}
        <Link
          href="/consumer/reservations"
          className="px-4 py-2 rounded bg-indigo-600 text-white hover:bg-indigo-700 transition"
        >
          My Reservations
        </Link>
        <Link
          href="/consumer/sessions"
          className="px-4 py-2 rounded bg-purple-600 text-white hover:bg-purple-700 transition"
        >
          My Sessions
        </Link>
      </div>

      {mode === "create" ? (
        <form
          className="w-full max-w-sm bg-white p-6 rounded-lg shadow-md"
          onSubmit={handleCreateOperator}
        >
          <label className="block text-gray-700 mb-2" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            className="w-full mb-4 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <label className="block text-gray-700 mb-2" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            required
            className="w-full mb-4 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition"
          >
            Create &amp; Go to Map
          </button>
        </form>
      ) : (
        <form
          className="w-full max-w-sm bg-white p-6 rounded-lg shadow-md"
          onSubmit={handleEnterOperator}
        >
          <label className="block text-gray-700 mb-2" htmlFor="operatorId">
            Existing Operator ID
          </label>
          <input
            id="operatorId"
            type="text"
            required
            className="w-full mb-4 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={operatorId}
            onChange={(e) => setOperatorId(e.target.value)}
          />
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition"
          >
            Go to Map
          </button>
        </form>
      )}

      {errorMsg && (
        <p className="mt-4 text-red-600 text-sm text-center">{errorMsg}</p>
      )}
    </div>
  );
}
