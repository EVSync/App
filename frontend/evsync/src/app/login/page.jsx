// src/app/login/page.jsx
"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function ConsumerLogin() {
  const router = useRouter();

  // Form fields & error state
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  // Keep track of whether a consumer is currently “logged in”
  const [consumerId, setConsumerId] = useState(null);

  useEffect(() => {
    // On mount, read localStorage for existing consumerId
    const savedConsumer = localStorage.getItem("consumerId");
    if (savedConsumer) {
      setConsumerId(savedConsumer);
    }
  }, []);

  // When the user submits the login form:
  function handleLogin(e) {
    e.preventDefault();
    setErrorMsg("");

    // Read out “consumers” from localStorage (array of {id,email,password} objects)
    const usersJson = localStorage.getItem("consumers") || "[]";
    const users = JSON.parse(usersJson);

    // Look for a matching user
    const found = users.find(
      (u) =>
        u.email === email.trim() &&
        u.password === password.trim()
    );
    if (!found) {
      setErrorMsg("Invalid email or password.");
      return;
    }

    // “Log in” by saving consumerId
    localStorage.setItem("consumerId", found.id);
    setConsumerId(found.id);

    // Redirect to the consumer map page
    router.push("/map");
  }

  // When the user clicks “Log Out”:
  function handleLogout() {
    localStorage.removeItem("consumerId");
    setConsumerId(null);
    // Clear any residual form fields or messages
    setEmail("");
    setPassword("");
    setErrorMsg("");
  }

  // --------------------------
  // If a consumerId is already present, show a “Logged in” view + a Log Out button.
  // Otherwise, show the normal login form.
  // --------------------------
  if (consumerId) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-100 px-4">
        <div className="w-full max-w-md bg-white p-6 rounded-lg shadow-md text-center">
          <h2 className="text-2xl font-semibold mb-4">You are already logged in</h2>
          <p className="mb-6">
            Logged in as <span className="font-medium">{consumerId}</span>
          </p>
          <button
            onClick={handleLogout}
            className="w-full bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
          >
            Log Out
          </button>
        </div>
      </div>
    );
  }

  // --------------------------
  // Otherwise: render the login form
  // --------------------------
  return (
    <div className="flex h-screen items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-md bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-2xl font-semibold mb-4 text-center">Consumer Login</h2>

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-gray-700 mb-1">
              Email
            </label>
            <input
              id="email"
              type="email"
              required
              className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-gray-700 mb-1">
              Password
            </label>
            <input
              id="password"
              type="password"
              required
              className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {errorMsg && <p className="text-red-600 text-sm text-center">{errorMsg}</p>}

          <button
            type="submit"
            className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
          >
            Log In
          </button>
        </form>

        <p className="mt-4 text-center text-gray-600">
          Don’t have an account?{" "}
          <Link href="/signup" className="text-blue-600 hover:underline">
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
}
