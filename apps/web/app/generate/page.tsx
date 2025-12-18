"use client";
import { useState } from "react";

export default function GeneratePage() {
  const [script, setScript] = useState("");
  const [loading, setLoading] = useState(false);

  async function generate() {
    setLoading(true);

    const res = await fetch("/api/generate", { method: "POST" });
    const data = await res.json();

    window.location.href = `/result?job=${data.jobId}`;
  }

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h2>Generate Video 🎬</h2>

        <textarea
          placeholder="Write your script here..."
          value={script}
          onChange={(e) => setScript(e.target.value)}
          style={styles.textarea}
        />

        <button onClick={generate} style={styles.button} disabled={loading}>
          {loading ? "Starting..." : "Generate"}
        </button>
      </div>
    </main>
  );
}

const styles = {
  main: {
    minHeight: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  card: {
    width: "100%",
    maxWidth: 700,
    background: "#111",
    padding: 32,
    borderRadius: 16,
  },
  textarea: {
    width: "100%",
    height: 180,
    borderRadius: 10,
    padding: 16,
    fontSize: 16,
    marginBottom: 16,
  },
  button: {
    padding: "12px 28px",
    background: "#00c853",
    color: "#000",
    border: "none",
    borderRadius: 10,
    fontWeight: "bold",
    cursor: "pointer",
  },
};
