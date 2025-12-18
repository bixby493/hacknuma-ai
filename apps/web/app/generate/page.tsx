"use client";

import { useState } from "react";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!script.trim()) {
      setStatus("❌ Script empty hai");
      return;
    }

    setLoading(true);
    setStatus("⏳ Video generation started...");

    // FAKE API CALL (demo)
    setTimeout(() => {
      const jobId = Math.floor(Math.random() * 100000);
      setStatus(`✅ Video generation started! Job ID: ${jobId}`);

      // RESULT PAGE REDIRECT
      setTimeout(() => {
        window.location.href = `/result?job=${jobId}`;
      }, 1200);
    }, 1500);
  }

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1 style={{ marginBottom: 8 }}>Generate Video 🎬</h1>
        <p style={{ color: "#aaa", marginBottom: 20 }}>
          Paste your script below
        </p>

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

        {status && (
          <p style={{ marginTop: 16, color: "#9f9" }}>{status}</p>
        )}
      </div>
    </main>
  );
}

const styles: any = {
  main: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #000000, #0f1f0f)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontFamily: "Arial, sans-serif",
    color: "#fff",
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
    height: 200,
    padding: 16,
    borderRadius: 10,
    border: "none",
    outline: "none",
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
};
