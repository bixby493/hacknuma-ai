"use client";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function ResultPage() {
  const job = useSearchParams().get("job");
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const i = setInterval(() => {
      setProgress((p) => {
        if (p >= 100) {
          clearInterval(i);
          return 100;
        }
        return p + 20;
      });
    }, 800);

    return () => clearInterval(i);
  }, []);

  return (
    <main style={{ minHeight: "100vh", padding: 40 }}>
      <h1>Processing Video…</h1>
      <p>Job ID: {job}</p>

      <div style={{ marginTop: 20 }}>
        Progress: {progress}%
      </div>

      {progress === 100 && <p>✅ Video Ready (demo)</p>}
    </main>
  );
}
