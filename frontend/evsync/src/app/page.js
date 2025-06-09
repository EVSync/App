// src/app/page.jsx
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { API_BASE_URL } from "@/lib/api";

export default function Home() {
  const router = useRouter();

  // ── Consumer state ──
  const [cMode,    setCMode]    = useState("create"); // "create" or "enter"
  const [cEmail,   setCEmail]   = useState("");
  const [cPassword,setCPassword]= useState("");
  const [cIdInput, setCIdInput] = useState("");
  const [cError,   setCError]   = useState("");

  // ── Operator state ──
  const [oMode,    setOMode]    = useState("create");
  const [oEmail,   setOEmail]   = useState("");
  const [oPassword,setOPassword]= useState("");
  const [oIdInput, setOIdInput] = useState("");
  const [oError,   setOError]   = useState("");

  // — Consumer create or enter ID —
  async function handleConsumerCreate(e) {
    e.preventDefault();
    setCError("");
    try {
      const resp = await fetch(API_BASE_URL.CONSUMER, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email:    cEmail.trim(),
          password: cPassword.trim()
        }),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const consumer = await resp.json();
      router.push(`/map?consumerId=${consumer.id}`);
    } catch (err) {
      console.error(err);
      setCError(err.message || "Failed to create consumer");
    }
  }

  function handleConsumerEnter(e) {
    e.preventDefault();
    setCError("");
    const idNum = parseInt(cIdInput, 10);
    if (!idNum || idNum <= 0) {
      setCError("Enter a valid consumer ID");
      return;
    }
    router.push(`/map?consumerId=${idNum}`);
  }

  // — Operator create or enter ID —
  async function handleOperatorCreate(e) {
    e.preventDefault();
    setOError("");
    try {
      const resp = await fetch(API_BASE_URL.OPERATOR, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email:         oEmail.trim(),
          password:      oPassword.trim(),
          operatorType: "OPERATOR",
        }),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const op = await resp.json();
      router.push(`/map?operatorId=${op.id}`);
    } catch (err) {
      console.error(err);
      setOError(err.message || "Failed to create operator");
    }
  }

  function handleOperatorEnter(e) {
    e.preventDefault();
    setOError("");
    const idNum = parseInt(oIdInput, 10);
    if (!idNum || idNum <= 0) {
      setOError("Enter a valid operator ID");
      return;
    }
    router.push(`/map?operatorId=${idNum}`);
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-gray-50 p-4 space-y-8">
      <h1 className="text-4xl font-bold">Aveiro EV Map</h1>

      {/* ── Consumer Panel ── */}
      <div className="w-full max-w-md bg-white p-6 rounded-lg shadow">
        <h2 className="text-2xl font-semibold mb-4 text-center">Consumer</h2>
        <div className="flex justify-center space-x-4 mb-4">
          <button
            className={cMode === "create"
              ? "px-4 py-2 bg-green-600 text-white rounded"
              : "px-4 py-2 border rounded"}
            onClick={() => { setCMode("create"); setCError(""); }}
          >
            Create
          </button>
          <button
            className={cMode === "enter"
              ? "px-4 py-2 bg-green-600 text-white rounded"
              : "px-4 py-2 border rounded"}
            onClick={() => { setCMode("enter"); setCError(""); }}
          >
            Enter ID
          </button>
        </div>

        {cMode === "create" ? (
          <form onSubmit={handleConsumerCreate} className="space-y-4">
            <div>
              <label className="block mb-1">Email</label>
              <input
                type="email"
                required
                value={cEmail}
                onChange={e => setCEmail(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <div>
              <label className="block mb-1">Password</label>
              <input
                type="password"
                required
                value={cPassword}
                onChange={e => setCPassword(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-green-600 text-white py-2 rounded"
            >
              Create & Go to Map
            </button>
          </form>
        ) : (
          <form onSubmit={handleConsumerEnter} className="space-y-4">
            <div>
              <label className="block mb-1">Consumer ID</label>
              <input
                type="text"
                required
                value={cIdInput}
                onChange={e => setCIdInput(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-green-600 text-white py-2 rounded"
            >
              Go to Map
            </button>
          </form>
        )}

        {cError && <p className="mt-2 text-red-600 text-center">{cError}</p>}
      </div>

      {/* ── Operator Panel ── */}
      <div className="w-full max-w-md bg-white p-6 rounded-lg shadow">
        <h2 className="text-2xl font-semibold mb-4 text-center">Operator</h2>
        <div className="flex justify-center space-x-4 mb-4">
          <button
            className={oMode === "create"
              ? "px-4 py-2 bg-blue-600 text-white rounded"
              : "px-4 py-2 border rounded"}
            onClick={() => { setOMode("create"); setOError(""); }}
          >
            Create
          </button>
          <button
            className={oMode === "enter"
              ? "px-4 py-2 bg-blue-600 text-white rounded"
              : "px-4 py-2 border rounded"}
            onClick={() => { setOMode("enter"); setOError(""); }}
          >
            Enter ID
          </button>
        </div>

        {oMode === "create" ? (
          <form onSubmit={handleOperatorCreate} className="space-y-4">
            <div>
              <label className="block mb-1">Email</label>
              <input
                type="email"
                required
                value={oEmail}
                onChange={e => setOEmail(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <div>
              <label className="block mb-1">Password</label>
              <input
                type="password"
                required
                value={oPassword}
                onChange={e => setOPassword(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 rounded"
            >
              Create & Go to Map
            </button>
          </form>
        ) : (
          <form onSubmit={handleOperatorEnter} className="space-y-4">
            <div>
              <label className="block mb-1">Operator ID</label>
              <input
                type="text"
                required
                value={oIdInput}
                onChange={e => setOIdInput(e.target.value)}
                className="w-full px-3 py-2 border rounded"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 rounded"
            >
              Go to Map
            </button>
          </form>
        )}

        {oError && <p className="mt-2 text-red-600 text-center">{oError}</p>}
      </div>
    </div>
  );
}
