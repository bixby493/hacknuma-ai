"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  async function handleGenerate() {
    if (!script.trim()) {
      alert("Script likho pehle");
      return;
    }

    setLoading(true);

    const res = await fetch("/api/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ script }),
    });

    const data = await res.json();

    router.push(`/result?job=${data.jobId}`);
  }

  return (
    <main
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
      }}
    >
      <div style={{ width: 600 }}>
        <h1>Generate Video 🎬</h1>

        <textarea
          value={script}
          onChange={(e) => setScript(e.target.value)}
          placeholder="Paste your video script here..."
          style={{
            width: "100%",
            height: 200,
            padding: 12,
            borderRadius: 8,
          }}
        />

        <br />
        <br />

        <button
          onClick={handleGenerate}
          disabled={loading}
          style={{
            padding: "12px 24px",
            fontSize: 16,
            cursor: "pointer",
          }}
        >
          {loading ? "Generating..." : "Generate Video"}
        </button>
      </div>
    </main>
  );
}
