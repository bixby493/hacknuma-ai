"use client";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function ResultPage() {
  const job = useSearchParams().get("job");
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const i = setInterval(() => {
      setProgress(p => p >= 100 ? 100 : p + 20);
    }, 800);
    return () => clearInterval(i);
  }, []);

  return (
    <main>
      <h1>Processing…</h1>
      <p>Job: {job}</p>
      <p>{progress}%</p>
      {progress === 100 && <p>✅ Done</p>}
    </main>
  );
}
