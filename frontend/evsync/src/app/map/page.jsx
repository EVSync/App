// src/app/map/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import AveiroMap from "@/components/AveiroMap";

export default function MapPage() {
  const searchParams = useSearchParams();
  const [operatorId, setOperatorId] = useState(null);

  useEffect(() => {
    setOperatorId(searchParams.get("operatorId")); // null if none provided
  }, [searchParams]);

  return (
    <div className="h-screen w-full flex flex-col">
      {/* If no operatorId → consumer mode → show reservation/session links */}
      {!operatorId && (
        <div className="flex justify-end space-x-4 p-4 bg-gray-100 border-b">
          <Link
            href="/consumer/reservations"
            className="px-3 py-1 rounded bg-indigo-600 text-white hover:bg-indigo-700 transition text-sm"
          >
            My Reservations
          </Link>
          <Link
            href="/consumer/sessions"
            className="px-3 py-1 rounded bg-purple-600 text-white hover:bg-purple-700 transition text-sm"
          >
            My Sessions
          </Link>
        </div>
      )}

      {/* The map always fills the rest of the screen */}
      <div className="flex-1">
        <AveiroMap operatorId={operatorId} />
      </div>
    </div>
  );
}
