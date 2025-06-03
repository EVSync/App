// src/app/page.jsx
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();
  const [mode, setMode] = useState("create"); // "create" or "enter"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [operatorId, setOperatorId] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const API_BASE = "http://localhost:8080"; // Adjust if your Spring runs elsewhere

  // 1) Create a new operator via POST /api/operators
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
      router.push(`/map?operatorId=${newId}`);
    } catch (err) {
      console.error("Create operator error:", err);
      setErrorMsg("Failed to create operator: " + (err.message || "Unknown"));
    }
  }

  // 2) Just enter an existing operator ID
  function handleEnterOperator(e) {
    e.preventDefault();
    setErrorMsg("");
    const parsed = parseInt(operatorId, 10);
    if (isNaN(parsed) || parsed <= 0) {
      setErrorMsg("Enter a valid numeric operator ID");
      return;
    }
    router.push(`/map?operatorId=${parsed}`);
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-gray-50 px-4">
      <h1 className="text-4xl font-bold mb-6">Aveiro EV Map</h1>

      {/* Toggle: Create vs Enter */}
      <div className="flex space-x-4 mb-8">
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
 