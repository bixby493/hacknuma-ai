"use client";

import { useState } from "react";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [status, setStatus] = useState("");
  const [progress, setProgress] = useState(0);

  async function handleGenerate() {
    setStatus("Starting...");
    setProgress(0);

    const res = await fetch("/api/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ script }),
    });

    const data = await res.json();

    if (!data.success) {
      setStatus("❌ Error generating video");
      return;
    }

    // simulate real pipeline
    for (const step of data.steps) {
      setStatus(step.label);
      setProgress(step.progress);
      await new Promise((r) => setTimeout(r, 1200));
    }

    setStatus(`✅ Video ready (Job ID: ${data.jobId})`);
  }

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1>Generate Video 🎬</h1>
        <p style={{ color: "#aaa" }}>Paste your script below</p>

        <textarea
          value={script}
          onChange={(e) => setScript(e.target.value)}
          placeholder="Write your video script here..."
          style={styles.textarea}
        />

        <button onClick={handleGenerate} style={styles.button}>
          Generate
        </button>

        {status && (
          <div style={{ marginTop: 20 }}>
            <p>{status}</p>
            <div style={styles.progressBar}>
              <div
                style={{
                  ...styles.progressFill,
                  width: `${progress}%`,
                }}
              />
            </div>
            <p>{progress}%</p>
          </div>
        )}
      </div>
    </main>
  );
}

const styles: any = {
  main: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #000, #0f1f0f)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    color: "#fff",
    fontFamily: "Arial",
  },
  card: {
    background: "#111",
    padding: 40,
    borderRadius: 16,
    width: "100%",
    maxWidth: 700,
    boxShadow: "0 0 40px rgba(0,255,0,0.2)",
  },
  textarea: {
    width: "100%",
    height: 180,
    padding: 16,
    borderRadius: 10,
    border: "none",
    marginTop: 10,
    fontSize: 16,
  },
  button: {
    marginTop: 20,
    padding: "14px 32px",
    background: "#00c853",
    border: "none",
    borderRadius: 10,
    fontWeight: "bold",
    cursor: "pointer",
  },
  progressBar: {
    width: "100%",
    height: 10,
    background: "#222",
    borderRadius: 10,
    marginTop: 10,
  },
  progressFill: {
    height: "100%",
    background: "#00c853",
    borderRadius: 10,
    transition: "width 0.5s",
  },
};
