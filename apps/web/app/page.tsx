"use client";

import { useState } from "react";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [jobId, setJobId] = useState<number | null>(null);

  async function handleGenerate() {
    if (!script.trim()) {
      setStatus("❌ Please enter a script first.");
      return;
    }

    setLoading(true);
    setStatus("⏳ Sending script to AI...");
    setJobId(null);

    try {
      const res = await fetch("/api/generate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ script }),
      });

      const data = await res.json();

      if (data.success) {
        setJobId(data.jobId);
        setStatus("✅ Video generation started!");
      } else {
        setStatus("❌ Something went wrong.");
      }
    } catch (err) {
      setStatus("❌ Server error. Try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #000000, #0f1f0f)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          background: "#111",
          padding: "40px",
          borderRadius: "16px",
          width: "100%",
          maxWidth: "720px",
          boxShadow: "0 0 40px rgba(0,255,0,0.25)",
        }}
      >
        <h1 style={{ color: "#fff", marginBottom: "6px" }}>
          Generate Video 🎬
        </h1>

        <p style={{ color: "#aaa", marginBottom: "20px" }}>
          Paste your script below
        </p>

        <textarea
          value={script}
          onChange={(e) => setScript(e.target.value)}
          placeholder="Write your video script here..."
          style={{
            width: "100%",
            height: "200px",
            padding: "16px",
            borderRadius: "10px",
            border: "none",
            outline: "none",
            fontSize: "16px",
            marginBottom: "20px",
            resize: "vertical",
          }}
        />

        <button
          onClick={handleGenerate}
          disabled={loading}
          style={{
            padding: "14px 32px",
            background: loading ? "#555" : "#00c853",
            color: "#000",
            fontWeight: "bold",
            border: "none",
            borderRadius: "10px",
            cursor: loading ? "not-allowed" : "pointer",
            fontSize: "16px",
          }}
        >
          {loading ? "Generating..." : "Generate"}
        </button>

        {status && (
          <p style={{ marginTop: "16px", color: "#9f9" }}>{status}</p>
        )}

        {jobId && (
          <p style={{ marginTop: "8px", color: "#7df" }}>
            Job ID: <strong>{jobId}</strong>
          </p>
        )}
      </div>
    </main>
  );
}
