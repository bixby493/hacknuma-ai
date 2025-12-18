"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const router = useRouter();

  async function handleGenerate() {
    const res = await fetch("/api/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ script }),
    });

    const data = await res.json();
    router.push(`/result?job=${data.jobId}`);
  }

  return (
    <main style={{ minHeight: "100vh", padding: 40 }}>
      <h1>Generate Video 🎬</h1>

      <textarea
        value={script}
        onChange={(e) => setScript(e.target.value)}
        placeholder="Write your script here..."
        style={{
          width: "100%",
          height: 200,
          marginTop: 20,
          padding: 16,
          borderRadius: 10,
          fontSize: 16,
        }}
      />

      <br />

      <button
        onClick={handleGenerate}
        style={{
          marginTop: 20,
          padding: "12px 28px",
          background: "#00c853",
          border: "none",
          borderRadius: 10,
          fontWeight: "bold",
          cursor: "pointer",
        }}
      >
        Generate
      </button>
    </main>
  );
}
