// src/app/map/page.jsx
"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import AveiroMap from "@/components/AveiroMap";

export default function MapPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [operatorId, setOperatorId] = useState(null);

  useEffect(() => {
    const opId = searchParams.get("operatorId");
    // if we explicitly want to guard operatorId, we can, but for consumer, opId will be null
    setOperatorId(opId);
  }, [searchParams, router]);

  // Always render the map (consumer mode if operatorId is null)
  return (
    <div className="h-screen w-full">
      <AveiroMap operatorId={operatorId} />
    </div>
  );
}
