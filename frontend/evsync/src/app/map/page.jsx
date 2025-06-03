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
    if (!opId) {
      // if someone visits /map without an operator, bounce back to /
      router.push("/");
      return;
    }
    setOperatorId(opId);
  }, [searchParams, router]);

  if (!operatorId) {
    return null; // or a loading spinner
  }

  return (
    <div className="h-screen w-full">
      <AveiroMap operatorId={operatorId} />
    </div>
  );
}
