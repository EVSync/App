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
  const [consumerId, setConsumerId] = useState(null);
  const [operatorId, setOperatorId] = useState(null);

  useEffect(() => {
    setOperatorId(searchParams.get("operatorId"));
    setConsumerId(searchParams.get("consumerId"));
  }, [searchParams]);


  return (
    <div className="h-screen w-full flex flex-col">
      {/* 
         If no operatorId ⇒ consumer mode ⇒ show reservation/session links. 
         If operatorId is a primitive ⇒ operator mode ⇒ hide those consumer links. 
      */}
      {!operatorId && (
        <div className="flex justify-end space-x-4 p-4 bg-gray-100 border-b">
        <Link href={`/consumer/reservations?consumerId=${consumerId}`}
              className="px-3 py-1 bg-indigo-600 text-white rounded text-sm">
          My Reservations
        </Link>
        <Link href={`/consumer/sessions?consumerId=${consumerId}`}
              className="px-3 py-1 bg-purple-600 text-white rounded text-sm">
          My Sessions
        </Link>
      </div>
      )}

      {/* The map fills the rest. Pass only the primitive operatorId down. */}
      <div className="flex-1">
        <AveiroMap operatorId={operatorId} consumerId={consumerId}/>
      </div>
    </div>
  );
}
