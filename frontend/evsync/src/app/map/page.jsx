// src/app/map/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import dynamic from "next/dynamic";

// We only load the map client‐side to avoid SSR errors
const AveiroMap = dynamic(() => import("@/components/AveiroMap"), {
  ssr: false,
});

export default function MapPage() {
  const searchParams = useSearchParams();
  const [operatorCandidate, setOperatorCandidate] = useState(null);

  useEffect(() => {
    // Grab raw ?operatorId= value from the URL.
    const rawOp = searchParams.get("operatorId");
    if (!rawOp) {
      setOperatorCandidate(null);
      return;
    }

    // Try to JSON.parse in case someone passed the entire operator object by mistake.
    let parsed;
    try {
      parsed = JSON.parse(rawOp);
    } catch {
      parsed = rawOp;
    }
    setOperatorCandidate(parsed);
  }, [searchParams]);

  // ── Important fix: make sure we do NOT treat `null` as an object ──
  const operatorId =
    operatorCandidate !== null && typeof operatorCandidate === "object"
      ? operatorCandidate.id
      : operatorCandidate;

  return (
    <div className="h-screen w-full flex flex-col">
      {/* 
         If no operatorId ⇒ consumer mode ⇒ show reservation/session links. 
         If operatorId is a primitive ⇒ operator mode ⇒ hide those consumer links. 
      */}
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

      {/* The map fills the rest. Pass only the primitive operatorId down. */}
      <div className="flex-1">
        <AveiroMap operatorId={operatorId} />
      </div>
    </div>
  );
}
