"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  async function handleGenerate() {
    if (!script.trim()) return alert("Script empty hai");

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
        background: "linear-gradient(135deg,#000,#0f1f0f)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "Arial",
      }}
    >
      <div
        style={{
          background: "#111",
          padding: 40,
          borderRadius: 16,
          width: "100%",
          maxWidth: 700,
          boxShadow: "0 0 40px rgba(0,255,0,0.2)",
        }}
      >
        <h1>Generate Video 🎬</h1>
        <p style={{ color: "#aaa" }}>Paste your script below</p>

        <textarea
          value={script}
          onChange={(e) => setScript(e.target.value)}
          placeholder="Write your script..."
          style={{
            width: "100%",
            height: 200,
            padding: 16,
            borderRadius: 10,
            fontSize: 16,
          }}
        />

        <br />
        <br />

        <button
          onClick={handleGenerate}
          disabled={loading}
          style={{
            padding: "14px 32px",
            background: "#00c853",
            border: "none",
            borderRadius: 10,
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          {loading ? "Starting..." : "Generate"}
        </button>
      </div>
    </main>
  );
}
