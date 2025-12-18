"use client";

import { useState } from "react";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [status, setStatus] = useState("");
  const [progress, setProgress] = useState(0);
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!script.trim()) {
      setStatus("❌ Script empty hai");
      return;
    }

    setLoading(true);
    setProgress(10);
    setStatus("⏳ Sending request...");

    try {
      setProgress(30);

      const res = await fetch("/api/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ script }),
      });

      setProgress(60);

      const data = await res.json();

      if (!data.success) {
        setStatus("❌ Failed");
        setLoading(false);
        return;
      }

      setProgress(100);
      setStatus(`✅ Job started. Job ID: ${data.jobId}`);

      setTimeout(() => {
        window.location.href = `/result?job=${data.jobId}`;
      }, 1200);

    } catch (err) {
      setStatus("❌ Server error");
      setLoading(false);
    }
  }

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1>Generate Video 🎬</h1>
        <p style={{ color: "#aaa" }}>Paste your script below</p>

        <textarea
          placeholder="Write your video script here..."
          value={script}
          onChange={(e) => setScript(e.target.value)}
          style={styles.textarea}
        />

        <button
          onClick={submit}
          disabled={loading}
          style={{
            ...styles.button,
            opacity: loading ? 0.6 : 1,
          }}
        >
          {loading ? "Generating..." : "Generate"}
        </button>

        {loading && (
          <div style={styles.progressWrap}>
            <div
              style={{
                ...styles.progressBar,
                width: `${progress}%`,
              }}
            />
          </div>
        )}

        {status && <p style={{ marginTop: 16 }}>{status}</p>}
      </div>
    </main>
  );
}

const styles: any = {
  main: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #000, #0f1f0f)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#fff",
    fontFamily: "Arial",
  },
  card: {
    background: "#111",
    padding: 40,
    borderRadius: 16,
    width: "100%",
    maxWidth: 700,
    boxShadow: "0 0 40px rgba(0,255,0,0.25)",
  },
  textarea: {
    width: "100%",
    height: 180,
    padding: 16,
    borderRadius: 10,
    border: "none",
    fontSize: 16,
    marginBottom: 20,
  },
  button: {
    padding: "14px 32px",
    background: "#00c853",
    color: "#000",
    fontWeight: "bold",
    border: "none",
    borderRadius: 10,
    cursor: "pointer",
    fontSize: 16,
  },
  progressWrap: {
    marginTop: 16,
    height: 10,
    background: "#222",
    borderRadius: 6,
    overflow: "hidden",
  },
  progressBar: {
    height: "100%",
    background: "#00c853",
    transition: "width 0.4s ease",
  },
};
